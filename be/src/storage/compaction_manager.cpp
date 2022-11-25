// This file is licensed under the Elastic License 2.0. Copyright 2021-present, StarRocks Inc.

#include "storage/compaction_manager.h"

#include "storage/compaction_scheduler.h"
#include "storage/data_dir.h"
#include "util/thread.h"

namespace starrocks {

CompactionManager::CompactionManager()
        : _next_task_id(0), _dispatch_update_candidate_thread(&CompactionManager::_dispatch_worker, this) {
    Thread::set_thread_name(_dispatch_update_candidate_thread, "dispatch_candidate");
    auto st = ThreadPoolBuilder("up_candidates")
                      .set_min_threads(1)
                      .set_max_threads(5)
                      .set_max_queue_size(100000)
                      .build(&_update_candidate_pool);
    DCHECK(st.ok());
}

CompactionManager::~CompactionManager() {
    _stop.store(true, std::memory_order_release);
    _dispatch_update_candidate_thread.join();
    _update_candidate_pool->wait();
}

void CompactionManager::init_max_task_num(int32_t num) {
    _max_task_num = num;
}

void CompactionManager::update_candidates(std::vector<CompactionCandidate> candidates) {
    size_t erase_num = 0;
    {
        std::lock_guard lg(_candidates_mutex);
        // TODO(meegoo): This is very inefficient to implement, just to fix bug, it will refactor later
        for (auto iter = _compaction_candidates.begin(); iter != _compaction_candidates.end();) {
            bool has_erase = false;
            for (auto& candidate : candidates) {
                if (candidate.tablet->tablet_id() == iter->tablet->tablet_id()) {
                    iter = _compaction_candidates.erase(iter);
                    erase_num++;
                    has_erase = true;
                    break;
                }
            }
            if (!has_erase) {
                iter++;
            }
        }
        for (auto& candidate : candidates) {
            VLOG(1) << "update candidate " << candidate.tablet->tablet_id() << " type "
                    << starrocks::to_string(candidate.type) << " score " << candidate.score;
            _compaction_candidates.emplace(std::move(candidate));
        }
    }
    _notify_schedulers();
}

bool CompactionManager::_check_precondition(const CompactionCandidate& candidate) {
    if (!candidate.tablet) {
        LOG(WARNING) << "candidate with null tablet";
        return false;
    }
    const TabletSharedPtr& tablet = candidate.tablet;
    if (tablet->tablet_state() != TABLET_RUNNING) {
        VLOG(2) << "skip tablet:" << tablet->tablet_id() << " because tablet state is:" << tablet->tablet_state()
                << ", not RUNNING";
        return false;
    }

    if (tablet->has_compaction_task()) {
        // tablet already has a running compaction task, skip it
        VLOG(2) << "skip tablet:" << tablet->tablet_id() << " because there is another running compaction task.";
        return false;
    }

    int64_t last_failure_ts = 0;
    DataDir* data_dir = tablet->data_dir();
    if (candidate.type == CUMULATIVE_COMPACTION) {
        std::unique_lock lk(tablet->get_cumulative_lock(), std::try_to_lock);
        if (!lk.owns_lock()) {
            VLOG(2) << "skip tablet:" << tablet->tablet_id() << " for cumulative lock";
            return false;
        }
        // control the concurrent running tasks's limit
        // allow overruns up to twice the configured limit
        uint16_t num = StorageEngine::instance()->compaction_manager()->running_cumulative_tasks_num_for_dir(data_dir);
        if (config::cumulative_compaction_num_threads_per_disk > 0 &&
            num >= config::cumulative_compaction_num_threads_per_disk * 2) {
            VLOG(2) << "skip tablet:" << tablet->tablet_id()
                    << " for limit of cumulative compaction task per disk. disk path:" << data_dir->path()
                    << ", running num:" << num;
            return false;
        }
        last_failure_ts = tablet->last_cumu_compaction_failure_time();
    } else if (candidate.type == BASE_COMPACTION) {
        std::unique_lock lk(tablet->get_base_lock(), std::try_to_lock);
        if (!lk.owns_lock()) {
            VLOG(2) << "skip tablet:" << tablet->tablet_id() << " for base lock";
            return false;
        }
        uint16_t num = StorageEngine::instance()->compaction_manager()->running_base_tasks_num_for_dir(data_dir);
        if (config::base_compaction_num_threads_per_disk > 0 &&
            num >= config::base_compaction_num_threads_per_disk * 2) {
            VLOG(2) << "skip tablet:" << tablet->tablet_id()
                    << " for limit of base compaction task per disk. disk path:" << data_dir->path()
                    << ", running num:" << num;
            return false;
        }
        last_failure_ts = tablet->last_base_compaction_failure_time();
    }

    int64_t now_ms = UnixMillis();
    if (candidate.type == CompactionType::CUMULATIVE_COMPACTION) {
        if (now_ms - last_failure_ts <= config::min_cmumulative_compaction_failure_interval_sec * 1000) {
            VLOG(1) << "Too often to schedule failure compaction, skip it."
                    << "compaction_type=" << starrocks::to_string(candidate.type)
                    << ", min_cmumulative_compaction_failure_interval_sec="
                    << config::min_cmumulative_compaction_failure_interval_sec
                    << ", last_failure_timestamp=" << last_failure_ts / 1000 << ", tablet_id=" << tablet->tablet_id();
            return false;
        }
    } else if (candidate.type == CompactionType::BASE_COMPACTION) {
        if (now_ms - last_failure_ts <= config::min_compaction_failure_interval_sec * 1000) {
            VLOG(1) << "Too often to schedule failure compaction, skip it."
                    << "compaction_type=" << starrocks::to_string(candidate.type)
                    << ", min_compaction_failure_interval_sec=" << config::min_compaction_failure_interval_sec
                    << ", last_failure_timestamp=" << last_failure_ts / 1000 << ", tablet_id=" << tablet->tablet_id();
            return false;
        }
    }

    return true;
}

bool CompactionManager::pick_candidate(CompactionCandidate* candidate) {
    std::lock_guard lg(_candidates_mutex);
    if (_compaction_candidates.empty()) {
        return false;
    }

    auto iter = _compaction_candidates.begin();
    while (iter != _compaction_candidates.end()) {
        if (_check_precondition(*iter)) {
            *candidate = *iter;
            _compaction_candidates.erase(iter);
            return true;
        }
        iter++;
    }

    return false;
}

void CompactionManager::_dispatch_worker() {
    while (!_stop.load(std::memory_order_consume)) {
        {
            std::lock_guard lock(_dispatch_mutex);
            if (!_dispatch_map.empty()) {
                for (auto& [id, tablet_pair] : _dispatch_map) {
                    auto& tablet = tablet_pair.first;
                    Status st = _update_candidate_pool->submit_func([tablet, this] { update_tablet(tablet); });
                    if (!st.ok()) {
                        LOG(WARNING) << "update candidate tablet " << id << "failed. status:" << st.to_string();
                    }
                }
                _dispatch_map.clear();
            }
        }
        int32_t left_seconds = 10;
        do {
            sleep(1);
            --left_seconds;
        } while (!_stop.load(std::memory_order_consume) && left_seconds > 0 &&
                 _max_dispatch_count < config::min_cumulative_compaction_num_singleton_deltas &&
                 _dispatch_map.size() < 10240);
    }
}

void CompactionManager::update_tablet_async(TabletSharedPtr tablet) {
    std::lock_guard lock(_dispatch_mutex);
    auto iter = _dispatch_map.find(tablet->tablet_id());
    if (iter != _dispatch_map.end()) {
        iter->second.first = tablet;
        iter->second.second++;
        if (iter->second.second > _max_dispatch_count) {
            _max_dispatch_count = iter->second.second;
        }
    } else {
        _dispatch_map.emplace(tablet->tablet_id(), std::make_pair(tablet, 0));
    }
}

void CompactionManager::update_tablet(TabletSharedPtr tablet) {
    if (_disable_update_tablet) {
        return;
    }
    VLOG(1) << "update tablet " << tablet->tablet_id();
    if (tablet->need_compaction()) {
        CompactionCandidate candidate;
        candidate.tablet = tablet;
        candidate.score = tablet->compaction_score();
        candidate.type = tablet->compaction_type();
        update_candidates({candidate});
    }
}

bool CompactionManager::register_task(CompactionTask* compaction_task) {
    if (!compaction_task || check_if_exceed_max_task_num()) {
        return false;
    }
    std::lock_guard lg(_tasks_mutex);
    TabletSharedPtr& tablet = compaction_task->tablet();
    DataDir* data_dir = tablet->data_dir();
    auto p = _running_tasks.insert(compaction_task);
    if (!p.second) {
        // duplicate task
        LOG(WARNING) << "duplicate task, compaction_task:" << compaction_task->task_id()
                     << ", tablet:" << tablet->tablet_id();
        return false;
    }
    if (compaction_task->compaction_type() == CUMULATIVE_COMPACTION) {
        _data_dir_to_cumulative_task_num_map[data_dir]++;
    } else {
        _data_dir_to_base_task_num_map[data_dir]++;
    }
    return true;
}

void CompactionManager::unregister_task(CompactionTask* compaction_task) {
    if (!compaction_task) {
        return;
    }
    std::lock_guard lg(_tasks_mutex);
    auto size = _running_tasks.erase(compaction_task);
    if (size > 0) {
        TabletSharedPtr& tablet = compaction_task->tablet();
        DataDir* data_dir = tablet->data_dir();
        if (compaction_task->compaction_type() == CUMULATIVE_COMPACTION) {
            _data_dir_to_cumulative_task_num_map[data_dir]--;
        } else {
            _data_dir_to_base_task_num_map[data_dir]--;
        }
    }
}

void CompactionManager::clear_tasks() {
    std::lock_guard lg(_tasks_mutex);
    _running_tasks.clear();
    _data_dir_to_cumulative_task_num_map.clear();
    _data_dir_to_base_task_num_map.clear();
}

void CompactionManager::_notify_schedulers() {
    std::lock_guard lg(_scheduler_mutex);
    for (auto& scheduler : _schedulers) {
        scheduler->notify();
    }
}

} // namespace starrocks