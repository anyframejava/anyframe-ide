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
package org.anyframe.ide.common.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;

import org.anyframe.ide.common.databases.JdbcOption;

/**
 * This is ConnectionUtil class.
 * 
 * @author Sujeong Lee
 */
// refer to DBViewer's concept
public class ConnectionUtil {

	public static Connection getConnection(JdbcOption jdbc) throws Exception {

		if (jdbc == null) {
			throw new IllegalStateException("There is no DB configuration.");

		}

		if (jdbc.getDriverClassName() == null) {
			throw new SQLException("Driver name is null for JDBC connection");
		}

		Driver driver = ConnectionUtil.getDriverFromPath(jdbc.getDriverJar(),
				jdbc.getDriverClassName());

		java.util.Properties info = new java.util.Properties();
		info.put("user", jdbc.getUserName());
		info.put("password", jdbc.getPassword());

		Connection connect = driver.connect(jdbc.getUrl(), info);
		// ((oracle.jdbc.OracleConnection)connect).setRemarksReporting(true);
		connect.setAutoCommit(false);
		return connect;

	}

	public static void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
				con = null;
			} catch (SQLException e) {
				PluginLogger.error(e);
			}
		}
	}

	/**
	 * load a Class from a path and get a class driver
	 * 
	 * @param path
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws MalformedURLException
	 */
	public static Driver getDriverFromPath(String path, String className)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, MalformedURLException {
		File dbLibFile = new File(path);
		if (!dbLibFile.exists()) {
			dbLibFile = new File(path);
		}
		URL[] url = { dbLibFile.toURL() };
		URLClassLoader loader = new URLClassLoader(url);
		Class<?> c = loader.loadClass(className);
		return (Driver) c.newInstance();
	}
}
