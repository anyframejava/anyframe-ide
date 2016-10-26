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

import org.anyframe.ide.command.common.PluginDBChanger;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This is an DBConfigChangeMojo class. This mojo is for changing all db
 * configurations based on selected db
 * 
 * @goal change-db
 * @author Soyon Lim
 */
public class ChangeDBMojo extends AbstractPluginMojo {
	/** @component role="org.anyframe.ide.command.common.DefaultPluginDBChanger" */
	PluginDBChanger pluginDBChanger;

	/**
	 * main method for executing ChangeDBMojo. This mojo is executed when you
	 * input 'mvn anyframe:change-db'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
			setRepository(request);

			pluginDBChanger
					.change(request, baseDir.getAbsolutePath(), encoding);
		} catch (Exception ex) {
			getLog().error(
					"Fail to execute DBChangeMojo. The reason is '"
							+ ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}
	}
}
