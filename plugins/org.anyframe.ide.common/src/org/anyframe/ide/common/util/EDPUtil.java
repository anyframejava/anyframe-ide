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
package org.anyframe.ide.common.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.anyframe.ide.common.databases.DatabasesSettingUtil;
import org.anyframe.ide.common.databases.JdbcOption;
import org.anyframe.ide.common.messages.Message;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.datatools.connectivity.ConnectionProfileConstants;
import org.eclipse.datatools.connectivity.ConnectionProfileException;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.IManagedConnection;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.datatools.connectivity.drivers.DriverInstance;
import org.eclipse.datatools.connectivity.drivers.DriverManager;
import org.eclipse.datatools.connectivity.drivers.IDriverMgmtConstants;
import org.eclipse.datatools.connectivity.drivers.IPropertySet;
import org.eclipse.datatools.connectivity.drivers.PropertySetImpl;
import org.eclipse.datatools.connectivity.drivers.jdbc.IJDBCConnectionProfileConstants;
import org.eclipse.datatools.connectivity.drivers.jdbc.IJDBCDriverDefinitionConstants;
import org.eclipse.datatools.connectivity.sqm.core.connection.ConnectionInfo;
import org.eclipse.datatools.modelbase.sql.schema.Database;

/**
 * This is EDPUtil class.
 * 
 * @author Junghwan Hong
 */
public class EDPUtil {

	static final String DB_NAME = "db.name";

	// private static Map<String, DBConnectionInfo> connection = new
	// HashMap<String, DBConnectionInfo>();

	private static JdbcOption jdbcOption;

	private static String providerID;

	private static String databseVendorPropId;

	private static String databseVersionPropId;

	private static String driverDefinitionID;

	private static String defnType;

	public static String[] getConnectionProfile(IProject project) {
		Map<String, JdbcOption> map = DatabasesSettingUtil
				.loadJdbcOptionMap(project);
		LinkedList<String> list = new LinkedList<String>();
		list.add(Message.properties_config_profilename);
		for (Object obj : map.keySet().toArray())
			list.add(obj.toString());
		return list.toArray(new String[list.size()]);
	}

	public static Connection getConnection(IProject project, String profileName)
			throws ConnectionProfileException, Exception {

		ProfileManager pm = ProfileManager.getInstance();
		
		IConnectionProfile pf;

		getJdbcOption(project, profileName);

		pf = pm.createTransientProfile(providerID, generateDBProperties(
				project, profileName));

		IStatus status = pf.connect();
		if (!(status.getCode() == IStatus.OK)) {
			if (status.getException() != null) {
				// status.getException().printStackTrace();
				throw new ConnectionProfileException(
						Message.exception_getsqlconnection);
			}
		}

		IManagedConnection sqlConnection = ((IConnectionProfile) pf)
				.getManagedConnection("java.sql.Connection");

		if (sqlConnection != null) {
			return (java.sql.Connection) sqlConnection.getConnection()
					.getRawConnection();
		} else {
			throw new ConnectionProfileException(Message.exception_getsqlconnection);
		}
	}

	public static Database getDatabase(IProject project, String profileName)
			throws ConnectionProfileException, Exception {

		ProfileManager pm = ProfileManager.getInstance();

		IConnectionProfile pf;

		getJdbcOption(project, profileName);

		pf = pm.createTransientProfile(providerID, generateDBProperties(
				project, profileName));
		IStatus status = pf.connect();
		if (!(status.getCode() == IStatus.OK)) {
			if (status.getException() != null) {
				// status.getException().printStackTrace();
				throw new ConnectionProfileException(
						Message.exception_getsqlconnection);
			}
		}
		IManagedConnection managedConnection = ((IConnectionProfile) pf)
				.getManagedConnection("org.eclipse.datatools.connectivity.sqm.core.connection.ConnectionInfo");

		if (managedConnection != null) {
			ConnectionInfo connectionInfo = (ConnectionInfo) managedConnection
					.getConnection().getRawConnection();
			if (connectionInfo != null)
				return connectionInfo.getSharedDatabase();
			else
				throw new ConnectionProfileException(
						Message.exception_getsqlconnection);
		} else {
			throw new ConnectionProfileException(Message.exception_getsqlconnection);
		}
	}

	public static boolean connectionTest(IProject project, String profileName)
			throws Exception {
		Map<String, JdbcOption> propMap = DatabasesSettingUtil
				.loadJdbcOptionMap(project);
		JdbcOption jdbc = propMap.get(profileName);

		Properties connectionProps = new Properties();
		connectionProps.put("user", jdbc.getUserName());
		connectionProps.put("password", jdbc.getPassword());

		try {
			Driver driver = ConnectionUtil.getDriverFromPath(jdbc
					.getDriverJar(), jdbc.getDriverClassName());
			Connection conn = driver.connect(jdbc.getUrl(), connectionProps);
			if (conn == null)
				return false;
			else
				return true;

		} catch (Exception e) {
			throw e;
		}
	}

	private static void getJdbcOption(IProject project, String profileName) {
		Map<String, JdbcOption> propMap = DatabasesSettingUtil
				.loadJdbcOptionMap(project);
		jdbcOption = propMap.get(profileName);

		String dbType = jdbcOption.getDbType();

		// DB Type 별 분기
		if ("hsqldb".equals(dbType)) {
			providerID = "org.eclipse.datatools.enablement.hsqldb.connectionProfile";
			databseVendorPropId = "HSQLDB";
			databseVersionPropId = "1.8";
			driverDefinitionID = "DriverDefn.org.eclipse.datatools.enablement.hsqldb.1_8.driver.HSQLDB JDBC Driver";
			defnType = "";

		} else if ("mysql".equals(dbType)) {
			providerID = "org.eclipse.datatools.enablement.mysql.connectionProfile";
			databseVendorPropId = "MySQL";
			databseVersionPropId = "5.0";
			driverDefinitionID = "DriverDefn.org.eclipse.datatools.enablement.mysql.5_1.driverTemplate.MySQL JDBC Driver";
			defnType = "org.eclipse.datatools.enablement.mysql.5_1.driverTemplate";

		} else if ("db2".equals(dbType)) {
			providerID = "org.eclipse.datatools.enablement.ibm.db2.luw.connectionProfile";
			databseVendorPropId = "DB2 for Linux,UNIX, and Windows";
			databseVersionPropId = "V9.1";
			driverDefinitionID = "DriverDefn.org.eclipse.datatools.enablement.ibm.db2.luw.driverTemplate.IBM Data Server Driver for JDBC and SQLJ";
			defnType = "org.eclipse.datatools.enablement.ibm.db2.luw.driverTemplate";

		} else if ("mssql".equals(dbType)) {
			providerID = "org.eclipse.datatools.enablement.msft.sqlserver.connectionProfile";
			databseVendorPropId = "SQL Server";
			databseVersionPropId = "2005";
			driverDefinitionID = "DriverDefn.org.eclipse.datatools.enablement.msft.sqlserver.2005.driverTemplate.Microsoft SQL Server 2005 JDBC Driver";
			defnType = "org.eclipse.datatools.enablement.msft.sqlserver.2005.driverTemplate";

		} else if ("oracle".equals(dbType)) {
			providerID = "org.eclipse.datatools.enablement.oracle.connectionProfile";
			databseVendorPropId = "Oracle";
			databseVersionPropId = "10";
			driverDefinitionID = "DriverDefn.org.eclipse.datatools.enablement.oracle.10.driverTemplate.Oracle Thin Driver";
			defnType = "org.eclipse.datatools.enablement.oracle.10.driverTemplate";

		} else if ("sybase".equals(dbType)) {
			providerID = "org.eclipse.datatools.enablement.sybase.asa.connectionProfile";
			databseVendorPropId = "Sybase_ASA";
			databseVersionPropId = "9.x";
			driverDefinitionID = "DriverDefn.org.eclipse.datatools.enablement.sybase.asa.drivertemplate.Sybase JDBC Driver for Sybase ASA 9.x";
			defnType = "org.eclipse.datatools.enablement.sybase.asa.drivertemplate";

		}
	}

	private static Properties generateDBProperties(IProject project,
			String profileName) {
		Properties baseProperties = new Properties();

		baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST,
				jdbcOption.getDriverJar());

		if ("Sybase_ASA".equals(databseVendorPropId)) {
			baseProperties.setProperty(
					IJDBCConnectionProfileConstants.DRIVER_CLASS_PROP_ID,
					"com.sybase.jdbc3.jdbc.SybDriver");
			setDriverDefn();

		} else
			baseProperties.setProperty(
					IJDBCConnectionProfileConstants.DRIVER_CLASS_PROP_ID,
					jdbcOption.getDriverClassName());
		baseProperties.setProperty(IJDBCConnectionProfileConstants.URL_PROP_ID,
				jdbcOption.getUrl());
		baseProperties.setProperty(
				IJDBCConnectionProfileConstants.USERNAME_PROP_ID, jdbcOption
						.getUserName());
		baseProperties.setProperty(
				IJDBCConnectionProfileConstants.PASSWORD_PROP_ID, jdbcOption
						.getPassword());
		baseProperties.setProperty(
				IJDBCConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, String
						.valueOf(true));
		// DB Type 별 분기
		baseProperties.setProperty(
				IJDBCConnectionProfileConstants.DATABASE_VENDOR_PROP_ID,
				databseVendorPropId);
		baseProperties.setProperty(
				IJDBCConnectionProfileConstants.DATABASE_VERSION_PROP_ID,
				databseVersionPropId);

		if ("Sybase_ASA".equals(databseVendorPropId)) {
			baseProperties
					.setProperty(
							ConnectionProfileConstants.PROP_DRIVER_DEFINITION_ID,
							"DriverDefn.org.eclipse.datatools.enablement.sybase.asa.drivertemplate.Sybase JDBC Driver for Sybase ASA 9.x");
			baseProperties
					.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE,
							"org.eclipse.datatools.enablement.sybase.asa.drivertemplate");
		}
		return baseProperties;
	}

	private static void setDriverDefn() {
		DriverInstance[] drivers = DriverManager.getInstance()
				.getAllDriverInstances();
		for (DriverInstance driver : drivers) {
			if (driver
					.getId()
					.equals(
							"DriverDefn.org.eclipse.datatools.enablement.sybase.asa.drivertemplate.Sybase JDBC Driver for Sybase ASA 9.x"))
				return;
		}

		final IPropertySet propSet = new PropertySetImpl(
				"Sybase JDBC Driver for Sybase ASA 9.x",
				"DriverDefn.org.eclipse.datatools.enablement.sybase.asa.drivertemplate.Sybase JDBC Driver for Sybase ASA 9.x");
		final Properties driverProperties = new Properties();
		driverProperties.setProperty(
				IJDBCDriverDefinitionConstants.DRIVER_CLASS_PROP_ID,
				"com.sybase.jdbc3.jdbc.SybDriver");

		driverProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST,
				jdbcOption.getDriverJar());
		driverProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE,
				"org.eclipse.datatools.enablement.sybase.asa.drivertemplate");

		driverProperties.setProperty(
				IJDBCDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID,
				"Sybase_ASA");
		driverProperties.setProperty(
				IJDBCDriverDefinitionConstants.DATABASE_VERSION_PROP_ID, "9.x");
		propSet.setBaseProperties(driverProperties);
		DriverManager.getInstance().addDriverInstance(propSet);
	}

	public static void close(Connection connection) throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}
}
