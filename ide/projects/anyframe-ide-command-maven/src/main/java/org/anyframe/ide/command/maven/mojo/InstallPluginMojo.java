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

import java.io.File;

import org.anyframe.ide.command.common.CommandException;
import org.anyframe.ide.command.common.PluginInstaller;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This is an InstallPluginMojo class. This mojo is for installing a new plugin
 * based on selected plugin name
 * 
 * @goal install
 * @author Soyon Lim
 */
public class InstallPluginMojo extends AbstractPluginMojo {
	/** @component role="org.anyframe.ide.command.common.DefaultPluginInstaller" */
	PluginInstaller pluginInstaller;

	/**
	 * The plugin's name.
	 * 
	 * @parameter expression="${name}"
	 */
	protected String name;

	/**
	 * The plugin's version.
	 * 
	 * @parameter expression="${version}"
	 */
	protected String version;

	/**
	 * The plugin's binary file.
	 * 
	 * @parameter expression="${file}"
	 */
	protected File file;

	/**
	 * The template's home.
	 * 
	 * @parameter expression="${templateHome}"
	 */
	protected String templateHome;

	/**
	 * The inspection resource's home.
	 * 
	 * @parameter expression="${inspectionHome}"
	 */
	protected String inspectionHome;

	/**
	 * The template's home.
	 * 
	 * @parameter expression="${excludeSrc}" default-value="${false}"
	 */
	protected boolean excludeSrc;

	/**
	 * execution mode
	 * 
	 * @parameter expression="${isCLIMode}" default-value="true"
	 */
	private boolean isCLIMode;

	/**
	 * main method for executing InstallPluginMojo. This mojo is executed when
	 * you input 'mvn anyframe:install [-options]'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (StringUtils.isEmpty(name) && file == null) {
				throw new CommandException(
						"One or more required plugin parameters are invalid/missing for 'anyframe:install'. Try mvn anyframe:help -Dcommand=install.");
			}

			ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
			setRepository(request);

			if (templateHome == null || templateHome.length() == 0) {
				templateHome = baseDir.getAbsolutePath()
						+ CommonConstants.TEMPLATE_HOME;
			}

			if (inspectionHome == null || inspectionHome.length() == 0) {
				inspectionHome = baseDir.getAbsolutePath()
						+ CommonConstants.INSPECTION_HOME;
			}

			pluginInstaller.install(request, baseDir.getAbsolutePath(), name,
					version, file, encoding, true, excludeSrc, templateHome,
					inspectionHome, isCLIMode);

		} catch (Exception ex) {
			getLog().error(
					"Fail to execute InstallPluginMojo. The reason is '"
							+ ex.getMessage() + "'.", ex);
			
			throw new MojoFailureException(null);
		}
	}
}
