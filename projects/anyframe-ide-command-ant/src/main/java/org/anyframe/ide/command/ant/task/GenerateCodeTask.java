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
import org.anyframe.ide.command.maven.mojo.GenerateCodeMojo;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.codehaus.plexus.components.interactivity.Prompter;

/**
 * This is a GenerateCodeTask class. This task is for generating source codes
 * based on templates.
 * 
 * @author Sooyeon Park
 */
public class GenerateCodeTask extends AbstractPluginTask {

	/** prompter component */
	private Prompter prompter;
	/** PluginInfoManager */
	private PluginInfoManager pluginInfoManager;
	/** entity class name */
	private String entity = "";
	/** base directory */
	private String baseDir = ".";
	/** project home directory */
	private String projecthome = "";
	/** base package name */
	private String packageName = "";
	/** generate scope(all, service, web) */
	private String scope = "all";
	/** check command line interface mode */
	private String isCLIMode = "false";

	/**
	 * main method for executing GenerateCodeTask this task is executed when you
	 * input 'anyframe create-crud'
	 */
	public void doExecute() throws BuildException {
		try {
			GenerateCodeMojo codeGenerator = new GenerateCodeMojo();

			codeGenerator.setPrompter(this.prompter);
			codeGenerator.setPluginInfoManager(this.pluginInfoManager);
			
			if (isEmpty("projecthome", this.projecthome))
				this.projecthome = new File(this.baseDir).getAbsolutePath();
			else
				this.baseDir = this.projecthome;

			codeGenerator.setBaseDir(new File(this.baseDir));
			codeGenerator.setEntity(this.entity);

			codeGenerator.setProjectHome(this.projecthome);

			if (isEmpty("package", this.packageName)) {
				this.packageName = null;
			}
			codeGenerator.setPackageName(this.packageName);

			if (isEmpty("scope", this.scope)) {
				this.scope = "all";
			}
			codeGenerator.setScope(this.scope);

			codeGenerator.setMavenProject(new MavenProject());

			if (isEmpty("isCLIMode", this.isCLIMode)) {
				this.isCLIMode = "true";
			}
			codeGenerator
					.setCLIMode(new Boolean(this.isCLIMode).booleanValue());

			codeGenerator.execute();
		} catch (Exception e) {
			log("Fail to execute GenerateCodeTask", e, Project.MSG_ERR);
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
	 * get entity class
	 * 
	 * @return entity class
	 */
	public String getEntity() {
		return entity;
	}

	/**
	 * set entity class
	 * 
	 * @param entity
	 *            entity class
	 */
	public void setEntity(String entity) {
		this.entity = entity;
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
	 * @param packageName
	 *            base package name
	 */
	public void setPackage(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * get code generation scope
	 * 
	 * @return code generation scope
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * set code generation scope
	 * 
	 * @param scope
	 *            code generation scope
	 */
	public void setScope(String scope) {
		this.scope = scope;
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
