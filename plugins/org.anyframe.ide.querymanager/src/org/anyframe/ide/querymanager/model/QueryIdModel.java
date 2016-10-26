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

import java.util.Vector;

import org.anyframe.ide.querymanager.build.Location;


/**
 * This is QueryIdModel class.
 * 
 * @author Surindhar.Kondoor
 * @author viswa.srikant
 */
public class QueryIdModel {

	private String ProjectName = "tag from configmodal";

	private String QueryFile = "atribute from configmodal";

	private String FilePath = "";

	private Location loc = new Location();

	static Vector QueryIdList = new Vector();

	public QueryIdModel() {

	}

	public QueryIdModel(String projectName, String queryName, String filePath) {

		this.ProjectName = projectName;
		this.QueryFile = queryName;
		this.FilePath = filePath;

	}

	public static Vector getQueryIdList() {
		// ConfigList.
		return QueryIdList;
	}

	public void addToQueryIdModelList(QueryIdModel queryIdModel) {

		QueryIdList.add(queryIdModel);

		QueryIdList.contains(queryIdModel);
	}

	public void removeFrmConfigModelList(QueryIdModel configModel) {
		QueryIdList.remove(configModel);
	}

	public String getProjectName() {
		return ProjectName;
	}

	public void setProjectName(String projectName) {
		ProjectName = projectName;
	}

	public static void setConfigList(Vector ConfigList) {
		QueryIdModel.QueryIdList = ConfigList;
	}

	public String getQueryFile() {
		return QueryFile;
	}

	public void setQueryFile(String queryFile) {
		QueryFile = queryFile;
	}

	public String getFilePath() {
		return FilePath;
	}

	public void setFilePath(String filePath) {
		FilePath = filePath;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

}
