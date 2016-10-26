/*   
 * Copyright 2008-2012 the original author or authors.   
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
package org.anyframe.ide.command.maven.mojo.codegen.dialect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.cfg.reveng.dialect.JDBCMetaDataDialect;
import org.hibernate.cfg.reveng.dialect.ResultSetIterator;
import org.hibernate.mapping.Table;

/**
 * This is a SybaseMetaDataDialect class.
 * 
 * @author Sooyeon Park
 */
public class SybaseMetaDataDialect extends JDBCMetaDataDialect {

	public Iterator getPrimaryKeys(final String xcatalog, final String xschema,
			final String xtable) {
		try {
			final String catalog = caseForSearch(xcatalog);
			final String schema = caseForSearch(xschema);
			final String table = caseForSearch(xtable);

			log.debug("getPrimaryKeys(" + catalog + "." + schema + "." + table
					+ ")");
			ResultSet tableRs = getMetaData().getPrimaryKeys(catalog, schema,
					table);

			return new ResultSetIterator(tableRs, getSQLExceptionConverter()) {

				Map element = new HashMap();

				protected Object convertRow(ResultSet rs) throws SQLException {
					element.clear();
					putTablePart(element, rs);
					element.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
					element.put("KEY_SEQ", new Short(rs.getShort("KEY_SEQ")));
					// element.put("PK_NAME", rs.getString("PK_NAME"));
					element.put("PK_NAME", null);
					return element;
				}

				protected Throwable handleSQLException(SQLException e) {
					throw getSQLExceptionConverter().convert(
							e,
							"Error while reading primary key meta data for "
									+ Table.qualify(catalog, schema, table),
							null);
				}
			};
		} catch (SQLException e) {
			throw getSQLExceptionConverter().convert(
					e,
					"Error while reading primary key meta data for "
							+ Table.qualify(xcatalog, xschema, xtable), null);
		}
	}

}
