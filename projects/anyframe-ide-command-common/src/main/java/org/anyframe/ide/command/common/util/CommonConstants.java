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
package org.anyframe.ide.command.common.util;

/**
 * This is a CommonConstants class. This class manages constants.
 * 
 * @author SoYon Lim
 */
public interface CommonConstants {
	String fileSeparator = System.getProperty("file.separator");

	// 1. plugin catalog
	String PLUGIN_CATALOG_ESSENTIAL_FILE = "plugin-catalog-essential.xml";
	String PLUGIN_CATALOG_OPTIONAL_FILE = "plugin-catalog-optional.xml";

	int PLUGIN_TYPE_ESSENTIAL = 1;
	int PLUGIN_TYPE_OPTIONAL = 2;

	// 2. plugin name
	String CORE_PLUGIN = "core";
	String DATASOURCE_PLUGIN = "datasource";
	String LOGGING_PLUGIN = "logging";
	String SPRING_PLUGIN = "spring";
	String HIBERNATE_PLUGIN = "hibernate";
	String MONITORING_PLUGIN = "monitoring";
	String QUERY_PLUGIN = "query";
	String TILES_PLUGIN = "tiles";
	String MIP_QUERY_PLUGIN = "mip-query";
	String IBATIS2_PLUGIN = "ibatis2";
	String SPRINGREST_PLUGIN = "springrest";
	String SIMPLEWEB_PLUGIN = "simpleweb";
	String FLEX_QUERY_PLUGIN = "flex-query";
//	mybatis add(2012.02.13) by junghwan.hong
	String MYBATIS_PLUGIN = "mybatis";
	String XP_QUERY_PLUGIN = "xp-query";
	String LOGBACK_PLUGIN = "logback";
	String I18N_PLUGIN = "i18n";

	// 3. plugin resources
	String PLUGIN_RESOURCES = "plugin-resources";
	String PLUGIN_FILE = "plugin.xml";
	String PLUGIN_INSTALLED_FILE = "plugin-installed.xml";
	String PLUGIN_BUILD_FILE = "plugin-build.xml";

	String COMMON_CONFIG_PREFS_FILE = "org.anyframe.ide.common.prefs";
	String COMMON_CONFIG_XML_FILE = "org.anyframe.ide.common.config.xml";
	String JDBC_CONFIG_XML_FILE = "org.anyframe.ide.common.jdbcdriver.config.xml";
	String DATABASE_CONFIG_XML_FILE = "org.anyframe.ide.common.databases.config.xml";

	String CONFIG_TX_FILE = "context-transaction.xml";
	String CONTEXT_PROPERTIES = "context.properties";
	String HIBERNATE_CFG_XML_FILE = "hibernate.cfg.xml";
	String SAMPLE_DATA_XML_FILE = "sample-data.xml";
	String TILES_XML_FILE = "tiles-views.xml";
	String WEB_XML_FILE = "web.xml";
	String WELCOME_FILE = "anyframe.jsp";
	String CONFIG_MESSAGE_FILE = "context-message.xml";
	String LOG4J_FILE = "log4j.xml";
	String LOGGING_ASPECT_CLASS = "LoggingAspect.java";
	
	String ARCHETYPE_REMOVE_POM = "remove-pom.xml";
	
	// 4. print patterns
	String PLUGININFO_DETAIL = " %-15s : %-60s";
	String PLUGININFO_DETAIL_REMAINS = " %-15s   %-60s";
	String PLUGININFO_NAME_LATEST_DESCRIPTION = " %-20s %-15s";
	String PLUGININFO_NAME_VERSION_LATEST = " %-20s %-15s  %-15s";
	
	String PLUGININFO_NAME_VERSION_LATEST_TITLE = " %-20s %-15s  %-15s";
	
	String ARCHETYPE_NAME_VERSION = " %-3s %-40s %-15s";
	String INSTALL_PLUGINS = " %-8s  %-20s %-35s";

	// 5. sample project - project configuration
	String COMMON_CONFIG_PREFS_KEY = "COMMON_CONFIG_PATH";

	String APP_DAOFRAMEWORK_TYPE = "project.daoframework";
	String APP_TEMPLATE_TYPE = "project.template";
	
	String CONFIG_ROOT = "anyframe";
	String ANYFRAME_HOME = "anyframe-home";
	String PROJECT_HOME = "pjthome";
	String PACKAGE_NAME = "package";
	String PROJECT_NAME = "pjtname";
	String PROJECT_BUILD_TYPE_ANT = "ant";
	String CONFIG_PATH = "path";
	
	String WEB_FRAMEWORK = "web.framework";
	String TEMPLATE_TYPE = "template.type";
	String PROJECT_BUILD_TYPE = "project.build";
	String PROJECT_BUILD_TYPE_MAVEN = "maven";
	String CONTEXT_ROOT = "context-root";
	
	String XML_TAG_TEMPLATE = "template";
	String XML_TAG_JDBCDRIVERS = "jdbcdrivers";
	String XML_TAG_DATABASES = "databases";
	

	String PROJECT_TYPE = "project.type";
	String PROJECT_TYPE_SERVICE = "service";
	String PROJECT_TYPE_WEB = "web";

	// 6. sample project - context.properties
	String APP_DB_DRIVER_CLASS = "driver";
	String APP_DB_PASSWORD = "password";
	String APP_DB_URL = "url";
	String APP_DB_USERNAME = "username";

	// 7. sample project - context-hibernate.xml
	String APP_DB_DIALECT = "dialect";

	// 8. sample project - directories
	String METAINF = fileSeparator + "META-INF" + fileSeparator;
	String METAINF_ANYFRAME = METAINF + "anyframe" + fileSeparator;
	String PLUGIN_INSTALLED_FILE_PATH = METAINF + PLUGIN_INSTALLED_FILE;
	
	String SRC_MAIN_JAVA = fileSeparator + "src" + fileSeparator + "main"
			+ fileSeparator + "java" + fileSeparator;
	String SRC_MAIN_RESOURCES = fileSeparator + "src" + fileSeparator + "main"
			+ fileSeparator + "resources" + fileSeparator;
	String SRC_MAIN_RESOURCES_SPRING = SRC_MAIN_RESOURCES + "spring" +fileSeparator;
	String SRC_TEST_JAVA = fileSeparator + "src" + fileSeparator + "test"
			+ fileSeparator + "java" + fileSeparator;
	String SRC_TEST_RESOURCES = fileSeparator + "src" + fileSeparator + "test"
			+ fileSeparator + "resources" + fileSeparator;

	String SRC_MAIN_WEBAPP = fileSeparator + "src" + fileSeparator + "main"
			+ fileSeparator + "webapp" + fileSeparator;
	String SRC_MAIN_WEBAPP_WEBINF = SRC_MAIN_WEBAPP + "WEB-INF" + fileSeparator;

	String SRC_MAIN_WEBAPP_CONFIG = SRC_MAIN_WEBAPP_WEBINF + "config"
			+ fileSeparator;
	String SRC_MAIN_WEBAPP_JSP = SRC_MAIN_WEBAPP_WEBINF + "jsp" + fileSeparator;

	String SRC_MAIN_WEBAPP_LIB = SRC_MAIN_WEBAPP_WEBINF + "lib" + fileSeparator;
	String SRC_MAIN_WEBAPP_SAMPLE = SRC_MAIN_WEBAPP + "sample" + fileSeparator;

	String DB_SCRIPTS = fileSeparator + "db" + fileSeparator + "scripts";
	String DB_RESOURCES = PLUGIN_RESOURCES + "/db/resources/";
	
	String PLUGIN_ASPECT_PACKAGE = "common" + fileSeparator + "aspect" + fileSeparator;
	String ORIGIN_PLUGIN_ASPECT_PACKAGE = "org"+ fileSeparator 
			+ "anyframe" + fileSeparator + "plugin" + fileSeparator+ PLUGIN_ASPECT_PACKAGE;
	
	String TEMPLATE_HOME = fileSeparator + ".settings" + fileSeparator + "anyframe" + fileSeparator + "templates";
	String INSPECTION_HOME = fileSeparator + ".settings" + fileSeparator + "anyframe" + fileSeparator + "inspection";

	String PREFS_FILE = ".settings" + fileSeparator + COMMON_CONFIG_PREFS_FILE;
	String SETTING_HOME = ".settings" + fileSeparator + "anyframe";
	
	// 9. database
	// db settings
	String XML_CONFIG_ROOT_PATH = "dbexplorer";
	String XML_TAG_DATASOURCE = "dataSource";
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
	String XML_TAG_DEFAULT = "isDefault";
	
	// 10. others
	String EXT_JAR = "jar";
	String EXT_JAVA = "java";
	String EXT_XML = "xml";
	String EXT_JSP = "jsp";
	String EXT_PROPERTIES = "properties";
	String OFFLINE = "offline";
	String REMOTE_CATALOG_PATH = "http://dev.anyframejava.org/maven/repo";
	String USER_HOME_ANYFRAME = CommonConstants.fileSeparator + ".anyframe"
			+ CommonConstants.fileSeparator;

	String VELOCITY_SHARP = "VELOCITY_ANYFRAME_SHARP";
	String VELOCITY_SHARP_BRACE = "VELOCITY_ANYFRAME_SHARP_BRACE";
	String VELOCITY_SUPPORT = "Velocity-Support";

	String TEMPLATE_CONFIG_FILE = "template.config";

	String DAO_IBATIS2 = IBATIS2_PLUGIN;
//	mybatis add(2012.02.13) by junghwan.hong
	String DAO_MYBATIS = MYBATIS_PLUGIN;
	
	String DAO_QUERY = QUERY_PLUGIN;
	String DAO_HIBERNATE = HIBERNATE_PLUGIN;
	String DAO_SPRINGJDBC = "springjdbc";

	String SCOPE_INTERCEPTOR = "interceptor";
	
	String ARCHETYPE_GROUP_ID = "org.anyframe.archetype";
	String ARCHETYPE_BASIC_ARTIFACT_ID = "anyframe-basic-archetype";
	String ARCHETYPE_SERVICE_ARTIFACT_ID = "anyframe-service-archetype";
	
	String LOG_LEVEL_DEBUG = "DEBUG";
	String LOG_LEVEL_ERROR = "ERROR";
	String LOG_LEVEL_INFO = "INFO";
	
	String UNINSTALLED_FOLDER = CommonConstants.fileSeparator + ".uninstalled";
	
	String PROJECT_NAME_CODE_GENERATOR = "codegenerator";
	String PROJECT_NAME_DAO_MANAGER = "daomanager";
	String PROJECT_NAME_VO_MANAGER = "vomanager";
	String PROJECT_NAME_UNITTEST_MANAGER = "unittestmanager";
	
	//for temp
	String METADATA_FILE = "project.mf";
	String PROJECT_TEMPLATE_HOME = "project.template.home";
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
}
