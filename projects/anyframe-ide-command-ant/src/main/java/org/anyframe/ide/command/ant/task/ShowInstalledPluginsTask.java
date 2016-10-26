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
package org.anyframe.ide.command.ant.task;

import java.util.Map;

import org.anyframe.ide.command.common.DefaultPluginInfoManager;
import org.anyframe.ide.command.common.PluginInfoManager;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * This is an ShowInstalledPluginsTask class. This task is for showing
 * information of installed plugins
 * 
 * @author SoYon Lim
 */
public class ShowInstalledPluginsTask extends AbstractPluginTask {
	private PluginInfoManager pluginInfoManager;

	/**
	 * main method for executing ShowInstalledPluginsTask this task is executed
	 * when you input 'anyframe installed'
	 */
	public void doExecute() {
		try {
			pluginInfoManager.showInstalledPlugins(getRequest(), getTarget());
		} catch (Exception e) {
			log("Fail to execute ShowInstalledPluginListTask", e,
					Project.MSG_ERR);
			throw new BuildException(e.getMessage());
		}
	}

	/**
	 * look up essential components which can install, uninstall, etc.
	 */
	public void lookupComponents() {
		pluginInfoManager = (PluginInfoManager) getPluginContainer().lookup(
				DefaultPluginInfoManager.class.getName());
	}

	// for anyframegen
	/**
	 * get all installed plugins
	 * 
	 * @param baseDir
	 *            folder which includes META-INF/plugin-installed.xml
	 * @return all installed plugins
	 */
	public Map<String, PluginInfo> getInstalledPlugins(String baseDir)
			throws Exception {
		initialize();
		lookupComponents();
		return pluginInfoManager.getInstalledPlugins(baseDir);
	}
}
