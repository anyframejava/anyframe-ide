/*
 * Copyright 2008-2013 the original author or authors.
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
package org.anyframe.ide.common.databases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.messages.Message;
import org.anyframe.ide.common.properties.PropertiesSettingUtil;
import org.anyframe.ide.common.util.EncryptUtil;
import org.anyframe.ide.common.util.ListUtil;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.ProjectUtil;
import org.anyframe.ide.common.util.StringUtil;
import org.anyframe.ide.common.util.XMLAnalyzer;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.w3c.dom.Document;

/**
 * This is DatabasesSettingUtil class.
 * 
 * @author Sujeong Lee
 */
public class DatabasesSettingUtil {

	public static List<JdbcOption> getDatasourcesByProject(IProject project) {
		return loadJdbcOptionList(project);
	}

	public static Map<String, JdbcOption> makeJdbcOptionMap(List<JdbcOption> list) {
		if (ListUtil.isEmptyOrNull(list)) {
			return null;
		}
		Map<String, JdbcOption> map = new LinkedHashMap<String, JdbcOption>();
		for (JdbcOption jdbcOption : list) {
			map.put(jdbcOption.getDbName(), jdbcOption);
		}
		return map;
	}

	public static Map<String, JdbcOption> loadJdbcOptionMap(IProject project) {
		return makeJdbcOptionMap(getDatasourcesByProject(project));
	}

	public static List<JdbcOption> loadJdbcOptionList(IProject project) {
		File file = new File(PropertiesSettingUtil.getDatabasesFile(project.getLocation().toOSString()));

		if (!file.exists()) {
			return null;
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(file);

			List<Map<String, String>> types = XMLAnalyzer.getData(document, Constants.XML_CONFIG_ROOT);
			if (types.size() > 0) {
				List<JdbcOption> jdbcOptions = new ArrayList<JdbcOption>();
				for (Map<String, String> type : types) {
					JdbcOption jdbc = new JdbcOption();
					jdbc.setDbType(type.get(Constants.XML_TAG_TYPE));
					jdbc.setDbName(type.get(Constants.XML_TAG_NAME));

					String driverPath = type.get(Constants.XML_TAG_DRIVER_PATH);
					if (!(driverPath.startsWith("\\") || driverPath.startsWith("/") || driverPath.indexOf(":") > 0)) {
						driverPath = new File(project.getLocation().toOSString() + Constants.FILE_SEPERATOR + driverPath).getAbsolutePath();
					}
					jdbc.setDriverJar(driverPath);
					jdbc.setDriverClassName(type.get(Constants.XML_TAG_DRIVER_CLASS_NAME));
					jdbc.setUrl(type.get(Constants.XML_TAG_URL));
					jdbc.setUserName(type.get(Constants.XML_TAG_USERNAME));
					jdbc.setPassword(EncryptUtil.decrypt(type.get(Constants.XML_TAG_PASSWORD)));
					jdbc.setSchema(type.get(Constants.XML_TAG_SCHEMA));

					jdbc.setDialect(type.get(Constants.XML_TAG_DIALECT));
					jdbc.setMvnGroupId(type.get(Constants.XML_TAG_DRIVER_GROUPID));
					jdbc.setMvnArtifactId(type.get(Constants.XML_TAG_DRIVER_ARTIFACTID));
					jdbc.setMvnVersion(type.get(Constants.XML_TAG_DRIVER_VERSION));

					jdbc.setUseDbSpecific("true".equalsIgnoreCase(type.get(Constants.XML_TAG_USE_DB_SPECIFIC)));
					jdbc.setRunExplainPaln("true".equalsIgnoreCase(type.get(Constants.XML_TAG_RUN_EXPLAIN_PLAN)));
					jdbc.setDefault("true".equalsIgnoreCase(type.get(Constants.XML_TAG_DEFAULT)));

					jdbcOptions.add(jdbc);
				}
				return jdbcOptions;
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, e.getMessage(), e);
		}
		return null;
	}

	public static boolean saveJdbcOptionList(IProject project, Collection<JdbcOption> list) {

		if (ListUtil.isEmptyOrNull(list)) {
			return false;
		}

		// IFile file = project.getFile(PropertiesSettingUtil
		// .getDatabasesFile(project.getLocation().toOSString()));
		File file = new File(PropertiesSettingUtil.getDatabasesFile(project.getLocation().toOSString()));

		StringBuilder contents = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<dbexplorer>\n");
		boolean isValidate = true;
		for (JdbcOption jdbc : list) {
			if (!validateData(project, jdbc)) {
				isValidate = false;
				break;
			}
			contents.append("\t<dataSource ");
			contents.append(makeTagElemStr(Constants.XML_TAG_TYPE, jdbc.getDbType()));
			contents.append(makeTagElemStr(Constants.XML_TAG_NAME, jdbc.getDbName()));
			contents.append(makeTagElemStr(Constants.XML_TAG_DRIVER_PATH, jdbc.getDriverJar()));
			contents.append(makeTagElemStr(Constants.XML_TAG_DRIVER_CLASS_NAME, jdbc.getDriverClassName()));
			contents.append(makeTagElemStr(Constants.XML_TAG_URL, jdbc.getUrl()));
			contents.append(makeTagElemStr(Constants.XML_TAG_USERNAME, jdbc.getUserName()));
			contents.append(makeTagElemStr(Constants.XML_TAG_PASSWORD, EncryptUtil.encrypt(jdbc.getPassword())));
			contents.append(makeTagElemStr(Constants.XML_TAG_SCHEMA, jdbc.getSchema() == null || jdbc.getSchema().equals(Constants.DB_NO_SCHEMA) ? ""
					: jdbc.getSchema()));

			contents.append(makeTagElemStr(Constants.XML_TAG_DIALECT, jdbc.getDialect()));
			contents.append(makeTagElemStr(Constants.XML_TAG_DRIVER_GROUPID, jdbc.getMvnGroupId()));
			contents.append(makeTagElemStr(Constants.XML_TAG_DRIVER_ARTIFACTID, jdbc.getMvnArtifactId()));
			contents.append(makeTagElemStr(Constants.XML_TAG_DRIVER_VERSION, jdbc.getMvnVersion()));

			contents.append(makeTagElemStr(Constants.XML_TAG_USE_DB_SPECIFIC, String.valueOf(jdbc.isUseDbSpecific())));
			contents.append(makeTagElemStr(Constants.XML_TAG_RUN_EXPLAIN_PLAN, String.valueOf(jdbc.isRunExplainPaln())));
			contents.append(makeTagElemStr(Constants.XML_TAG_DEFAULT, String.valueOf(jdbc.getDefault())));
			contents.append("/>\n");
		}
		contents.append("</dbexplorer>");
		try {
			if (!isValidate) {
				return false;
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(contents.toString());
			writer.close();

		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, e.getMessage(), e);
		}
		return true;
	}

	private static String makeTagElemStr(String xmlTagName, String tagValue) {
		return StringUtil.isEmptyOrNull(tagValue) ? "" : (xmlTagName + "=\"" + tagValue + "\" ");
	}

	// from here Community Database Setting Util
	/*
	 * public static Map<Object,Object> checkConnection(AnyframeConfig
	 * anyframeConfig) { String driverJarName =
	 * anyframeConfig.getJdbcDriverJar(); String dbDriver =
	 * anyframeConfig.getJdbcDriverName(); String userName =
	 * anyframeConfig.getDbUsername(); String password =
	 * anyframeConfig.getDbPassword(); String dbUrl = getDbUrl(anyframeConfig);
	 * return checkConnection( driverJarName,dbDriver,dbUrl,userName,password);
	 * }
	 */
	public static Map<Object, Object> checkConnection(String projectHome, String driverJarName, String dbDriver, String dbUrl, String userName,
			String password) {
		Map<Object, Object> result = new HashMap<Object, Object>();
		result.put(Constants.DB_CON_CHK_KEY, Constants.DB_CON_CHK);
		result.put(Constants.DB_CON_MSG_KEY, Constants.DB_CON_MSG);
		Connection connection = null;
		try {
			connection = getConnection(projectHome, driverJarName, dbDriver, dbUrl, userName, password);
		} catch (Exception e) {
			result.put(Constants.DB_CON_MSG_KEY, e.getMessage());
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_getconnection, e);
		} finally {
			if (connection != null)
				result.put(Constants.DB_CON_CHK_KEY, true);
			close(connection);
		}
		return result;
	}

	/*
	 * private static Connection getConnection(AnyframeConfig anyframeConfig)
	 * throws Exception { String driverJarName =
	 * anyframeConfig.getJdbcDriverJar(); String dbDriver =
	 * anyframeConfig.getJdbcDriverName(); String userName =
	 * anyframeConfig.getDbUsername(); String password =
	 * anyframeConfig.getDbPassword(); String dbUrl = getDbUrl(anyframeConfig);
	 * // String dbUrl //=
	 * "jdbc:sybase:Tds:129.100.254.241:3000?ServiceName=cti" ; return
	 * getConnection(driverJarName, dbDriver, dbUrl, userName, password); }
	 */
	public static Connection getConnection(String projectHome, String driverJarName, String dbDriver, String dbUrl, String userName, String password)
			throws Exception {
		Properties connectionProps = new Properties();
		connectionProps.put("user", userName);
		connectionProps.put("password", password);

		try {
			Driver driver = getDriverFromPath(projectHome, driverJarName, dbDriver);
			return driver.connect(dbUrl, connectionProps);
		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_getconnection, e);
			throw e;
		}
	}

	public static void close(Connection connection) {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_closeconnection, e);
		}
	}

	/*
	 * public static String getDbUrl(AnyframeConfig anyframeConfig) { String
	 * dbType = anyframeConfig.getDbType(); String dbName =
	 * anyframeConfig.getDbName(); String dbServer =
	 * anyframeConfig.getDbServer(); String dbPort =
	 * String.valueOf(anyframeConfig.getDbPort()); return getDbUrl(dbType,
	 * dbName, dbServer, dbPort); }
	 */
	public static String getDbUrl(String dbType, String dbName, String dbServer, String dbPort) {
		String dbUrl = "";
		if ("oracle".equals(dbType))
			dbUrl = Constants.DB_ORACLE_URL;
		else if ("hsqldb".equals(dbType) && "file".equals(dbServer))
			dbUrl = Constants.DB_HSQL_FILE_URL;
		else if ("hsqldb".equals(dbType) && "-1".equals(dbPort))
			dbUrl = Constants.DB_HSQL_SERVER_URL;
		else if ("hsqldb".equals(dbType) && !"-1".equals(dbPort))
			dbUrl = Constants.DB_HSQL_SERVER_PORT_URL;
		else if ("mysql".equals(dbType))
			dbUrl = Constants.DB_MYSQL_URL;
		else if ("sybase".equals(dbType))
			dbUrl = Constants.DB_SYBASE_URL;
		else if ("db2".equals(dbType))
			dbUrl = Constants.DB_DB2_URL;
		else if ("mssql".equals(dbType))
			dbUrl = Constants.DB_MSSQL_URL;

		dbUrl = dbUrl.replace("<server>", dbServer);
		dbUrl = dbUrl.replace("<port>", dbPort);
		dbUrl = dbUrl.replace("<database_name>", dbName);

		return dbUrl;
	}

	public static Driver getDriverFromPath(String projectHome, String path, String className) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, MalformedURLException {
		File dbLibFile = new File(path);
		if (!dbLibFile.exists()) {
			dbLibFile = new File(projectHome, path);
		}

		URL[] url = { dbLibFile.toURI().toURL() };

		URLClassLoader loader = new URLClassLoader(url);

		Class<?> c = loader.loadClass(className);
		Object instance = c.newInstance();

		return (Driver) instance;
	}

	public static synchronized String[] getTables(String projectHome, String driverJarName, String dbDriver, String dbUrl, String userName,
			String password, String dbType, String schemaName) throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = getConnection(projectHome, driverJarName, dbDriver, dbUrl, userName, password);
			// ".*", ".BIN"
			String tableNamePattern = null;
			if ("sybase".equals(dbType))
				schemaName = null;
			rs = conn.getMetaData().getTables(conn.getCatalog(), schemaName, tableNamePattern, new String[] { "TABLE" });
			return getRsList(rs, "TABLE_NAME");
		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_gettable, e);
			throw e;
		} finally {
			close(conn);
			if (rs != null)
				rs.close();
		}
	}

	public static synchronized String[] getSchemas(String projectHome, String driverJarName, String dbDriver, String dbUrl, String userName,
			String password) throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = getConnection(projectHome, driverJarName, dbDriver, dbUrl, userName, password);
			rs = conn.getMetaData().getSchemas();
			return getRsList(rs, "TABLE_SCHEM");
		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_getschema, e);
			throw e;
		} finally {
			close(conn);
			if (rs != null)
				rs.close();
		}
	}

	private static synchronized String[] getRsList(ResultSet rs, String columnName) throws SQLException {
		final ArrayList<String> list = new ArrayList<String>();
		try {
			if (rs != null) {
				while (rs.next()) {
					list.add((String) rs.getString(columnName));
				}
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		return list.toArray(new String[list.size()]);
	}

	public static boolean validateData(IProject project, JdbcOption jdbcOption) {
		// Database Name, Server, Driver Class Name,
		// Driver Jar Path
		if (jdbcOption.getDbName() == null || jdbcOption.getDbName().length() == 0) {
			MessageDialogUtil.openDetailMessageDialog(CommonActivator.PLUGIN_ID, Message.ide_message_title, Message.properties_validation_dbname,
					Message.properties_validation_dbname_detail, MessageDialog.ERROR);
			return false;
		} else if (!ProjectUtil.validateName(jdbcOption.getDbName())) {
			MessageDialogUtil.openDetailMessageDialog(CommonActivator.PLUGIN_ID, Message.ide_message_title,
					Message.properties_validation_error_dbname, Message.properties_validation_dbname_detail, MessageDialog.ERROR);
			return false;
		} else if (StringUtils.isNotEmpty(jdbcOption.getUserName())) {
			if (!ProjectUtil.validateName(jdbcOption.getUserName())) {
				MessageDialogUtil.openDetailMessageDialog(CommonActivator.PLUGIN_ID, Message.ide_message_title,
						Message.properties_validation_username_error, Message.properties_validation_username_detail, MessageDialog.ERROR);
				return false;
			}
		} else if (StringUtils.isNotEmpty(jdbcOption.getPassword())) {
			if (!ProjectUtil.validateName(jdbcOption.getPassword())) {
				MessageDialogUtil.openDetailMessageDialog(CommonActivator.PLUGIN_ID, Message.ide_message_title,
						Message.properties_validation_password_error, Message.properties_validation_password_detail, MessageDialog.ERROR);
				return false;
			}
		} else if (jdbcOption.getUrl() == null || jdbcOption.getUrl().length() == 0) {
			MessageDialogUtil.openDetailMessageDialog(CommonActivator.PLUGIN_ID, Message.ide_message_title, Message.properties_validation_dbserver,
					Message.properties_validation_dbserver_detail, MessageDialog.ERROR);
			return false;
		} else if (!ProjectUtil.validateName(jdbcOption.getUrl())) {
			MessageDialogUtil.openDetailMessageDialog(CommonActivator.PLUGIN_ID, Message.ide_message_title,
					Message.properties_validation_dbserver_error, Message.properties_validation_dbserver_detail, MessageDialog.ERROR);
			return false;
		} else if (jdbcOption.getDriverClassName() == null || jdbcOption.getDriverClassName().length() == 0) {
			MessageDialogUtil.openDetailMessageDialog(CommonActivator.PLUGIN_ID, Message.ide_message_title,
					Message.properties_validation_dbclassname, Message.properties_validation_dbclassname_detail, MessageDialog.ERROR);
			return false;
		} else if (!ProjectUtil.validateName(jdbcOption.getDriverClassName())) {
			MessageDialogUtil.openDetailMessageDialog(CommonActivator.PLUGIN_ID, Message.ide_message_title,
					Message.properties_validation_dbclassname_error, Message.properties_validation_dbclassname_detail, MessageDialog.ERROR);
			return false;
		} else if (jdbcOption.getDriverJar() == null || jdbcOption.getDriverJar().length() == 0) {
			MessageDialogUtil.openDetailMessageDialog(CommonActivator.PLUGIN_ID, Message.ide_message_title, Message.properties_validation_dbjar,
					Message.properties_validation_dbjar_detail, MessageDialog.ERROR);
			return false;
		} else if (!ProjectUtil.existPath(jdbcOption.getDriverJar())) {
			MessageDialogUtil.openDetailMessageDialog(CommonActivator.PLUGIN_ID, Message.ide_message_title,
					Message.properties_validation_dbjar_valid, Message.properties_validation_dbjar_detail, MessageDialog.ERROR);
			return false;
		}

		// Schema
		if (!(jdbcOption.getDbType().equals(Constants.DB_TYPE_SYBASE)|| jdbcOption.getDbType().equals(Constants.DB_TYPE_MYSQL)
				|| jdbcOption.getDbType().equals(Constants.DB_TYPE_MSSQL))) {
			if (jdbcOption.getSchema() == null || jdbcOption.getSchema().trim().length() == 0) {
				MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.properties_validation_dbschema, MessageDialog.INFORMATION);
				return false;
			} else if (jdbcOption.getSchema().equals(Message.wizard_jdbc_defaultschema)) {
				MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.properties_validation_dbschema, MessageDialog.INFORMATION);
				return false;
			}
		}

		return true;
	}

}
