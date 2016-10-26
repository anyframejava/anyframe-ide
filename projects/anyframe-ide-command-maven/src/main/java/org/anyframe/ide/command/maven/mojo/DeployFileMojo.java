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
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.anyframe.ide.command.common.CommandException;
import org.anyframe.ide.command.common.DefaultPluginPomManager;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.AttachedArtifact;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;

/**
 * This is an DeployFileMojo class. This mojo is for deploy service binary file
 * and sources simply.
 * 
 * @goal deploy-file
 * @execute phase="package"
 * @author Soyon Lim
 */
public class DeployFileMojo extends AbstractPluginMojo {
	/** @component role="org.anyframe.ide.command.common.DefaultPluginPomManager" */
	DefaultPluginPomManager pluginPomManager;

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
	 * Component used to deploy an artifact.
	 * 
	 * @component
	 */
	private ArtifactDeployer deployer;

	/**
	 * Component used to create an artifact.
	 * 
	 * @component
	 */
	protected ArtifactFactory artifactFactory;

	/**
	 * Component used to create a repository.
	 * 
	 * @component
	 */
	ArtifactRepositoryFactory repositoryFactory;

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Flag whether Maven is currently in online/offline mode.
	 * 
	 * @parameter default-value="${settings.offline}"
	 * @readonly
	 */
	private boolean offline;

	/**
	 * Map that contains the layouts.
	 * 
	 * @component role=
	 *            "org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
	 */
	private Map<String, ArtifactRepositoryLayout> repositoryLayouts;

	/**
	 * The type of remote repository layout to deploy to. Try <i>legacy</i> for
	 * a Maven 1.x-style repository layout.
	 * 
	 * @parameter expression="${repositoryLayout}" default-value="default"
	 */
	private String repositoryLayout;

	/**
	 * name of target file to be deployed
	 * 
	 * @parameter expression="${targetFileName}"
	 */
	private String targetFileName;

	/**
	 * Flag whether target project is assembly-project.
	 * 
	 * @parameter expression="${assembled}" default-value="false"
	 */
	private boolean isAssemblyProject;

	/**
	 * main method for executing DeployFileMojo. This mojo is executed when you
	 * input 'mvn anyframe:deploy-file [-options]'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			// 1. initialize
			ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
			setRepository(request);

			// 2. check maven mode
			if (offline) {
				throw new MojoFailureException(
						"Fail to deploy artifacts when Maven is in offline mode");
			}

			// 3. check an artifact to deploy
			if (targetFileName == null || "".equals(targetFileName)) {
				targetFileName = project.getArtifactId() + "-"
						+ project.getVersion() + "."
						+ (isAssemblyProject ? ".jar" : project.getPackaging());
			}

			File file = new File(baseDir + CommonConstants.fileSeparator
					+ "target", targetFileName);
			if (!file.exists()) {
				if (isAssemblyProject) {
					return;
				}
				throw new CommandException("You need target/" + targetFileName
						+ " file to deploy artifacts into remote repository.");
			}

			// 4. find a deployment repository
			ArtifactRepositoryLayout layout = (ArtifactRepositoryLayout) repositoryLayouts
					.get(repositoryLayout);
			url = "dav:" + url;
			ArtifactRepository deploymentRepository = repositoryFactory
					.createDeploymentArtifactRepository(repositoryId, url,
							layout, true);

			// 5. set metadata with a generated pom information
			Artifact artifact = null;
			if (!isAssemblyProject) {
				artifact = project.getArtifact();
			} else {
				artifact = artifactFactory.createArtifactWithClassifier(
						project.getGroupId(), project.getArtifactId(),
						project.getVersion(), "jar", null);
			}

			ArtifactMetadata metadata = new ProjectArtifactMetadata(artifact,
					generateNewPomFile());
			artifact.addMetadata(metadata);

			// 6. try to deploy an artifact
			deployer.deploy(file, artifact, deploymentRepository,
					request.getLocalRepository());

			// 7. check an attached artifact (sources) to deploy
			String attachedFileName = project.getArtifactId() + "-"
					+ project.getVersion() + "-sources.jar";

			File attachedFile = new File(baseDir
					+ CommonConstants.fileSeparator + "target",
					attachedFileName);

			// 8. try to deploy an attached artifact
			if (attachedFile.exists()) {
				AttachedArtifact attachedArtifact = new AttachedArtifact(
						artifact, "java-source", "sources",
						artifact.getArtifactHandler());

				deployer.deploy(attachedFile, attachedArtifact,
						deploymentRepository, request.getLocalRepository());
			} else {
				getLog().info(
						"Deploying sources is skipped. The reason is a '"
								+ attachedFile + "' doesn't exist.");
			}
		} catch (Exception ex) {
			getLog().error(
					"Fail to execute DeployFileMojo. The reason is '"
							+ ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}
	}

	/**
	 * Generates a new pom.xml from the generated model.
	 * 
	 * @return The generated pom.xml file
	 */
	private File generateNewPomFile() throws MojoExecutionException {
		Model model = generateModel();

		Writer writer = null;
		try {
			File tempFile = File.createTempFile("mvndeploy", ".pom");
			tempFile.deleteOnExit();

			writer = WriterFactory.newXmlWriter(tempFile);
			new MavenXpp3Writer().write(writer, model);

			return tempFile;
		} catch (IOException e) {
			throw new MojoExecutionException(
					"Error writing temporary pom file: " + e.getMessage(), e);
		} finally {
			IOUtil.close(writer);
		}
	}

	/**
	 * Generates a new model from the user-supplied artifact information.
	 * 
	 * @return The generated model, never <code>null</code>.
	 */
	private Model generateModel() {
		Model model = new Model();

		model.setModelVersion("4.0.0");
		model.setGroupId(project.getGroupId());
		model.setArtifactId(project.getArtifactId());
		model.setVersion(project.getVersion());
		model.setPackaging(project.getPackaging());
		model.setDescription(project.getDescription());

		return model;
	}
}