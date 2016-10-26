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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import org.apache.maven.settings.Server;

/**
 * This is an DeployPluginFileMojo class. This mojo is for deploy plugin binary
 * file simply.
 * 
 * @goal deploy-pluginfile
 * @execute goal="package-plugin"
 * @author Soyon Lim
 */
public class DeployPluginFileMojo extends AbstractPluginMojo {
	/** @component role="org.anyframe.ide.command.common.DefaultPluginPomManager" */
	DefaultPluginPomManager pluginPomManager;

	/**
	 * @component 
	 *            role="org.anyframe.ide.command.common.DefaultPluginCatalogManager"
	 */
	PluginCatalogManager pluginCatalogManager;

	/**
	 * URL where the artifact will be deployed.
	 * 
	 * @parameter expression="${url}"
	 *            default-value="http://dev.anyframejava.org/maven/repo"
	 */
	private String url;

	/**
	 * Server Id to map on the &lt;id&gt; under &lt;server&gt; section of
	 * settings.xml In most cases, this parameter will be required for
	 * authentication.
	 * 
	 * @parameter expression="${repositoryId}"
	 *            default-value="anyframe-repository"
	 */
	private String repositoryId;
	
	/**
	 * define whether version of target plugin is latest. If this paramter value
	 * is 'true', then change value of latest in plugin-catalog-xxx.xml
	 * 
	 * @parameter expression="${isLatest}" default-value="true"
	 */
	private boolean isLatest;	

	/**
	 * main method for executing DeployPluginFileMojo. This mojo is executed
	 * when you input 'mvn anyframe:deploy-pluginfile [-options]'
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

			// 4. execute deploy-file mojo
			String mavenHome = System.getenv().get("MAVEN_HOME");
			String mvnCommand = "mvn";
			if (mavenHome != null) {
				mvnCommand = mavenHome + "/bin/mvn";
			}

			String os = System.getProperty("os.name");
			String extensions = "";

			if (os.startsWith("Windows")) {
				extensions = ".bat";
			}

			BufferedReader bufferedReader = null;
			InputStream inputStream = null;
			try {
				Runtime run = Runtime.getRuntime();

				Process pr = run.exec(mvnCommand + extensions
						+ " deploy:deploy-file -Dfile=./target/" + fileName
						+ " -DpomFile=./target/temp/pom.xml" + " -Durl=dav:"
						+ url + " -DrepositoryId=" + repositoryId + " -q");
				// pr.waitFor();

				inputStream = pr.getInputStream();
				bufferedReader = new BufferedReader(new InputStreamReader(
						inputStream));
				String line = "";

				StringBuffer lines = new StringBuffer();
				while ((line = bufferedReader.readLine()) != null) {
					lines.append(line + "\n");
				}

				if (lines.toString().contains("ERROR")) {
					throw new CommandException("Fail to deploy " + fileName
							+ " to remote repository ['" + url + "'].");
				}

				System.out.println(lines.toString());
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			}

			// 5. read a plugin-build.xml
			File pluginBuildXML = new File(baseDir,
					CommonConstants.PLUGIN_BUILD_FILE);

			PluginInfo pluginInfo = (PluginInfo) FileUtil
					.getObjectFromXML(pluginBuildXML);

			Server server = super.settings.getServer(repositoryId);
			if (server == null) {
				throw new CommandException(
						"You need define authentication profile about "
								+ repositoryId + " into settings.xml");
			}

			// 6. update plugin catalig file in remote repository
			pluginCatalogManager.updatePluginCatalog(request, baseDir, url,
					server.getUsername(), server.getPassword(), pluginInfo
							.isEssential(), isLatest, pluginInfo);
		} catch (Exception ex) {
			getLog().error(
					"Fail to execute DeployPluginFileMojo. The reason is '"
							+ ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}
	}
}
