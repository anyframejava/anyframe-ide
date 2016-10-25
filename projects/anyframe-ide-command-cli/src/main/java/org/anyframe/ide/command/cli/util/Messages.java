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
package org.anyframe.ide.command.cli.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a Messages class. This class is for managing command line message.
 * 
 * @author Sooyeon Park
 */
public class Messages implements Serializable {

	private static final long serialVersionUID = 5751186957741297522L;

	protected Messages() {
		// prevents calls from subclass
		throw new UnsupportedOperationException();
	}

	public static final String WRONG_ARGS = "You used wrong command.\n";

	public static final String WRONG_ARG_PAIR = "You defined argument pair wrongly.\n";

	public static final String WRONG_PLUGIN_NAME = "You used wrong plugin name.\n";

	public static final String EXCEPTION_ERROR = "\nFail to execute build command.\n";

	public static final String NO_ARGS = "You need more command arguments. Try anyframe -help.\n";

	public static final String WRONG_ARGS_IGNORED = "You used wrong command arguments. so, that arguments are ignored. \n";

	public static final String INSIDE_PROJECT = "\nYou should run commands inside the project folder.\n";

	public static final String NOT_SUPPORTED_ERROR = "\nThe requested command is not supported because your project is a service type project.\n";

	public static final String NOT_SUPPORTED_FOLDER_ERROR = "\nThe requested command is not supported in current directory. \n";

	public static final String DB_ERROR = "\nYou should start DB Server first to create projects. (To build TestCases)\n";

	public static final String ARGUMENTS_ERROR = "\nArguments are not mismatched.\n";

	public static final String NOT_SUPPORTED_COMMAND = "\nThe requested command is not supported because your project is maven project.\n";

	public static final String USER_CANCEL = "\nUser canceled operation.\n";

	public static final String CRUD_KEEP_OVERWRITE = "\nFiles listed above already exists. Are you sure to overwrite them? (Y/N)";

	public static final String WRONG_DIR_PATH = "\nYou used wrong argument value. You should use directory path.\n";

	public static final String WELCOME = "\nWelcome to Anyframe IDE 2.1.0 - http://anyframejava.org\n"
			+ "Licensed under Apache Standard License 2.0\n"
			+ "ANYFRAME_HOME is set to: ";

	public static final String ANT_HELP = "Usage (optionals marked with *): anyframe [command] [options]*\n\n"
			+ "Available Commands (type 'anyframe -help -command [command]' for more information) \n"
			+ "anyframe create-project \n"
			+ "anyframe install [PLUGIN_NAME] \n"
			+ "anyframe uninstall [PLUGIN_NAME] \n"
			+ "anyframe installed \n"
			+ "anyframe info [PLUGIN_NAME] \n"
			+ "anyframe list \n"
			+ "anyframe list-update \n"
			+ "anyframe update-catalog \n"
			+ "anyframe change-db \n"
			+ "anyframe create-model \n"
			+ "anyframe create-crud [ENTITY] \n"
			+ "anyframe build \n"
			+ "anyframe run \n";

	// + "anyframe activate-plugin \n"
	// + "anyframe deactivate-plugin \n"
	// + "anyframe package-plugin \n";

	public static final String ANT_HELP_CREATE_PROJECT = "\nDescription : Creates a Anyframe based application for the given name \n"
			+ "Usage : anyframe create-project [-options]\n"
			+ "where options include : \n"
			+ " -pjtname  project name to be created \n"
			+ " -pjttype  project type [ web | service ] \n"
			+ " -package  project’s representative package name \n"
			+ " -offline  When the network condition is not easy, if the offline option is set as true, for all the command execution, operates in offline mode \n";

	public static final String ANT_HELP_INSTALL = "\nDescription : Installs a plugin for given name and version \n"
			+ "Usage : anyframe install [PLUGIN_NAME] [-options]\n"
			+ "where options include : \n"
			+ " -version  plugin version to be installed \n"
			+ " -file  location of plugin jar file in local directory \n";

	public static final String ANT_HELP_UNINSTALL = "\nDescription : Uninstalls a plugin for given name \n"
			+ "Usage : anyframe uninstall [PLUGIN_NAME] \n"
			+ "where options include : \n" 
			+ " -excludes  files to exclude \n";

	public static final String ANT_HELP_INSTALLED = "\nDescription : Shows installed plugins of target project \n"
			+ "Usage : anyframe installed \n";

	public static final String ANT_HELP_INFO = "\nDescription : Shows a detail information of plugin \n"
			+ "Usage : anyframe info [PLUGIN_NAME] \n";

	public static final String ANT_HELP_LIST = "\nDescription : Shows available plugins on Anyframe repositories \n"
			+ "Usage : anyframe list \n";

	public static final String ANT_HELP_LIST_UPDATE = "\nDescription : Checks installed plugin versions against latest releases on repositories \n"
			+ "Usage : anyframe list-update \n";

	public static final String ANT_HELP_UPDATE_CATALOG = "\nDescription : Updates the plugin catalog file with the latest information \n"
			+ "Usage : anyframe update-catalog \n";

	public static final String ANT_HELP_CHANGE_DB = "\nDescription : Changes the operating DBMS of target project \n"
			+ "Usage : anyframe changedb \n";

	public static final String ANT_HELP_CREATE_MODEL = "\nDescription : Creates a new domain class \n"
			+ "Usage : anyframe create-model [-options] \n"
			+ "where options include : \n"
			+ " -table  the table name for the domain class creation \n"
			+ " -package  the created domain class package \n";

	public static final String ANT_HELP_CREATE_CRUD = "\nDescription : Creates a new classes that provides CRUD functionality \n"
			+ "Usage : anyframe create-crud [ENTITY] [-options] \n"
			+ "where options include : \n"
			+ " -package  the created CRUD source code's package \n"
			+ " -scope  [ service | all ] \n";

	public static final String ANT_HELP_BUILD = "\nDescription : Build the current sample application using the Ant \n"
			+ "Usage : anyframe build [ENTITY] [-options] \n"
			+ "where options include : \n"
			+ " -deploy  project distribution type [ class | jar ] \n"
			+ " -war  if -war option is used, create the [projectname].war file in 'dist' folder  \n" 
			+ " -clean  if -clean option is added, carry out the build after removing all of the 'dist' folder \n";

	public static final String ANT_HELP_RUN = "\nDescription : Build and deploy the current sample application, based on the Jetty \n"
			+ "Usage : anyframe run [-options] \n"
			+ "where options include : \n"
			+ " -deploy  project distribution type [ class | jar ] \n"
			+ " -war  if -war option is used, create the [projectname].war file in 'dist' folder \n" 
			+ " -clean  if -clean option is added, carry out the build after removing all of the 'dist' folder \n";

	public static final Map<String, String> ANT_HELP_MESSAGES_BY_COMMAND = new HashMap<String, String>();

	static {
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_CREATE_PROJECT,
				ANT_HELP_CREATE_PROJECT);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_INSTALL,
				ANT_HELP_INSTALL);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_UNINSTALL,
				ANT_HELP_UNINSTALL);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_INSTALLED_LIST,
				ANT_HELP_INSTALLED);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_INFO, ANT_HELP_INFO);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_LIST, ANT_HELP_LIST);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_LIST_UPDATE,
				ANT_HELP_LIST_UPDATE);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_UPDATE_CATALOG,
				ANT_HELP_UPDATE_CATALOG);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_CHANGE_DB,
				ANT_HELP_CHANGE_DB);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_CREATE_MODEL,
				ANT_HELP_CREATE_MODEL);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_CREATE_CRUD,
				ANT_HELP_CREATE_CRUD);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_BUILD_APP,
				ANT_HELP_BUILD);
		ANT_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_RUN, ANT_HELP_RUN);
	}

	// MAVEN
	public static final String MAVEN_HELP = "\nUsage (optionals marked with *): mvn anyframe:[command] [options]*\n\n"
			+ "Available Commands (type 'mvn anyframe:help -Dcommand=[command]' for more information) \n"
			+ "mvn anyframe:install \n"
			+ "mvn anyframe:uninstall \n"
			+ "mvn anyframe:inplace \n"
			+ "mvn anyframe:installed \n"
			+ "mvn anyframe:info \n"
			+ "mvn anyframe:list \n"
			+ "mvn anyframe:list-update \n"
			+ "mvn anyframe:update-catalog \n"
			+ "mvn anyframe:change-db \n"
			+ "mvn anyframe:create-model \n"
			+ "mvn anyframe:create-crud \n"
			+ "mvn anyframe:activate-plugin \n"
			+ "mvn anyframe:deactivate-plugin \n"
			+ "mvn anyframe:package-plugin \n"
			+ "mvn anyframe:install-pluginfile \n"
			+ "mvn anyframe:deploy-pluginfile \n";

	public static final String MAVEN_HELP_INSTALL = "\nDescription : Installs a plugin for given name and version \n"
			+ "Usage : mvn anyframe:install -Dname=... [-options]\n"
			+ "where options include : \n"
			+ " -Dversion  plugin version to be installed \n"
			+ " -Dfile  location of plugin jar file in local directory \n";

	public static final String MAVEN_HELP_UNINSTALL = "\nDescription : Uninstalls a plugin for given name \n"
			+ "Usage : mvn anyframe:uninstall -Dname=... [-options] \n"
			+ "where options include : \n" + " -Dexcludes  files to exclude \n";

	public static final String MAVEN_HELP_INPLACE = "\nDescription : Copies dependent libraries into target project \n"
			+ "Usage : mvn anyframe:inplace \n";

	public static final String MAVEN_HELP_INSTALLED = "\nDescription : Shows installed plugins of target project \n"
			+ "Usage : mvn anyframe:installed \n";

	public static final String MAVEN_HELP_INFO = "\nDescription : Shows a detail information of plugin \n"
			+ "Usage : mvn anyframe:info -Dname=... \n";

	public static final String MAVEN_HELP_LIST = "\nDescription : Shows available plugins on Anyframe repositories \n"
			+ "Usage : mvn anyframe:list \n";

	public static final String MAVEN_HELP_LIST_UPDATE = "\nDescription : Checks installed plugin versions against latest releases on repositories \n"
			+ "Usage : mvn anyframe:list-update \n";

	public static final String MAVEN_HELP_UPDATE_CATALOG = "\nDescription : Updates the plugin catalog file with the latest information \n"
			+ "Usage : mvn anyframe:update-catalog \n";

	public static final String MAVEN_HELP_CHANGE_DB = "\nDescription : Changes the operating DBMS of target project \n"
			+ "Usage : mvn anyframe:change-db \n";

	public static final String MAVEN_HELP_CREATE_MODEL = "\nDescription : Creates a new domain class \n"
			+ "Usage : mvn anyframe:create-model [-options] \n"
			+ "where options include : \n"
			+ " -Dtable  the table name for the domain class creation \n"
			+ " -Dpackage  the created domain class package \n";

	public static final String MAVEN_HELP_CREATE_CRUD = "\nDescription : Creates a new classes that provides CRUD functionality \n"
			+ "Usage : mvn anyframe:create-crud -Dentity=... [-options] \n"
			+ "where options include : \n"
			+ " -Dpackage  the created CRUD source code's package \n"
			+ " -Dscope [ service | all ] \n";

	public static final String MAVEN_HELP_ACTIVATE_PLUGIN = "\nDescription : Converts target project to Anyframe plugin project \n"
			+ "Usage : mvn anyframe:activate-plugin \n";

	public static final String MAVEN_HELP_DEACTIVATE_PLUGIN = "\nDescription : Removes capability of Anyframe plugin project \n"
			+ "Usage : mvn anyframe:deactivate-plugin \n";

	public static final String MAVEN_HELP_PACKAGE_PLUGIN = "\nDescription : Packages a plugin as a jar archive which can be installed into another application \n"
			+ "Usage : mvn anyframe:package-plugin \n";

	public static final String MAVEN_HELP_INSTALL_PLUGINFILE = "\nDescription : Installs a plugin to the local Anyframe plugin repository  \n"
			+ "Usage : mvn anyframe:install-pluginfile \n";

	public static final String MAVEN_HELP_DEPLOY_PLUGINFILE = "\nDescription : Deploys a plugin to the remote Anyframe plugin repository \n"
			+ "Usage : mvn anyframe:deploy-pluginfile \n";

	public static final Map<String, String> MAVEN_HELP_MESSAGES_BY_COMMAND = new HashMap<String, String>();

	static {
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_INSTALL,
				MAVEN_HELP_INSTALL);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_UNINSTALL,
				MAVEN_HELP_UNINSTALL);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_INPLACE,
				MAVEN_HELP_INPLACE);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_INSTALLED_LIST,
				MAVEN_HELP_INSTALLED);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_INFO,
				MAVEN_HELP_INFO);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_LIST,
				MAVEN_HELP_LIST);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_LIST_UPDATE,
				MAVEN_HELP_LIST_UPDATE);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_UPDATE_CATALOG,
				MAVEN_HELP_UPDATE_CATALOG);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_CHANGE_DB,
				MAVEN_HELP_CHANGE_DB);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_CREATE_MODEL,
				MAVEN_HELP_CREATE_MODEL);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_CREATE_CRUD,
				MAVEN_HELP_CREATE_CRUD);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_ACTIVATE_PLUGIN,
				MAVEN_HELP_ACTIVATE_PLUGIN);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_DEACTIVATE_PLUGIN,
				MAVEN_HELP_DEACTIVATE_PLUGIN);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_PACKAGE_PLUGIN,
				MAVEN_HELP_PACKAGE_PLUGIN);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_INSTALL_PLUGINFILE,
				MAVEN_HELP_INSTALL_PLUGINFILE);
		MAVEN_HELP_MESSAGES_BY_COMMAND.put(CommandUtil.CMD_DEPLOY_PLUGINFILE,
				MAVEN_HELP_PACKAGE_PLUGIN);
	}
}
