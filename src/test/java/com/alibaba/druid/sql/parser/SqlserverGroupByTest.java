package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerStatementParser;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Created by magicdoom on 2016/8/16.
 */
public class SqlserverGroupByTest extends TestCase
{
    public void testGroupBy() throws Exception {
        String sql = "SELECT a.workflowid, COUNT(1) FROM workflow_base a GROUP BY a.workflowid";
        SQLStatementParser parser = new SQLServerStatementParser(sql);
        SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement(); //
        try {
            stmt.toString();
        }  catch (ClassCastException e)
        {
            Assert.fail(e.getMessage());
        }


    }



    public void testNolock() throws Exception {
        String sql = "select top 10 * from  MZSFJLB/*门诊收费记录表*/ a(nolock)";
        SQLStatementParser parser = new SQLServerStatementParser(sql);
        SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement(); //
        try {
            stmt.toString();
        }  catch (ClassCastException e)
        {
            Assert.fail(e.getMessage());
        }


    }

    public void testWithNolock() throws Exception {
        String sql = "select * from  MZSFJLB/*门诊收费记录表*/ with(nolock)";
        SQLStatementParser parser = new SQLServerStatementParser(sql);
        SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement(); //
        try {
            stmt.toString();
        }  catch (ClassCastException e)
        {
            Assert.fail(e.getMessage());
        }


    }
}
