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

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.anyframe.ide.command.common.CommandException;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * This is an PlacePluginDependenciesMojo class. This mojo is for copying
 * dependent libraries of a current pom file
 * 
 * @goal inplace
 * @requiresDependencyResolution compile
 * @author Soyon Lim
 */
public class PlacePluginDependenciesMojo extends AbstractPluginMojo {

	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * main method for executing PlacePluginDependenciesMojo. This mojo is
	 * executed when you input 'mvn anyframe:inplace'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
			setRepository(request);

			File metadataFile = new File(baseDir.getAbsolutePath()
					+ CommonConstants.METAINF, CommonConstants.METADATA_FILE);

			if (!metadataFile.exists()) {
				throw new CommandException("Can not find a '"
						+ metadataFile.getAbsolutePath()
						+ "' file. Please check a location of your project.");
			}

			PropertiesIO pio = new PropertiesIO(metadataFile.getAbsolutePath());
			String projectType = pio.readValue(CommonConstants.PROJECT_TYPE);

			File destination = null;
			if (projectType.equals(CommonConstants.PROJECT_TYPE_WEB)) {
				destination = new File(baseDir,
						CommonConstants.SRC_MAIN_WEBAPP_LIB);
			} else {
				destination = new File(baseDir, "lib");
			}

			if (!destination.exists()) {
				destination.mkdirs();
			}

			copyDependencyLibs(request, destination);
			
			System.out.println("Dependent libraries of a current project are copied to the web library folder successfully.");
			
		} catch (Exception ex) {
			getLog().error(
					"Fail to execute PlacePluginDependenciesMojo. The reason is '"
							+ ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}
	}

	@SuppressWarnings("unchecked")
	private void copyDependencyLibs(ArchetypeGenerationRequest request,
			File destination) throws Exception {
		Set dependencyArtifacts = project.getDependencyArtifacts();

		Iterator<Artifact> dependencyArtifact = dependencyArtifacts.iterator();

		while (dependencyArtifact.hasNext()) {
			Artifact artifact = dependencyArtifact.next();
			if (artifact.getScope() == null || artifact.getScope().equals("")
					|| artifact.getScope().equals("compile")) {
				FileUtil.copyDir(artifact.getFile().getCanonicalFile(),
						destination);
			}
		}
	}
}
