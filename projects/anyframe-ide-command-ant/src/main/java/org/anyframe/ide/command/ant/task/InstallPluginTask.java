/*
 * Copyright 2002-2008 the original author or authors.
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

import org.anyframe.ide.command.cli.util.PropertiesIO;
import org.anyframe.ide.command.common.CommandException;
import org.anyframe.ide.command.common.DefaultPluginInstaller;
import org.anyframe.ide.command.common.PluginInstaller;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * This is an InstallPluginTask class. This task is for installing a new plugin
 * based on selected plugin name
 * 
 * @author SoYon Lim
 */
public class InstallPluginTask extends AbstractPluginTask {
	private PluginInstaller pluginInstaller;

	private String excludeSrc = "false";

	private String version = "";

	private String file = "";

	/** check command line interface mode */
	private String isCLIMode = "false";

	/**
	 * main method for executing InstallPluginTask this task is executed when
	 * you input 'anyframe install PLUGIN_NAME [-options]'
	 */
	public void doExecute() throws BuildException {
		try {
			File metadataFile = new File(new File(getTarget())
					+ CommonConstants.METAINF, CommonConstants.METADATA_FILE);

			if (!metadataFile.exists()) {
				throw new CommandException("Can not find a '"
						+ metadataFile.getAbsolutePath()
						+ "' file. Please check a location of your project.");
			}

			PropertiesIO pio = new PropertiesIO(metadataFile.getAbsolutePath());
			String templateHome = pio.readValue(CommonConstants.ANYFRAME_HOME)
					+ CommonConstants.fileSeparator + "templates";

			String inspectionHome = pio
					.readValue(CommonConstants.ANYFRAME_HOME)
					+ CommonConstants.fileSeparator
					+ "ide"
					+ CommonConstants.fileSeparator + "inspection";

			if (isEmpty("version", this.version)) {
				this.version = null;
			}

			if (isEmpty("excludeSrc", this.excludeSrc)) {
				this.excludeSrc = "false";
			}

			File pluginJar = null;
			if (!isEmpty("file", this.file)) {
				pluginJar = new File(this.file);
			}

			if (isEmpty("isCLIMode", this.isCLIMode)) {
				this.isCLIMode = "true";
			}

			pluginInstaller.install(getRequest(), getTarget(), getName(),
					version, pluginJar, getEncoding(), false, new Boolean(
							excludeSrc).booleanValue(), templateHome,
					inspectionHome, new Boolean(this.isCLIMode).booleanValue());
		} catch (Exception e) {
			log("Fail to execute InstallPluginTask", e, Project.MSG_ERR);
			throw new BuildException(e.getMessage());
		}
	}

	/**
	 * look up essential components which can install, uninstall, etc.
	 */
	public void lookupComponents() {
		pluginInstaller = (PluginInstaller) getPluginContainer().lookup(
				DefaultPluginInstaller.class.getName());
	}

	public void setExcludeSrc(String excludeSrc) {
		this.excludeSrc = excludeSrc;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public void setIsCLIMode(String isCLIMode) {
		this.isCLIMode = isCLIMode;
	}

}
