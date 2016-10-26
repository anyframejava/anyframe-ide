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
package org.anyframe.ide.common;

/**
 * This is Constants class.
 * 
 * @author Sujeong Lee
 */
public interface Constants {

	String FILE_SEPERATOR = System.getProperty("file.separator");

	String SETTING_HOME = "." + FILE_SEPERATOR + ".settings" + FILE_SEPERATOR
			+ "anyframe";
	String DFAULT_TEMPLATE_HOME = SETTING_HOME + FILE_SEPERATOR + "templates";

	String METADATA_FILE = "project.mf";
	String DB_SETTINGS_XML_FILE = "org.anyframe.ide.common.databases.config.xml";
	String DRIVER_SETTING_XML_FILE = "org.anyframe.ide.common.jdbcdriver.config.xml";

	// project.mf
	String ANYFRAME_HOME = "anyframe.home";

	String APP_DAOFRAMEWORK_TYPE = "project.daoframework";
	String APP_TEMPLATE_TYPE = "project.template";

	String DB_ARTIFACTID = "db.artifactId";
	String DB_DIALECT = "db.dialect";
	String DB_DRIVER_CLASS = "db.driver";
	String DB_DRIVER_PATH = "db.lib";
	String DB_GROUPID = "db.groupId";
	String DB_PASSWORD = "db.password";
	String DB_SCHEMA = "db.schema";
	String DB_SCHEMA_USE = "db.schema.use";
	String DB_TYPE = "db.type";
	String DB_URL = "db.url";
	String DB_USERNAME = "db.userId";
	String DB_VERSION = "db.version";
	String DB_NAME = "db.name";
	String DB_SERVER = "db.server";
	String DB_PORT = "db.port";

	String PACKAGE_NAME = "package.name";
	String WEB_FRAMEWORK = "web.framework";
	String TEMPLATE_TYPE = "template.type";
	String PROJECT_BUILD_TYPE = "project.build";
	String PROJECT_BUILD_TYPE_ANT = "ant";
	String PROJECT_BUILD_TYPE_MAVEN = "maven";

	String PROJECT_HOME = "project.home";
	String PROJECT_NAME = "project.name";
	String PROJECT_TEMPLATE_HOME = "project.template.home";

	String PROJECT_TYPE = "project.type";
	String PROJECT_TYPE_SERVICE = "service";
	String PROJECT_TYPE_WEB = "web";

	// sample project - context.properties
	String APP_DB_DRIVER_CLASS = "driver";
	String APP_DB_PASSWORD = "password";
	String APP_DB_URL = "url";
	String APP_DB_USERNAME = "username";

	// directories
	String METAINF = FILE_SEPERATOR + "META-INF" + FILE_SEPERATOR;
	String METAINF_ANYFRAME = METAINF + "anyframe" + FILE_SEPERATOR;

	// db settings
	String XML_CONFIG_ROOT_PATH = "/dbexplorer/dataSource";
	String XML_TAG_TYPE = "type";
	String XML_TAG_NAME = "name";
	String XML_TAG_DRIVER_PATH = "driverJar";
	String XML_TAG_DRIVER_CLASS_NAME = "driverClassName";
	String XML_TAG_URL = "url";
	String XML_TAG_USERNAME = "username";
	String XML_TAG_PASSWORD = "password";
	String XML_TAG_SCHEMA = "schema";

	// community maven info
	String XML_TAG_DIALECT = "dialect";
	String XML_TAG_DRIVER_GROUPID = "driverGroupId";
	String XML_TAG_DRIVER_ARTIFACTID = "driverArtifactId";
	String XML_TAG_DRIVER_VERSION = "driverVersion";

	// optional
	String XML_TAG_USE_DB_SPECIFIC = "useDbSpecific";
	String XML_TAG_RUN_EXPLAIN_PLAN = "runExplainPlan";

	// default db connection url
	String DB_HSQL_FILE_URL = "jdbc:hsqldb:file:<database_name>";
	String DB_HSQL_SERVER_URL = "jdbc:hsqldb:hsql://<server>/<database_name>";
	String DB_HSQL_SERVER_PORT_URL = "jdbc:hsqldb:hsql://<server>:<port>/<database_name>";
	String DB_ORACLE_URL = "jdbc:oracle:thin:@<server>:<port>:<database_name>";
	String DB_MYSQL_URL = "jdbc:mysql://<server>:<port>/<database_name>";
	String DB_SYBASE_URL = "jdbc:sybase:Tds:<server>:<port>?ServiceName=<database_name>";
	String DB_DB2_URL = "jdbc:db2://<server>:<port>/<database_name>";
	String DB_MSSQL_URL = "jdbc:sqlserver://<server>:<port>;DatabaseName=<database_name>";
	String DB_CON_CHK_KEY = "DB_CON_CHK";
	String DB_CON_MSG_KEY = "DB_CON_MSG";
	boolean DB_CON_CHK = false;
	String DB_CON_MSG = "";

	String DB_TYPE_SYBASE = "sybase";
	String DB_NO_SCHEMA = "No Schema";
	
	String XML_DRIVER_CONFIG_OTHERS = "Others...";

	// for online runtime - check whether enterprise version(online) is installed or not
	String TEMPLATE_TYPE_ONLINE = "online";
	
	//Pling id ID
	String EDITOR_ID_DAO	= "com.anyframe.ide.daomanager.daoeditor.editor.impl.DAOEditorMultiPageEditorPart";
	String EDITOR_ID_VO		= "com.anyframe.ide.vomanager.ui.views.VOEditor";
}
