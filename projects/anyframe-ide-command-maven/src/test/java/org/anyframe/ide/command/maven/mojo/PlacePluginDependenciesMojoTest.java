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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.StringUtils;

/**
 * TestCase Name : PlacePluginDependenciesMojoTest <br>
 * <br>
 * [Description] : Test for Custom task 'PlacePluginDependenciesMojo'<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : copy all dependent libraries to target folder (based
 * on current pom file)</li>
 * </ul>
 * 
 * @author Soyon Lim
 */
public class PlacePluginDependenciesMojoTest extends AbstractMojoTest {
	protected ArtifactFactory artifactFactory;
	protected ArtifactResolver resolver;
	PlacePluginDependenciesMojo placePluginDependenciesMojo;

	/**
	 * initialize
	 */
	protected void setUp() throws Exception {
		// 1. initialize plexus container
		super.setUp();

		artifactFactory = (ArtifactFactory) lookup(ArtifactFactory.ROLE);
		resolver = (ArtifactResolver) lookup(ArtifactResolver.ROLE);
	}

	/**
	 * [Flow #-1] Positive Case : copy all dependent libraries to target folder
	 * (based on current pom file)
	 * 
	 * @throws Exception
	 */
	public void testPlacePluginDependencies() throws Exception {
		String basedir = getBasedir();
		basedir = StringUtils.replace(basedir, "\\",
				CommonConstants.fileSeparator);
		// 1. lookup placePluginDependenciesMojo
		placePluginDependenciesMojo = (PlacePluginDependenciesMojo) lookupMojo("inplace");

		// 2. set variable value to mojo
		VersionRange versionRange = VersionRange.createFromVersionSpec("2.7.7");

		Artifact artifact = artifactFactory.createDependencyArtifact("antlr",
				"antlr", versionRange, "jar", null, "compile", false);
		resolver.resolve(artifact, remoteArtifactRepositories, localRepository);

		Set<Artifact> dependencyArtifacts = new HashSet<Artifact>();
		dependencyArtifacts.add(artifact);

		MavenProject project = new MavenProject();
		project.setDependencyArtifacts(dependencyArtifacts);

		setVariableValueToObject(placePluginDependenciesMojo, "baseDir",
				new File("./src/test/resources/project/sample"));
		setVariableValueToObject(placePluginDependenciesMojo,
				"localRepository", localRepository);
		setVariableValueToObject(placePluginDependenciesMojo,
				"remoteArtifactRepositories", remoteArtifactRepositories);
		setVariableValueToObject(placePluginDependenciesMojo, "project",
				project);
		setVariableValueToObject(placePluginDependenciesMojo, "settings",
				new Settings());

		// 3. execute mojo
		placePluginDependenciesMojo.execute();

		// 4. assert
		File libDir = new File(
				new File("./src/test/resources/project/sample")
						.getAbsolutePath() + "/src/main/webapp/WEB-INF/lib");
		List jarFiles = FileUtil.findFiles(libDir.getAbsolutePath(),
				new String[] { "jar" }, false);
		assertEquals("Fail to place plugin dependencies.", 2, jarFiles.size());
	}
}
