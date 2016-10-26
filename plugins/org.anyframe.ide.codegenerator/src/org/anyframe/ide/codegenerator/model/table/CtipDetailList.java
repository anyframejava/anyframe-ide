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
package org.anyframe.ide.codegenerator.model.table;

/**
 * This is a CtipDetailList class.
 * 
 * @author junghwan.hong
 * @author Sooyeon Park
 */
public class CtipDetailList {

	private String jobName;

	private final String status;

	private String buildType;

	private String workSpace;

	private String scmServerType;

	private String scmServerUrl;

	private String schedule;

	private String otherProject;

	/**
	 * CtipInfoList Default Constructor
	 * 
	 * @param project
	 */
	public CtipDetailList(String jobName, String status) {
		this.jobName = jobName;
		this.status = status;
	}

	/**
	 * CtipInfoList Constructor
	 * 
	 * @param project
	 */
	public CtipDetailList(String jobName, String status, String buildType,
			String workSpace, String scmServerType, String scmServerUrl,
			String schedule, String otherProject) {
		this.jobName = jobName;
		this.status = status;
		this.buildType = buildType;
		this.workSpace = workSpace;
		this.scmServerType = scmServerType;
		this.scmServerUrl = scmServerUrl;
		this.schedule = schedule;
		this.otherProject = otherProject;
	}

	/**
	 * @return
	 */
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * @return
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return
	 */
	public String getBuildType() {
		return buildType;
	}

	/**
	 * @param buildType
	 */
	public void setBuildType(String buildType) {
		this.buildType = buildType;
	}

	/**
	 * @return
	 */
	public String getWorkSpace() {
		return workSpace;
	}

	/**
	 * @param workSpace
	 */
	public void setWorkSpace(String workSpace) {
		this.workSpace = workSpace;
	}

	/**
	 * @return
	 */
	public String getScmServerType() {
		return scmServerType;
	}

	/**
	 * @param scmServerType
	 */
	public void setScmServerType(String scmServerType) {
		this.scmServerType = scmServerType;
	}

	/**
	 * @return
	 */
	public String getScmServerUrl() {
		return scmServerUrl;
	}

	/**
	 * @param scmServerUrl
	 */
	public void setScmServerUrl(String scmServerUrl) {
		this.scmServerUrl = scmServerUrl;
	}

	/**
	 * @return
	 */
	public String getSchedule() {
		return schedule;
	}

	/**
	 * @param schedule
	 */
	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	/**
	 * @return
	 */
	public String getOtherProject() {
		return otherProject;
	}

	/**
	 * @param otherProject
	 */
	public void setOtherProject(String otherProject) {
		this.otherProject = otherProject;
	}
}
