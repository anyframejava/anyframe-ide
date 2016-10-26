/*
 * Copyright 2002-2012 the original author or authors.
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
package org.anyframe.ide.command.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.StringUtils;

/**
 * AbstractCommandTest is a commom class for other test classes.
 */
public abstract class AbstractCommandTest extends PlexusTestCase {
	protected String currentPath = new File(".").getAbsolutePath();
	protected String REPO = currentPath + CommonConstants.SRC_TEST_RESOURCES
			+ "repo";
	protected String PATH_SAMPLE_PROJECT = currentPath
			+ CommonConstants.SRC_TEST_RESOURCES + "project"
			+ CommonConstants.fileSeparator + "sample";

	
	protected String REPO_CUSTOM_PLUGIN = REPO
			+ "/anyframe/plugin/anyframe.plugin.custom/4.2.0";
	protected String PATH_CUSTOM_PLUGIN = currentPath
			+ CommonConstants.SRC_TEST_RESOURCES + "plugin"
			+ CommonConstants.fileSeparator + "custom"
			+ CommonConstants.fileSeparator;

	protected String PATH_ORIGINAL_PROJECT = currentPath
			+ CommonConstants.SRC_TEST_RESOURCES + "project"
			+ CommonConstants.fileSeparator + "original";

	private PlexusContainer container;

	/**
	 * sets up a Plexus container instance for running test.
	 */
	protected void setUp() throws Exception {
		// call this to enable super class to setup a Plexus container test
		// instance and enable component lookup.
		super.setUp();
		
		System.setProperty("user.home", currentPath + CommonConstants.SRC_TEST_RESOURCES);

		File file = new File("./src/test/resources/components.xml");
		InputStream configuration = new FileInputStream(file);
		getContainer().setConfigurationResource(
				new InputStreamReader(configuration));

		getContainer().initialize();
		getContainer().start();
	}

	/**
	 * lookup a component
	 */
	public Object lookup(String role) throws Exception {
		return getContainer().lookup(role);
	}

	public ArchetypeGenerationRequest createRequest(String baseDir) throws Exception {
		// 1. set repository
		File repo = new File(REPO);
		String repoPath = StringUtils.replace(repo.getAbsolutePath(), "\\",
				CommonConstants.fileSeparator);

		// 2. make a request with repository information
		ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();

		ArtifactRepositoryLayout repositoryLayout = (ArtifactRepositoryLayout) lookup(ArtifactRepositoryLayout.ROLE);
		ArtifactRepository localArtifactRepository = new DefaultArtifactRepository(
				"local", "file://" + repoPath, repositoryLayout);
		request.setLocalRepository(localArtifactRepository);
		request.setRemoteArtifactRepositories(new ArrayList());
		request.setOutputDirectory(baseDir);

		return request;
	}
}
