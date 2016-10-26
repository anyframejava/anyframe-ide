/*
 * Copyright 2008-2013 the original author or authors.
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
package org.anyframe.ide.command.common.util;


/**
 * This is JdbcOption class.
 * 
 * @author Sujeong Lee
 */
public class JdbcOption {

	private String dbType;

	private String dbName;
	private String driverJar;
	private String driverClassName;
	private String url;
	private String userName;
	private String password;
	private String schema;

	private boolean useDbSpecific = false;
	private boolean runExplainPaln = false;

	private boolean isDefault;

	private String dialect;
	private String mvnGroupId;
	private String mvnArtifactId;
	private String mvnVersion;

	private String projectName;

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDriverJar() {
		return driverJar;
	}

	public void setDriverJar(String driverJar) {
		this.driverJar = driverJar;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		if(schema == null || "".equals(schema)){
			this.schema = "";
		}else{
			this.schema = schema;
		}
	}

	public boolean isUseDbSpecific() {
		return useDbSpecific;
	}

	public void setUseDbSpecific(boolean useDbSpecific) {
		this.useDbSpecific = useDbSpecific;
	}

	public boolean isRunExplainPaln() {
		return runExplainPaln;
	}

	public void setRunExplainPaln(boolean runExplainPaln) {
		this.runExplainPaln = runExplainPaln;
	}

	public boolean getDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public String getMvnGroupId() {
		return mvnGroupId;
	}

	public void setMvnGroupId(String mvnGroupId) {
		this.mvnGroupId = mvnGroupId;
	}

	public String getMvnArtifactId() {
		return mvnArtifactId;
	}

	public void setMvnArtifactId(String mvnArtifactId) {
		this.mvnArtifactId = mvnArtifactId;
	}

	public String getMvnVersion() {
		return mvnVersion;
	}

	public void setMvnVersion(String mvnVersion) {
		this.mvnVersion = mvnVersion;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getKey() {
		return projectName + "." + dbName;
	}

	@Override
	public String toString() {
		return "JdbcOption [dbName=" + dbName + ", dbType=" + dbType
				+ ", dialect=" + dialect + ", driverClassName="
				+ driverClassName + ", driverJar=" + driverJar + ", isDefault="
				+ isDefault + ", mvnArtifactId=" + mvnArtifactId
				+ ", mvnGroupId=" + mvnGroupId + ", mvnVersion=" + mvnVersion
				+ ", password=" + password + ", runExplainPaln="
				+ runExplainPaln + ", schema=" + schema + ", url=" + url
				+ ", useDbSpecific=" + useDbSpecific + ", userName=" + userName
				+ "]";
	}

}