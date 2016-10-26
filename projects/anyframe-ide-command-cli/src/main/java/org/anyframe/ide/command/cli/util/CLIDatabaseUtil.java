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
package org.anyframe.ide.command.cli.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This is an DatabaseUtil class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class CLIDatabaseUtil {

	public static synchronized String[] getTableListAsDomainName(
			PropertiesIO pio) throws Exception {
		String driverJarName = pio.readValue(PluginConstants.DB_DRIVER_PATH);
		String dbDriver = pio.readValue(PluginConstants.DB_DRIVER_CLASS);
		String dbUrl = pio.readValue(PluginConstants.DB_URL);
		String userName = pio.readValue(PluginConstants.DB_USERNAME);
		String password = pio.readValue(PluginConstants.DB_PASSWORD);
		String schemaName = pio.readValue(PluginConstants.DB_SCHEMA);
		String dbType = pio.readValue(PluginConstants.DB_TYPE);

		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = getConnection(driverJarName, dbDriver, dbUrl, userName,
					password);
			String tableNamePattern = null;
			if (dbType.equalsIgnoreCase("Sybase")) {
				schemaName = null;
			}

			rs = conn.getMetaData().getTables(conn.getCatalog(), schemaName,
					tableNamePattern, new String[] { "TABLE" });
			return getDomainNameFromRsList(rs, "TABLE_NAME");

		} catch (Exception e) {
			throw e;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception ex) { /* ignore */
				}
			if (conn != null)
				try {
					conn.close();
				} catch (Exception ex) { /* ignore */
				}
		}
	}

	private static String[] getDomainNameFromRsList(ResultSet rs,
			String columnName) throws SQLException {
		final ArrayList<String> list = new ArrayList<String>();
		if (rs != null) {
			while (rs.next()) {
				String tableName = (String) rs.getString(columnName);
				String domainName = getDomainName(tableName);
				list.add(domainName);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	private static String getDomainName(String tableName) {
		StringTokenizer st = new StringTokenizer(tableName.toUpperCase(), "_");
		String domainName = "";
		while (st.hasMoreElements()) {
			String token = (String) st.nextElement();
			domainName += token.substring(0, 1).toUpperCase()
					+ token.substring(1).toLowerCase();
		}
		return domainName;
	}

	private static Connection getConnection(String driverJarName,
			String dbDriver, String dbUrl, String userName, String password)
			throws Exception {
		Properties connectionProps = new Properties();
		connectionProps.put("user", userName);
		connectionProps.put("password", password);

		try {
			Driver driver = getDriverFromPath(driverJarName, dbDriver);
			return driver.connect(dbUrl, connectionProps);
		} catch (Exception e) {
			throw e;
		}
	}

	private static Driver getDriverFromPath(String path, String className)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, MalformedURLException {
		URL[] url = { new File(path).toURL() };

		URLClassLoader loader = new URLClassLoader(url);

		Class<?> c = loader.loadClass(className);
		Object instance = c.newInstance();

		return (Driver) instance;
	}
}
