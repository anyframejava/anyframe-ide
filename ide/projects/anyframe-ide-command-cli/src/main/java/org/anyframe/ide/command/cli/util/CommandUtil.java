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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a CommandUtil class. This class is for managing Commands.
 * 
 * @author SooYeon Park
 */
public class CommandUtil implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Map<String, String> commandBuildXmlMap = new HashMap<String, String>();
	private static List<String> commandWithPluginTypeList = new ArrayList<String>();
	private static List<String> commandList = new ArrayList<String>();
	private static List<String> pluginNameOptionalCommandList = new ArrayList<String>();
	private static List<String> pluginNameNecessaryCommandList = new ArrayList<String>();

	public static final String CMD_CREATE_MODEL = "create-model";
	public static final String CMD_CREATE_CRUD = "create-crud";
	public static final String CMD_BUILD_APP = "build";
	public static final String CMD_RUN = "run";
	public static final String CMD_CHANGE_DB = "change-db";

	public static final String CMD_HELP = "-help";
	public static final String CMD_CREATE_PROJECT = "create-project";
	public static final String CMD_INSTALL = "install";
	public static final String CMD_UNINSTALL = "uninstall";
	public static final String CMD_LIST = "list";
	public static final String CMD_LIST_UPDATE = "list-update";
	public static final String CMD_INFO = "info";
	public static final String CMD_INSTALLED_LIST = "installed";
	public static final String CMD_UPDATE_CATALOG = "update-catalog";
	public static final String CMD_INPLACE = "inplace";
	public static final String CMD_ACTIVATE_PLUGIN = "activate-plugin";
	public static final String CMD_DEACTIVATE_PLUGIN = "deactivate-plugin";
	public static final String CMD_PACKAGE_PLUGIN = "package-plugin";
	public static final String CMD_INSTALL_PLUGINFILE = "install-pluginfile";
	public static final String CMD_DEPLOY_PLUGINFILE = "deploy-pluginfile";

	public static final String BUILDXML_BUILD_APP = "application-build.xml";
	public static final String BUILDXML_CREATE_PROJECT = "project-creation.xml";
	public static final String BUILDXML_BUILD_MODULE = "project-build.xml";
	public static final String BUILDXML_GENERATE_CODE = "code-generation.xml";
	
	public static final String[] HELP_COMMANDS = { "install", "uninstall",
			"inplace", "installed", "info", "list", "list-update", "change-db",
			"create-model", "create-crud", "activate-plugin",
			"deactivate-plugin", "package-plugin", "update-catalog" };

	static {
		commandBuildXmlMap.put(CMD_CREATE_PROJECT, "project-creation.xml");
		commandBuildXmlMap.put(CMD_CREATE_MODEL, "code-generation.xml");
		commandBuildXmlMap.put(CMD_CREATE_CRUD, "code-generation.xml");
		commandBuildXmlMap.put(CMD_BUILD_APP, "application-build.xml");
		commandBuildXmlMap.put(CMD_RUN, "application-build.xml");
		commandBuildXmlMap.put(CMD_CHANGE_DB, "db-config-change.xml");
	}

	public static boolean containsCommandBuildXml(String command) {
		return commandBuildXmlMap.containsKey(command);
	}

	public static String getBuildXmlFile(String command) {
		return commandBuildXmlMap.get(command);
	}

	public static boolean containsCommandWithPluginType(String command) {
		return commandWithPluginTypeList.contains(command);
	}

	static {
		commandList.add(CMD_CREATE_PROJECT);
		commandList.add(CMD_INSTALL);
		commandList.add(CMD_UNINSTALL);
		commandList.add(CMD_LIST);
		commandList.add(CMD_LIST_UPDATE);
		commandList.add(CMD_INFO);
		commandList.add(CMD_INSTALLED_LIST);
		commandList.add(CMD_UPDATE_CATALOG);

		pluginNameNecessaryCommandList.add(CMD_INSTALL);
		pluginNameNecessaryCommandList.add(CMD_UNINSTALL);
		pluginNameNecessaryCommandList.add(CMD_INFO);
	}

	protected CommandUtil() {
		// prevents calls from subclass
		throw new UnsupportedOperationException();
	}

	public static boolean containsCommand(String command) {
		return commandList.contains(command);
	}

	public static List<String> getCommandList() {
		return commandList;
	}

	public static boolean containsPluginNameOptionalCommand(String command) {
		return pluginNameOptionalCommandList.contains(command);
	}

	public static boolean containsPluginNameNecessaryCommand(String command) {
		return pluginNameNecessaryCommandList.contains(command);
	}

}
