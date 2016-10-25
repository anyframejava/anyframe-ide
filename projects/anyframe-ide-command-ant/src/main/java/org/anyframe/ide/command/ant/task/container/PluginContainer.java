/*
 * Copyright 2002-2008 the original author or authors.
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
package org.anyframe.ide.command.ant.task.container;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.tools.ant.BuildException;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.embed.Embedder;

/**
 * This is an PluginContainer class. This class is for initializing
 * plexus-container and creating request based on settings.xml file
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author modified by Jeryeon Kim
 * 
 */
public class PluginContainer {
	ArtifactRepository localArtifactRepository;
	List<ArtifactRepository> remoteArtifactRepositories = new ArrayList<ArtifactRepository>();
	Artifact pomArtifact;
	PlexusContainer container;
	ArchetypeGenerationRequest request;

	private String anyframeHome = "";

	public PluginContainer(String anyframeHome, String baseDir, boolean isOffline) {
		try {
			ClassWorld classWorld = new ClassWorld();
			classWorld.newRealm("plexus.core", getClass().getClassLoader());
			Embedder embedder = new Embedder();
			embedder.start(classWorld);
			this.container = embedder.getContainer();

			this.anyframeHome = anyframeHome;

			setRepository(isOffline);
			createRequest(baseDir);

			if (isOffline) {
				WagonManager wagonManager = (WagonManager) this.container
						.lookup(WagonManager.class.getName());
				wagonManager.setOnline(false);
			}
		} catch (Exception e) {
			throw new BuildException("Unable to start plexus container.", e);
		}
	}

	public Object lookup(String role) {
		try {
			return this.container.lookup(role);
		} catch (ComponentLookupException e) {
			throw new BuildException("Unable to find component: " + role, e);
		}
	}

	public Object lookup(String role, String roleHint) {
		try {
			return this.container.lookup(role, roleHint);
		} catch (ComponentLookupException e) {
			throw new BuildException("Unable to find component: " + role + "["
					+ roleHint + "]", e);
		}
	}

	public ArchetypeGenerationRequest getRequest() {
		return this.request;
	}

	/*********************************************************************/
	/************ set up artifact repository *****************************/
	/*********************************************************************/
	private void setRepository(boolean isOffline) throws Exception {
		localArtifactRepository = createLocalArtifactRepository();
		remoteArtifactRepositories = createRemoteArtifactRepositories(isOffline);

		ArtifactFactory artifactFactory = (ArtifactFactory) lookup(ArtifactFactory.ROLE);
		pomArtifact = artifactFactory.createBuildArtifact("org.apache.maven",
				"super-pom", "2.0", "jar");
	}

	public void createRequest(String baseDir) throws Exception {
		request = new ArchetypeGenerationRequest();
		request.setLocalRepository(this.localArtifactRepository);
		request.setRemoteArtifactRepositories(this.remoteArtifactRepositories);
		request.setOutputDirectory(baseDir);
	}

	private ArtifactRepository createLocalArtifactRepository() {
		ArtifactRepositoryLayout repositoryLayout = (ArtifactRepositoryLayout) lookup(ArtifactRepositoryLayout.ROLE);

		ArtifactRepositoryPolicy snapshots = new ArtifactRepositoryPolicy(true,
				ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER,
				ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE);

		ArtifactRepositoryPolicy releases = new ArtifactRepositoryPolicy(true,
				ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER,
				ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE);

		return new DefaultArtifactRepository("local", "file://"
				+ new File(this.anyframeHome, "repo"), repositoryLayout,
				snapshots, releases);
	}

	private List<ArtifactRepository> createRemoteArtifactRepositories(
			boolean isOffline) throws Exception {

		List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();

		if (!isOffline) {
			remoteRepositories.add(createRemoteArtifactRepository());
		}
		return remoteRepositories;
	}

	private ArtifactRepository createRemoteArtifactRepository() {
		ArtifactRepositoryLayout repositoryLayout = (ArtifactRepositoryLayout) lookup(
				ArtifactRepositoryLayout.ROLE, "default");

		ArtifactRepositoryFactory repositoryFactory = null;

		ArtifactRepository artifactRepository;

		try {
			repositoryFactory = (ArtifactRepositoryFactory) lookup(ArtifactRepositoryFactory.ROLE);

			ArtifactRepositoryPolicy snapshots = new ArtifactRepositoryPolicy(
					true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER,
					ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE);

			ArtifactRepositoryPolicy releases = new ArtifactRepositoryPolicy(
					true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER,
					ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE);

			artifactRepository = repositoryFactory.createArtifactRepository(
					"anyframe-repository", CommonConstants.REMOTE_CATALOG_PATH,
					repositoryLayout, snapshots, releases);
		} finally {
			releaseArtifactRepositoryFactory(repositoryFactory);
		}

		return artifactRepository;
	}

	protected void releaseArtifactRepositoryFactory(
			ArtifactRepositoryFactory repositoryFactory) {
		try {
			this.container.release(repositoryFactory);
		} catch (ComponentLifecycleException e) {
			// TODO: Warn the user, or not?
		}
	}

}
