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
package org.anyframe.ide.command.maven.mojo;

import org.anyframe.ide.command.common.PluginUninstaller;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This is an UninstallPluginMojo class. This mojo is for uninstalling a
 * specified plugin
 * 
 * @goal uninstall
 * @author Soyon Lim
 */
public class UninstallPluginMojo extends AbstractPluginMojo {
	/**
	 * @component 
	 *            role="org.anyframe.ide.command.common.DefaultPluginUninstaller"
	 */
	PluginUninstaller pluginUninstaller;

	/**
	 * The plugin's type name.
	 * 
	 * @parameter expression="${name}"
	 * @required
	 */
	protected String name;

	/**
	 * file names to exclude.
	 * 
	 * @parameter expression="${excludes}"
	 */
	protected String excludes = "";

	/**
	 * main method for executing UninstallPluginMojo. This mojo is executed when
	 * you input 'mvn anyframe:uninstall [-options]'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			// 1. initialize request
			ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
			setRepository(request);

			// 2. try to uninstall
			pluginUninstaller.uninstall(request, baseDir.getAbsolutePath(),
					name, excludes, encoding, true);
		} catch (Exception ex) {
			getLog().error("Fail to execute UninstallPluginMojo");
			throw new MojoFailureException(ex.getMessage());
		}
	}
}
