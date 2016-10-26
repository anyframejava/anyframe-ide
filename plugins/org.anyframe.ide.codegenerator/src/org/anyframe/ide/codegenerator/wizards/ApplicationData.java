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
package org.anyframe.ide.codegenerator.wizards;

/**
 * This is an ApplicationData class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class ApplicationData {

	private String location = "";
	private String pjtName = "";
	private String commonProjectName = "";
	private String serviceProjectName = "";
	private String webProjectName = "";
	private boolean secondStage = false;
	private boolean useHibernate = false;
	private boolean dynamicReloading = false;
	private String databaseType = "";
	private String databaseName = "";
	private String schema = "";
	private String useName = "";
	private String password = "";
	private String server = "";
	private String port = "";
	private String url = "";
	private String dialect = "";
	private String driverClassName = "";
	private String driverJar = "";
	private String templateType = "";
	// added
	private String projectName = "";
	private String pjtLocation = "";
	private String type = "";
	// multi or single project structure
	private boolean isWebTypeProject = false;
	private String appPackage = "";
	private String anyframeHome = "";

	private boolean isAntProject = false;
	private String pjtGroupId = "";
	private String pjtArtifactId = "";
	private String pjtVersion = "";

	private String driverGroupId = "";
	private String driverArtifactId = "";
	private String driverVersion = "";

	private String pjtTemplateHome = "";
	private String inspectionHome = "";

	private boolean isOffine = false;

	public boolean isOffine() {
		return isOffine;
	}

	public void setOffine(boolean isOffine) {
		this.isOffine = isOffine;
	}

	public String getInspectionHome() {
		return inspectionHome;
	}

	public void setInspectionHome(String inspectionHome) {
		this.inspectionHome = inspectionHome;
	}

	public String getPjtTemplateHome() {
		return pjtTemplateHome;
	}

	public void setPjtTemplateHome(String pjtTemplateHome) {
		this.pjtTemplateHome = pjtTemplateHome;
	}

	public void setAntProject(boolean isAntProject) {
		this.isAntProject = isAntProject;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPjtLocation() {
		return pjtLocation;
	}

	public void setPjtLocation(String pjtLocation) {
		this.pjtLocation = pjtLocation;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setUseHibernate(boolean useHibernate) {
		this.useHibernate = useHibernate;
	}

	public void setDynamicReloading(boolean dynamicReloading) {
		this.dynamicReloading = dynamicReloading;
	}

	public ApplicationData() {
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setPjtName(String pjtName) {
		this.pjtName = pjtName;
	}

	public void setCommonProjectName(String commonProjectName) {
		this.commonProjectName = commonProjectName;
	}

	public void setNextFirstStage(boolean nextFirstStage) {
		this.secondStage = nextFirstStage;
	}

	public void setServiceProjectName(String serviceProjectName) {
		this.serviceProjectName = serviceProjectName;
	}

	public void setWebProjectName(String webProjectName) {
		this.webProjectName = webProjectName;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public void setUseName(String useName) {
		this.useName = useName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public void setDriverJar(String driverJar) {
		this.driverJar = driverJar;
	}

	public void setSecondStage(boolean secondStage) {
		this.secondStage = secondStage;
	}

	public String getLocation() {
		return location;
	}

	public String getPjtName() {
		return pjtName;
	}

	public String getCommonProjectName() {
		return commonProjectName;
	}

	public String getServiceProjectName() {
		return serviceProjectName;
	}

	public String getWebProjectName() {
		return webProjectName;
	}

	public boolean isSecondStage() {
		return secondStage;
	}

	public boolean isUseHibernate() {
		return useHibernate;
	}

	public boolean isDynamicReloading() {
		return dynamicReloading;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getSchema() {
		return schema;
	}

	public String getUseName() {
		return useName;
	}

	public String getPassword() {
		return password;
	}

	public String getServer() {
		return server;
	}

	public String getPort() {
		return port;
	}

	public String getUrl() {
		return url;
	}
	
	public String getDialect() {
		return dialect;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public String getDriverJar() {
		return driverJar;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public boolean isWebTypeProject() {
		return isWebTypeProject;
	}

	public void setWebTypeProject(boolean isWebTypeProject) {
		this.isWebTypeProject = isWebTypeProject;
	}

	public void setAppPackage(String appPackage) {
		this.appPackage = appPackage;
	}

	public void setAnyframeHome(String anyframeHome) {
		this.anyframeHome = anyframeHome;
	}

	public String getAppPackage() {
		return appPackage;
	}

	public String getAnyframeHome() {
		return anyframeHome;
	}

	public boolean isAntProject() {
		return isAntProject;
	}

	public String getPjtGroupId() {
		return pjtGroupId;
	}

	public void setPjtGroupId(String pjtGroupId) {
		this.pjtGroupId = pjtGroupId;
	}

	public String getPjtArtifactId() {
		return pjtArtifactId;
	}

	public void setPjtArtifactId(String pjtArtifactId) {
		this.pjtArtifactId = pjtArtifactId;
	}

	public String getPjtVersion() {
		return pjtVersion;
	}

	public void setPjtVersion(String pjtVersion) {
		this.pjtVersion = pjtVersion;
	}

	public String getDriverGroupId() {
		return driverGroupId;
	}

	public void setDriverGroupId(String driverGroupId) {
		this.driverGroupId = driverGroupId;
	}

	public String getDriverArtifactId() {
		return driverArtifactId;
	}

	public void setDriverArtifactId(String driverArtifactId) {
		this.driverArtifactId = driverArtifactId;
	}

	public String getDriverVersion() {
		return driverVersion;
	}

	public void setDriverVersion(String driverVersion) {
		this.driverVersion = driverVersion;
	}
}
