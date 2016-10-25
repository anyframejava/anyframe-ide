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
package org.anyframe.ide.command.ant.task;

import org.anyframe.ide.command.common.DefaultPluginUninstaller;
import org.anyframe.ide.command.common.PluginUninstaller;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * This is an UninstallPluginTask class. This task is for uninstalling a
 * specified plugin
 * 
 * @author SoYon Lim
 */
public class UninstallPluginTask extends AbstractPluginTask {
	private PluginUninstaller pluginUninstaller;
	private String excludes = "";

	/**
	 * main method for executing UninstallPluginTask this task is executed when
	 * you input 'anyframe uninstall PLUGIN_NAME [-options]'
	 */
	public void doExecute() throws BuildException {
		try {
			pluginUninstaller.uninstall(getRequest(), getTarget(),
					getName(), excludes, getEncoding(), false);
		} catch (Exception e) {
			log("Fail to execute UninstallPluginTask", e, Project.MSG_ERR);
			throw new BuildException(e.getMessage());
		}
	}

	/**
	 * look up essential components which can install, uninstall, etc.
	 */
	public void lookupComponents() {
		pluginUninstaller = (PluginUninstaller) getPluginContainer().lookup(
				DefaultPluginUninstaller.class.getName());
	}

	/*********************************************************************/
	/************ getter, setter *****************************************/
	/*********************************************************************/
	public void setExcludes(String excludes) {
		this.excludes = excludes;
	}
}
