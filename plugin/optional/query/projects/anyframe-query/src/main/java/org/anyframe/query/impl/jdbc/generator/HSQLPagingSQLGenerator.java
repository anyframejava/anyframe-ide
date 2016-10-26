/*
 * Copyright 2002-2012 the original author or authors.
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
package org.anyframe.query.impl.jdbc.generator;

import java.sql.Types;

/**
 * @author Soyon Lim
 */
public class HSQLPagingSQLGenerator extends AbstractPagingSQLGenerator {
    // override because hsql can't execute a sql, in
    // case inner sql has OrderBy
    // phase.
    public String getCountSQL(String originalSql) {
        int idx = originalSql.toLowerCase().lastIndexOf("order by");
        StringBuilder sql = new StringBuilder("SELECT count(*) FROM ( ");
        if (idx != -1)
            sql.append(originalSql.substring(0, idx));
        else
            sql.append(originalSql); 
        sql.append(" )");
        return sql.toString();
    }

    public String getPaginationSQL(String originalSql, Object[] originalArgs,
            int[] originalArgTypes, int pageIndex, int pageSize) {
        return
            new StringBuilder(originalSql.length() + 10).append(originalSql)
                .insert(originalSql.toLowerCase().indexOf("select") + 6,
                    " limit ? ?").toString();
    }

    public Object[] setQueryArgs(Object[] originalArgs, int pageIndex,
            int pageSize) {
        Object[] args = new Object[originalArgs.length + 2];

        args[0] = new Integer((pageIndex - 1) * pageSize);
        args[1] = new Integer(pageSize);
        System.arraycopy(originalArgs, 0, args, 2, originalArgs.length);
        
        return args;
    }

    public int[] setQueryArgTypes(int[] originalArgTypes) {
        int[] argTypes = new int[originalArgTypes.length + 2];

        argTypes[0] = Types.INTEGER;
        argTypes[1] = Types.INTEGER;
        System.arraycopy(originalArgTypes, 0, argTypes, 2, originalArgTypes.length);
        
        return argTypes;
    }
}
