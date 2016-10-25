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
package org.anyframe.ide.command.ant.task;

import java.io.File;

import org.anyframe.ide.command.common.DefaultPluginInfoManager;
import org.anyframe.ide.command.common.PluginInfoManager;
import org.anyframe.ide.command.maven.mojo.GenerateModelMojo;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.codehaus.plexus.components.interactivity.Prompter;

/**
 * This is a GenerateModelTask class. This task is for generating model classes
 * based on db tables and templates .
 * 
 * @author Sooyeon Park
 */
public class GenerateModelTask extends AbstractPluginTask {

	/** prompter component */
	private Prompter prompter;
	/** PluginInfoManager */
	private PluginInfoManager pluginInfoManager;
	/** db table name */
	private String table = "";
	/** base package name */
	private String packageName = "";
	/** project home directory */
	private String projecthome = "";
	/** base directory */
	private String baseDir = ".";
	/** check command line interface mode */
	private String isCLIMode = "false";

	/**
	 * main method for executing GenerateModelTask this task is executed when
	 * you input 'anyframe create-model'
	 */
	public void doExecute() {
		try {
			GenerateModelMojo modelGen = new GenerateModelMojo();

			modelGen.setPrompter(this.prompter);
			modelGen.setPluginInfoManager(this.pluginInfoManager);

			if (isEmpty("projecthome", this.projecthome))
				this.projecthome = new File(this.baseDir).getAbsolutePath();
			else
				this.baseDir = this.projecthome;

			modelGen.setProjectHome(this.projecthome);

			modelGen.setBaseDir(new File(this.baseDir));

			if (isEmpty("table", this.table))
				this.table = "*";
			modelGen.setTable(this.table);

			if (isEmpty("package", this.packageName)) {
				this.packageName = null;
			}
			modelGen.setPackageName(packageName);

			modelGen.setMavenProject(new MavenProject());

			if (isEmpty("isCLIMode", this.isCLIMode)) {
				this.isCLIMode = "true";
			}
			modelGen.setCLIMode(new Boolean(this.isCLIMode).booleanValue());

			modelGen.execute();
		} catch (Exception e) {
			log("[error] Fail to execute GenerateModelTask", e, Project.MSG_ERR);
			throw new BuildException(e.getMessage());
		}
	}

	/**
	 * look up essential components.
	 */
	public void lookupComponents() {
		this.prompter = (Prompter) getPluginContainer().lookup(
				Prompter.class.getName());
		
		this.pluginInfoManager = (PluginInfoManager) getPluginContainer().lookup(
				DefaultPluginInfoManager.class.getName());
		
	}

	/**
	 * get db table name
	 * 
	 * @return db table name
	 */
	public String getTable() {
		return table;
	}

	/**
	 * set db table name
	 * 
	 * @param table
	 *            db table name
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * get project home directory
	 * 
	 * @return project home directory
	 */
	public String getProjecthome() {
		return projecthome;
	}

	/**
	 * set project home directory
	 * 
	 * @param projecthome
	 *            project home directory
	 */
	public void setProjecthome(String projecthome) {
		this.projecthome = projecthome;
	}

	/**
	 * get base package name
	 * 
	 * @return base package name
	 */
	public String getPackage() {
		return packageName;
	}

	/**
	 * set base package name
	 * 
	 * @param base
	 *            package name
	 */
	public void setPackage(String pacakgeName) {
		this.packageName = pacakgeName;
	}

	/**
	 * set base directory
	 * 
	 * @param baseDir
	 *            base directory
	 */
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	/**
	 * set command line interface mode checking
	 * 
	 * @param isCLIMode
	 *            command line interface mode checking
	 */
	public void setIsCLIMode(String isCLIMode) {
		this.isCLIMode = isCLIMode;
	}
}
