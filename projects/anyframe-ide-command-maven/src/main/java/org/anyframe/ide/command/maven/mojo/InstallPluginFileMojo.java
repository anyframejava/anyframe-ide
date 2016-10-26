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
import org.anyframe.ide.command.common.DefaultPluginPomManager;
import org.anyframe.ide.command.common.PluginCatalogManager;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.install.InstallFileMojo;

/**
 * This is an InstallPluginFileMojo class. This mojo is for install plugin
 * binary file simply.
 * 
 * @goal install-pluginfile
 * @execute goal="package-plugin"
 * @author Soyon Lim
 */
public class InstallPluginFileMojo extends AbstractPluginMojo {
	/** @component role="org.anyframe.ide.command.common.DefaultPluginPomManager" */
	DefaultPluginPomManager pluginPomManager;

	/**
	 * @component 
	 *            role="org.anyframe.ide.command.common.DefaultPluginCatalogManager"
	 */
	PluginCatalogManager pluginCatalogManager;

	/**
	 * @component role="org.apache.maven.plugin.Mojo" role-hint=
	 *            "org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file"
	 */
	InstallFileMojo installFileMojo;

	/**
	 * main method for executing InstallPluginFileMojo. This mojo is executed
	 * when you input 'mvn anyframe:install-pluginfile [-options]'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			// 1. initialize
			ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
			setRepository(request);

			// 2. read pom.xml
			File pomFile = new File(baseDir + CommonConstants.fileSeparator
					+ "target" + CommonConstants.fileSeparator + "temp",
					Constants.ARCHETYPE_POM);
			if (!pomFile.exists()) {
				throw new CommandException(
						"You need target/temp/pom.xml file to install your plugin into your local repository. Please try package-plugin first.");
			}
			Model model = pluginPomManager.readPom(pomFile);

			// 3. read packging file
			String fileName = model.getArtifactId() + "-" + model.getVersion()
					+ "." + CommonConstants.EXT_JAR;
			File file = new File(baseDir + CommonConstants.fileSeparator
					+ "target", fileName);
			if (!file.exists()) {
				throw new CommandException(
						"You need target/"
								+ fileName
								+ " file to install your plugin into your local repository. Please try package-plugin first.");
			}

			// 4. execute install-file mojo
			setMojoVariable(installFileMojo, "file", file);
			setMojoVariable(installFileMojo, "pomFile", pomFile);
			setMojoVariable(installFileMojo, "localRepository", localRepository);
			installFileMojo.execute();

			// 5. read a plugin-build.xml
			File pluginBuildXML = new File(baseDir,
					CommonConstants.PLUGIN_BUILD_FILE);

			PluginInfo pluginInfo = (PluginInfo) FileUtil
					.getObjectFromXML(pluginBuildXML);

			// 6. update a plugin catalog file in local repository
			pluginCatalogManager.updatePluginCatalog(request, pluginInfo
					.isEssential(), pluginInfo);

		} catch (Exception ex) {
			getLog().error(
					"Fail to execute InstallPluginFileMojo. The reason is '"
							+ ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}
	}
}
