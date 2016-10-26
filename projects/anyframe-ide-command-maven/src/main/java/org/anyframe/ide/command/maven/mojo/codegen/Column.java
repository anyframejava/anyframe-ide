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
package org.anyframe.ide.command.maven.mojo.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This is Column class for generate Domain class.
 * 
 * @author Sujeong Lee
 */
public class Column {

	String columnType;
	String columnName;
	String length;

	String fieldType;
	String fieldName;
	String fkTable;
	String[] sampleDataArray; // for sample-data.xml
	String testData; // for test case input

	boolean isKey;
	boolean isFkey;
	boolean notNull;

	// for sample-data.xml with foreigh key list
	List<Map<String, String>> fkSampleDataList = new ArrayList<Map<String, String>>();

	public static final String COLUMN_NAME = "ColumnName";
	public static final String COLUMN_TYPE = "ColumnType";
	public static final String LENGTH = "Length";
	public static final String KEY = "PK";
	public static final String FKEY = "FK";
	public static final String NOT_NULL = "NotNull";

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFkTable() {
		return fkTable;
	}

	public void setFkTable(String fkTable) {
		this.fkTable = fkTable;
	}

	public String[] getSampleDataArray() {
		return sampleDataArray;
	}

	public void setSampleDataArray(String[] sampleDataArray) {
		this.sampleDataArray = sampleDataArray;
	}

	public String getTestData() {
		return testData;
	}

	public void setTestData(String testData) {
		this.testData = testData;
	}

	public boolean getIsKey() {
		return isKey;
	}

	public void setIsKey(boolean isKey) {
		this.isKey = isKey;
	}

	public boolean getIsFkey() {
		return isFkey;
	}

	public void setFkey(boolean isFkey) {
		this.isFkey = isFkey;
	}

	public boolean getNotNull() {
		return notNull;
	}

	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}

	public List<Map<String, String>> getFkSampleDataList() {
		return fkSampleDataList;
	}

	public void setFkSampleDataList(List<Map<String, String>> fkSampleDataList) {
		this.fkSampleDataList = fkSampleDataList;
	}

	public String toString() {
		return "Column [columnType=" + columnType + ", columnName=" + columnName + ", length=" + length + ", fieldType=" + fieldType + ", fieldName="
				+ fieldName + ", fkTable=" + fkTable + ", sampleDataArray=" + Arrays.toString(sampleDataArray) + ", testData=" + testData
				+ ", isKey=" + isKey + ", isFkey=" + isFkey + ", notNull=" + notNull + "]";
	}

}
