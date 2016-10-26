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

/**
 * The class QueryOutputAttribute contains information about output attributes
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QueryOutputAttribute {

	// Query Column name
	private String columnName;

	// VO attribute name corresponding to column name
	private String attribute;

	// VO atribute's java data type
	private String dataType;

	/**
	 * getter
	 * 
	 * @return dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * setter
	 * 
	 * @param dataType
	 *            VO atribute's java data type
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * getter
	 * 
	 * @return columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * setter
	 * 
	 * @param columnName
	 *            Query Column name
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * getter
	 * 
	 * @return attribute
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * setter
	 * 
	 * @param attribute
	 *            VO attribute name corresponding to column name
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

}
