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
package org.anyframe.ide.command.ant.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.anyframe.ide.command.ant.task.container.PluginContainer;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.artifact.ant.AntResolutionListener;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.codehaus.plexus.util.IOUtil;

/**
 * Base class for plugin tasks.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author modified by Soyon Lim
 * @version $Id: AbstractArtifactTask.java 773075 2009-05-08 20:16:17Z pgier $
 */
public abstract class AbstractPluginTask extends Task {

	private String anyframeHome = "";
	private String target = "";
	private PluginInfo pluginInfo;
	private String name = "";
	private String packageName = "";
	private String encoding = "";

	// for testcase (in place of local repository in settings.xml)
	private String repo = "";

	// for setup repository
	List<AntResolutionListener> listeners;
	PluginContainer pluginContainer;

	public abstract void doExecute() throws BuildException;

	public abstract void lookupComponents();

	/**
	 * main method for executing custom task
	 */
	public void execute() {
		ClassLoader originalClassLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			initialize();
			lookupComponents();
			doExecute();
		} catch (Exception e) {
			throw new BuildException(e.getMessage());
		} finally {
			// plexus container changes context classloader of current thread,
			// so recover
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	/**
	 * create PluginContainer and List<AntResolutionListener> listeners
	 * 
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		File metadataFile = new File(new File(this.target)
				+ CommonConstants.METAINF, CommonConstants.METADATA_FILE);

		if (metadataFile.exists()) {
			PropertiesIO pio = new PropertiesIO(metadataFile.getAbsolutePath());
			String isOffline = pio.readValue(CommonConstants.OFFLINE);

			boolean offline = (isOffline != null) ? new Boolean(isOffline
					.substring(0, 1).toUpperCase()
					+ isOffline.substring(1).toLowerCase()).booleanValue()
					: false;
			if (offline) {
				log("Ant is executing in offline mode. Any libraries not already in your local repository will be inaccessible.");
			}
			this.initialize(offline);
		}
	}

	/**
	 * initialize a plugin container
	 * 
	 * @param isOffline
	 *            whether offline mode is
	 */
	public void initialize(boolean isOffline) throws Exception {
		if (this.pluginContainer == null) {
			this.pluginContainer = new PluginContainer(this.anyframeHome,
					getTarget(), isOffline);
		}

		listeners = Collections.singletonList(new AntResolutionListener(
				getProject(), false));
	}

	/*********************************************************************/
	/************ manage PlexusContainer, lookup PlexusContainer **********/
	/*********************************************************************/

	/**
	 * get ant project information
	 */
	public Project getProject() {
		Project project = super.getProject();
		if (project == null) {
			project = new Project();
			project.init();
			project.getBaseDir();
		}

		return project;
	}

	/*********************************************************************/
	/************ common methods ******************************************/
	/*********************************************************************/

	/**
	 * copy pom.xml file to temporary folder
	 * 
	 * @param pluginZip
	 *            zip file includes plugin binary file
	 */
	public void copyPomFile(ZipFile pluginZip, String fileName)
			throws Exception {
		InputStream inputStream = null;
		try {
			ZipEntry input = pluginZip.getEntry(fileName);
			inputStream = pluginZip.getInputStream(input);

			File temporaryDir = new File(getTarget()
					+ CommonConstants.fileSeparator + "temp");
			temporaryDir.mkdirs();
			IOUtil.copy(inputStream, new FileOutputStream(new File(
					temporaryDir, "pom_" + this.name + ".xml")));
		} finally {
			IOUtil.close(inputStream);
		}
	}

	/**
	 * check value of a specified property
	 * 
	 * @param property
	 *            task property
	 * @param value
	 *            value of task property
	 * @return true if value is empty or equals to ${property}
	 */
	protected boolean isEmpty(String property, String value) {
		if (value == null || value.equals("")
				|| value.equals("${" + property + "}"))
			return true;
		return false;
	}

	/*********************************************************************/
	/************ getter, setter *****************************************/
	/*********************************************************************/

	public PluginContainer getPluginContainer() {
		return this.pluginContainer;
	}

	public ArchetypeGenerationRequest getRequest() {
		return getPluginContainer().getRequest();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTarget() {
		return target;
	}

	public String getEncoding() {
		return ((null == this.encoding) || "".equals(this.encoding)) ? "UTF-8"
				: this.encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getPackage() {
		return this.packageName;
	}

	public void setPackage(String packageName) {
		this.packageName = packageName;
	}

	public PluginInfo getPluginInfo() {
		return pluginInfo;
	}

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	// for test case (set pluginCatalogFile location)
	public void setPluginCatalogLog(String catalogFile) {
		if (this.pluginContainer == null) {
			this.pluginContainer = new PluginContainer(this.anyframeHome,
					getTarget(), false);
		}
	}

	public void setAnyframeHome(String anyframeHome) {
		this.anyframeHome = anyframeHome;
	}

	public String getAnyframeHome() {
		return anyframeHome;
	}
}
