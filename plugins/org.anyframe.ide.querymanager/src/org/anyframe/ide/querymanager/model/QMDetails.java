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
package org.anyframe.ide.querymanager.model;

import org.eclipse.core.resources.IFile;

/**
 * This is QMDetails class.
 * 
 * @author Junghwan Hong
 */
public class QMDetails {

	private String querryID;

	private String query;

	private String projectName;

	private String pluginXMLFileName;

	private String filePath;

	private String used;

	private IFile xmlFile;

	// Author of query

	private String author;

	// Date created

	private String dateCreated;

	// Date Modified

	private String dateModified;

	// Start Location in the file "file"
	int charStart;

	// End Location in the file "file"
	int charEnd;

	private String category;

	private String totalQuery;

	// static Vector QueryList = new Vector();

	public QMDetails() {

	}

	public QMDetails(String QuerryID, String Query, String PluginXMLFileName,
			IFile xmlFile, String ProjectName, String filePath, String Used,
			String author, String dateCreated, String dateModified,
			int charStart, int charEnd, String category, String totalQuery) {
		this.filePath = filePath;
		this.querryID = QuerryID;
		this.pluginXMLFileName = PluginXMLFileName;
		this.xmlFile = xmlFile;
		this.projectName = ProjectName;
		this.used = Used;
		this.query = Query;
		this.author = author;
		this.dateCreated = dateCreated;
		this.dateModified = dateModified;
		this.charStart = charStart;
		this.charEnd = charEnd;
		this.category = category;
		this.totalQuery = totalQuery;
	}

	// public static Vector getQueryList() {
	//
	// return QueryList;
	//
	// }

	public int getCharStart() {
		return charStart;
	}

	public int getCharEnd() {
		return charEnd;
	}

	public String getQuerryID() {
		return querryID;
	}

	public void setEmpFirstName(String QuerryID) {
		this.querryID = QuerryID;
	}

	public String getPluginXmlFile() {
		return pluginXMLFileName;
	}

	public void setPluginXMLFileName(String PluginXMLFileName) {
		this.pluginXMLFileName = PluginXMLFileName;
	}

	public IFile getXmlFile() {
		return xmlFile;
	}

	public void setXmlFile(IFile xmlFile) {
		this.xmlFile = xmlFile;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getUsed() {
		return used;
	}

	public void setUsed(String used) {
		this.used = used;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String querry) {
		this.query = querry;
	}

	public String getAuthor() {
		return author;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public String getDateModified() {
		return dateModified;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the totalQuery
	 */
	public String getTotalQuery() {
		return totalQuery;
	}

	/**
	 * @param totalQuery
	 *            the totalQuery to set
	 */
	public void setTotalQuery(String totalQuery) {
		this.totalQuery = totalQuery;
	}

}
