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

import org.anyframe.ide.command.common.catalog.RemotePluginCatalogDataSource;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * This is an UpdatePluginCatalogTask class. This task is for updating
 * plugin-catalog-essential.xml, plugin-catalog-optional file.
 * 
 * @author Soyon Lim
 */
public class UpdatePluginCatalogTask extends AbstractPluginTask {
	private RemotePluginCatalogDataSource remotePluginCatalogDataSource;

	/**
	 * main method for executing UpdatePluginCatalogTask this task is executed
	 * when you input 'anyframe update-catalog'
	 */
	public void doExecute() throws BuildException {
		try {
			remotePluginCatalogDataSource.readPluginCatalog(getRequest());
			
			System.out.println("Plugin catalog files were updated successfully.");
		} catch (Exception e) {
			log("Fail to execute UpdatePluginCatalogTask", e, Project.MSG_ERR);
			throw new BuildException(e.getMessage());
		}
	}

	/**
	 * look up essential components which can install, uninstall, etc.
	 */
	public void lookupComponents() {
		remotePluginCatalogDataSource = (RemotePluginCatalogDataSource) getPluginContainer()
				.lookup(RemotePluginCatalogDataSource.class.getName());
	}
}
