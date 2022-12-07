// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.starrocks.scheduler.mv;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import com.starrocks.common.io.Text;
import com.starrocks.common.io.Writable;
import com.starrocks.persist.gson.GsonUtils;
import com.starrocks.thrift.TMVEpoch;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

/**
 * The incremental maintenance of MV consists of epochs, whose lifetime is defined as:
 * 1. Triggered by transaction publish
 * 2. Acquire the last-committed binlog LSN and latest binlog LSN
 * 3. Start a transaction for incremental maintaining the MV
 * 4. Schedule task executor to consume binlog since last-committed, and apply these changes to MV
 * 5. Commit the transaction to make is visible to user
 * 6. Commit the binlog consumption LSN(be atomic with transaction commitment to make)
 */
public class MVEpoch implements Writable {
    public static final long MAX_EXEC_MILLIS = 1000;
    public static final long MAX_SCAN_ROWS = 10 * 10000;

    @SerializedName("mvId")
    private long mvId;
    @SerializedName("epochState")
    private EpochState state;
    @SerializedName("binlogState")
    private BinlogConsumeStateVO binlogState;
    @SerializedName("startTimeMilli")
    private long startTimeMilli;
    @SerializedName("commitTimeMilli")
    private long commitTimeMilli;

    // Ephemeral states
    private transient long txnId;

    public MVEpoch(long mvId) {
        this.mvId = mvId;
        this.startTimeMilli = System.currentTimeMillis();
        this.state = EpochState.INIT;
        this.binlogState = new BinlogConsumeStateVO();
    }

    public void onReady() {
        Preconditions.checkState(state.equals(EpochState.INIT));
        this.state = EpochState.READY;
    }

    public boolean onSchedule() {
        if (state.equals(EpochState.READY)) {
            this.state = EpochState.RUNNING;
            this.startTimeMilli = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void onCommitting() {
        Preconditions.checkState(state.equals(EpochState.RUNNING));
        this.state = EpochState.COMMITTING;
    }

    public void onCommitted(BinlogConsumeStateVO binlogState) {
        Preconditions.checkState(state.equals(EpochState.COMMITTING));
        this.state = EpochState.COMMITTED;
        this.binlogState = binlogState;
        this.commitTimeMilli = System.currentTimeMillis();
    }

    public void onFailed() {
        this.state = EpochState.FAILED;
    }

    public void reset() {
        Preconditions.checkState(state.equals(EpochState.COMMITTED));
        this.state = EpochState.INIT;
    }

    public static MVEpoch read(DataInput input) throws IOException {
        return GsonUtils.GSON.fromJson(Text.readString(input), MVEpoch.class);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        Text.writeString(out, GsonUtils.GSON.toJson(this));
    }

    public TMVEpoch toThrift() {
        TMVEpoch res = new TMVEpoch();
        res.setMax_exec_millis(MAX_EXEC_MILLIS);
        res.setMax_scan_rows(MAX_SCAN_ROWS);
        // TODO(murphy) generate an epoch id instead of txn id
        res.setEpoch_id(txnId);
        res.setStart_ts(startTimeMilli);
        // TODO(murphy) retrieve actual binlog state
        res.setBinlog_scan(binlogState.toThrift());

        return res;
    }

    /*
     *          txnPublish       onSchedule
     * ┌────────┐      ┌────────┐     ┌───────────┐           ┌────────┐
     * │  INIT  ├──────┤ READY  ├────►│ RUNNING   ├──────────►│ FAILED │
     * └────────┘      └────────┘     └─────┬─────┘           └────────┘
     *     ▲                                │                     ▲
     *     │                                │   executed          │
     *     │                                │                     │
     *     │                          ┌─────▼─────┐               │
     *     │                          │ COMMITTING├───────────────┤
     *     │                          └─────┬─────┘               │
     *     │                                │                     │
     *     │                                │   onTxnCommitted    │
     *     │                                │                     │
     *     │         reset            ┌─────▼─────┐               │
     *     └──────────────────────────┤ COMMITTED ├───────────────┘
     *                                └───────────┘
     */
    public enum EpochState {
        // Wait for data
        INIT,
        // Ready for scheduling
        READY,
        // Scheduled and under execution
        RUNNING,
        // Execution finished and start committing
        COMMITTING,
        // Committed epoch
        COMMITTED,
        // Failed for any reason
        FAILED;

        public boolean isFailed() {
            return this.equals(FAILED);
        }

    }

    public long getMvId() {
        return mvId;
    }

    public void setMvId(long mvId) {
        this.mvId = mvId;
    }

    public EpochState getState() {
        return state;
    }

    public void setState(EpochState state) {
        this.state = state;
    }

    public BinlogConsumeStateVO getBinlogState() {
        return binlogState;
    }

    public void setBinlogState(BinlogConsumeStateVO binlogState) {
        this.binlogState = binlogState;
    }

    public long getStartTimeMilli() {
        return startTimeMilli;
    }

    public void setStartTimeMilli(long startTimeMilli) {
        this.startTimeMilli = startTimeMilli;
    }

    public long getCommitTimeMilli() {
        return commitTimeMilli;
    }

    public void setCommitTimeMilli(long commitTimeMilli) {
        this.commitTimeMilli = commitTimeMilli;
    }

    public long getTxnId() {
        return txnId;
    }

    public void setTxnId(long txnId) {
        this.txnId = txnId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MVEpoch mvEpoch = (MVEpoch) o;
        return mvId == mvEpoch.mvId &&
                commitTimeMilli == mvEpoch.commitTimeMilli && txnId == mvEpoch.txnId && state == mvEpoch.state &&
                Objects.equals(binlogState, mvEpoch.binlogState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mvId, state, binlogState, txnId);
    }

    @Override
    public String toString() {
        return "MVEpoch{" +
                "mvId=" + mvId +
                ", state=" + state +
                ", binlogState=" + binlogState +
                ", startTimeMilli=" + startTimeMilli +
                ", commitTimeMilli=" + commitTimeMilli +
                ", txnId=" + txnId +
                '}';
    }
}