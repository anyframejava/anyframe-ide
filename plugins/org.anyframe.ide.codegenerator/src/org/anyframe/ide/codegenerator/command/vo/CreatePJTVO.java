/*   
 * Copyright 2002-2013 the original author or authors.   
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
package org.anyframe.ide.codegenerator.command.vo;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * This is a CreatePJTVO class.
 * 
 * @author Sooyeon Park
 */
public class CreatePJTVO extends CommandVO {

	private String projectType = "";

	private String projectGroupId = "";

	private String projectVersion = "";

	private String databaseType = "";

	private String databaseName = "";

	private String databaseDriverPath = "";

	private String databaseSchema = "";

	private String databaseUserId = "";

	private String databasePassword = "";

	private String databaseServer = "";

	private String databasePort = "";

	private String databaseUrl = "";

	private String databaseDialect = "";

	private String databaseDriver = "";

	private String databaseGroupId = "";

	private String databaseArtifactId = "";

	private String databaseVersion = "";

	private String templateHome = "";

	private String inspectionHome = "";

	private boolean isOffline = false;

	private String pluginName = "";

	public boolean isOffline() {
		return isOffline;
	}

	public void setOffline(boolean isOffline) {
		this.isOffline = isOffline;
	}

	public String getDatabaseSchema() {
		return databaseSchema;
	}

	public void setDatabaseSchema(String databaseSchema) {
		this.databaseSchema = databaseSchema;
	}

	public String getDatabaseUserId() {
		return databaseUserId;
	}

	public void setDatabaseUserId(String databaseUserId) {
		this.databaseUserId = databaseUserId;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	public String getDatabaseServer() {
		return databaseServer;
	}

	public void setDatabaseServer(String databaseServer) {
		this.databaseServer = databaseServer;
	}

	public String getDatabasePort() {
		return databasePort;
	}

	public void setDatabasePort(String databasePort) {
		this.databasePort = databasePort;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}

	public String getDatabaseDialect() {
		return databaseDialect;
	}

	public void setDatabaseDialect(String databaseDialect) {
		this.databaseDialect = databaseDialect;
	}

	public String getDatabaseDriver() {
		return databaseDriver;
	}

	public void setDatabaseDriver(String databaseDriver) {
		this.databaseDriver = databaseDriver;
	}

	public String getDatabaseGroupId() {
		return databaseGroupId;
	}

	public void setDatabaseGroupId(String databaseGroupId) {
		this.databaseGroupId = databaseGroupId;
	}

	public String getDatabaseArtifactId() {
		return databaseArtifactId;
	}

	public void setDatabaseArtifactId(String databaseArtifactId) {
		this.databaseArtifactId = databaseArtifactId;
	}

	public String getDatabaseVersion() {
		return databaseVersion;
	}

	public void setDatabaseVersion(String databaseVersion) {
		this.databaseVersion = databaseVersion;
	}

	public String getInspectionHome() {
		return inspectionHome;
	}

	public void setInspectionHome(String inspectionHome) {
		this.inspectionHome = inspectionHome;
	}

	public String getTemplateHome() {
		return templateHome;
	}

	public void setTemplateHome(String templateHome) {
		this.templateHome = templateHome;
	}

	public String getDatabaseDriverPath() {
		return databaseDriverPath;
	}

	public void setDatabaseDriverPath(String databaseDriverPath) {
		this.databaseDriverPath = databaseDriverPath;
	}

	private IStructuredSelection selection = null;

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getProjectType() {
		return projectType;
	}

	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}

	public IStructuredSelection getSelection() {
		return selection;
	}

	public void setSelection(IStructuredSelection selection) {
		this.selection = selection;
	}

	public String getProjectGroupId() {
		return projectGroupId;
	}

	public void setProjectGroupId(String projectGroupId) {
		this.projectGroupId = projectGroupId;
	}

	public String getProjectVersion() {
		return projectVersion;
	}

	public void setProjectVersion(String projectVersion) {
		this.projectVersion = projectVersion;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

}
