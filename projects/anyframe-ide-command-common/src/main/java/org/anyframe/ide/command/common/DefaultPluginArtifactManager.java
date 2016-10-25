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
package org.anyframe.ide.command.common;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.common.DefaultArchetypeArtifactManager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DebugResolutionListener;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.apache.maven.artifact.resolver.WarningResolutionListener;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

/**
 * This is an DefaultPluginArtifactManager class. This class is overrided for
 * recognizing plugin descriptor. Originally, DefaultArchetypeArtifactManager
 * find a archetype descriptor under the META-INF/maven folder. But
 * DefaultPluginArtifactManager find a plugin descriptor under the
 * plugin-resources folder.
 * 
 * @plexus.component 
 *                   role="org.anyframe.ide.command.common.DefaultPluginArtifactManager"
 * @author SoYon Lim
 */
public class DefaultPluginArtifactManager extends
		DefaultArchetypeArtifactManager {

	/** @plexus.requirement */
	ArchetypeArtifactManager archetypeArtifactManager;

	/** @plexus.requirement */
	ArchetypeRegistryManager archetypeRegistryManager;

	/** @plexus.requirement */
	ArtifactFactory artifactFactory;

	/** @plexus.requirement */
	ArtifactResolver resolver;

	/** @plexus.requirement */
	MavenProjectBuilder projectBuilder;

	/**
	 * @plexus.requirement 
	 *                     role="org.anyframe.ide.command.common.DefaultPluginInfoManager"
	 */
	PluginInfoManager pluginInfoManager;

	/**
	 * @plexus.requirement 
	 *                     role="org.apache.maven.artifact.resolver.ArtifactCollector"
	 */
	protected ArtifactCollector artifactCollector;

	/**
	 * @plexus.requirement 
	 *                     role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
	 *                     hint="maven"
	 */
	protected ArtifactMetadataSource artifactMetadataSource;

	/**
	 * download dependency libraries from local/remote repository
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param dependencies
	 *            dependency libraries
	 * @return dowdloaded libraries
	 */
	public Set<Artifact> downloadArtifact(ArchetypeGenerationRequest request,
			List<Dependency> dependencies) throws Exception {
		Set<Artifact> dependencyArtifacts = new HashSet<Artifact>();

		for (Dependency dependency : dependencies) {
			VersionRange versionRange = VersionRange
					.createFromVersionSpec(dependency.getVersion());

			Artifact artifact = artifactFactory.createDependencyArtifact(
					dependency.getGroupId(), dependency.getArtifactId(),
					versionRange, dependency.getType(), dependency
							.getClassifier(), dependency.getScope(), dependency
							.isOptional());

			try {
				resolver.resolve(artifact, request
						.getRemoteArtifactRepositories(), request
						.getLocalRepository());
				dependencyArtifacts.add(artifact);
			} catch (Exception e) {
				getLogger().warn(
						"A dependendent library doesn't exist in repository ("
								+ dependency.getGroupId() + ":"
								+ dependency.getArtifactId() + ":"
								+ dependency.getVersion() + ":"
								+ dependency.getClassifier() + ")", e);
			}
		}

		return dependencyArtifacts;
	}

	/**
	 * make a classloader for a specified artifact
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param groupId
	 *            a groupId of a specified artifact
	 * @param artifactId
	 *            a artifactId of a specified artifact
	 * @param version
	 *            a version of a specified artifact
	 * @param originalUrls
	 *            urls which a classloader of plugin library has
	 * @return a classloader
	 */
	@SuppressWarnings("unchecked")
	public URLClassLoader makeArtifactClassLoader(
			ArchetypeGenerationRequest request, String groupId,
			String artifactId, String version, URL[] originalUrls)
			throws Exception {
		Artifact pluginArtifact = artifactFactory.createProjectArtifact(
				groupId, artifactId, version, Artifact.SCOPE_RUNTIME);

		MavenProject pluginProject = projectBuilder.buildFromRepository(
				pluginArtifact, request.getRemoteArtifactRepositories(),
				request.getLocalRepository());

		List listeners = new ArrayList();
		if (getLogger().isDebugEnabled()) {
			listeners.add(new DebugResolutionListener(getLogger()));
		}
		listeners.add(new WarningResolutionListener(getLogger()));

		Map managedVersions = createManagedVersionMap(artifactFactory,
				pluginProject.getId(), pluginProject.getDependencyManagement());

		ArtifactResolutionResult pluginDependencies = artifactCollector
				.collect(downloadArtifact(request, pluginProject
						.getDependencies()), pluginProject.getArtifact(),
						managedVersions, request.getLocalRepository(), request
								.getRemoteArtifactRepositories(),
						artifactMetadataSource, null, listeners);

		URL[] newUrls = new URL[pluginDependencies.getArtifactResolutionNodes()
				.size()
				+ originalUrls.length];

		System.arraycopy(originalUrls, 0, newUrls, 0, originalUrls.length);

		int i = originalUrls.length;

		Iterator<ResolutionNode> dependencyItr = pluginDependencies
				.getArtifactResolutionNodes().iterator();

		while (dependencyItr.hasNext()) {
			ResolutionNode node = dependencyItr.next();
			Artifact dependencyArtifact = node.getArtifact();

			resolver.resolve(dependencyArtifact, request
					.getRemoteArtifactRepositories(), request
					.getLocalRepository());

			newUrls[i++] = dependencyArtifact.getFile().toURI().toURL();
		}

		return new URLClassLoader(newUrls);
	}

	/**
	 * 
	 * @param artifactFactory
	 * @param projectId
	 * @param dependencyManagement
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private Map createManagedVersionMap(ArtifactFactory artifactFactory,
			String projectId, DependencyManagement dependencyManagement)
			throws Exception {
		Map map;
		if (dependencyManagement != null
				&& dependencyManagement.getDependencies() != null) {
			map = new HashMap();
			for (Iterator i = dependencyManagement.getDependencies().iterator(); i
					.hasNext();) {
				Dependency d = (Dependency) i.next();

				try {
					VersionRange versionRange = VersionRange
							.createFromVersionSpec(d.getVersion());
					Artifact artifact = artifactFactory
							.createDependencyArtifact(d.getGroupId(), d
									.getArtifactId(), versionRange,
									d.getType(), d.getClassifier(), d
											.getScope(), d.isOptional());

					// handleExclusions(artifact, d);
					map.put(d.getManagementKey(), artifact);
				} catch (InvalidVersionSpecificationException e) {
					throw e;
				}
			}
		} else {
			map = Collections.EMPTY_MAP;
		}
		return map;
	}

	public void setArchetypeArtifactManager(
			ArchetypeArtifactManager archetypeArtifactManager) {
		this.archetypeArtifactManager = archetypeArtifactManager;
	}
}
