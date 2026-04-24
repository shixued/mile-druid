/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerSelectQueryBlock;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerTop;
import com.alibaba.druid.sql.parser.*;

public class SQLServerSelectParser extends SQLSelectParser {

    public SQLServerSelectParser(String sql){
        super(new SQLServerExprParser(sql));
    }

    public SQLServerSelectParser(SQLExprParser exprParser){
        super(exprParser);
    }

    public SQLServerSelectParser(SQLExprParser exprParser, SQLSelectListCache selectListCache){
        super(exprParser, selectListCache);
    }

    public SQLSelect select() {
        SQLSelect select = new SQLSelect();

        if (lexer.token() == Token.WITH) {
            SQLWithSubqueryClause with = this.parseWith();
            select.setWithSubQuery(with);
        }

        select.setQuery(query());
        select.setOrderBy(parseOrderBy());

        if (select.getOrderBy() == null) {
            select.setOrderBy(parseOrderBy());
        }

        if (lexer.token() == Token.FOR) {
            lexer.nextToken();

            if (lexer.identifierEquals("BROWSE")) {
                lexer.nextToken();
                select.setForBrowse(true);
            } else if (lexer.identifierEquals("XML")) {
                lexer.nextToken();

                for (;;) {
                    if (lexer.identifierEquals("AUTO") //
                        || lexer.identifierEquals("TYPE") //
                        || lexer.identifierEquals("XMLSCHEMA") //
                    ) {
                        select.getForXmlOptions().add(lexer.stringVal());
                        lexer.nextToken();
                    } else if (lexer.identifierEquals("ELEMENTS")) {
                        lexer.nextToken();
                        if (lexer.identifierEquals("XSINIL")) {
                            lexer.nextToken();
                            select.getForXmlOptions().add("ELEMENTS XSINIL");
                        } else {
                            select.getForXmlOptions().add("ELEMENTS");
                        }
                    } else if (lexer.identifierEquals("PATH")) {
                        SQLExpr xmlPath = this.exprParser.expr();
                        select.setXmlPath(xmlPath);
                    } else {
                        break;
                    }
                    
                    if (lexer.token() == Token.COMMA) {
                        lexer.nextToken();
                        continue;
                    } else {
                        break;
                    }
                }
            } else {
                throw new ParserException("syntax error, not support option : " + lexer.token() + ", " + lexer.info());
            }
        }
        
        if (lexer.identifierEquals("OFFSET")) {
            lexer.nextToken();
            SQLExpr offset = this.expr();
            
            acceptIdentifier("ROWS");
            select.setOffset(offset);
            
            if (lexer.token() == Token.FETCH) {
                lexer.nextToken();
                acceptIdentifier("NEXT");
                
                SQLExpr rowCount = expr();
                acceptIdentifier("ROWS");
                acceptIdentifier("ONLY");
                select.setRowCount(rowCount);
            }
        }

        return select;
    }

    public SQLSelectQuery query(SQLObject parent, boolean acceptUnion) {
        if (lexer.token() == Token.LPAREN) {
            lexer.nextToken();

            SQLSelectQuery select = query();
            accept(Token.RPAREN);

            return queryRest(select, acceptUnion);
        }

        SQLServerSelectQueryBlock queryBlock = new SQLServerSelectQueryBlock();

        if (lexer.token() == Token.SELECT) {
            lexer.nextToken();

            if (lexer.token() == Token.COMMENT) {
                lexer.nextToken();
            }

            if (lexer.token() == Token.DISTINCT) {
                queryBlock.setDistionOption(SQLSetQuantifier.DISTINCT);
                lexer.nextToken();
            } else if (lexer.token() == Token.ALL) {
                queryBlock.setDistionOption(SQLSetQuantifier.ALL);
                lexer.nextToken();
            }

            if (lexer.token() == Token.TOP) {
                SQLServerTop top = this.createExprParser().parseTop();
                queryBlock.setTop(top);
            }

            parseSelectList(queryBlock);
        }

        if (lexer.token() == Token.INTO) {
            lexer.nextToken();

            SQLTableSource into = this.parseTableSource();
            queryBlock.setInto((SQLExprTableSource) into);
        }

        parseFrom(queryBlock);

        parseWhere(queryBlock);

        parseGroupBy(queryBlock);

        queryBlock.setOrderBy(this.exprParser.parseOrderBy());

        parseFetchClause(queryBlock);

        return queryRest(queryBlock, acceptUnion);
    }

    protected SQLServerExprParser createExprParser() {
        return new SQLServerExprParser(lexer);
    }

    // 解析 SQLServer 的表 Hint，形如：
    // (NOLOCK)、(INDEX(ix_xxx))、(NOLOCK, INDEX(ix_xxx))
    private void parseTableHints(SQLTableSource tableSource) {
        accept(Token.LPAREN);
        for (;;) {
            SQLExpr expr = this.expr();
            SQLExprHint hint = new SQLExprHint(expr);
            hint.setParent(tableSource);
            tableSource.getHints().add(hint);
            if (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                continue;
            }
            break;
        }
        accept(Token.RPAREN);
    }

    private boolean isOldStyleTableHintStart() {
        if (lexer.token() == Token.INDEX) {
            return true;
        }

        if (lexer.token() != Token.IDENTIFIER) {
            return false;
        }

        String ident = lexer.stringVal();
        if (ident == null) {
            return false;
        }

        return "nolock".equalsIgnoreCase(ident)
                || "holdlock".equalsIgnoreCase(ident)
                || "readpast".equalsIgnoreCase(ident)
                || "readuncommitted".equalsIgnoreCase(ident)
                || "readcommitted".equalsIgnoreCase(ident)
                || "readcommittedlock".equalsIgnoreCase(ident)
                || "repeatableread".equalsIgnoreCase(ident)
                || "serializable".equalsIgnoreCase(ident)
                || "snapshot".equalsIgnoreCase(ident)
                || "paglock".equalsIgnoreCase(ident)
                || "rowlock".equalsIgnoreCase(ident)
                || "tablock".equalsIgnoreCase(ident)
                || "tablockx".equalsIgnoreCase(ident)
                || "updlock".equalsIgnoreCase(ident)
                || "xlock".equalsIgnoreCase(ident)
                || "forceseek".equalsIgnoreCase(ident)
                || "forcescan".equalsIgnoreCase(ident)
                || "nowait".equalsIgnoreCase(ident)
                || "noexpand".equalsIgnoreCase(ident);
    }

    public SQLTableSource parseTableSourceRest(SQLTableSource tableSource) {
        // 兼容 SQLServer 老写法：alias(nolock)
        // 这里必须先 mark/reset 探测，否则会破坏正常别名解析流程。
        if (tableSource.getAlias() == null && lexer.token() == Token.IDENTIFIER) {
            Lexer.SavePoint mark = lexer.mark();
            String alias = lexer.stringVal();
            lexer.nextToken();
            if (lexer.token() == Token.LPAREN) {
                Lexer.SavePoint lparenMark = lexer.mark();
                lexer.nextToken();
                if (isOldStyleTableHintStart()) {
                    // 回退到 '('，让 parseTableHints 从完整 Hint 块起始位置统一消费。
                    lexer.reset(lparenMark);
                    tableSource.setAlias(alias);
                    parseTableHints(tableSource);
                } else {
                    // 不是 Hint，恢复到探测前，交给原有解析逻辑处理。
                    lexer.reset(mark);
                }
            } else {
                lexer.reset(mark);
            }
        }

        if (lexer.token() == Token.WITH) {
            lexer.nextToken();
            parseTableHints(tableSource);
        } else if (lexer.token() == Token.LPAREN) {
            // 兼容 SQLServer 老写法：table(nolock)
            // 如果这里不消费该括号块，'(' 会泄漏到 statement 级解析，
            // 最终可能落到 SQLStatementParser 的 TODO 分支并抛异常。
            Lexer.SavePoint mark = lexer.mark();
            lexer.nextToken();
            if (isOldStyleTableHintStart()) {
                // 回退到 '('，再统一按 Hint 语法消费。
                lexer.reset(mark);
                parseTableHints(tableSource);
            } else {
                lexer.reset(mark);
            }
        }

        return super.parseTableSourceRest(tableSource);
    }
}
