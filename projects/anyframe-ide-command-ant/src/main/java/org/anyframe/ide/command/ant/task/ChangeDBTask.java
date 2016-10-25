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

import java.io.File;

import org.anyframe.ide.command.common.DefaultPluginDBChanger;
import org.anyframe.ide.command.common.PluginDBChanger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * This is a ChangeDBTask class. This task is for changing db information.
 * 
 * @author Sooyeon Park
 */
public class ChangeDBTask extends AbstractPluginTask {

	/**
	 * plugin db changer component
	 */
	private PluginDBChanger pluginDBChanger;

	/**
	 * project home directory
	 */
	private String projectHome = "";

	/**
	 * main method for executing ChangeDBTask this task is executed when you
	 * input 'anyframe change-db'
	 */
	public void doExecute() {
		try {
			if (isEmpty("projectHome", getProjectHome())) {
				this.projectHome = new File(".").getAbsolutePath();
			}
			pluginDBChanger.change(getRequest(), this.projectHome,
					getEncoding());
		} catch (Exception e) {
			log("[error] Fail to execute ChangeDBTask", e, Project.MSG_ERR);
			throw new BuildException(e.getMessage());
		}
	}

	/**
	 * look up essential components.
	 */
	public void lookupComponents() {
		pluginDBChanger = (PluginDBChanger) getPluginContainer().lookup(
				DefaultPluginDBChanger.class.getName());
	}

	/**
	 * get project home directory
	 * 
	 * @return project home
	 */
	public String getProjectHome() {
		return projectHome;
	}

	/**
	 * set project home directory
	 * 
	 * @param projectHome
	 *            project home
	 */
	public void setProjectHome(String projectHome) {
		this.projectHome = projectHome;
	}
}
