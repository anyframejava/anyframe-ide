/*
 * Copyright 2008-2011 the original author or authors.
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
package org.anyframe.ide.command.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.codehaus.plexus.util.IOUtil;

/**
 * This is an DBUtils class. This class is a utility for handling db.
 * 
 * @author SoYon Lim
 */
public class DBUtil {

	public static void runStatements(File targetDir, String pluginName,
			ZipFile pluginZipFile, List<String> dbScripts, String encoding,
			Properties metadata) throws Exception {
		Project project = new Project();

		SimpleSQLExec exec = new SimpleSQLExec();
		exec.setProject(project);
		exec.setDriver(metadata.getProperty("db.driver").trim());
		exec.setUrl(metadata.getProperty("db.url").trim());
		exec.setUserid(metadata.getProperty("db.userId").trim());
		exec.setPassword(metadata.getProperty("db.password").trim());
		exec.setEncoding(((null == encoding) || "".equals(encoding)) ? "UTF-8"
				: encoding);

		Path path = new Path(project);

		String dbLib = metadata.getProperty("db.lib").trim();

		File dbLibFile = new File(dbLib);
		if (!dbLibFile.exists()) {
			dbLibFile = new File(targetDir, metadata.getProperty("db.lib").trim());
		}
		path.setLocation(dbLibFile);
		exec.setClasspath(path);

		File temporaryDir = new File(targetDir, "temp-script");
		temporaryDir.mkdir();
		try {
			for (int i = 0; i < dbScripts.size(); i++) {
				String dbScript = (String) dbScripts.get(i);

				ZipEntry zipEntry = pluginZipFile.getEntry(dbScript);

				InputStream inputStream = pluginZipFile
						.getInputStream(zipEntry);

				String outputFileName = dbScript.lastIndexOf("/") != -1 ? dbScript
						.substring(dbScript.lastIndexOf("/") + 1)
						: dbScript;

				IOUtil.copy(inputStream, new FileOutputStream(new File(
						temporaryDir, outputFileName)));

				exec.setSrc(new File(temporaryDir, outputFileName));
			}
			exec.execute();
		} finally {
			FileUtil.deleteDir(temporaryDir, new ArrayList<String>());
		}
	}

	public static synchronized String[] getTableListAsDomainName(
			PropertiesIO pio) throws Exception {
		String driverJarName = pio.readValue(CommonConstants.DB_DRIVER_PATH);
		String dbDriver = pio.readValue(CommonConstants.DB_DRIVER_CLASS);
		String dbUrl = pio.readValue(CommonConstants.DB_URL);
		String userName = pio.readValue(CommonConstants.DB_USERNAME);
		String password = pio.readValue(CommonConstants.DB_PASSWORD);
		String schemaName = pio.readValue(CommonConstants.DB_SCHEMA);
		String dbType = pio.readValue(CommonConstants.DB_TYPE);

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
				} catch (Exception ex) {
					// ignore
				}
			if (conn != null)
				try {
					conn.close();
				} catch (Exception ex) {
					// ignore
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
