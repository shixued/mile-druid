package com.alibaba.druid.bvt.sql.sqlserver.select;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.util.JdbcConstants;
import junit.framework.TestCase;

import java.util.List;

public class SQLServerSelectTest35_oldstyle_nolock extends TestCase {

    public void test_oldstyle_nolock() throws Exception {
        String sql = "select count(*) from mzjzjlb b(nolock)";

        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.SQL_SERVER);

        String text = SQLUtils.toSQLString(stmtList, JdbcConstants.SQL_SERVER);
        assertEquals("SELECT count(*)\n" +
                "FROM mzjzjlb b WITH (NOLOCK)", text);
    }
}
