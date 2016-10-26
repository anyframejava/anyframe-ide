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
package org.anyframe.ide.querymanager.model;

import java.util.HashMap;
import java.util.Vector;

/**
 * The class QueryAttribute contains information abt query id
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QueryAttribute {

	// Query Id
	private String queryId;

	// Query Statement
	private String query;

	// Indicates wether query is Dynamic query or not
	private boolean isDynamic;

	// Array of input parameter
	private Vector inputParamVect;

	// Array of output parameter
	private Vector attrOutputVect;

	// Source folder name
	private String srcfolder;

	// Package name
	private String pkg;

	// VO class name
	private String voclass;

	private String resultMapperClass;

	// Indicates wether VO is exist or not
	private boolean voexist;

	// Array of VO Attributes
	private String[] voAttribute;

	// Array of VO Attribute's data type
	private String[] voDataTypes;

	// Indicates wether Reslt Mapping is required or
	// not
	boolean resltMapping;

	// Maping of Vo attribute and its data type
	private HashMap attrDatTypeMap = new HashMap();

	private boolean classOnlyMapping;

	/**
	 * Default constructor
	 */
	public QueryAttribute() {
		inputParamVect = new Vector();
		attrOutputVect = new Vector();
	}

	/**
	 * getter
	 * 
	 * @return voexist
	 */
	public boolean isVoexist() {
		return voexist;
	}

	/**
	 * setter
	 * 
	 * @param voexist
	 *            Indicates wether VO is exist or not
	 */
	public void setVoexist(boolean voexist) {
		this.voexist = voexist;
	}

	/**
	 * getter
	 * 
	 * @return attrVect
	 */
	public Vector getInputParamVect() {
		return inputParamVect;
	}

	/**
	 * getter
	 * 
	 * @return query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * getter
	 * 
	 * @return queryId
	 */
	public String getQueryId() {
		return queryId;
	}

	/**
	 * getter
	 * 
	 * @return attrOutputVect
	 */
	public Vector getAttrOutputVect() {
		return attrOutputVect;
	}

	/**
	 * getter
	 * 
	 * @return isDynamic
	 */
	public boolean isDynamic() {
		return isDynamic;
	}

	/**
	 * setter
	 * 
	 * @param query
	 *            The query string
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * setter
	 * 
	 * @param queryId
	 *            The query id
	 */
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	/**
	 * setter
	 * 
	 * @param attrOutputVect
	 *            vector of attributes.
	 */
	public void setAttrOutputVect(Vector attrOutputVect) {
		this.attrOutputVect = attrOutputVect;
	}

	/**
	 * setter
	 * 
	 * @param attrVect
	 *            vector of attributes.
	 */
	public void setInputParamVect(Vector inputParamVect) {
		this.inputParamVect = inputParamVect;
	}

	/**
	 * setter
	 * 
	 * @param isDynamic
	 *            boolean to represent whether the query is dynamic or not
	 */
	public void setDynamic(boolean isDynamic) {
		this.isDynamic = isDynamic;
	}

	/**
	 * getter
	 * 
	 * @return voclass
	 */
	public String getVoclass() {
		return voclass;
	}

	/**
	 * setter
	 * 
	 * @param voclass
	 *            VO class name
	 */
	public void setVoclass(String voclass) {
		this.voclass = voclass;
	}

	public String getResultMapperClass() {
		return resultMapperClass;
	}

	public void setResultMapperClass(String resultMapperClass) {
		this.resultMapperClass = resultMapperClass;
	}

	/**
	 * getter
	 * 
	 * @return voAttribute
	 */
	public String[] getVoAttribute() {
		return voAttribute;
	}

	/**
	 * setter
	 * 
	 * @param voAttribute
	 *            Array of VO Attributes
	 */
	public void setVoAttribute(String[] voAttribute) {
		this.voAttribute = voAttribute;
	}

	/**
	 * getter
	 * 
	 * @return pkg
	 */
	public String getPkg() {
		return pkg;
	}

	/**
	 * setter
	 * 
	 * @param pkg
	 *            Package name
	 */
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	/**
	 * getter
	 * 
	 * @return srcfolder
	 */
	public String getSrcfolder() {
		return srcfolder;
	}

	/**
	 * setter
	 * 
	 * @param srcfolder
	 *            Source folder name
	 */
	public void setSrcfolder(String srcfolder) {
		this.srcfolder = srcfolder;
	}

	/**
	 * getter
	 * 
	 * @return resltMapping
	 */
	public boolean isResltMapping() {
		return resltMapping;
	}

	/**
	 * setter
	 * 
	 * @param resltMapping
	 *            Indicates wether Reslt Mapping is required or not
	 */
	public void setResltMapping(boolean resltMapping) {
		this.resltMapping = resltMapping;
	}

	/**
	 * getter
	 * 
	 * @return attrDatTypeMap
	 */
	public HashMap getAttrDatTypeMap() {
		return attrDatTypeMap;
	}

	/**
	 * setter
	 * 
	 * @param attrDatTypeMap
	 *            Maping of Vo attribute and its data type
	 */
	public void setAttrDatTypeMap(HashMap attrDatTypeMap) {
		this.attrDatTypeMap = attrDatTypeMap;
	}

	/**
	 * getter
	 * 
	 * @return voDataTypes
	 */
	public String[] getVoDataTypes() {
		return voDataTypes;
	}

	/**
	 * setter
	 * 
	 * @param voDataTypes
	 *            Array of VO Attribute's data type
	 */
	public void setVoDataTypes(String[] voDataTypes) {
		this.voDataTypes = voDataTypes;
	}

	public void setClassOnlyMapping(boolean b) {
		this.classOnlyMapping = b;
	}

	public boolean isClassOnlyMapping() {
		return classOnlyMapping;
	}

}
