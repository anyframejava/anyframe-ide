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
package org.anyframe.ide.command.maven.mojo;

import java.io.File;
import java.util.ArrayList;

import org.anyframe.ide.command.common.DefaultPluginInfoManager;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.maven.artifact.ant.Authentication;
import org.apache.maven.artifact.ant.Proxy;
import org.apache.maven.artifact.ant.RemoteRepository;
import org.apache.maven.artifact.ant.RepositoryPolicy;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;

/**
 * AbstractMojoTest is a commom class for other test classes.
 * 
 * In case testing in Eclipse Platform, this class needs to have a plugin.xml
 * file which was created when mvn plugin:descriptor. so, we added a
 * src/test/resources/META-INF/maven/plugin.xml file. In case testing in Maven,
 * maven creates that file automatically.
 * 
 * @author Soyon Lim
 */
public class AbstractMojoTest extends AbstractMojoTestCase {
	protected ArtifactRepositoryLayout repositoryLayout;
	protected DefaultPluginInfoManager pluginInfoManager;

	protected ArrayList remoteArtifactRepositories = new ArrayList();
	protected ArtifactRepository localRepository;

	/**
	 * initialize
	 */
	protected void setUp() throws Exception {
		// 1. initialize plexus container
		super.setUp();

		// 2. lookup components
		repositoryLayout = (ArtifactRepositoryLayout) lookup(ArtifactRepositoryLayout.ROLE);
		pluginInfoManager = (DefaultPluginInfoManager) lookup(DefaultPluginInfoManager.class
				.getName());

		// 3. set local/remote repositories
		setLocalArtifactRepository();
		setRemoteArtifactRepositories();
	}

	protected Mojo lookupMojo(String goalName) throws Exception {
		File pluginConfigXml = new File(getBasedir(),
				"/src/test/resources/plugin-config.xml");
		return lookupMojo(goalName, pluginConfigXml);
	}

	protected void setRemoteArtifactRepositories() throws Exception {
		RemoteRepository remoteRepository = new RemoteRepository();
		remoteRepository.setId("anyframe-repository");
		remoteRepository.setUrl("http://dev.anyframejava.org/maven/repo/");
		RepositoryPolicy snapshots = new RepositoryPolicy();
		snapshots.setEnabled(true);
		remoteRepository.addSnapshots(snapshots);
		remoteArtifactRepositories
				.add(createRemoteArtifactRepository(remoteRepository));
	}

	protected void setLocalArtifactRepository() throws Exception {
		localRepository = new DefaultArtifactRepository("local", "file://"
				+ new File(".").getAbsolutePath()
				+ CommonConstants.SRC_TEST_RESOURCES + "repo", repositoryLayout);
	}

	private ArtifactRepository createRemoteArtifactRepository(
			RemoteRepository repository) throws Exception {
		ArtifactRepositoryLayout repositoryLayout = (ArtifactRepositoryLayout) lookup(
				ArtifactRepositoryLayout.ROLE, repository.getLayout());

		ArtifactRepositoryFactory repositoryFactory = null;

		ArtifactRepository artifactRepository;

		try {
			repositoryFactory = getArtifactRepositoryFactory(repository);

			ArtifactRepositoryPolicy snapshots = buildArtifactRepositoryPolicy(repository
					.getSnapshots());
			ArtifactRepositoryPolicy releases = buildArtifactRepositoryPolicy(repository
					.getReleases());

			artifactRepository = repositoryFactory.createArtifactRepository(
					repository.getId(), repository.getUrl(), repositoryLayout,
					snapshots, releases);
		} finally {
			releaseArtifactRepositoryFactory(repositoryFactory);
		}

		return artifactRepository;
	}

	private ArtifactRepositoryFactory getArtifactRepositoryFactory(
			RemoteRepository repository) throws Exception {
		WagonManager manager = (WagonManager) lookup(WagonManager.ROLE);

		Authentication authentication = repository.getAuthentication();
		if (authentication != null) {
			manager.addAuthenticationInfo(repository.getId(), authentication
					.getUserName(), authentication.getPassword(),
					authentication.getPrivateKey(), authentication
							.getPassphrase());
		}

		Proxy proxy = repository.getProxy();
		if (proxy != null) {
			manager.addProxy(proxy.getType(), proxy.getHost(), proxy.getPort(),
					proxy.getUserName(), proxy.getPassword(), proxy
							.getNonProxyHosts());
		}

		return (ArtifactRepositoryFactory) lookup(ArtifactRepositoryFactory.ROLE);
	}

	private ArtifactRepositoryPolicy buildArtifactRepositoryPolicy(
			RepositoryPolicy policy) {
		boolean enabled = true;
		String updatePolicy = null;
		String checksumPolicy = null;

		if (policy != null) {
			enabled = policy.isEnabled();
			if (policy.getUpdatePolicy() != null) {
				updatePolicy = policy.getUpdatePolicy();
			}
			if (policy.getChecksumPolicy() != null) {
				checksumPolicy = policy.getChecksumPolicy();
			}
		}

		return new ArtifactRepositoryPolicy(enabled, updatePolicy,
				checksumPolicy);
	}

	private void releaseArtifactRepositoryFactory(
			ArtifactRepositoryFactory repositoryFactory) {
		try {
			getContainer().release(repositoryFactory);
		} catch (ComponentLifecycleException e) {
			// TODO: Warn the user, or not?
		}
	}

}
