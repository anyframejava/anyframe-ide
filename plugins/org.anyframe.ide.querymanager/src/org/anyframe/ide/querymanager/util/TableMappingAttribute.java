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
package org.anyframe.ide.querymanager.util;

/**
 * This is TableMappingAttribute class.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class TableMappingAttribute {

	String tableName = "";

	String voClass = "";

	String[] dataTypes = null;;

	String[] attributes = null;

	String[] columns = null;

	String[] primaryKeys = null;

	/**
	 * @return the attributes
	 */
	public String[] getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the dataTypes
	 */
	public String[] getDataTypes() {
		return dataTypes;
	}

	/**
	 * @param dataTypes
	 *            the dataTypes to set
	 */
	public void setDataTypes(String[] dataTypes) {
		this.dataTypes = dataTypes;
	}

	/**
	 * @return the primaryKeys
	 */
	public String[] getPrimaryKeys() {
		return primaryKeys;
	}

	/**
	 * @param primaryKeys
	 *            the primaryKeys to set
	 */
	public void setPrimaryKeys(String[] primaryKeys) {
		this.primaryKeys = primaryKeys;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName
	 *            the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the voClass
	 */
	public String getVoClass() {
		return voClass;
	}

	/**
	 * @param voClass
	 *            the voClass to set
	 */
	public void setVoClass(String voClass) {
		this.voClass = voClass;
	}

	/**
	 * @return the columns
	 */
	public String[] getColumns() {
		return columns;
	}

	/**
	 * @param columns
	 *            table columns
	 */
	public void setColumns(String[] columns) {
		this.columns = columns;
	}

}
