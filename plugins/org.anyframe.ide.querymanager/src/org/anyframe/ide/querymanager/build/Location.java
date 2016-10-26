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
package org.anyframe.ide.querymanager.build;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * This is a Value Object class which helps to hold the information of all the
 * Query Ids exist in a project. This vo object is used to mark the error
 * markers at locations of the query ids that exist as a duplciates in the
 * project.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class Location {
	// File in which the query id exists.
	public IFile file;
	public IPath filePath;

	// QueryService Query Id
	public String key;

	// Service Type
	public String serviceType;

	// Query Statement
	public String query;

	// Used or UnUsed in DAOs.
	public String used = "Used";

	// Start Location in the file "file"
	public int charStart;

	// End Location in the file "file"
	public int charEnd;

	// Author of query

	public String author;

	// Date created

	public String dateCreated;

	// Date Modified

	public String dateModified;

	/**
	 * @return integer
	 */
	public int getCharEnd() {
		return charEnd;
	}

	private String totalQuery;
	private String category;
	private boolean duplicate;

	public boolean isDuplicate() {
		return duplicate;
	}

	public void setDuplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}

	/**
	 * @param charEnd
	 *            integer
	 */
	public void setCharEnd(int charEnd) {
		this.charEnd = charEnd;
	}

	/**
	 * @return integer
	 */
	public int getCharStart() {
		return charStart;
	}

	/**
	 * @param charStart
	 *            integer
	 */
	public void setCharStart(int charStart) {
		this.charStart = charStart;
	}

	/**
	 * @return IFile object
	 */
	public IFile getFile() {
		return file;
	}

	/**
	 * @param file
	 *            IFile object
	 */
	public void setFile(IFile file) {
		this.file = file;
	}

	/**
	 * @return string represenation of key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            string represenation of key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @see java.lang.Object#toString() customised version of toString method.
	 * @return Stirng representation of this object.
	 */
	public String toString() {
		return " Location File Name = " + this.file.getName() + " charStart = "
				+ this.charStart + " charEnd == " + this.charEnd;
	}

	public IPath getFilePath() {
		return filePath;
	}

	public void setFilePath(IPath filePath) {
		this.filePath = filePath;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getUsed() {
		return used;
	}

	public void setUsed(String used) {
		this.used = used;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getDateModified() {
		return dateModified;
	}

	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
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

}
