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

import org.anyframe.ide.command.common.PluginBuildManager;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This is an ActivatePluginMojo class. This mojo is for creating a plugin build
 * file.
 * 
 * @goal activate-plugin
 * @author Sooyeon Park
 */
public class ActivatePluginMojo extends AbstractPluginMojo {

	/**
	 * @component 
	 *            role="org.anyframe.ide.command.common.DefaultPluginBuildManager"
	 */
	PluginBuildManager pluginBuildManager;

	/**
	 * main method for executing ActivatePluginMojo. This mojo is executed when
	 * you input 'mvn anyframe:activate-plugin'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
			setRepository(request);

			pluginBuildManager.activate(request, baseDir.getAbsolutePath());
		} catch (Exception ex) {
			getLog().error(
					"Fail to execute ActivatePluginMojo. The reason is '"
							+ ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}

	}
}
