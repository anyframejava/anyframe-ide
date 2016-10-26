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
package org.anyframe.ide.command.maven.mojo.container;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.command.common.DefaultPluginPomManager;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;
import org.apache.maven.settings.RuntimeInfo;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.SettingsUtils;
import org.apache.maven.settings.TrackableBase;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.embed.Embedder;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.interpolation.EnvarBasedValueSource;
import org.codehaus.plexus.util.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * This is an PluginContainer class. This class is for initializing
 * plexus-container and creating request based on settings.xml file
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author modified by Soyon Lim
 * 
 */
public class PluginContainer {
	Settings settings;
	ArtifactRepository localArtifactRepository;
	List<ArtifactRepository> remoteArtifactRepositories = new ArrayList<ArtifactRepository>();
	Artifact pomArtifact;
	PlexusContainer container;
	ArchetypeGenerationRequest request;

	String baseDir;
	public static String SLASH = System.getProperty("file.separator");

	public PluginContainer(String baseDir) throws Exception {
		try {
			ClassWorld classWorld = new ClassWorld();
			classWorld.newRealm("plexus.core", getClass().getClassLoader());
			Embedder embedder = new Embedder();
			embedder.start(classWorld);
			this.container = embedder.getContainer();

			this.baseDir = baseDir;

			initSettings();
			setRepository();
			createRequest();
		} catch (Exception e) {
			throw new Exception("Unable to start plexus container.", e);
		}
	}

	/**
	 * lookup a component using component's role
	 * 
	 * @param role
	 *            component's identifier
	 * @return a component
	 */
	public Object lookup(String role) throws Exception {
		try {
			return this.container.lookup(role);
		} catch (ComponentLookupException e) {
			throw new Exception("Unable to find component: " + role, e);
		}
	}

	/**
	 * lookup a component using component's role and hint
	 * 
	 * @param role
	 *            component's identifier
	 * @param roleHint
	 *            component's identifier
	 * @return a component
	 */
	public Object lookup(String role, String roleHint) throws Exception {
		try {
			return this.container.lookup(role, roleHint);
		} catch (ComponentLookupException e) {
			throw new Exception("Unable to find component: " + role + "["
					+ roleHint + "]", e);
		}
	}

	public ArchetypeGenerationRequest getRequest() {
		return this.request;
	}

	/**
	 * initialize maven's settings
	 */
	private void initSettings() {
		File userSettingsFile = new File(new File(System
				.getProperty("user.home"), ".m2"), "settings.xml");
		Settings userSettings = loadSettings(userSettingsFile);

		// look in ${M2_HOME}/conf
		File globalSettingsFile = null;

		String maveHome = System.getenv("MAVEN_HOME");

		if (maveHome != null) {
			globalSettingsFile = new File(maveHome + SLASH + "conf" + SLASH
					+ "settings.xml");
		}

		Settings globalSettings = loadSettings(globalSettingsFile);

		SettingsUtils.merge(userSettings, globalSettings,
				TrackableBase.GLOBAL_LEVEL);
		settings = userSettings;

		if (StringUtils.isEmpty(settings.getLocalRepository())) {
			String location = new File(new File(
					System.getProperty("user.home"), ".m2"), "repository")
					.getAbsolutePath();
			settings.setLocalRepository(location);
		}
	}

	/**
	 * create local/remote repository
	 */
	private void setRepository() throws Exception {
		localArtifactRepository = createLocalArtifactRepository();
		remoteArtifactRepositories = createRemoteArtifactRepositories();

		ArtifactFactory artifactFactory = (ArtifactFactory) lookup(ArtifactFactory.ROLE);
		pomArtifact = artifactFactory.createBuildArtifact("org.apache.maven",
				"super-pom", "2.0", "jar");
	}

	/**
	 * create a request and set remote/local repositories
	 */
	public void createRequest() throws Exception {
		request = new ArchetypeGenerationRequest();
		request.setLocalRepository(this.localArtifactRepository);
		request.setRemoteArtifactRepositories(this.remoteArtifactRepositories);
	}

	/**
	 * create local repository
	 * 
	 * @return local repository
	 */
	private ArtifactRepository createLocalArtifactRepository() {
		ArtifactRepositoryLayout repositoryLayout = new DefaultRepositoryLayout();

		ArtifactRepositoryPolicy snapshots = new ArtifactRepositoryPolicy(true,
				ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER,
				ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE);

		ArtifactRepositoryPolicy releases = new ArtifactRepositoryPolicy(true,
				ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER,
				ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE);

		return new DefaultArtifactRepository("local", "file://"
				+ settings.getLocalRepository(), repositoryLayout, snapshots,
				releases);
	}

	/**
	 * create remote repositories
	 * 
	 * @return remote reopsitories
	 */
	@SuppressWarnings("unchecked")
	private List<ArtifactRepository> createRemoteArtifactRepositories()
			throws Exception {
		List<ArtifactRepository> list = new ArrayList<ArtifactRepository>();

		if (!settings.isOffline()) {
			if (baseDir != null) {
				// read a pom.xml
				File pomFile = new File(baseDir, Constants.ARCHETYPE_POM);
				if (pomFile.exists()) {
					DefaultPluginPomManager pomManager = (DefaultPluginPomManager) lookup(DefaultPluginPomManager.class
							.getName());

					Model model = pomManager.readPom(new File(baseDir,
							Constants.ARCHETYPE_POM));
					List<Repository> repositories = model.getRepositories();
					for (Repository repository : repositories) {
						list.add(createRemoteArtifactRepository(repository));
					}
				}
			}

			// read a settings.xml
			List<String> activeProfiles = settings.getActiveProfiles();
			for (String activeProfileId : activeProfiles) {
				Profile activeProfile = (Profile) settings.getProfilesAsMap()
						.get(activeProfileId);

				List<Repository> repositories = activeProfile.getRepositories();

				for (Repository repository : repositories) {
					list.add(createRemoteArtifactRepository(repository));
				}
			}
		}
		return list;
	}

	/**
	 * create remote repository
	 * 
	 * @return remote reopsitory
	 */
	private ArtifactRepository createRemoteArtifactRepository(
			Repository repository) throws Exception {
		ArtifactRepositoryLayout repositoryLayout = (ArtifactRepositoryLayout) lookup(
				ArtifactRepositoryLayout.ROLE, repository.getLayout());

		ArtifactRepositoryFactory repositoryFactory = null;

		ArtifactRepository artifactRepository;

		try {
			repositoryFactory = getArtifactRepositoryFactory(repository);

			ArtifactRepositoryPolicy snapshots = buildRemoteArtifactRepositoryPolicy(repository
					.getSnapshots());
			ArtifactRepositoryPolicy releases = buildRemoteArtifactRepositoryPolicy(repository
					.getReleases());

			artifactRepository = repositoryFactory.createArtifactRepository(
					repository.getId(), repository.getUrl(), repositoryLayout,
					snapshots, releases);
		} finally {
			releaseArtifactRepositoryFactory(repositoryFactory);
		}

		return artifactRepository;
	}

	/**
	 * configure wagon manager and lookup a ArtifactRepositoryFactory
	 * 
	 * @param repository
	 *            repository information
	 * @return ArtifactRepositoryFactory instance
	 */
	private ArtifactRepositoryFactory getArtifactRepositoryFactory(
			Repository repository) throws Exception {
		WagonManager manager = (WagonManager) lookup(WagonManager.ROLE);

		Server server = settings.getServer(repository.getId());

		if (server != null) {
			manager.addAuthenticationInfo(repository.getId(), server
					.getUsername(), server.getPassword(), server
					.getPrivateKey(), server.getPassphrase());
		}

		Proxy proxy = settings.getActiveProxy();
		if (proxy != null) {
			manager.addProxy(proxy.getProtocol(), proxy.getHost(), proxy
					.getPort(), proxy.getUsername(), proxy.getPassword(), proxy
					.getNonProxyHosts());
		}

		Mirror mirror = settings.getMirrorOf(repository.getId());
		if (mirror == null) {
			mirror = settings.getMirrorOf("*");
		}
		if (mirror != null) {
			repository.setUrl(mirror.getUrl());
		}

		return (ArtifactRepositoryFactory) lookup(ArtifactRepositoryFactory.ROLE);
	}

	/**
	 * set policy of remote repository
	 * 
	 * @param policy
	 *            repository policy
	 * @return remote repository information
	 */
	private ArtifactRepositoryPolicy buildRemoteArtifactRepositoryPolicy(
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

	/**
	 * load settings file
	 * 
	 * @param settingsFile
	 *            maven's settings.xml file
	 * @return maven's settings
	 */
	private Settings loadSettings(File settingsFile) {
		Settings settings = null;
		try {
			if (settingsFile != null && settingsFile.exists()) {
				settings = readSettings(settingsFile);
			}
		} catch (Exception e) {
			// ignore - reading settings file
		}

		if (settings == null) {
			settings = new Settings();
			RuntimeInfo rtInfo = new RuntimeInfo(settings);
			settings.setRuntimeInfo(rtInfo);
		}

		return settings;
	}

	/**
	 * read settings file
	 * 
	 * @param settingsFile
	 *            maven's settings.xml file
	 * @return maven's settings
	 */
	private Settings readSettings(File settingsFile) throws IOException,
			XmlPullParserException {
		Settings settings = null;
		Reader reader = null;
		try {
			reader = ReaderFactory.newXmlReader(settingsFile);
			StringWriter sWriter = new StringWriter();

			IOUtil.copy(reader, sWriter);

			String rawInput = sWriter.toString();

			try {
				RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
				interpolator.addValueSource(new EnvarBasedValueSource());

				rawInput = interpolator.interpolate(rawInput, "settings");
			} catch (Exception e) {
				// ignore - initialize environment variable resolver
			}

			StringReader sReader = new StringReader(rawInput);

			SettingsXpp3Reader modelReader = new SettingsXpp3Reader();

			settings = modelReader.read(sReader);

			RuntimeInfo rtInfo = new RuntimeInfo(settings);

			rtInfo.setFile(settingsFile);

			settings.setRuntimeInfo(rtInfo);
		} finally {
			IOUtil.close(reader);
		}
		return settings;
	}

	/**
	 * release a ArtifactRepositoryFactory
	 * 
	 * @param repositoryFactory
	 *            artifactRepositoryFactory instance
	 */
	private void releaseArtifactRepositoryFactory(
			ArtifactRepositoryFactory repositoryFactory) {
		try {
			this.container.release(repositoryFactory);
		} catch (ComponentLifecycleException e) {
			// TODO: Warn the user, or not?
		}
	}
}
