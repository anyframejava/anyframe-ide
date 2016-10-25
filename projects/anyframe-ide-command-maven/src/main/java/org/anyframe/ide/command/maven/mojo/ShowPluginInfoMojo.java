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

import org.anyframe.ide.command.common.PluginInfoManager;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This is an ShowPluginInfoMojo class. This mojo is for showing information of
 * a specific plugin.
 * 
 * @goal info
 * @author Soyon Lim
 */
public class ShowPluginInfoMojo extends AbstractPluginMojo {
	/**
	 * @component 
	 *            role="org.anyframe.ide.command.common.DefaultPluginInfoManager"
	 */
	protected PluginInfoManager pluginInfoManager;

	/**
	 * The plugin's name.
	 * 
	 * @parameter expression="${name}"
	 * @required
	 */
	protected String name;

	/**
	 * main method for executing ShowPluginInfoMojo. This mojo is
	 * executed when you input 'mvn anyframe:info'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
			setRepository(request);

			pluginInfoManager.showPluginInfo(request, name);
		} catch (Exception ex) {
			getLog().error("Fail to execute ShowPluginInfoMojo");
			throw new MojoFailureException(ex.getMessage());
		}
	}
}
