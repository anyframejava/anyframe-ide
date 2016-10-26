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

import java.util.ArrayList;
import java.util.Map;

/**
 * The information of file with query ids map data for make-up Query Explorer
 * View.
 *
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class FileInfoVO {
	String fileName;
	String alias;
	String path;
	Map queryId;
	ArrayList result;
	Map tempTest;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Map getQueryId() {
		return queryId;
	}

	public void setQueryId(Map queryId) {
		this.queryId = queryId;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ArrayList getResult() {
		return result;
	}

	public void setResult(ArrayList arrayList) {
		this.result = arrayList;
	}

	public Map getTempTest() {
		return tempTest;
	}

	public void setTempTest(Map tempTest) {
		this.tempTest = tempTest;
	}
}
