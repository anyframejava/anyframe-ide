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
	String PLUGIN_CATALOG_CUSTOM_FILE = "plugin-catalog-custom.xml";

	int PLUGIN_TYPE_ESSENTIAL = 1;
	int PLUGIN_TYPE_OPTIONAL = 2;
	int PLUGIN_TYPE_CUSTOM = 3;

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

	// 3. plugin resources
	String PLUGIN_RESOURCES = "plugin-resources";
	String PLUGIN_FILE = "plugin.xml";
	String PLUGIN_INSTALLED_FILE = "plugin-installed.xml";
	String PLUGIN_BUILD_FILE = "plugin-build.xml";

	String CONFIG_TX_FILE = "context-transaction.xml";
	String CONTEXT_PROPERTIES = "context.properties";
	String HIBERNATE_CFG_XML_FILE = "hibernate.cfg.xml";
	String METADATA_FILE = "project.mf";
	String SAMPLE_DATA_XML_FILE = "sample-data.xml";
	String TILES_XML_FILE = "tiles-views.xml";
	String WEB_XML_FILE = "web.xml";
	String WELCOME_FILE = "anyframe.jsp";

	// 4. print patterns
	String PLUGININFO_DETAIL = " %-15s : %-40s";
	// String PLUGININFO_DEPENDENCIES = " %-15s : %-20s| %-15s";
	// String PLUGININFO_DEPENDENCIES_BODY = " %-15s   %-20s| %-15s";
	// String PLUGININFO_DEPENDENCIES_LINE = " %-15s   %-20s";
	String PLUGININFO_NAME_LATEST_DESCRIPTION = " %-20s| %-15s | %-35s";
	// String PLUGININFO_NAME_VERSION = " %-20s| %-15s";
	String PLUGININFO_NAME_VERSION_LATEST = " %-20s| %-15s | %-15s";
	// String PLUGININFO_RELEASES = " %-15s : %-15s";
	// String PLUGININFO_RELEASES_BODY = " %-15s   %-15s";
	String ARCHETYPE_NAME_VERSION = " %-3s| %-40s| %-15s";
	String INSTALL_PLUGINS = " %-8s | %-20s| %-35s";

	// 5. sample project - project.mf
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

	String SRC_MAIN_JAVA = fileSeparator + "src" + fileSeparator + "main"
			+ fileSeparator + "java" + fileSeparator;
	String SRC_MAIN_RESOURCES = fileSeparator + "src" + fileSeparator + "main"
			+ fileSeparator + "resources" + fileSeparator;
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
	String DB_RESOURCES = PLUGIN_RESOURCES + fileSeparator + "db"
			+ fileSeparator + "resources" + fileSeparator;

	// 9. others
	String EXT_JAR = "jar";
	String EXT_JAVA = "java";
	String EXT_XML = "xml";
	String OFFLINE = "offline";
	String REMOTE_CATALOG_PATH = "http://dev.anyframejava.org/maven/repo";
	String USER_HOME_ANYFRAME = CommonConstants.fileSeparator + ".anyframe"
			+ CommonConstants.fileSeparator;

	String VELOCITY_SHARP = "VELOCITY_ANYFRAME_SHARP";
	String VELOCITY_SHARP_BRACE = "VELOCITY_ANYFRAME_SHARP_BRACE";
	String VELOCITY_SUPPORT = "Velocity-Support";

	String TEMPLATE_CONFIG_FILE = "template.config";

	String DAO_IBATIS2 = IBATIS2_PLUGIN;
	String DAO_QUERY = QUERY_PLUGIN;
	String DAO_HIBERNATE = HIBERNATE_PLUGIN;
	String DAO_SPRINGJDBC = "springjdbc";

	String SCOPE_INTERCEPTOR = "interceptor";
	
	String ARCHETYPE_GROUP_ID = "org.anyframe.archetype";
	String ARCHETYPE_BASIC_ARTIFACT_ID = "anyframe-basic-archetype";
	String ARCHETYPE_SERVICE_ARTIFACT_ID = "anyframe-service-archetype";
	
	// 1. plugin name
	/*
	 * String MIPLATFORM_PLUGIN = "miplatform"; String SECURITY_PLUGIN =
	 * "security"; String MIPSAMPLE_PLUGIN = "mipsample"; String REMOTING_PLUGIN
	 * = "remoting";
	 * 
	 * String CACHE_PLUGIN = "cache"; String CXF_PLUGIN = "cxf"; String
	 * STRUTS_PLUGIN = "struts";
	 * 
	 * String PROJECT_VERSION = "project.version"; String WEB_ARCHETYPE_ID =
	 * "anyframe-basic-archetype"; String SERVICE_ARCHETYPE_ID =
	 * "anyframe-service-archetype";
	 * 
	 * // 3. files String FORMAT_PLUGININFO = " %-16s| %-16s| %-34s| %-15s";
	 * String SIMPLE_FORMAT_PLUGININFO = " %-16s| %-16s"; String
	 * FORMAT_PLUGININFO_WITH_LATEST = " %-16s| %-16s| %-34s| %-15s | %-15s";
	 * String INSTALLABLE_FORMAT_PLUGININFO = " %-3s | %-20s| %-15s"; String
	 * INSTALLABLE_FORMAT_ARCHETYPE = " %-3s | %-40s| %-15s";
	 * 
	 * // 5. etc String anyframeHome = System.getProperty("anyframeHome");
	 * String CUSTOM_PLUGIN_PATH = CommonConstants.fileSeparator + "anyframe" +
	 * CommonConstants.fileSeparator + "plugin" + CommonConstants.fileSeparator
	 * + "anyframe.plugin.custom-list";
	 */
}
