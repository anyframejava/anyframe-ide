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
package org.anyframe.ide.command.common.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.archiver.PomPropertiesUtil;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.ManifestException;

/**
 * This is a PluginArchiver class. This class is for making an archive file with
 * plugin resources not using maven artifact.
 * 
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @author modified by Sooyeon Park
 */
public class PluginArchiver extends MavenArchiver {

	private File archiveFile;

	@SuppressWarnings("rawtypes")
	public void createArchive(MavenProject project,
			MavenArchiveConfiguration archiveConfiguration)
			throws ArchiverException, ManifestException, IOException,
			DependencyResolutionRequiredException {
		MavenProject workingProject = new MavenProject(project);

		boolean forced = archiveConfiguration.isForced();
		if (archiveConfiguration.isAddMavenDescriptor()) {
			String groupId = workingProject.getGroupId();
			String artifactId = workingProject.getArtifactId();
			getArchiver()
					.addFile(
							project.getFile(),
							"META-INF/maven/" + groupId + "/" + artifactId
									+ "/pom.xml");

			// ----------------------------------------------------------------------
			// Create pom.properties file
			// ----------------------------------------------------------------------
			File pomPropertiesFile = archiveConfiguration
					.getPomPropertiesFile();
			if (pomPropertiesFile == null) {
				File dir = new File(workingProject.getBuild().getDirectory(),
						"maven-archiver");
				pomPropertiesFile = new File(dir, "pom.properties");
			}
			new PomPropertiesUtil().createPomProperties(workingProject,
					getArchiver(), pomPropertiesFile, forced);
		}

		// ----------------------------------------------------------------------
		// Create the manifest
		// ----------------------------------------------------------------------
		File manifestFile = archiveConfiguration.getManifestFile();

		if (manifestFile != null) {
			getArchiver().setManifest(manifestFile);
		}

		Manifest manifest = getManifest(workingProject, archiveConfiguration);

		// Configure the jar
		getArchiver().addConfiguredManifest(manifest);
		getArchiver().setCompress(archiveConfiguration.isCompress());
		getArchiver().setIndex(archiveConfiguration.isIndex());
		getArchiver().setDestFile(archiveFile);

		// make the archiver index the jars on the classpath, if we are adding
		// that to the manifest
		if (archiveConfiguration.getManifest().isAddClasspath()) {
			List artifacts = project.getRuntimeClasspathElements();
			for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
				File f = new File((String) iter.next());
				getArchiver().addConfiguredIndexJars(f);
			}
		}
		getArchiver().setForced(forced);

		// create archive
		getArchiver().createArchive();
	}

	public void setOutputFile(File outputFile) {
		archiveFile = outputFile;
	}
}
