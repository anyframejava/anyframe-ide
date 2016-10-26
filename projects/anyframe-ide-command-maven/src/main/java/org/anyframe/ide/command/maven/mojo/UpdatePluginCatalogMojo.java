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
package org.anyframe.ide.command.maven.mojo;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.anyframe.ide.command.common.catalog.RemotePluginCatalogDataSource;

/**
 * This is an UpdatePluginCatalogMojo class. This mojo is for updating
 * plugin-catalog-optional.xml, plugin-catalog-essential.xml file.
 * 
 * @goal update-catalog
 * @author Soyon Lim
 */
public class UpdatePluginCatalogMojo extends AbstractPluginMojo {
	/**
	 * @component role=
	 *            "org.anyframe.ide.command.common.catalog.RemotePluginCatalogDataSource"
	 */
	protected RemotePluginCatalogDataSource pluginCatalogDataSource;

	/**
	 * main method for executing UpdatePluginCatalogMojo. This mojo is executed
	 * when you input 'mvn anyframe:update-catalog'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
			setRepository(request);

			pluginCatalogDataSource.readPluginCatalog(request);
			
			System.out.println("Plugin catalog files were updated successfully.");
		} catch (Exception ex) {
			getLog().error(
					"Fail to execute UpdatePluginCatalogMojo. The reason is '"
							+ ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}
	}

}
