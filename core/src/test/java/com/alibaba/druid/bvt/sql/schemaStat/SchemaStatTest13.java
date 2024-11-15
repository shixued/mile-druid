package com.alibaba.druid.bvt.sql.schemaStat;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import junit.framework.TestCase;

import java.util.Set;

public class SchemaStatTest13 extends TestCase {
    public void test_schemaStat() throws Exception {
        String sql = "select a.id, b.name from (select t1.* from table1 t1 left join table3 t3 on cast(t1.id as bigint) = t3.id) a inner join table2 b on a.id = b.id";

        DbType dbType = JdbcConstants.ORACLE;
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        SQLStatement stmt = parser.parseStatementList().get(0);

        System.out.println(stmt);

        SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(dbType);
        stmt.accept(statVisitor);

        Set<TableStat.Relationship> relationships = statVisitor.getRelationships();

        System.out.println("columns : " + statVisitor.getColumns());
        System.out.println("groups : " + statVisitor.getGroupByColumns()); // group by
        System.out.println("relationships : " + statVisitor.getRelationships()); // group by
        System.out.println("conditions : " + statVisitor.getConditions());
//        assertEquals(2, relationships.size());

        assertEquals(5, statVisitor.getColumns().size());
//        assertEquals(3, statVisitor.getConditions().size());
        assertEquals(0, statVisitor.getFunctions().size());
    }
}
