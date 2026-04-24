package com.alibaba.druid.bvt.sql.sqlserver;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerStatementParser;
import com.alibaba.druid.sql.test.TestUtils;
import junit.framework.TestCase;
import org.junit.Assert;

public class SQLServerUpdateTest7_oldstyle_nolock extends TestCase {

    public void test_update_oldstyle_nolock() throws Exception {
        String sql = "update a set a.yydah='[基]'+yydah from mzghb a,mzjzjlb b(nolock) where a.jzlsh=b.jzlsh";

        SQLServerStatementParser parser = new SQLServerStatementParser(sql);
        SQLStatement stmt = parser.parseStatementList().get(0);

        String text = TestUtils.outputSqlServer(stmt);
        Assert.assertEquals("UPDATE a\n" +
                "SET a.yydah = '[基]' + yydah\n" +
                "FROM mzghb a, mzjzjlb b WITH (NOLOCK)\n" +
                "WHERE a.jzlsh = b.jzlsh", text);
    }
}
