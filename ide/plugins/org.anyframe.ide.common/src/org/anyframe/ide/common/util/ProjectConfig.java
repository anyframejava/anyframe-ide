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
package org.anyframe.ide.common.util;

import org.anyframe.ide.common.Constants;

/**
 * This is a ProjectConfig class. This class is a vo for config xml file.
 * 
 * @author Sujeong Lee
 */
public class ProjectConfig {

	// 1. project common info
	private String pjtName;
	private String packageName;
	private String pjtHome;

	// 2. ant build info
	private String anyframeHome;
	private String contextRoot;
	private String offline;

	// 3. path info
	private String templatePath;
	private String jdbcdriverPath;
	private String databasesPath;

	public String getAnyframeHome() {
		return anyframeHome;
	}

	public void setAnyframeHome(String anyframeHome) {
		this.anyframeHome = anyframeHome;
	}

	public String getPjtHome() {
		return pjtHome;
	}

	public void setPjtHome(String pjtHome) {
		this.pjtHome = pjtHome;
	}

	public String getPjtName() {
		return pjtName;
	}

	public void setPjtName(String pjtName) {
		this.pjtName = pjtName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getContextRoot() {
		return contextRoot;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}

	public String getOffline() {
		return offline;
	}

	public void setOffline(String offline) {
		this.offline = offline;
	}

	public String getTemplateHomePath() {
		return templatePath;
	}

	public void setTemplateHomePath(String templatePath) {
		this.templatePath = templatePath;
	}

	public String getJdbcdriverPath() {
		return jdbcdriverPath;
	}

	public void setJdbcdriverPath(String jdbcdriverPath) {
		this.jdbcdriverPath = jdbcdriverPath;
	}

	public String getDatabasesPath() {
		return databasesPath;
	}

	public void setDatabasesPath(String databasesPath) {
		this.databasesPath = databasesPath;
	}

	public String getTemplatePath(String productName) {
		return templatePath + Constants.FILE_SEPERATOR + productName;
	}
}
