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

package com.starrocks.planner;

import com.google.common.collect.ImmutableList;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class MaterializedViewTest extends MaterializedViewTestBase {
    private static final List<String> outerJoinTypes = ImmutableList.of("left", "right");

    @BeforeClass
    public static void setUp() throws Exception {
        MaterializedViewTestBase.setUp();

        starRocksAssert.useDatabase(MATERIALIZED_DB_NAME);

        starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `customer` (\n" +
                "    `c_custkey` int(11) NOT NULL COMMENT \"\",\n" +
                "    `c_name` varchar(26) NOT NULL COMMENT \"\",\n" +
                "    `c_address` varchar(41) NOT NULL COMMENT \"\",\n" +
                "    `c_city` varchar(11) NOT NULL COMMENT \"\",\n" +
                "    `c_nation` varchar(16) NOT NULL COMMENT \"\",\n" +
                "    `c_region` varchar(13) NOT NULL COMMENT \"\",\n" +
                "    `c_phone` varchar(16) NOT NULL COMMENT \"\",\n" +
                "    `c_mktsegment` varchar(11) NOT NULL COMMENT \"\"\n" +
                ") ENGINE=OLAP\n" +
                "DUPLICATE KEY(`c_custkey`)\n" +
                "COMMENT \"OLAP\"\n" +
                "DISTRIBUTED BY HASH(`c_custkey`) BUCKETS 12\n" +
                "PROPERTIES (\n" +
                "    \"replication_num\" = \"1\",\n" +
                "    \"colocate_with\" = \"groupa2\",\n" +
                "    \"in_memory\" = \"false\",\n" +
                "    \"unique_constraints\" = \"c_custkey\"\n" +
                ")\n");
        starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `dates` (\n" +
                "    `d_datekey` int(11) NOT NULL COMMENT \"\",\n" +
                "    `d_date` varchar(20) NOT NULL COMMENT \"\",\n" +
                "    `d_dayofweek` varchar(10) NOT NULL COMMENT \"\",\n" +
                "    `d_month` varchar(11) NOT NULL COMMENT \"\",\n" +
                "    `d_year` int(11) NOT NULL COMMENT \"\",\n" +
                "    `d_yearmonthnum` int(11) NOT NULL COMMENT \"\",\n" +
                "    `d_yearmonth` varchar(9) NOT NULL COMMENT \"\",\n" +
                "    `d_daynuminweek` int(11) NOT NULL COMMENT \"\",\n" +
                "    `d_daynuminmonth` int(11) NOT NULL COMMENT \"\",\n" +
                "    `d_daynuminyear` int(11) NOT NULL COMMENT \"\",\n" +
                "    `d_monthnuminyear` int(11) NOT NULL COMMENT \"\",\n" +
                "    `d_weeknuminyear` int(11) NOT NULL COMMENT \"\",\n" +
                "    `d_sellingseason` varchar(14) NOT NULL COMMENT \"\",\n" +
                "    `d_lastdayinweekfl` int(11) NOT NULL COMMENT \"\",\n" +
                "    `d_lastdayinmonthfl` int(11) NOT NULL COMMENT \"\",\n" +
                "    `d_holidayfl` int(11) NOT NULL COMMENT \"\",\n" +
                "    `d_weekdayfl` int(11) NOT NULL COMMENT \"\"\n" +
                ") ENGINE=OLAP\n" +
                "DUPLICATE KEY(`d_datekey`)\n" +
                "COMMENT \"OLAP\"\n" +
                "DISTRIBUTED BY HASH(`d_datekey`) BUCKETS 1\n" +
                "PROPERTIES (\n" +
                "    \"replication_num\" = \"1\",\n" +
                "    \"in_memory\" = \"false\",\n" +
                "    \"colocate_with\" = \"groupa3\",\n" +
                "    \"unique_constraints\" = \"d_datekey\"\n" +
                ")");
        starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `part` (\n" +
                "    `p_partkey` int(11) NOT NULL COMMENT \"\",\n" +
                "    `p_name` varchar(23) NOT NULL COMMENT \"\",\n" +
                "    `p_mfgr` varchar(7) NOT NULL COMMENT \"\",\n" +
                "    `p_category` varchar(8) NOT NULL COMMENT \"\",\n" +
                "    `p_brand` varchar(10) NOT NULL COMMENT \"\",\n" +
                "    `p_color` varchar(12) NOT NULL COMMENT \"\",\n" +
                "    `p_type` varchar(26) NOT NULL COMMENT \"\",\n" +
                "    `p_size` int(11) NOT NULL COMMENT \"\",\n" +
                "    `p_container` varchar(11) NOT NULL COMMENT \"\"\n" +
                ") ENGINE=OLAP\n" +
                "DUPLICATE KEY(`p_partkey`)\n" +
                "COMMENT \"OLAP\"\n" +
                "DISTRIBUTED BY HASH(`p_partkey`) BUCKETS 12\n" +
                "PROPERTIES (\n" +
                "    \"replication_num\" = \"1\",\n" +
                "    \"colocate_with\" = \"groupa5\",\n" +
                "    \"in_memory\" = \"false\",\n" +
                "    \"unique_constraints\" = \"p_partkey\"\n" +
                ")");
        starRocksAssert.withTable(" CREATE TABLE IF NOT EXISTS `supplier` (\n" +
                "    `s_suppkey` int(11) NOT NULL COMMENT \"\",\n" +
                "    `s_name` varchar(26) NOT NULL COMMENT \"\",\n" +
                "    `s_address` varchar(26) NOT NULL COMMENT \"\",\n" +
                "    `s_city` varchar(11) NOT NULL COMMENT \"\",\n" +
                "    `s_nation` varchar(16) NOT NULL COMMENT \"\",\n" +
                "    `s_region` varchar(13) NOT NULL COMMENT \"\",\n" +
                "    `s_phone` varchar(16) NOT NULL COMMENT \"\"\n" +
                ") ENGINE=OLAP\n" +
                "DUPLICATE KEY(`s_suppkey`)\n" +
                "COMMENT \"OLAP\"\n" +
                "DISTRIBUTED BY HASH(`s_suppkey`) BUCKETS 12\n" +
                "PROPERTIES (\n" +
                "    \"replication_num\" = \"1\",\n" +
                "    \"colocate_with\" = \"groupa4\",\n" +
                "    \"in_memory\" = \"false\",\n" +
                "    \"unique_constraints\" = \"s_suppkey\"\n" +
                ")");

        starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `lineorder` (\n" +
                "    `lo_orderkey` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_linenumber` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_custkey` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_partkey` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_suppkey` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_orderdate` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_orderpriority` varchar(16) NOT NULL COMMENT \"\",\n" +
                "    `lo_shippriority` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_quantity` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_extendedprice` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_ordtotalprice` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_discount` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_revenue` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_supplycost` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_tax` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_commitdate` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_shipmode` varchar(11) NOT NULL COMMENT \"\"\n" +
                ") ENGINE=OLAP\n" +
                "DUPLICATE KEY(`lo_orderkey`)\n" +
                "COMMENT \"OLAP\"\n" +
                "PARTITION BY RANGE(`lo_orderdate`)\n" +
                "(\n" +
                "    PARTITION p1 VALUES [(\"-2147483648\"), (\"19930101\")),\n" +
                "    PARTITION p2 VALUES [(\"19930101\"), (\"19940101\")),\n" +
                "    PARTITION p3 VALUES [(\"19940101\"), (\"19950101\")),\n" +
                "    PARTITION p4 VALUES [(\"19950101\"), (\"19960101\")),\n" +
                "    PARTITION p5 VALUES [(\"19960101\"), (\"19970101\")),\n" +
                "    PARTITION p6 VALUES [(\"19970101\"), (\"19980101\")),\n" +
                "    PARTITION p7 VALUES [(\"19980101\"), (\"19990101\")))\n" +
                "DISTRIBUTED BY HASH(`lo_orderkey`) BUCKETS 48\n" +
                "PROPERTIES (\n" +
                "    \"replication_num\" = \"1\",\n" +
                "    \"colocate_with\" = \"groupa1\",\n" +
                "    \"in_memory\" = \"false\",\n" +
                "    \"foreign_key_constraints\" = \"(lo_custkey) REFERENCES customer(c_custkey);" +
                " (lo_partkey) REFERENCES part(p_partkey);  (lo_suppkey) REFERENCES supplier(s_suppkey);" +
                "  (lo_orderdate) REFERENCES dates(d_datekey)\"\n" +
                ")");

        starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `lineorder_null` (\n" +
                "    `lo_orderkey` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_linenumber` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_custkey` int(11) COMMENT \"\",\n" +
                "    `lo_partkey` int(11) COMMENT \"\",\n" +
                "    `lo_suppkey` int(11) COMMENT \"\",\n" +
                "    `lo_orderdate` int(11) COMMENT \"\",\n" +
                "    `lo_orderpriority` varchar(16) NOT NULL COMMENT \"\",\n" +
                "    `lo_shippriority` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_quantity` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_extendedprice` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_ordtotalprice` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_discount` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_revenue` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_supplycost` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_tax` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_commitdate` int(11) NOT NULL COMMENT \"\",\n" +
                "    `lo_shipmode` varchar(11) NOT NULL COMMENT \"\"\n" +
                ") ENGINE=OLAP\n" +
                "DUPLICATE KEY(`lo_orderkey`)\n" +
                "COMMENT \"OLAP\"\n" +
                "PARTITION BY RANGE(`lo_orderdate`)\n" +
                "(\n" +
                "    PARTITION p1 VALUES [(\"-2147483648\"), (\"19930101\")),\n" +
                "    PARTITION p2 VALUES [(\"19930101\"), (\"19940101\")),\n" +
                "    PARTITION p3 VALUES [(\"19940101\"), (\"19950101\")),\n" +
                "    PARTITION p4 VALUES [(\"19950101\"), (\"19960101\")),\n" +
                "    PARTITION p5 VALUES [(\"19960101\"), (\"19970101\")),\n" +
                "    PARTITION p6 VALUES [(\"19970101\"), (\"19980101\")),\n" +
                "    PARTITION p7 VALUES [(\"19980101\"), (\"19990101\")))\n" +
                "DISTRIBUTED BY HASH(`lo_orderkey`) BUCKETS 48\n" +
                "PROPERTIES (\n" +
                "    \"replication_num\" = \"1\",\n" +
                "    \"colocate_with\" = \"groupa1\",\n" +
                "    \"in_memory\" = \"false\",\n" +
                "    \"foreign_key_constraints\" = \"(lo_custkey) REFERENCES customer(c_custkey);" +
                " (lo_partkey) REFERENCES part(p_partkey);  (lo_suppkey) REFERENCES supplier(s_suppkey);" +
                "  (lo_orderdate) REFERENCES dates(d_datekey)\"\n" +
                ")");

        starRocksAssert.withTable(" CREATE TABLE IF NOT EXISTS `t2` (\n" +
                "    `c5` int(11) NOT NULL COMMENT \"\",\n" +
                "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                ") ENGINE=OLAP\n" +
                "DUPLICATE KEY(`c5`)\n" +
                "COMMENT \"OLAP\"\n" +
                "DISTRIBUTED BY HASH(`c5`) BUCKETS 12\n" +
                "PROPERTIES (\n" +
                "    \"replication_num\" = \"1\",\n" +
                "    \"colocate_with\" = \"groupa4\",\n" +
                "    \"in_memory\" = \"false\",\n" +
                "    \"unique_constraints\" = \"c5\"\n" +
                ")");

        starRocksAssert.withTable(" CREATE TABLE IF NOT EXISTS `t3` (\n" +
                "    `c5` int(11) NOT NULL COMMENT \"\",\n" +
                "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                ") ENGINE=OLAP\n" +
                "DUPLICATE KEY(`c5`)\n" +
                "COMMENT \"OLAP\"\n" +
                "DISTRIBUTED BY HASH(`c5`) BUCKETS 12\n" +
                "PROPERTIES (\n" +
                "    \"replication_num\" = \"1\",\n" +
                "    \"colocate_with\" = \"groupa4\",\n" +
                "    \"in_memory\" = \"false\",\n" +
                "    \"unique_constraints\" = \"c5\"\n" +
                ")");

        starRocksAssert.withTable(" CREATE TABLE IF NOT EXISTS `t1` (\n" +
                "    `c1` int(11) NOT NULL COMMENT \"\",\n" +
                "    `c2` int(11) NOT NULL COMMENT \"\",\n" +
                "    `c3` int(11) NOT NULL COMMENT \"\"\n" +
                ") ENGINE=OLAP\n" +
                "DUPLICATE KEY(`c1`)\n" +
                "COMMENT \"OLAP\"\n" +
                "DISTRIBUTED BY HASH(`c1`) BUCKETS 12\n" +
                "PROPERTIES (\n" +
                "    \"replication_num\" = \"1\",\n" +
                "    \"colocate_with\" = \"groupa4\",\n" +
                "    \"in_memory\" = \"false\",\n" +
                "    \"foreign_key_constraints\" = \"(c2) REFERENCES t2(c5);(c3) REFERENCES t2(c5)\"\n" +
                ")");

        String userTagTable = "create table user_tags " +
                "(time date, user_id int, user_name varchar(20), tag_id int) " +
                "partition by range (time) (partition p1 values less than MAXVALUE) " +
                "distributed by hash(time) " +
                "buckets 3 " +
                "properties('replication_num' = '1');";
        starRocksAssert
                .withTable(userTagTable);
    }

    @Test
    public void testFilter0() {
        String mv = "select empid + 1 as col1 from emps where deptno = 10";
        testRewriteOK(mv, "select empid + 1 from emps where deptno = 10");
        testRewriteOK(mv, "select max(empid + 1) from emps where deptno = 10");
        testRewriteFail(mv, "select max(empid) from emps where deptno = 10");
        // with typecast, cast(empid as bigint) is rewritten as col1 - 1
        testRewriteOK(mv, "select max(empid + 2) from emps where deptno = 10").contains("col1 - 1 + 2");

        testRewriteFail(mv, "select max(empid) from emps where deptno = 11");
        testRewriteFail(mv, "select max(empid) from emps");
        testRewriteFail(mv, "select empid from emps where deptno = 10");

        // no typecasting, empid is rewritten as col1 - 1
        mv = "select empid + 1 as col1 from emps_bigint where deptno = 10";
        testRewriteOK(mv, "select max(empid) from emps_bigint where deptno = 10").contains("col1 - 1");
        testRewriteOK(mv, "select empid from emps_bigint where deptno = 10").contains("col1 - 1");
    }

    @Test
    public void testFilterProject0() {
        String mv = "select empid, locationid * 2 as col2 from emps where deptno = 10";
        testRewriteOK(mv, "select empid + 1 from emps where deptno = 10");
        testRewriteOK(mv, "select empid from emps where deptno = 10 and (locationid * 2) < 10");
    }

    @Test
    public void testSwapInnerJoin() {
        String mv = "select count(*) as col1 from emps join locations on emps.locationid = locations.locationid";
        testRewriteOK(mv, "select count(*) from locations join emps on emps.locationid = locations.locationid");
        testRewriteOK(mv, "select count(*) + 1 from  locations join emps on emps.locationid = locations.locationid");
    }

    @Test
    public void testsInnerJoinComplete() {
        String mv = "select deptno as col1, empid as col2, emps.locationid as col3 from emps " +
                "join locations on emps.locationid = locations.locationid";
        testRewriteOK(mv, "select count(*)  from emps " +
                "join locations on emps.locationid = locations.locationid ");
        testRewriteOK(mv, "select count(*)  from emps " +
                "join locations on emps.locationid = locations.locationid " +
                "and emps.locationid > 10");
        testRewriteOK(mv, "select empid as col2, emps.locationid from emps " +
                "join locations on emps.locationid = locations.locationid " +
                "and emps.locationid > 10");
        testRewriteOK(mv, "select empid as col2, locations.locationid from emps " +
                "join locations on emps.locationid = locations.locationid " +
                "and locations.locationid > 10");
    }

    @Test
    public void testMultiInnerJoinQueryDelta() {
        String mv = "select deptno as col1, empid as col2, locations.locationid as col3 " +
                "from emps join locations on emps.locationid = locations.locationid";
        testRewriteOK(mv, "select count(*)  from emps join locations " +
                "join depts on emps.locationid = locations.locationid and depts.deptno = emps.deptno");
    }

    @Test
    public void testMultiInnerJoinViewDelta() {
        String mv = "select emps.deptno as col1, empid as col2, locations.locationid as col3 " +
                "from emps join locations join depts " +
                "on emps.locationid = locations.locationid and depts.deptno = emps.deptno";
        testRewriteOK(mv, "select deptno as col1, empid as col2, locations.locationid as col3 " +
                "from emps join locations on emps.locationid = locations.locationid");
    }

    @Test
    public void testSwapOuterJoin() {
        for (String joinType : outerJoinTypes) {
            String mv = "select count(*) as col1 from " +
                    "emps " + joinType + " join locations on emps.locationid = locations.locationid";
            testRewriteOK(mv, "select count(*)  + 1 from " +
                    "emps " + joinType + " join locations on emps.locationid = locations.locationid");

            // outer join cannot swap join orders
            testRewriteFail(mv, "select count(*) as col1 from " +
                    "locations " + joinType + " join emps on emps.locationid = locations.locationid");
            testRewriteFail(mv, "select count(*) + 1 as col1 from " +
                    "locations " + joinType + " join emps on emps.locationid = locations.locationid");

            // outer join cannot change join type
            if (joinType.equalsIgnoreCase("right")) {
                testRewriteFail(mv, "select count(*)  as col1 from " +
                        "emps left join locations on emps.locationid = locations.locationid");
            } else {
                testRewriteFail(mv, "select count(*) as col1 from " +
                        "emps right join locations on emps.locationid = locations.locationid");
            }
        }
    }

    @Test
    public void testLeftOuterJoinQueryComplete() {
        String mv = "select deptno as col1, empid as col2, emps.locationid as col3 from emps " +
                " left join locations on emps.locationid = locations.locationid";
        testRewriteOK(mv, "select count(*) from " +
                "emps  left join locations on emps.locationid = locations.locationid");
        testRewriteOK(mv, "select empid as col2, emps.locationid from " +
                "emps  left join locations on emps.locationid = locations.locationid " +
                "where emps.deptno > 10");
        testRewriteOK(mv, "select count(*) from " +
                "emps  left join locations on emps.locationid = locations.locationid " +
                "where emps.deptno > 10");
        testRewriteOK(mv, "select empid as col2, locations.locationid from " +
                "emps left join locations on emps.locationid = locations.locationid " +
                "where emps.locationid > 10");
        // TODO: Query's left outer join will be converted to Inner Join.
        testRewriteFail(mv, "select empid as col2, locations.locationid from " +
                "emps left join locations on emps.locationid = locations.locationid " +
                "where locations.locationid > 10");
        testRewriteFail(mv, "select empid as col2, locations.locationid from " +
                "emps inner join locations on emps.locationid = locations.locationid " +
                "and locations.locationid > 10");
        testRewriteFail(mv, "select empid as col2, locations.locationid from " +
                "emps inner join locations on emps.locationid = locations.locationid " +
                "and emps.locationid > 10");
    }

    @Test
    public void testRightOuterJoinQueryComplete() {
        String mv = "select deptno as col1, empid as col2, emps.locationid as col3 from emps " +
                " right join locations on emps.locationid = locations.locationid";
        testRewriteOK(mv, "select count(*) from " +
                "emps  right join locations on emps.locationid = locations.locationid");
        // TODO: Query's right outer join will be converted to Inner Join.
        testRewriteFail(mv, "select empid as col2, emps.locationid from " +
                "emps  right join locations on emps.locationid = locations.locationid " +
                "where emps.deptno > 10");
        // TODO: Query's right outer join will be converted to Inner Join.
        testRewriteFail(mv, "select count(*) from " +
                "emps  right join locations on emps.locationid = locations.locationid " +
                "where emps.deptno > 10");
        testRewriteOK(mv, "select empid as col2, locations.locationid from " +
                "emps  right join locations on emps.locationid = locations.locationid " +
                "where locations.locationid > 10");
        testRewriteFail(mv, "select empid as col2, locations.locationid from " +
                "emps inner join locations on emps.locationid = locations.locationid " +
                "and locations.locationid > 10");
        testRewriteFail(mv, "select empid as col2, locations.locationid from " +
                "emps inner join locations on emps.locationid = locations.locationid " +
                "and emps.locationid > 10");
    }

    @Test
    public void testMultiOuterJoinQueryDelta() {
        for (String joinType : outerJoinTypes) {
            String mv = "select deptno as col1, empid as col2, locations.locationid as col3 from emps " +
                    "" + joinType + " join locations on emps.locationid = locations.locationid";
            testRewriteOK(mv, "select count(*)  from emps " +
                    "" + joinType + " join locations on emps.locationid = locations.locationid " +
                    "" + joinType + " join depts on depts.deptno = emps.deptno");
        }
    }

    @Test
    public void testAggregateBasic() {
        String mv = "select deptno as col1, locationid + 1 as b, count(*) as c, sum(empid) as s " +
                "from emps group by locationid + 1, deptno";
        testRewriteOK(mv, "select count(*) as c, deptno from emps group by deptno");
        testRewriteOK(mv, "select count(*) as c, locationid + 1 from emps group by locationid + 1");
    }

    @Test
    public void testAggregate0() {
        testRewriteOK("select deptno, count(*) as c, empid + 2 as d, sum(empid) as s "
                        + "from emps group by empid, deptno",
                "select count(*) + 1 as c, deptno from emps group by deptno");
    }

    @Test
    public void testAggregate1() {
        testRewriteOK("select empid, deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps group by empid, deptno",
                "select deptno from emps group by deptno");
    }

    @Test
    public void testAggregate2() {
        testRewriteOK("select empid, deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps group by empid, deptno",
                "select deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps group by deptno");
    }

    @Test
    public void testAggregate3() {
        testRewriteOK("select empid, deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps group by empid, deptno",
                "select deptno, empid, sum(empid) as s, count(*) as c\n"
                        + "from emps group by empid, deptno");
    }

    @Test
    public void testAggregate4() {
        testRewriteOK("select empid, deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps where deptno >= 10 group by empid, deptno",
                "select deptno, sum(empid) as s\n"
                        + "from emps where deptno > 10 group by deptno");
    }

    @Test
    public void testAggregate5() {
        testRewriteOK("select empid, deptno, count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps where deptno >= 10 group by empid, deptno",
                "select deptno, sum(empid) + 1 as s\n"
                        + "from emps where deptno > 10 group by deptno");
    }

    @Test
    public void testAggregate6() {
        testRewriteOK("select empid, deptno, count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps where deptno >= 10 group by empid, deptno",
                "select deptno + 1, sum(empid) + 1 as s\n"
                        + "from emps where deptno > 10 group by deptno");
    }

    @Test
    public void testAggregate7() {

        testRewriteFail("select empid, deptno, count(*) + 1 as c, sum(empid) + 2 as s\n"
                        + "from emps where deptno >= 10 group by empid, deptno",
                "select deptno, sum(empid) + 1 as s\n"
                        + "from emps where deptno > 10 group by deptno");
    }

    @Test
    public void testAggregate8() {
        //TODO: Support deptno + 1 rewrite to deptno (considering typecasting)
        testRewriteFail("select empid, deptno + 1, count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps where deptno >= 10 group by empid, deptno",
                "select deptno + 1, sum(empid) + 1 as s\n"
                        + "from emps where deptno > 10 group by deptno");

        testRewriteOK("select empid, deptno + 1, count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps_bigint where deptno >= 10 group by empid, deptno",
                "select deptno + 1, sum(empid) + 1 as s\n"
                        + "from emps_bigint where deptno > 10 group by deptno");
    }

    @Test
    public void testAggregate9() {
        testRewriteOK("select sum(salary) as col1, count(salary) + 1 from emps",
                "select sum(salary), count(salary) + 1 from emps");
        testRewriteFail("select empid, deptno," +
                        " sum(salary) as total, count(salary) + 1 as cnt" +
                        " from emps group by empid, deptno ",
                "select sum(salary), count(salary) + 1 from emps");
    }

    @Test
    public void testAggregateWithAggExpr() {
        // support agg expr: empid -> abs(empid)
        testRewriteOK("select empid, deptno," +
                        " sum(salary) as total, count(salary) + 1 as cnt" +
                        " from emps group by empid, deptno ",
                "select abs(empid), sum(salary) from emps group by empid");
        testRewriteOK("select empid, deptno," +
                        " sum(salary) as total, count(salary) + 1 as cnt" +
                        " from emps group by empid, deptno ",
                "select abs(empid), sum(salary) from emps group by empid, deptno");
    }

    @Test
    public void testAggregateWithGroupByKeyExpr() {
        testRewriteOK("select empid, deptno," +
                        " sum(salary) as total, count(salary) + 1 as cnt" +
                        " from emps group by empid, deptno ",
                "select abs(empid), sum(salary) from emps group by abs(empid), deptno")
                .contains("  0:OlapScanNode\n" +
                        "     TABLE: mv0")
                .contains("  2:AGGREGATE (update serialize)\n" +
                        "  |  STREAMING\n" +
                        "  |  output: sum(11: total)\n" +
                        "  |  group by: 14: abs, 10: deptno");
    }

    @Test
    @Ignore
    // TODO: Remove const group by keys
    public void testAggregateWithContGroupByKey() {
        testRewriteOK("select empid, floor(cast('1997-01-20 12:34:56' as DATETIME)), "
                        + "count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps\n"
                        + "group by empid, floor(cast('1997-01-20 12:34:56' as DATETIME))",
                "select floor(cast('1997-01-20 12:34:56' as DATETIME)), sum(empid) as s\n"
                        + "from emps group by floor(cast('1997-01-20 12:34:56' as DATETIME))");
    }

    @Test
    public void testAggregateRollup() {
        String mv = "select deptno, count(*) as c, sum(empid) as s from emps group by locationid, deptno";
        testRewriteOK(mv, "select count(*) as c, deptno from emps group by deptno");
        testRewriteOK(mv, "select sum(empid), count(*) as c, deptno from emps group by deptno");
        testRewriteOK(mv, "select count(*) + 1 as c, deptno from emps group by deptno");
    }

    @Test
    public void testsInnerJoinAggregate() {
        String mv = "select count(*) as col1, emps.deptno from " +
                "emps join locations on emps.locationid = locations.locationid + 1 " +
                "group by emps.deptno";
        testRewriteOK(mv, "select count(*) from " +
                "locations join emps on emps.locationid = locations.locationid + 1 " +
                "group by emps.deptno");
        testRewriteFail(mv, "select count(*) , emps.deptno from " +
                "locations join emps on emps.locationid = locations.locationid + 1 " +
                "where emps.deptno > 10 " +
                "group by emps.deptno");
    }

    @Test
    public void testUnionAll0() {
        String mv = "select deptno as col1, count(*) as c, sum(empid) as s from emps group by locationid + 1, deptno";
        testRewriteOK(mv, "select count(*) as c, deptno from emps group by deptno " +
                "union all " +
                "select count(*) as c, deptno from emps where deptno > 10 group by deptno ");
    }

    @Test
    public void testAggregateProject() {
        testRewriteOK("select deptno, count(*) as c, empid + 2, sum(empid) as s "
                        + "from emps group by empid, deptno",
                "select count(*) as c, deptno from emps group by deptno");
        testRewriteOK("select deptno, count(*) as c, empid + 2, sum(empid) as s "
                        + "from emps group by empid, deptno",
                "select count(*) + 1 as c, deptno from emps group by deptno");
    }

    @Test
    public void testAggregateMaterializationNoAggregateFuncs1() {
        testRewriteOK("select empid, deptno from emps group by empid, deptno",
                "select empid, deptno from emps group by empid, deptno");
    }

    @Test
    public void testAggregateMaterializationNoAggregateFuncs2() {
        testRewriteOK("select empid, deptno from emps group by empid, deptno",
                "select deptno from emps group by deptno");
    }

    @Test
    public void testAggregateMaterializationNoAggregateFuncs3() {
        testRewriteFail("select deptno from emps group by deptno",
                "select empid, deptno from emps group by empid, deptno");
    }

    @Test
    public void testAggregateMaterializationNoAggregateFuncs4() {
        testRewriteOK("select empid, deptno\n"
                        + "from emps where deptno = 10 group by empid, deptno",
                "select deptno from emps where deptno = 10 group by deptno");
    }

    @Test
    public void testAggregateMaterializationNoAggregateFuncs5() {
        testRewriteFail("select empid, deptno\n"
                        + "from emps where deptno = 5 group by empid, deptno",
                "select deptno from emps where deptno = 10 group by deptno");
    }

    @Test
    public void testAggregateMaterializationNoAggregateFuncs6() {
        testRewriteOK("select empid, deptno\n"
                        + "from emps where deptno > 5 group by empid, deptno",
                "select deptno from emps where deptno > 10 group by deptno");
    }

    @Test
    public void testAggregateMaterializationNoAggregateFuncs7() {
        testRewriteFail("select empid, deptno\n"
                        + "from emps where deptno > 5 group by empid, deptno",
                "select deptno from emps where deptno < 10 group by deptno");
    }

    @Test
    public void testAggregateMaterializationNoAggregateFuncs8() {
        testRewriteFail("select empid from emps group by empid, deptno",
                "select deptno from emps group by deptno");
    }

    @Test
    public void testAggregateMaterializationNoAggregateFuncs9() {
        testRewriteFail("select empid, deptno from emps\n"
                        + "where salary > 1000 group by name, empid, deptno",
                "select empid from emps\n"
                        + "where salary > 2000 group by name, empid");
    }

    @Test
    public void testAggregateMaterializationAggregateFuncs1() {
        testRewriteOK("select empid, deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps group by empid, deptno",
                "select deptno from emps group by deptno");
    }

    @Test
    public void testAggregateMaterializationAggregateFuncs2() {
        testRewriteOK("select empid, deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps group by empid, deptno",
                "select deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps group by deptno");
    }

    @Test
    public void testAggregateMaterializationAggregateFuncs3() {
        testRewriteOK("select empid, deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps group by empid, deptno",
                "select deptno, empid, sum(empid) as s, count(*) as c\n"
                        + "from emps group by empid, deptno");
    }

    @Test
    public void testAggregateMaterializationAggregateFuncs4() {
        testRewriteOK("select empid, deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps where deptno >= 10 group by empid, deptno",
                "select deptno, sum(empid) as s\n"
                        + "from emps where deptno > 10 group by deptno");
    }

    @Test
    public void testAggregateMaterializationAggregateFuncs5() {
        testRewriteOK("select empid, deptno, count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps where deptno >= 10 group by empid, deptno",
                "select deptno, sum(empid) + 1 as s\n"
                        + "from emps where deptno > 10 group by deptno");
    }

    @Test
    public void testAggregateMaterializationAggregateFuncs6() {
        testRewriteFail("select empid, deptno, count(*) + 1 as c, sum(empid) + 2 as s\n"
                        + "from emps where deptno >= 10 group by empid, deptno",
                "select deptno, sum(empid) + 1 as s\n"
                        + "from emps where deptno > 10 group by deptno");
    }

    @Test
    public void testAggregateMaterializationAggregateFuncs7() {
        testRewriteOK("select empid, deptno, count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps where deptno >= 10 group by empid, deptno",
                "select deptno + 1, sum(empid) + 1 as s\n"
                        + "from emps where deptno > 10 group by deptno");
    }

    @Test
    public void testAggregateMaterializationAggregateFuncs8() {
        //TODO: Support deptno + 1 rewrite to deptno (considering typecasting)
        testRewriteFail("select empid, deptno + 1, count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps where deptno >= 10 group by empid, deptno",
                "select deptno + 1, sum(empid) + 1 as s\n"
                        + "from emps where deptno > 10 group by deptno");

        testRewriteOK("select empid, deptno + 1 as col, count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps_bigint where deptno >= 10 group by empid, deptno",
                "select deptno + 1, sum(empid) + 1 as s\n"
                        + "from emps_bigint where deptno > 10 group by deptno").contains("col - 1");
    }

    @Test
    @Ignore
    // TODO: Remove const group by keys
    public void testAggregateMaterializationAggregateFuncsWithConstGroupByKeys() {
        testRewriteOK("select empid, floor(cast('1997-01-20 12:34:56' as DATETIME)), "
                        + "count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps\n"
                        + "group by empid, floor(cast('1997-01-20 12:34:56' as DATETIME))",
                "select floor(cast('1997-01-20 12:34:56' as DATETIME)), sum(empid) as s\n"
                        + "from emps group by floor(cast('1997-01-20 12:34:56' as DATETIME))");
        testRewriteOK("select empid, floor(cast('1997-01-20 12:34:56' as DATETIME)), "
                        + "count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps\n"
                        + "group by empid, floor(cast('1997-01-20 12:34:56' as DATETIME))",
                "select floor(cast('1997-01-20 12:34:56' as DATETIME)), sum(empid) + 1 as s\n"
                        + "from emps group by floor(cast('1997-01-20 12:34:56' as DATETIME))");
        testRewriteOK("select empid, floor(cast('1997-01-20 12:34:56' as DATETIME)), "
                        + "count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps\n"
                        + "group by empid, floor(cast('1997-01-20 12:34:56' as DATETIME))",
                "select floor(cast('1997-01-20 12:34:56' as DATETIME) ), sum(empid) as s\n"
                        + "from emps group by floor(cast('1997-01-20 12:34:56' as DATETIME) )");
        testRewriteOK("select empid, floor(cast('1997-01-20 12:34:56' as DATETIME)), "
                        + "count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps\n"
                        + "group by empid, floor(cast('1997-01-20 12:34:56' as DATETIME))",
                "select floor(cast('1997-01-20 12:34:56' as DATETIME)), sum(empid) as s\n"
                        + "from emps group by floor(cast('1997-01-20 12:34:56' as DATETIME))");
        testRewriteOK("select empid, cast('1997-01-20 12:34:56' as DATETIME), "
                        + "count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps\n"
                        + "group by empid, cast('1997-01-20 12:34:56' as DATETIME)",
                "select floor(cast('1997-01-20 12:34:56' as DATETIME)), sum(empid) as s\n"
                        + "from emps group by floor(cast('1997-01-20 12:34:56' as DATETIME))");
        testRewriteOK("select empid, floor(cast('1997-01-20 12:34:56' as DATETIME)), "
                        + "count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps\n"
                        + "group by empid, floor(cast('1997-01-20 12:34:56' as DATETIME))",
                "select floor(cast('1997-01-20 12:34:56' as DATETIME)), sum(empid) as s\n"
                        + "from emps group by floor(cast('1997-01-20 12:34:56' as DATETIME))");
        testRewriteOK("select eventid, floor(cast(ts as DATETIME)), "
                        + "count(*) + 1 as c, sum(eventid) as s\n"
                        + "from events group by eventid, floor(cast(ts as DATETIME))",
                "select floor(cast(ts as DATETIME) ), sum(eventid) as s\n"
                        + "from events group by floor(cast(ts as DATETIME) )");
        testRewriteOK("select eventid, cast(ts as DATETIME), count(*) + 1 as c, sum(eventid) as s\n"
                        + "from events group by eventid, cast(ts as DATETIME)",
                "select floor(cast(ts as DATETIME)), sum(eventid) as s\n"
                        + "from events group by floor(cast(ts as DATETIME))");
        testRewriteOK("select eventid, floor(cast(ts as DATETIME)), "
                        + "count(*) + 1 as c, sum(eventid) as s\n"
                        + "from events group by eventid, floor(cast(ts as DATETIME))",
                "select floor(cast(ts as DATETIME)), sum(eventid) as s\n"
                        + "from events group by floor(cast(ts as DATETIME))");
    }

    @Test
    public void testAggregateMaterializationAggregateFuncs18() {
        testRewriteOK("select empid, deptno, count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps group by empid, deptno",
                "select empid*deptno, sum(empid) as s\n"
                        + "from emps group by empid*deptno");
    }


    @Test
    public void testAggregateMaterializationAggregateFuncs19() {
        testRewriteOK("select empid, deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps group by empid, deptno",
                "select empid + 10, count(*) + 1 as c\n"
                        + "from emps group by empid + 10");
    }


    // TODO: Don't support group by position.
    @Ignore
    public void testAggregateMaterializationAggregateFuncs20() {
        testRewriteOK("select 11 as empno, 22 as sal, count(*) from emps group by 11",
                "select * from\n"
                        + "(select 11 as empno, 22 as sal, count(*)\n"
                        + "from emps group by 11, 22 ) tmp\n"
                        + "where sal = 33");
    }

    @Test
    public void testJoinAggregateMaterializationNoAggregateFuncs1() {
        // If agg push down is open, cannot rewrite.
        testRewriteOK("select empid, depts.deptno from emps\n"
                        + "join depts using (deptno) where depts.deptno > 10\n"
                        + "group by empid, depts.deptno",
                "select empid from emps\n"
                        + "join depts using (deptno) where depts.deptno > 20\n"
                        + "group by empid, depts.deptno");
    }

    @Test
    public void testJoinAggregateMaterializationNoAggregateFuncs2() {
        testRewriteOK("select depts.deptno, empid from depts\n"
                        + "join emps using (deptno) where depts.deptno > 10\n"
                        + "group by empid, depts.deptno",
                "select empid from emps\n"
                        + "join depts using (deptno) where depts.deptno > 20\n"
                        + "group by empid, depts.deptno");
    }

    @Test
    public void testJoinAggregateMaterializationNoAggregateFuncs3() {
        // It does not match, Project on top of query
        testRewriteFail("select empid from emps\n"
                        + "join depts using (deptno) where depts.deptno > 10\n"
                        + "group by empid, depts.deptno",
                "select empid from emps\n"
                        + "join depts using (deptno) where depts.deptno > 20\n"
                        + "group by empid, depts.deptno");
    }

    @Test
    public void testJoinAggregateMaterializationNoAggregateFuncs4() {
        testRewriteOK("select empid, depts.deptno from emps\n"
                        + "join depts using (deptno) where emps.deptno > 10\n"
                        + "group by empid, depts.deptno",
                "select empid from emps\n"
                        + "join depts using (deptno) where depts.deptno > 20\n"
                        + "group by empid, depts.deptno");
    }

    @Test
    public void testJoinAggregateMaterializationNoAggregateFuncs5() {
        testRewriteOK("select depts.deptno, emps.empid from depts\n"
                        + "join emps using (deptno) where emps.empid > 10\n"
                        + "group by depts.deptno, emps.empid",
                "select depts.deptno from depts\n"
                        + "join emps using (deptno) where emps.empid > 15\n"
                        + "group by depts.deptno, emps.empid");
    }

    @Test
    public void testJoinAggregateMaterializationNoAggregateFuncs6() {
        testRewriteOK("select depts.deptno, emps.empid from depts\n"
                        + "join emps using (deptno) where emps.empid > 10\n"
                        + "group by depts.deptno, emps.empid",
                "select depts.deptno from depts\n"
                        + "join emps using (deptno) where emps.empid > 15\n"
                        + "group by depts.deptno");
    }

    @Test
    @Ignore
    // TODO: union all support
    public void testJoinAggregateMaterializationNoAggregateFuncs7() {
        testRewriteOK("select depts.deptno, dependents.empid\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join locations on (locations.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 11\n"
                        + "group by depts.deptno, dependents.empid",
                "select dependents.empid\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join locations on (locations.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 10\n"
                        + "group by dependents.empid");
    }

    @Test
    public void testJoinAggregateMaterializationNoAggregateFuncs8() {
        testRewriteFail("select depts.deptno, dependents.empid\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join locations on (locations.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 20\n"
                        + "group by depts.deptno, dependents.empid",
                "select dependents.empid\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join locations on (locations.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 10 and depts.deptno < 20\n"
                        + "group by dependents.empid");
    }

    @Test
    @Ignore
    // TODO: union all support
    public void testJoinAggregateMaterializationNoAggregateFuncs9() {
        testRewriteOK("select depts.deptno, dependents.empid\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join locations on (locations.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 11 and depts.deptno < 19\n"
                        + "group by depts.deptno, dependents.empid",
                "select dependents.empid\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join locations on (locations.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 10 and depts.deptno < 20\n"
                        + "group by dependents.empid");
    }

    @Test
    public void testJoinAggregateMaterializationNoAggregateFuncs10() {
        testRewriteOK("select depts.name, dependents.name as name2, "
                        + "emps.deptno, depts.deptno as deptno2, "
                        + "dependents.empid\n"
                        + "from depts, dependents, emps\n"
                        + "where depts.deptno > 10\n"
                        + "group by depts.name, dependents.name, "
                        + "emps.deptno, depts.deptno, "
                        + "dependents.empid",
                "select dependents.empid\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 10\n"
                        + "group by dependents.empid");
    }


    @Test
    @Ignore
    // TODO: This test relies on FK-UK relationship
    public void testJoinAggregateMaterializationAggregateFuncs1() {
        testRewriteOK("select empid, depts.deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps join depts using (deptno)\n"
                        + "group by empid, depts.deptno",
                "select deptno from emps group by deptno");
    }

    @Test
    @Ignore
    public void testJoinAggregateMaterializationAggregateFuncs2() {
        testRewriteOK("select empid, emps.deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps join depts using (deptno)\n"
                        + "group by empid, emps.deptno",
                "select depts.deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps join depts using (deptno)\n"
                        + "group by depts.deptno");
    }

    @Ignore
    @Test
    // TODO: This test relies on FK-UK relationship
    public void testJoinAggregateMaterializationAggregateFuncs3() {
        testRewriteOK("select empid, depts.deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps join depts using (deptno)\n"
                        + "group by empid, depts.deptno",
                "select deptno, empid, sum(empid) as s, count(*) as c\n"
                        + "from emps group by empid, deptno");
    }

    @Test
    @Ignore
    public void testJoinAggregateMaterializationAggregateFuncs4() {
        testRewriteOK("select empid, emps.deptno, count(*) as c, sum(empid) as s\n"
                        + "from emps join depts using (deptno)\n"
                        + "where emps.deptno >= 10 group by empid, emps.deptno",
                "select depts.deptno, sum(empid) as s\n"
                        + "from emps join depts using (deptno)\n"
                        + "where emps.deptno > 10 group by depts.deptno");
    }

    @Test
    @Ignore
    public void testJoinAggregateMaterializationAggregateFuncs5() {
        testRewriteOK("select empid, depts.deptno, count(*) + 1 as c, sum(empid) as s\n"
                        + "from emps join depts using (deptno)\n"
                        + "where depts.deptno >= 10 group by empid, depts.deptno",
                "select depts.deptno, sum(empid) + 1 as s\n"
                        + "from emps join depts using (deptno)\n"
                        + "where depts.deptno > 10 group by depts.deptno");
    }

    @Test
    @Ignore
    public void testJoinAggregateMaterializationAggregateFuncs6() {
        final String m = "select depts.name, sum(salary) as s\n"
                + "from emps\n"
                + "join depts on (emps.deptno = depts.deptno)\n"
                + "group by depts.name";
        final String q = "select dependents.empid, sum(salary) as s\n"
                + "from emps\n"
                + "join depts on (emps.deptno = depts.deptno)\n"
                + "join dependents on (depts.name = dependents.name)\n"
                + "group by dependents.empid";
        testRewriteOK(m, q);
    }

    @Test
    @Ignore
    public void testJoinAggregateMaterializationAggregateFuncs7() {
        testRewriteOK("select dependents.empid, emps.deptno, sum(salary) as s\n"
                        + "from emps\n"
                        + "join dependents on (emps.empid = dependents.empid)\n"
                        + "group by dependents.empid, emps.deptno",
                "select dependents.empid, sum(salary) as s\n"
                        + "from emps\n"
                        + "join depts on (emps.deptno = depts.deptno)\n"
                        + "join dependents on (emps.empid = dependents.empid)\n"
                        + "group by dependents.empid");
    }

    @Test
    @Ignore
    public void testJoinAggregateMaterializationAggregateFuncs8() {
        testRewriteOK("select dependents.empid, emps.deptno, sum(salary) as s\n"
                        + "from emps\n"
                        + "join dependents on (emps.empid = dependents.empid)\n"
                        + "group by dependents.empid, emps.deptno",
                "select depts.name, sum(salary) as s\n"
                        + "from emps\n"
                        + "join depts on (emps.deptno = depts.deptno)\n"
                        + "join dependents on (emps.empid = dependents.empid)\n"
                        + "group by depts.name");
    }

    @Test
    public void testJoinAggregateMaterializationAggregateFuncs9() {
        testRewriteOK("select dependents.empid, emps.deptno, count(distinct salary) as s\n"
                        + "from emps\n"
                        + "join dependents on (emps.empid = dependents.empid)\n"
                        + "group by dependents.empid, emps.deptno",
                "select emps.deptno, count(distinct salary) as s\n"
                        + "from emps\n"
                        + "join dependents on (emps.empid = dependents.empid)\n"
                        + "group by dependents.empid, emps.deptno");
    }

    @Test
    public void testJoinAggregateMaterializationAggregateFuncs10() {
        testRewriteFail("select dependents.empid, emps.deptno, count(distinct salary) as s\n"
                        + "from emps\n"
                        + "join dependents on (emps.empid = dependents.empid)\n"
                        + "group by dependents.empid, emps.deptno",
                "select emps.deptno, count(distinct salary) as s\n"
                        + "from emps\n"
                        + "join dependents on (emps.empid = dependents.empid)\n"
                        + "group by emps.deptno");
    }

    @Test
    @Ignore
    public void testJoinAggregateMaterializationAggregateFuncs11() {
        testRewriteOK("select depts.deptno, dependents.empid, count(emps.salary) as s\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join locations on (locations.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 11 and depts.deptno < 19\n"
                        + "group by depts.deptno, dependents.empid",
                "select dependents.empid, count(emps.salary) + 1\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join locations on (locations.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 10 and depts.deptno < 20\n"
                        + "group by dependents.empid");
    }

    @Test
    public void testJoinAggregateMaterializationAggregateFuncs12() {
        testRewriteFail("select depts.deptno, dependents.empid, "
                        + "count(distinct emps.salary) as s\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join locations on (locations.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 11 and depts.deptno < 19\n"
                        + "group by depts.deptno, dependents.empid",
                "select dependents.empid, count(distinct emps.salary) + 1\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join locations on (locations.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 10 and depts.deptno < 20\n"
                        + "group by dependents.empid");
    }

    @Test
    public void testJoinAggregateMaterializationAggregateFuncs13() {
        testRewriteFail("select dependents.empid, emps.deptno, count(distinct salary) as s\n"
                        + "from emps\n"
                        + "join dependents on (emps.empid = dependents.empid)\n"
                        + "group by dependents.empid, emps.deptno",
                "select emps.deptno, count(salary) as s\n"
                        + "from emps\n"
                        + "join dependents on (emps.empid = dependents.empid)\n"
                        + "group by dependents.empid, emps.deptno");
    }

    @Test
    @Ignore
    public void testJoinAggregateMaterializationAggregateFuncs14() {
        testRewriteOK("select empid, emps.name, emps.deptno, depts.name, "
                        + "count(*) as c, sum(empid) as s\n"
                        + "from emps join depts using (deptno)\n"
                        + "where (depts.name is not null and emps.name = 'a') or "
                        + "(depts.name is not null and emps.name = 'b')\n"
                        + "group by empid, emps.name, depts.name, emps.deptno",
                "select depts.deptno, sum(empid) as s\n"
                        + "from emps join depts using (deptno)\n"
                        + "where depts.name is not null and emps.name = 'a'\n"
                        + "group by depts.deptno");
    }

    @Test
    @Ignore
    public void testJoinAggregateMaterializationAggregateFuncs15() {
        final String m = ""
                + "SELECT deptno,\n"
                + "  COUNT(*) AS dept_size,\n"
                + "  SUM(salary) AS dept_budget\n"
                + "FROM emps\n"
                + "GROUP BY deptno";
        final String q = ""
                + "SELECT FLOOR(CREATED_AT TO YEAR) AS by_year,\n"
                + "  COUNT(*) AS num_emps\n"
                + "FROM (SELECTdeptno\n"
                + "    FROM emps) AS t\n"
                + "JOIN (SELECT deptno,\n"
                + "        inceptionDate as CREATED_AT\n"
                + "    FROM depts2) using (deptno)\n"
                + "GROUP BY FLOOR(CREATED_AT TO YEAR)";
        testRewriteOK(m, q);
    }

    @Test
    public void testJoinMaterialization1() {
        String q = "select depts.deptno, empid, locationid, t.name \n"
                + "from (select * from emps where empid < 300) t \n"
                + "join depts using (deptno)";
        testRewriteOK("select deptno, empid, locationid, name from emps where empid < 500", q);
    }

    @Test
    public void testJoinMaterialization2() {
        String q = "select *\n"
                + "from emps\n"
                + "join depts using (deptno)";
        String m = "select deptno, empid, locationid, name,\n"
                + "salary, commission from emps";
        testRewriteOK(m, q);
    }

    @Test
    public void testJoinMaterializationStar() {
        String q = "select *\n"
                + "from emps\n"
                + "join depts using (deptno)";
        String m = "select * from emps";
        testRewriteOK(m, q);
    }

    @Test
    public void testJoinMaterialization3() {
        String q = "select empid deptno from emps\n"
                + "join depts using (deptno) where empid = 1";
        String m = "select empid deptno from emps\n"
                + "join depts using (deptno)";
        testRewriteOK(m, q);
    }

    @Test
    public void testJoinMaterialization4() {
        testRewriteOK("select empid deptno from emps\n"
                        + "join depts using (deptno)",
                "select empid deptno from emps\n"
                        + "join depts using (deptno) where empid = 1");
    }

    @Test
    @Ignore
    // TODO: Need add no-loss-cast in lineage factory.
    public void testJoinMaterialization5() {
        testRewriteOK("select cast(empid as BIGINT) as a from emps\n"
                        + "join depts using (deptno)",
                "select empid from emps\n"
                        + "join depts using (deptno) where empid > 1");
    }

    @Test
    @Ignore
    // TODO: Need add no-loss-cast in lineage factory.
    public void testJoinMaterialization6() {
        testRewriteOK("select cast(empid as BIGINT) as a from emps\n"
                        + "join depts using (deptno)",
                "select empid deptno from emps\n"
                        + "join depts using (deptno) where empid = 1");
    }

    @Test
    public void testJoinMaterialization7() {
        testRewriteOK("select depts.name\n"
                        + "from emps\n"
                        + "join depts on (emps.deptno = depts.deptno)",
                "select dependents.empid\n"
                        + "from emps\n"
                        + "join depts on (emps.deptno = depts.deptno)\n"
                        + "join dependents on (depts.name = dependents.name)");
    }

    @Test
    public void testJoinMaterialization8() {
        testRewriteOK("select depts.name\n"
                        + "from emps\n"
                        + "join depts on (emps.deptno = depts.deptno)",
                "select dependents.empid\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)");
    }

    @Test
    public void testJoinMaterialization9() {
        testRewriteOK("select depts.name\n"
                        + "from emps\n"
                        + "join depts on (emps.deptno = depts.deptno)",
                "select dependents.empid\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join locations on (locations.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)");
    }


    @Test
    @Ignore
    public void testJoinMaterialization10() {
        testRewriteOK("select depts.deptno, dependents.empid\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 30",
                "select dependents.empid\n"
                        + "from depts\n"
                        + "join dependents on (depts.name = dependents.name)\n"
                        + "join emps on (emps.deptno = depts.deptno)\n"
                        + "where depts.deptno > 10");
    }

    @Test
    public void testJoinMaterialization11() {
        testRewriteFail("select empid from emps\n"
                        + "join depts using (deptno)",
                "select empid from emps\n"
                        + "where deptno in (select deptno from depts)");
    }

    @Test
    @Ignore
    public void testJoinMaterialization12() {
        testRewriteOK("select empid, emps.name as a, emps.deptno, depts.name as b \n"
                        + "from emps join depts using (deptno)\n"
                        + "where (depts.name is not null and emps.name = 'a') or "
                        + "(depts.name is not null and emps.name = 'b') or "
                        + "(depts.name is not null and emps.name = 'c')",
                "select depts.deptno, depts.name\n"
                        + "from emps join depts using (deptno)\n"
                        + "where (depts.name is not null and emps.name = 'a') or "
                        + "(depts.name is not null and emps.name = 'b')");
    }


    @Test
    @Ignore
    // TODO: agg push down below Join
    public void testAggregateOnJoinKeys() {
        testRewriteOK("select deptno, empid, salary "
                        + "from emps\n"
                        + "group by deptno, empid, salary",
                "select empid, depts.deptno "
                        + "from emps\n"
                        + "join depts on depts.deptno = empid group by empid, depts.deptno");
    }


    @Test
    @Ignore
    // TODO: agg need push down below join
    public void testAggregateOnJoinKeys2() {
        testRewriteOK("select deptno, empid, salary, sum(1) as c "
                        + "from emps\n"
                        + "group by deptno, empid, salary",
                "select sum(1) "
                        + "from emps\n"
                        + "join depts on depts.deptno = empid group by empid, depts.deptno");
    }

    @Test
    public void testAggregateMaterializationOnCountDistinctQuery1() {
        // The column empid is already unique, thus DISTINCT is not
        // in the COUNT of the resulting rewriting
        testRewriteOK("select deptno, empid, salary\n"
                        + "from emps\n"
                        + "group by deptno, empid, salary",
                "select deptno, count(distinct empid) as c from (\n"
                        + "select deptno, empid\n"
                        + "from emps\n"
                        + "group by deptno, empid) t\n"
                        + "group by deptno");
    }

    @Test
    public void testAggregateMaterializationOnCountDistinctQuery2() {
        // The column empid is already unique, thus DISTINCT is not
        // in the COUNT of the resulting rewriting
        testRewriteOK("select deptno, salary, empid\n"
                        + "from emps\n"
                        + "group by deptno, salary, empid",
                "select deptno, count(distinct empid) as c from (\n"
                        + "select deptno, empid\n"
                        + "from emps\n"
                        + "group by deptno, empid) t \n"
                        + "group by deptno");
    }

    @Test
    public void testAggregateMaterializationOnCountDistinctQuery3() {
        // The column salary is not unique, thus we end up with
        // a different rewriting
        testRewriteOK("select deptno, empid, salary\n"
                        + "from emps\n"
                        + "group by deptno, empid, salary",
                "select deptno, count(distinct salary) from (\n"
                        + "select deptno, salary\n"
                        + "from emps\n"
                        + "group by deptno, salary) t \n"
                        + "group by deptno");
    }

    @Test
    public void testAggregateMaterializationOnCountDistinctQuery4() {
        // Although there is no DISTINCT in the COUNT, this is
        // equivalent to previous query
        testRewriteOK("select deptno, salary, empid\n"
                        + "from emps\n"
                        + "group by deptno, salary, empid",
                "select deptno, count(salary) from (\n"
                        + "select deptno, salary\n"
                        + "from emps\n"
                        + "group by deptno, salary) t\n"
                        + "group by deptno");
    }

    @Test
    public void testInnerJoinViewDelta() {
        connectContext.getSessionVariable().setOptimizerExecuteTimeout(300000000);
        String mv = "SELECT" +
                " `l`.`LO_ORDERKEY` as col1, `l`.`LO_ORDERDATE`, `l`.`LO_LINENUMBER`, `l`.`LO_CUSTKEY`, `l`.`LO_PARTKEY`," +
                " `l`.`LO_SUPPKEY`, `l`.`LO_ORDERPRIORITY`, `l`.`LO_SHIPPRIORITY`, `l`.`LO_QUANTITY`," +
                " `l`.`LO_EXTENDEDPRICE`, `l`.`LO_ORDTOTALPRICE`, `l`.`LO_DISCOUNT`, `l`.`LO_REVENUE`," +
                " `l`.`LO_SUPPLYCOST`, `l`.`LO_TAX`, `l`.`LO_COMMITDATE`, `l`.`LO_SHIPMODE`," +
                " `c`.`C_NAME`, `c`.`C_ADDRESS`, `c`.`C_CITY`, `c`.`C_NATION`, `c`.`C_REGION`, `c`.`C_PHONE`," +
                " `c`.`C_MKTSEGMENT`, `s`.`S_NAME`, `s`.`S_ADDRESS`, `s`.`S_CITY`, `s`.`S_NATION`, `s`.`S_REGION`," +
                " `s`.`S_PHONE`, `p`.`P_NAME`, `p`.`P_MFGR`, `p`.`P_CATEGORY`, `p`.`P_BRAND`, `p`.`P_COLOR`," +
                " `p`.`P_TYPE`, `p`.`P_SIZE`, `p`.`P_CONTAINER`\n" +
                "FROM `lineorder` AS `l` INNER" +
                " JOIN `customer` AS `c` ON `c`.`C_CUSTKEY` = `l`.`LO_CUSTKEY`" +
                " INNER JOIN `supplier` AS `s` ON `s`.`S_SUPPKEY` = `l`.`LO_SUPPKEY`" +
                " INNER JOIN `part` AS `p` ON `p`.`P_PARTKEY` = `l`.`LO_PARTKEY`;";

        String query = "SELECT `lineorder`.`lo_orderkey`, `lineorder`.`lo_orderdate`, `customer`.`c_custkey` AS `cd`\n" +
                "FROM `lineorder` INNER JOIN `customer` ON `lineorder`.`lo_custkey` = `customer`.`c_custkey`\n" +
                "WHERE `lineorder`.`lo_orderkey` = 100;";

        testRewriteOK(mv, query);
    }

    @Test
    public void testOuterJoinViewDelta() {
        connectContext.getSessionVariable().setOptimizerExecuteTimeout(300000000);
        String mv = "SELECT" +
                " `l`.`LO_ORDERKEY` as col1, `l`.`LO_ORDERDATE`, `l`.`LO_LINENUMBER`, `l`.`LO_CUSTKEY`, `l`.`LO_PARTKEY`," +
                " `l`.`LO_SUPPKEY`, `l`.`LO_ORDERPRIORITY`, `l`.`LO_SHIPPRIORITY`, `l`.`LO_QUANTITY`," +
                " `l`.`LO_EXTENDEDPRICE`, `l`.`LO_ORDTOTALPRICE`, `l`.`LO_DISCOUNT`, `l`.`LO_REVENUE`," +
                " `l`.`LO_SUPPLYCOST`, `l`.`LO_TAX`, `l`.`LO_COMMITDATE`, `l`.`LO_SHIPMODE`," +
                " `c`.`C_NAME`, `c`.`C_ADDRESS`, `c`.`C_CITY`, `c`.`C_NATION`, `c`.`C_REGION`, `c`.`C_PHONE`," +
                " `c`.`C_MKTSEGMENT`, `s`.`S_NAME`, `s`.`S_ADDRESS`, `s`.`S_CITY`, `s`.`S_NATION`, `s`.`S_REGION`," +
                " `s`.`S_PHONE`, `p`.`P_NAME`, `p`.`P_MFGR`, `p`.`P_CATEGORY`, `p`.`P_BRAND`, `p`.`P_COLOR`," +
                " `p`.`P_TYPE`, `p`.`P_SIZE`, `p`.`P_CONTAINER`\n" +
                "FROM `lineorder` AS `l` " +
                " LEFT OUTER JOIN `customer` AS `c` ON `c`.`C_CUSTKEY` = `l`.`LO_CUSTKEY`" +
                " LEFT OUTER JOIN `supplier` AS `s` ON `s`.`S_SUPPKEY` = `l`.`LO_SUPPKEY`" +
                " LEFT OUTER JOIN `part` AS `p` ON `p`.`P_PARTKEY` = `l`.`LO_PARTKEY`;";

        String query = "SELECT `lineorder`.`lo_orderkey`, `lineorder`.`lo_orderdate`, `customer`.`c_custkey` AS `cd`\n" +
                "FROM `lineorder` LEFT OUTER JOIN `customer` ON `lineorder`.`lo_custkey` = `customer`.`c_custkey`\n" +
                "WHERE `lineorder`.`lo_orderkey` = 100;";

        testRewriteOK(mv, query);

        String mv2 = "SELECT" +
                " `l`.`LO_ORDERKEY` as col1, `l`.`LO_ORDERDATE`, `l`.`LO_LINENUMBER`, `l`.`LO_CUSTKEY`, `l`.`LO_PARTKEY`," +
                " `l`.`LO_SUPPKEY`, `l`.`LO_ORDERPRIORITY`, `l`.`LO_SHIPPRIORITY`, `l`.`LO_QUANTITY`," +
                " `l`.`LO_EXTENDEDPRICE`, `l`.`LO_ORDTOTALPRICE`, `l`.`LO_DISCOUNT`, `l`.`LO_REVENUE`," +
                " `l`.`LO_SUPPLYCOST`, `l`.`LO_TAX`, `l`.`LO_COMMITDATE`, `l`.`LO_SHIPMODE`," +
                " `c`.`C_NAME`, `c`.`C_ADDRESS`, `c`.`C_CITY`, `c`.`C_NATION`, `c`.`C_REGION`, `c`.`C_PHONE`," +
                " `c`.`C_MKTSEGMENT`, `s`.`S_NAME`, `s`.`S_ADDRESS`, `s`.`S_CITY`, `s`.`S_NATION`, `s`.`S_REGION`," +
                " `s`.`S_PHONE`, `p`.`P_NAME`, `p`.`P_MFGR`, `p`.`P_CATEGORY`, `p`.`P_BRAND`, `p`.`P_COLOR`," +
                " `p`.`P_TYPE`, `p`.`P_SIZE`, `p`.`P_CONTAINER`\n" +
                "FROM `lineorder_null` AS `l` " +
                " LEFT OUTER JOIN `customer` AS `c` ON `c`.`C_CUSTKEY` = `l`.`LO_CUSTKEY`" +
                " LEFT OUTER JOIN `supplier` AS `s` ON `s`.`S_SUPPKEY` = `l`.`LO_SUPPKEY`" +
                " LEFT OUTER JOIN `part` AS `p` ON `p`.`P_PARTKEY` = `l`.`LO_PARTKEY`;";

        String query2 = "SELECT `lineorder_null`.`lo_orderkey`, `lineorder_null`.`lo_orderdate`, `customer`.`c_custkey` AS `cd`\n" +
                "FROM `lineorder_null` LEFT OUTER JOIN `customer` ON `lineorder_null`.`lo_custkey` = `customer`.`c_custkey`\n" +
                "WHERE `lineorder_null`.`lo_orderkey` = 100;";

        testRewriteOK(mv2, query2);
    }

    @Test
    public void testAggJoinViewDelta() {
        connectContext.getSessionVariable().setOptimizerExecuteTimeout(300000000);
        String mv = "SELECT" +
                " `LO_ORDERKEY` as col1, C_CUSTKEY, S_SUPPKEY, P_PARTKEY," +
                " sum(LO_QUANTITY) as total_quantity, sum(LO_ORDTOTALPRICE) as total_price, count(*) as num" +
                " FROM `lineorder` AS `l` " +
                " LEFT OUTER JOIN `customer` AS `c` ON `c`.`C_CUSTKEY` = `l`.`LO_CUSTKEY`" +
                " LEFT OUTER JOIN `supplier` AS `s` ON `s`.`S_SUPPKEY` = `l`.`LO_SUPPKEY`" +
                " LEFT OUTER JOIN `part` AS `p` ON `p`.`P_PARTKEY` = `l`.`LO_PARTKEY`" +
                " group by col1, C_CUSTKEY, S_SUPPKEY, P_PARTKEY";

        String query = "SELECT `lineorder`.`lo_orderkey`, sum(LO_QUANTITY), sum(LO_ORDTOTALPRICE), count(*)\n" +
                "FROM `lineorder` LEFT OUTER JOIN `customer` ON `lineorder`.`lo_custkey` = `customer`.`c_custkey`\n" +
                " group by lo_orderkey";

        testRewriteOK(mv, query);
    }

    @Test
    public void testHiveViewDeltaJoinUKFK1() {
        String mv = "select a.c1, a.c2 from\n"
                + "(select * from hive0.partitioned_db.t1 where c1 = 1) a\n"
                + "join hive0.partitioned_db2.t2 using (c2)";
        String query = "select c2 from hive0.partitioned_db.t1 where c1 = 1";
        testRewriteOK(mv, query)
                .contains("0:OlapScanNode\n" +
                        "     TABLE: mv0\n" +
                        "     PREAGGREGATION: ON\n");
    }

    @Test
    public void testHiveViewDeltaJoinUKFK2() {
        String mv = "select t1.c1, l.c2 as c2_1, r.c2 as c2_2, r.c3 from hive0.partitioned_db.t1 " +
                "join hive0.partitioned_db2.t2 l on t1.c2= l.c2 " +
                "join hive0.partitioned_db2.t2 r on t1.c3 = r.c2";
        String query = "select t1.c1, t2.c2 from hive0.partitioned_db.t1 join hive0.partitioned_db2.t2 on t1.c2 = t2.c2";
        testRewriteOK(mv, query)
                .contains("0:OlapScanNode\n" +
                        "     TABLE: mv0\n" +
                        "     PREAGGREGATION: ON\n");
    }

    @Test
    public void testHiveViewDeltaJoinUKFK3() {
        String mv = "select t1.c1, l.c2 as c2_1, r.c2 as c2_2, r.c3, t3.c1 as new_c1 from hive0.partitioned_db.t1 " +
                "join hive0.partitioned_db2.t2 l on t1.c2 = l.c2 " +
                "join hive0.partitioned_db2.t2 r on t1.c3 = r.c2 " +
                "join hive0.partitioned_db.t3 on t1.c2 = t3.c2";
        String query = "select t1.c1, t3.c1 from hive0.partitioned_db.t1 join hive0.partitioned_db.t3 on t1.c2 = t3.c2";
        testRewriteOK(mv, query)
                .contains("0:OlapScanNode\n" +
                        "     TABLE: mv0\n" +
                        "     PREAGGREGATION: ON\n");
    }

    @Test
    public void testHiveViewDeltaJoinUKFK4() {
        String mv = "select a.c1, a.c2 from\n" +
                "(select * from hive0.partitioned_db.t1 where c1 = 1) a\n" +
                "join hive0.partitioned_db2.t2 on a.c2 = t2.c2\n" +
                "join hive0.partitioned_db.t3 on a.c2 = t3.c2";
        String query = "select a.c2 from (select * from hive0.partitioned_db.t1 where c1 = 1) a join hive0.partitioned_db.t3 " +
                "on a.c2 = hive0.partitioned_db.t3.c2";
        testRewriteOK(mv, query)
                .contains("0:OlapScanNode\n" +
                        "     TABLE: mv0\n" +
                        "     PREAGGREGATION: ON\n");
    }

    @Test
    public void testHiveViewDeltaJoinUKFK5() {
        String mv = "select a.c1, a.c2 from\n" +
                "(select * from hive0.partitioned_db.t1 where c1 = 1) a\n" +
                "join hive0.partitioned_db2.t2 on a.c2 = t2.c2\n" +
                "join hive0.partitioned_db.t3 on a.c2 = t3.c2";
        String query = "select a.c3 from (select * from hive0.partitioned_db.t1 where c1 = 1) a join hive0.partitioned_db.t3 " +
                "on a.c2 = hive0.partitioned_db.t3.c2";
        testRewriteFail(mv, query);
    }

    @Test
    public void testHiveViewDeltaJoinUKFK6() {
        String mv = "select t1.c1, l.c2 as c2_1, r.c2 as c2_2, r.c3 from hive0.partitioned_db.t1 " +
                "join hive0.partitioned_db2.t2 l on t1.c2= l.c2 " +
                "join hive0.partitioned_db2.t2 r on t1.c3 = r.c2 " +
                "where t1.c1 = 1";
        String query = "select t1.c1, t2.c2 from hive0.partitioned_db.t1 join hive0.partitioned_db2.t2 on t1.c2 = t2.c2 " +
                "where t1.c1 = 1";
        testRewriteOK(mv, query)
                .contains("0:OlapScanNode\n" +
                        "     TABLE: mv0\n" +
                        "     PREAGGREGATION: ON\n");
    }

    @Test
    public void testHiveViewDeltaJoinUKFK7() {
        String mv = "select t1.c1, l.c2 as c2_1, r.c2 as c2_2, r.c3 from hive0.partitioned_db.t1 " +
                "join hive0.partitioned_db2.t2 l on t1.c2= l.c1 " +
                "join hive0.partitioned_db2.t2 r on t1.c3 = r.c1 " +
                "where t1.c1 = 1";
        String query = "select t1.c1, t2.c2 from hive0.partitioned_db.t1 join hive0.partitioned_db2.t2 on t1.c2 = t2.c1 " +
                "where t1.c1 = 1";
        testRewriteFail(mv, query);
    }

    @Test
    public void testHiveViewDeltaJoinUKFK8() {
        String mv = "select t1.c1, l.c2 as c2_1, r.c2 as c2_2, t3.c3 from hive0.partitioned_db.t1 " +
                "left join hive0.partitioned_db2.t2 l on t1.c2= l.c2 " +
                "join hive0.partitioned_db2.t2 r on t1.c3 = r.c2 " +
                "join hive0.partitioned_db.t3 on t1.c2 = t3.c2 " +
                "where t1.c1 = 1";
        String query = "select t1.c1, t3.c3 from hive0.partitioned_db.t1 join hive0.partitioned_db.t3 on t1.c2 = t3.c2 " +
                "where t1.c1 = 1";
        testRewriteOK(mv, query);
    }

    @Test
    public void testHiveViewDeltaJoinUKFK9() {
        String mv = "select t1.c1, l.c2 as c2_1, r.c2 as c2_2, t3.c3 from hive0.partitioned_db.t1 " +
                "join hive0.partitioned_db.t3 on t1.c2 = t3.c2 " +
                "left join hive0.partitioned_db2.t2 l on t1.c2= l.c2 " +
                "join hive0.partitioned_db2.t2 r on t1.c3 = r.c2 " +
                "where t1.c1 = 1";
        String query = "select t1.c1, t3.c3 from hive0.partitioned_db.t1 join hive0.partitioned_db.t3 on t1.c2 = t3.c2 " +
                "where t1.c1 = 1";
        testRewriteOK(mv, query);
    }

    @Test
    public void testHiveViewDeltaJoinUKFK10() {
        String mv = "select t1.c1, l.c2 as c2_1, r.c2 as c2_2, t3.c3 from hive0.partitioned_db.t1 " +
                "join hive0.partitioned_db.t3 on t1.c2 = t3.c2 " +
                "join hive0.partitioned_db2.t2 r on t1.c3 = r.c2 " +
                "left join hive0.partitioned_db2.t2 l on t1.c2= l.c2 " +
                "where t1.c1 = 1";
        String query = "select t1.c1, t3.c3 from hive0.partitioned_db.t1 join hive0.partitioned_db.t3 on t1.c2 = t3.c2 " +
                "where t1.c1 = 1";
        testRewriteOK(mv, query);
    }

    @Test
    public void testHiveViewDeltaJoinUKFK11() {
        String mv = "select t1.c1, r.c2, t3.c3 from hive0.partitioned_db.t1 " +
                "join hive0.partitioned_db2.t2 r on t1.c3 = r.c2 " +
                "left join hive0.partitioned_db.t3 on t1.c2= t3.c2 " +
                "where t1.c1 = 1";
        String query = "select t1.c1, t3.c3 from hive0.partitioned_db.t1 left join hive0.partitioned_db.t3 on t1.c2 = t3.c2 " +
                "where t1.c1 = 1";
        testRewriteOK(mv, query);
    }

    // mv: t1, t2, t2
    // query: t1, t2
    @Test
    public void testViewDeltaJoinUKFK1() {
        String mv = "select c1 as col1, c2, c3, l.c6, r.c7" +
                " from t1 join t2 l on t1.c2 = l.c5" +
                " join t2 r on t1.c3 = r.c5";
        String query = "select c1, c2, c3, c6 from t1 join t2 on t1.c2 = t2.c5";
        testRewriteOK(mv, query);
    }

    // mv: t1, t3, t2, t2
    // query: t1, t3
    @Test
    public void testViewDeltaJoinUKFK2() {
        String mv = "select c1 as col1, c2, c3, l.c6, r.c7" +
                " from t1 join t2 l on t1.c2 = l.c5" +
                " join t2 r on t1.c3 = r.c5" +
                " join t3 on t1.c2 = t3.c5";
        String query = "select c1, c2, c3, t3.c5 from t1 join t3 on t1.c2 = t3.c5";
        testRewriteOK(mv, query);
    }

    @Test
    public void testViewDeltaJoinUKFK3() {
        String mv = "select a.empid, a.deptno from\n"
                + "(select * from emps where empid = 1) a\n"
                + "join depts using (deptno)\n"
                + "join dependents using (empid)";
        String query = "select a.empid from \n"
                + "(select * from emps where empid = 1) a\n"
                + "join dependents using (empid)";
        testRewriteOK(mv, query);
    }

    @Test
    public void testViewDeltaJoinUKFK4() {
        String mv = "select a.empid, a.deptno from\n"
                + "(select * from emps where empid = 1) a\n"
                + "join depts using (deptno)\n"
                + "join dependents using (empid)";
        String query = "select a.name from \n"
                + "(select * from emps where empid = 1) a\n"
                + "join dependents using (empid)";
        testRewriteFail(mv, query);
    }

    @Test
    public void testViewDeltaJoinUKFK5() {
        String mv = "select a.empid, a.deptno from\n"
                + "(select * from emps where empid = 1) a\n"
                + "join depts using (deptno)";
        String query = "select empid from emps where empid = 1";
        testRewriteOK(mv, query)
                .contains("0:OlapScanNode\n" +
                        "     TABLE: mv0\n" +
                        "     PREAGGREGATION: ON\n");
    }

    @Test
    public void testViewDeltaJoinUKFK6() {
        String mv = "select emps.empid, emps.deptno from emps\n"
                + "join depts using (deptno)\n"
                + "join dependents using (empid)"
                + "where emps.empid = 1";
        String query = "select emps.empid from emps\n"
                + "join dependents using (empid)\n"
                + "where emps.empid = 1";
        testRewriteOK(mv, query);
    }

    @Test
    public void testViewDeltaJoinUKFK7() {
        String mv = "select emps.empid, emps.deptno from emps\n"
                + "join depts a on (emps.deptno=a.deptno)\n"
                + "join depts b on (emps.deptno=b.deptno)\n"
                + "join dependents using (empid)"
                + "where emps.empid = 1";

        String query = "select emps.empid from emps\n"
                + "join dependents using (empid)\n"
                + "where emps.empid = 1";
        testRewriteOK(mv, query);
    }

    // join key is not foreign key
    @Test
    public void testViewDeltaJoinUKFK8() {
        String mv = "select emps.empid, emps.deptno from emps\n"
                + "join depts a on (emps.name=a.name)\n"
                + "join depts b on (emps.name=b.name)\n"
                + "join dependents using (empid)"
                + "where emps.empid = 1";

        String query = "select emps.empid from emps\n"
                + "join dependents using (empid)\n"
                + "where emps.empid = 1";
        testRewriteFail(mv, query);
    }

    // join key is not foreign key
    @Test
    public void testViewDeltaJoinUKFK9() {
        String mv = "select emps.empid, emps.deptno from emps\n"
                + "join depts a on (emps.deptno=a.deptno)\n"
                + "join depts b on (emps.name=b.name)\n"
                + "join dependents using (empid)"
                + "where emps.empid = 1";

        String query = "select emps.empid from emps\n"
                + "join dependents using (empid)\n"
                + "where emps.empid = 1";
        testRewriteFail(mv, query);
    }

    @Test
    public void testViewDeltaJoinUKFK10() {
        String mv = "select emps.empid as empid1, emps.name, emps.deptno, dependents.empid as empid2 from emps\n"
                + "join dependents using (empid)";

        String query = "select emps.empid, dependents.empid, emps.deptno\n"
                + "from emps\n"
                + "join dependents using (empid)"
                + "join depts a on (emps.deptno=a.deptno)\n"
                + "where emps.name = 'Bill'";
        testRewriteOK(mv, query);
    }

    @Test
    public void testViewDeltaJoinUKFK11() {
        String mv = "select emps.empid, emps.deptno, dependents.name from emps\n"
                + "left outer join depts a on (emps.deptno=a.deptno)\n"
                + "inner join depts b on (emps.deptno=b.deptno)\n"
                + "inner join dependents using (empid)"
                + "where emps.empid = 1";

        String query = "select emps.empid, dependents.name from emps\n"
                + "inner join dependents using (empid)\n"
                + "where emps.empid = 1";
        testRewriteOK(mv, query);
    }

    @Test
    public void testViewDeltaJoinUKFK12() {
        String mv = "select emps.empid, emps.deptno, dependents.name from emps\n"
                + "inner join dependents using (empid)"
                + "left outer join depts a on (emps.deptno=a.deptno)\n"
                + "inner join depts b on (emps.deptno=b.deptno)\n"
                + "where emps.empid = 1";

        String query = "select emps.empid, dependents.name from emps\n"
                + "inner join dependents using (empid)\n"
                + "where emps.empid = 1";
        testRewriteOK(mv, query);
    }

    @Test
    public void testViewDeltaJoinUKFK13() {
        String mv = "select emps.empid, emps.deptno, dependents.name from emps\n"
                + "inner join dependents using (empid)"
                + "inner join depts b on (emps.deptno=b.deptno)\n"
                + "left outer join depts a on (emps.deptno=a.deptno)\n"
                + "where emps.empid = 1";

        String query = "select emps.empid, dependents.name from emps\n"
                + "inner join dependents using (empid)\n"
                + "where emps.empid = 1";
        testRewriteOK(mv, query);
    }

    @Test
    public void testViewDeltaJoinUKFK14() {
        String mv = "select emps.empid, emps.deptno, dependents.name from emps\n"
                + "inner join depts b on (emps.deptno=b.deptno)\n"
                + "left outer join dependents using (empid)"
                + "where emps.empid = 1";

        String query = "select emps.empid, dependents.name from emps\n"
                + "left outer join dependents using (empid)\n"
                + "where emps.empid = 1";
        testRewriteOK(mv, query);
    }

    @Test
    public void testViewDeltaColumnCaseSensitiveOnDuplicate() throws Exception {
        {
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_03` (\n" +
                    "    `c5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c5`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c5`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"colocate_with\" = \"groupa4\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"unique_constraints\" = \"C5\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_02` (\n" +
                    "    `c5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c5`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c5`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"colocate_with\" = \"groupa4\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"unique_constraints\" = \"C5\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_01` (\n" +
                    "    `c1` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C2` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c3` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C4` decimal(38, 19) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c1`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c1`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"colocate_with\" = \"groupa4\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"foreign_key_constraints\" = \"(c2) REFERENCES tbl_02(C5);(C3) REFERENCES tbl_02(C5)\"\n" +
                    ");");

            String mv = "select c1 as col1, c2, c3, l.c6, r.c7\n" +
                    " from tbl_01 join tbl_02 l on tbl_01.c2 = l.c5\n" +
                    " join tbl_02 r on tbl_01.c3 = r.c5\n" +
                    " join tbl_03 on tbl_01.c2 = tbl_03.c5";
            String query = "select c1, c2, c3, tbl_03.c5 from tbl_01 join tbl_03 on tbl_01.c2 = tbl_03.c5;";
            testRewriteOK(mv, query);

            starRocksAssert.dropTable("tbl_01");
            starRocksAssert.dropTable("tbl_02");
            starRocksAssert.dropTable("tbl_03");
        }

        {
            // multi key columns
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_03` (\n" +
                    "    `c5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c5`, `c6`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c5`, `c6`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"unique_constraints\" = \"C5\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_02` (\n" +
                    "    `C5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`C5`, `C6`, `c7`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`C5`, `C6`, `c7`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"unique_constraints\" = \"C5, C6, c7\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_01` (\n" +
                    "    `c1` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C2` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c3` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C4` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c1`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c1`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"foreign_key_constraints\" = \"(c2, c4, C3) REFERENCES tbl_02(c5, C7, C6)\"\n" +
                    ");");

            String mv = "select c1 as col1, c2, c3, l.c6, l.c7\n" +
                    " from tbl_01 join tbl_02 l on tbl_01.c2 = l.C5 and tbl_01.c3 = l.c6 and tbl_01.c4 = l.c7\n" +
                    " join tbl_03 on tbl_01.c2 = tbl_03.c5";
            String query = "select c1, c2, c3, tbl_03.c5 from tbl_01 join tbl_03 on tbl_01.c2 = tbl_03.c5;";
            testRewriteOK(mv, query);

            starRocksAssert.dropTable("tbl_01");
            starRocksAssert.dropTable("tbl_02");
            starRocksAssert.dropTable("tbl_03");
        }
    }

    @Test
    public void testViewDeltaColumnCaseSensitiveOnPrimary() throws Exception {
        {
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_03` (\n" +
                    "    `c5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c5`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c5`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"colocate_with\" = \"groupa4\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"unique_constraints\" = \"C5\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_02` (\n" +
                    "    `c5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "PRIMARY KEY(`c5`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c5`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"colocate_with\" = \"groupa4\",\n" +
                    "    \"in_memory\" = \"false\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_01` (\n" +
                    "    `c1` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C2` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c3` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C4` decimal(38, 19) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c1`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c1`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"colocate_with\" = \"groupa4\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"foreign_key_constraints\" = \"(c2) REFERENCES tbl_02(C5);(C3) REFERENCES tbl_02(c5)\"\n" +
                    ");");

            String mv = "select c1 as col1, c2, c3, l.c6, r.c7\n" +
                    " from tbl_01 join tbl_02 l on tbl_01.c2 = l.c5\n" +
                    " join tbl_02 r on tbl_01.c3 = r.c5\n" +
                    " join tbl_03 on tbl_01.c2 = tbl_03.c5";
            String query = "select c1, c2, c3, tbl_03.c5 from tbl_01 join tbl_03 on tbl_01.c2 = tbl_03.c5;";
            testRewriteOK(mv, query);

            starRocksAssert.dropTable("tbl_01");
            starRocksAssert.dropTable("tbl_02");
            starRocksAssert.dropTable("tbl_03");
        }

        {
            // multi key columns
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_03` (\n" +
                    "    `c5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c5`, `c6`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c5`, `c6`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"unique_constraints\" = \"C5\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_02` (\n" +
                    "    `C5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "PRIMARY KEY(`C5`, `C6`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`C5`, `C6`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_01` (\n" +
                    "    `c1` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C2` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c3` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C4` decimal(38, 19) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c1`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c1`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"foreign_key_constraints\" = \"(c2, C3) REFERENCES tbl_02(c5, C6)\"\n" +
                    ");");

            String mv = "select c1 as col1, c2, c3, l.c6, l.c7\n" +
                    " from tbl_01 join tbl_02 l on tbl_01.c2 = l.C5 and tbl_01.c3 = l.c6\n" +
                    " join tbl_03 on tbl_01.c2 = tbl_03.c5";
            String query = "select c1, c2, c3, tbl_03.c5 from tbl_01 join tbl_03 on tbl_01.c2 = tbl_03.c5;";
            testRewriteOK(mv, query);

            starRocksAssert.dropTable("tbl_01");
            starRocksAssert.dropTable("tbl_02");
            starRocksAssert.dropTable("tbl_03");
        }
    }


    @Test
    public void testViewDeltaColumnCaseSensitiveOnUnique() throws Exception {
        {
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_03` (\n" +
                    "    `c5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c5`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c5`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"unique_constraints\" = \"C5\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_02` (\n" +
                    "    `C5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "UNIQUE KEY(`C5`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`C5`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_01` (\n" +
                    "    `c1` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C2` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c3` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C4` decimal(38, 19) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c1`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c1`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"foreign_key_constraints\" = \"(c2) REFERENCES tbl_02(C5);(C3) REFERENCES tbl_02(c5)\"\n" +
                    ");");

            String mv = "select c1 as col1, c2, c3, l.c6, r.c7\n" +
                    " from tbl_01 join tbl_02 l on tbl_01.c2 = l.C5\n" +
                    " join tbl_02 r on tbl_01.c3 = r.c5\n" +
                    " join tbl_03 on tbl_01.c2 = tbl_03.c5";
            String query = "select c1, c2, c3, tbl_03.c5 from tbl_01 join tbl_03 on tbl_01.c2 = tbl_03.c5;";
            testRewriteOK(mv, query);

            starRocksAssert.dropTable("tbl_01");
            starRocksAssert.dropTable("tbl_02");
            starRocksAssert.dropTable("tbl_03");
        }

        {
            // multi key columns
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_03` (\n" +
                    "    `c5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c5`, `c6`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c5`, `c6`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"unique_constraints\" = \"C5\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_02` (\n" +
                    "    `C5` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c6` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c7` int(11) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "UNIQUE KEY(`C5`, `C6`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`C5`, `C6`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\"\n" +
                    ");");
            starRocksAssert.withTable("CREATE TABLE IF NOT EXISTS `tbl_01` (\n" +
                    "    `c1` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C2` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `c3` int(11) NOT NULL COMMENT \"\",\n" +
                    "    `C4` decimal(38, 19) NOT NULL COMMENT \"\"\n" +
                    ") ENGINE=OLAP\n" +
                    "DUPLICATE KEY(`c1`)\n" +
                    "COMMENT \"OLAP\"\n" +
                    "DISTRIBUTED BY HASH(`c1`) BUCKETS 12\n" +
                    "PROPERTIES (\n" +
                    "    \"replication_num\" = \"1\",\n" +
                    "    \"in_memory\" = \"false\",\n" +
                    "    \"foreign_key_constraints\" = \"(C3, c2) REFERENCES tbl_02(C6, c5)\"\n" +
                    ");");

            String mv = "select c1 as col1, c2, c3, l.c6, l.c7\n" +
                    " from tbl_01 join tbl_02 l on tbl_01.c2 = l.C5 and tbl_01.c3 = l.c6\n" +
                    " join tbl_03 on tbl_01.c2 = tbl_03.c5";
            String query = "select c1, c2, c3, tbl_03.c5 from tbl_01 join tbl_03 on tbl_01.c2 = tbl_03.c5;";
            testRewriteOK(mv, query);

            starRocksAssert.dropTable("tbl_01");
            starRocksAssert.dropTable("tbl_02");
            starRocksAssert.dropTable("tbl_03");
        }
    }

    @Test
    public void testJoinTypeMismatchRewriteForViewDelta() throws Exception {
        {
            String query = "select sum(lo_linenumber) from `lineorder`" +
                    " LEFT OUTER JOIN `dates`" +
                    " ON `lineorder`.`lo_orderdate` = `dates`.`d_datekey` WHERE `lineorder`.`lo_orderdate` = 19961001;";

            String mv = "SELECT\n" +
                    "       l.LO_ORDERKEY AS LO_ORDERKEY,\n" +
                    "       l.LO_LINENUMBER AS LO_LINENUMBER,\n" +
                    "       l.LO_CUSTKEY AS LO_CUSTKEY,\n" +
                    "       l.LO_PARTKEY AS LO_PARTKEY,\n" +
                    "       l.LO_SUPPKEY AS LO_SUPPKEY,\n" +
                    "       l.LO_ORDERDATE AS LO_ORDERDATE,\n" +
                    "       l.LO_ORDERPRIORITY AS LO_ORDERPRIORITY,\n" +
                    "       l.LO_SHIPPRIORITY AS LO_SHIPPRIORITY,\n" +
                    "       l.LO_QUANTITY AS LO_QUANTITY,\n" +
                    "       l.LO_EXTENDEDPRICE AS LO_EXTENDEDPRICE,\n" +
                    "       l.LO_ORDTOTALPRICE AS LO_ORDTOTALPRICE,\n" +
                    "       l.LO_DISCOUNT AS LO_DISCOUNT,\n" +
                    "       l.LO_REVENUE AS LO_REVENUE,\n" +
                    "       l.LO_SUPPLYCOST AS LO_SUPPLYCOST,\n" +
                    "       l.LO_TAX AS LO_TAX,\n" +
                    "       l.LO_COMMITDATE AS LO_COMMITDATE,\n" +
                    "       l.LO_SHIPMODE AS LO_SHIPMODE,\n" +
                    "       c.C_NAME AS C_NAME,\n" +
                    "       c.C_ADDRESS AS C_ADDRESS,\n" +
                    "       c.C_CITY AS C_CITY,\n" +
                    "       c.C_NATION AS C_NATION,\n" +
                    "       c.C_REGION AS C_REGION,\n" +
                    "       c.C_PHONE AS C_PHONE,\n" +
                    "       c.C_MKTSEGMENT AS C_MKTSEGMENT,\n" +
                    "       s.S_NAME AS S_NAME,\n" +
                    "       s.S_ADDRESS AS S_ADDRESS,\n" +
                    "       s.S_CITY AS S_CITY,\n" +
                    "       s.S_NATION AS S_NATION,\n" +
                    "       s.S_REGION AS S_REGION,\n" +
                    "       s.S_PHONE AS S_PHONE,\n" +
                    "       p.P_NAME AS P_NAME,\n" +
                    "       p.P_MFGR AS P_MFGR,\n" +
                    "       p.P_CATEGORY AS P_CATEGORY,\n" +
                    "       p.P_BRAND AS P_BRAND,\n" +
                    "       p.P_COLOR AS P_COLOR,\n" +
                    "       p.P_TYPE AS P_TYPE,\n" +
                    "       p.P_SIZE AS P_SIZE,\n" +
                    "       p.P_CONTAINER AS P_CONTAINER,\n" +
                    "       d.d_date AS d_date,\n" +
                    "       d.d_dayofweek AS d_dayofweek,\n" +
                    "       d.d_month AS d_month,\n" +
                    "       d.d_year AS d_year,\n" +
                    "       d.d_yearmonthnum AS d_yearmonthnum,\n" +
                    "       d.d_yearmonth AS d_yearmonth,\n" +
                    "       d.d_daynuminweek AS d_daynuminweek,\n" +
                    "       d.d_daynuminmonth AS d_daynuminmonth,\n" +
                    "       d.d_daynuminyear AS d_daynuminyear,\n" +
                    "       d.d_monthnuminyear AS d_monthnuminyear,\n" +
                    "       d.d_weeknuminyear AS d_weeknuminyear,\n" +
                    "       d.d_sellingseason AS d_sellingseason,\n" +
                    "       d.d_lastdayinweekfl AS d_lastdayinweekfl,\n" +
                    "       d.d_lastdayinmonthfl AS d_lastdayinmonthfl,\n" +
                    "       d.d_holidayfl AS d_holidayfl,\n" +
                    "       d.d_weekdayfl AS d_weekdayfl\n" +
                    "   FROM lineorder AS l\n" +
                    "            INNER JOIN customer AS c ON c.C_CUSTKEY = l.LO_CUSTKEY\n" +
                    "            INNER JOIN supplier AS s ON s.S_SUPPKEY = l.LO_SUPPKEY\n" +
                    "            INNER JOIN part AS p ON p.P_PARTKEY = l.LO_PARTKEY\n" +
                    "            INNER JOIN dates AS d ON l.lo_orderdate = d.d_datekey;";
            testRewriteFail(mv, query);
        }

        {
            String mv = "select" +
                    " l.LO_ORDERKEY AS LO_ORDERKEY,\n" +
                    " l.LO_LINENUMBER AS LO_LINENUMBER,\n" +
                    " l.LO_CUSTKEY AS LO_CUSTKEY,\n" +
                    " l.LO_PARTKEY AS LO_PARTKEY," +
                    " c.C_NAME AS C_NAME,\n" +
                    " c.C_ADDRESS AS C_ADDRESS,\n" +
                    " c.C_CITY AS C_CITY," +
                    " s.S_NAME AS S_NAME,\n" +
                    " s.S_ADDRESS AS S_ADDRESS,\n" +
                    " s.S_CITY AS S_CITY" +
                    " from lineorder AS l\n" +
                    " LEFT OUTER JOIN customer AS c ON c.C_CUSTKEY = l.LO_CUSTKEY\n" +
                    " LEFT OUTER JOIN supplier AS s ON s.S_SUPPKEY = l.LO_SUPPKEY";

            String query = "select" +
                    " l.LO_ORDERKEY AS LO_ORDERKEY,\n" +
                    " l.LO_LINENUMBER AS LO_LINENUMBER,\n" +
                    " l.LO_CUSTKEY AS LO_CUSTKEY,\n" +
                    " l.LO_PARTKEY AS LO_PARTKEY," +
                    " c.C_NAME AS C_NAME,\n" +
                    " c.C_ADDRESS AS C_ADDRESS,\n" +
                    " c.C_CITY AS C_CITY" +
                    " from customer AS c\n" +
                    " LEFT OUTER JOIN lineorder AS l ON c.C_CUSTKEY = l.LO_CUSTKEY";
            testRewriteFail(mv, query);
        }
    }
    @Test
    public void testRewriteAvg1() {
        String mv1 = "select user_id, avg(tag_id) from user_tags group by user_id;";
        testRewriteOK(mv1, "select user_id, avg(tag_id) from user_tags group by user_id;");
        String mv2 = "select user_id, sum(tag_id), count(tag_id) from user_tags group by user_id;";
        testRewriteOK(mv2, "select user_id, avg(tag_id) from user_tags group by user_id;");
    }

    @Test
    public void testRewriteAvg2() {
        String mv2 = "select user_id, time, sum(tag_id), count(tag_id) from user_tags group by user_id, time;";
        testRewriteOK(mv2, "select user_id, avg(tag_id) from user_tags group by user_id;");
    }

    @Test
    public void testRewriteAvg3() {
        String mv2 = "select user_id, time, sum(tag_id % 10), count(tag_id % 10) from user_tags group by user_id, time;";
        testRewriteOK(mv2, "select user_id, avg(tag_id % 10) from user_tags group by user_id;");
    }

    @Test
    public void testCountDistinctToBitmapCount1() {
        String mv = "select user_id, bitmap_union(to_bitmap(tag_id)) from user_tags group by user_id;";
        testRewriteOK(mv, "select user_id, bitmap_union(to_bitmap(tag_id)) x from user_tags group by user_id;");
        testRewriteOK(mv, "select user_id, bitmap_count(bitmap_union(to_bitmap(tag_id))) x from user_tags group by user_id;");
        testRewriteOK(mv, "select user_id, count(distinct tag_id) x from user_tags group by user_id;");
    }

    @Test
    public void testCountDistinctToBitmapCount2() {
        String mv = "select user_id, time, bitmap_union(to_bitmap(tag_id)) from user_tags group by user_id, time;";
        testRewriteOK(mv, "select user_id, bitmap_count(bitmap_union(to_bitmap(tag_id))) x from user_tags group by user_id;");
        // rewrite count distinct to bitmap_count(bitmap_union(to_bitmap(x)));
        testRewriteOK(mv, "select user_id, count(distinct tag_id) x from user_tags group by user_id;");
    }

    @Test
    public void testCountDistinctToBitmapCount3() {
        String mv = "select user_id, time, bitmap_union(to_bitmap(tag_id % 10)) from user_tags group by user_id, time;";
        testRewriteOK(mv, "select user_id, bitmap_union(to_bitmap(tag_id % 10)) x from user_tags group by user_id;");
        testRewriteOK(mv, "select user_id, bitmap_count(bitmap_union(to_bitmap(tag_id % 10))) x from user_tags group by user_id;");
        // rewrite count distinct to bitmap_count(bitmap_union(to_bitmap(x)));
        testRewriteOK(mv, "select user_id, count(distinct tag_id % 10) x from user_tags group by user_id;");
    }

    @Test
    public void testCountDistinctToBitmapCount4() {
        String mv = "select user_id, tag_id from user_tags where user_id > 10;";
        testRewriteOK(mv, "select user_id, bitmap_union(to_bitmap(tag_id)) x from user_tags where user_id > 10 group by user_id ;");
        testRewriteOK(mv, "select user_id, bitmap_count(bitmap_union(to_bitmap(tag_id))) x from user_tags where user_id > 10 group by user_id;");
        testRewriteOK(mv, "select user_id, count(distinct tag_id) x from user_tags  where user_id > 10 group by user_id;");
    }

    @Test
    public void testCountDistinctToBitmapCount5() {
        String mv = "select user_id, tag_id from user_tags;";
        testRewriteOK(mv, "select user_id, bitmap_union(to_bitmap(tag_id)) x from user_tags where user_id > 10 group by user_id ;");
        testRewriteOK(mv, "select user_id, bitmap_count(bitmap_union(to_bitmap(tag_id))) x from user_tags where user_id > 10 group by user_id;");
        testRewriteOK(mv, "select user_id, count(distinct tag_id) x from user_tags group by user_id;");
    }

    @Test
    public void testCountDistinctToBitmapCount6() {
        String mv = "select user_id, count(tag_id) from user_tags group by user_id;";
        testRewriteFail(mv, "select user_id, bitmap_union(to_bitmap(tag_id)) x from user_tags where user_id > 10 group by user_id ;");
        testRewriteFail(mv, "select user_id, bitmap_count(bitmap_union(to_bitmap(tag_id))) x from user_tags where user_id > 10 group by user_id;");
        testRewriteFail(mv, "select user_id, count(distinct tag_id) x from user_tags group by user_id;");
    }

    @Test
    public void testBitmapUnionCountToBitmapCount1() {
        String mv = "select user_id, bitmap_union(to_bitmap(tag_id)) from user_tags group by user_id;";
        testRewriteOK(mv, "select user_id, bitmap_union_count(to_bitmap(tag_id)) x from user_tags group by user_id;");
    }

    @Test
    public void testBitmapUnionCountToBitmapCount2() {
        String mv = "select user_id, time, bitmap_union(to_bitmap(tag_id)) from user_tags group by user_id, time;";
        testRewriteOK(mv, "select user_id, bitmap_union_count(to_bitmap(tag_id)) x from user_tags group by user_id;");
    }

    @Test
    public void testApproxCountToHLL1() {
        String mv = "select user_id, time, hll_union(hll_hash(tag_id)) from user_tags group by user_id, time;";
        testRewriteOK(mv, "select user_id, approx_count_distinct(tag_id) x from user_tags group by user_id;");
        testRewriteOK(mv, "select user_id, ndv(tag_id) x from user_tags group by user_id;");
        testRewriteOK(mv, "select user_id, hll_union(hll_hash(tag_id)) x from user_tags group by user_id;");
    }

    @Test
    public void testApproxCountToHLL2() {
        String mv = "select user_id, hll_union(hll_hash(tag_id)) from user_tags group by user_id;";
        testRewriteOK(mv, "select user_id, approx_count_distinct(tag_id) x from user_tags group by user_id;");
        testRewriteOK(mv, "select user_id, ndv(tag_id) x from user_tags group by user_id;");
        testRewriteOK(mv, "select user_id, hll_union(hll_hash(tag_id)) x from user_tags group by user_id;");
    }

    @Test
    public void testPercentile1() {
        String mv = "select user_id, percentile_union(percentile_hash(tag_id)) from user_tags group by user_id;";
        testRewriteOK(mv, "select user_id, percentile_approx(tag_id, 1) x from user_tags group by user_id;");
        testRewriteOK(mv, "select user_id, percentile_approx(tag_id, 0) x from user_tags group by user_id;");
        // testRewriteOK(mv, "select user_id, round(percentile_approx(tag_id, 0)) x from user_tags group by user_id;");
    }

    @Test
    public void testPercentile2() {
        String mv = "select user_id, time, percentile_union(percentile_hash(tag_id)) from user_tags group by user_id, time;";
        testRewriteOK(mv, "select user_id, percentile_approx(tag_id, 1) x from user_tags group by user_id;");
        testRewriteOK(mv, "select user_id, round(percentile_approx(tag_id, 0)) x from user_tags group by user_id;");
    }

    @Test
    public void testQueryWithLimitRewrite() throws Exception {
        starRocksAssert.withTable("CREATE TABLE `aggregate_table_with_null` (\n" +
                "  `k1` date NULL COMMENT \"\",\n" +
                "  `k2` datetime NULL COMMENT \"\",\n" +
                "  `k3` char(20) NULL COMMENT \"\",\n" +
                "  `k4` varchar(20) NULL COMMENT \"\",\n" +
                "  `k5` boolean NULL COMMENT \"\",\n" +
                "  `v1` bigint(20) SUM NULL COMMENT \"\",\n" +
                "  `v2` bigint(20) SUM NULL COMMENT \"\",\n" +
                "  `v3` bigint(20) SUM NULL COMMENT \"\",\n" +
                "  `v4` bigint(20) MAX NULL COMMENT \"\",\n" +
                "  `v5` largeint(40) MAX NULL COMMENT \"\",\n" +
                "  `v6` float MIN NULL COMMENT \"\",\n" +
                "  `v7` double MIN NULL COMMENT \"\",\n" +
                "  `v8` decimal128(38, 9) SUM NULL COMMENT \"\"\n" +
                ") ENGINE=OLAP\n" +
                "AGGREGATE KEY(`k1`, `k2`, `k3`, `k4`, `k5`)\n" +
                "COMMENT \"OLAP\"\n" +
                "DISTRIBUTED BY HASH(`k1`, `k2`, `k3`, `k4`, `k5`) BUCKETS 3\n" +
                "PROPERTIES (\n" +
                "\"replication_num\" = \"1\",\n" +
                "\"in_memory\" = \"false\",\n" +
                "\"storage_format\" = \"V2\",\n" +
                "\"enable_persistent_index\" = \"false\",\n" +
                "\"replicated_storage\" = \"true\",\n" +
                "\"compression\" = \"LZ4\"\n" +
                ");");

        starRocksAssert.withTable("CREATE TABLE `duplicate_table_with_null_partition` (\n" +
                "  `k1` date NULL COMMENT \"\",\n" +
                "  `k2` datetime NULL COMMENT \"\",\n" +
                "  `k3` char(20) NULL COMMENT \"\",\n" +
                "  `k4` varchar(20) NULL COMMENT \"\",\n" +
                "  `k5` boolean NULL COMMENT \"\",\n" +
                "  `k6` tinyint(4) NULL COMMENT \"\",\n" +
                "  `k7` smallint(6) NULL COMMENT \"\",\n" +
                "  `k8` int(11) NULL COMMENT \"\",\n" +
                "  `k9` bigint(20) NULL COMMENT \"\",\n" +
                "  `k10` largeint(40) NULL COMMENT \"\",\n" +
                "  `k11` float NULL COMMENT \"\",\n" +
                "  `k12` double NULL COMMENT \"\",\n" +
                "  `k13` decimal128(27, 9) NULL COMMENT \"\"\n" +
                ") ENGINE=OLAP\n" +
                "DUPLICATE KEY(`k1`, `k2`, `k3`, `k4`, `k5`)\n" +
                "COMMENT \"OLAP\"\n" +
                "PARTITION BY RANGE(`k1`)\n" +
                "(PARTITION p202006 VALUES [(\"0000-01-01\"), (\"2020-07-01\")),\n" +
                "PARTITION p202007 VALUES [(\"2020-07-01\"), (\"2020-08-01\")),\n" +
                "PARTITION p202008 VALUES [(\"2020-08-01\"), (\"2020-09-01\")))\n" +
                "DISTRIBUTED BY HASH(`k1`, `k2`, `k3`, `k4`, `k5`) BUCKETS 3\n" +
                "PROPERTIES (\n" +
                "\"replication_num\" = \"1\",\n" +
                "\"in_memory\" = \"false\",\n" +
                "\"storage_format\" = \"V2\",\n" +
                "\"enable_persistent_index\" = \"false\",\n" +
                "\"replicated_storage\" = \"true\",\n" +
                "\"compression\" = \"LZ4\"\n" +
                ");");
        {
            String query = "SELECT t1.k1 k1, t2.k2 k2" +
                    " from duplicate_table_with_null_partition t1 join aggregate_table_with_null t2" +
                    " on t1.k1 = t2.k1 limit 10";
            String mv = "SELECT t1.k1 k1, t2.k2 k2" +
                    " from duplicate_table_with_null_partition t1 join aggregate_table_with_null t2" +
                    " on t1.k1 = t2.k1";
            MVRewriteChecker checker = testRewriteOK(mv, query);
            checker.contains("limit: 10");
        }

        {
            String query = "SELECT t1.k1 k1, sum(t2.v1) as s" +
                    " from duplicate_table_with_null_partition t1 join aggregate_table_with_null t2" +
                    " on t1.k1 = t2.k1 group by t1.k1 limit 10";
            String mv = "SELECT t1.k1 k1, sum(t2.v1) as s" +
                    " from duplicate_table_with_null_partition t1 join aggregate_table_with_null t2" +
                    " on t1.k1 = t2.k1 group by t1.k1";
            MVRewriteChecker checker = testRewriteOK(mv, query);
            checker.contains("limit: 10");
        }
        starRocksAssert.dropTable("duplicate_table_with_null_partition");
        starRocksAssert.dropTable("aggregate_table_with_null");
    }
}
