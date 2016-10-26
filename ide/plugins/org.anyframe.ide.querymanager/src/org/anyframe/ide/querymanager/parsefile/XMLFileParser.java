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
package org.anyframe.ide.querymanager.parsefile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.anyframe.ide.querymanager.build.BuilderHelper;
import org.anyframe.ide.querymanager.build.Location;
import org.anyframe.ide.querymanager.properties.QMPropertiesXMLUtil;
import org.anyframe.ide.querymanager.util.BuilderUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This is XMLFileParser class.
 * 
 * @author Junghwan Hong
 */
public class XMLFileParser {
	// <K,V> -> <id + duplicateCount, id>
	HashMap duplicateIdsMap = new HashMap();

	public HashMap getDuplicateIdsMap() {
		return duplicateIdsMap;
	}

	BuilderHelper builderHelper = BuilderHelper.getInstance();

	/**
	 * Helper method for public HashMap collectAllQueryIds(IProject project)
	 * method.
	 * 
	 * @param project
	 *            The project, <code>IProject</code> instance for which all the
	 *            query ids should be found.
	 * @param filePath
	 *            One file, <code>IFile</code> instance of the project.
	 * @param allQueryIds
	 *            All the Query Ids of the project.
	 * @return Map of All Query Ids from the File filePath.
	 */
	public Map collectXMLQueryIds(IProject project, String filePath,
			HashMap allQueryIds, IProgressMonitor monitor) {
		IFile file = project.getFile(filePath);
		if (file.exists()) {
			return checkDuplicateQuerieIds(project, file, allQueryIds, monitor);
		}
		return null;
	}

	/**
	 * Helper method for public HashMap collectAllQueryIds(IProject project)
	 * method.
	 * 
	 * @param project
	 *            The project, <code>IProject</code> instance for which all the
	 *            query ids should be found.
	 * @param resource
	 *            One file, <code>IFile</code> instance of the project.
	 * @param allQueryIds
	 *            All the Query Ids of the project.
	 * @return Map of All Query Ids from the File filePath.
	 */
	private Map checkDuplicateQuerieIds(IProject project, IResource resource,
			HashMap allQueryIds, IProgressMonitor monitor) {
		// HashMap<String, Collection> duplicateIds =
		// new HashMap<String,
		// Collection>();
		HashMap duplicateIds = new HashMap();
		String authorString = "@author";
		String dateCreatedString = "@date.created";
		String dateModifiedString = "@date.modified";
		if (builderHelper.duplicateIds.containsKey(project.getName())
				&& builderHelper.duplicateIds.get(project.getName()) != null) {
			duplicateIds = (HashMap) builderHelper.duplicateIds.get(project
					.getName());
		}
		if (duplicateIds == null)
			duplicateIds = new HashMap();
		if (resource == null)
			return null;
		// HashMap<String, Location> keys = new
		// HashMap<String, Location>();
		HashMap keys = new HashMap();
		IFile file = (IFile) resource;
		String content = BuilderUtil.readFile(file);
		int commentStart = -1;
		int commentEnd = -1;
		if (content == null)
			return keys;

		QMPropertiesXMLUtil util = new QMPropertiesXMLUtil();
		boolean dupl = util.getPropertiesDupl2(project);
		// from here table mapping
		HashMap mappingClassMap = builderHelper.getMappingClassMap();
		Collection mappingCollection = null;
		int tableMappingStart = content.indexOf("<table-mapping>");
		int queriesStart = content.indexOf("<queries>");
		if (tableMappingStart < queriesStart) {
			int startTableMapping = tableMappingStart;
			if (mappingClassMap.get(project.getName()) != null)
				mappingCollection = (Collection) mappingClassMap.get(project
						.getName());
			else
				mappingCollection = new ArrayList();
			// if <table-mapping> is before <queries>
			// then it is a valid <table-mapping>
			tableMapLoop: while (true) {
				int tableStart = content.indexOf("<table", startTableMapping);
				if (tableStart < 0)
					break tableMapLoop;
				int classStart = content.indexOf("class", tableStart);
				if (classStart < 0)
					break tableMapLoop;
				int firstDblQuote = content.indexOf("\"", classStart);
				if (firstDblQuote < 0)
					break tableMapLoop;
				int secondDblQuote = content.indexOf("\"", firstDblQuote + 1);
				if (secondDblQuote < 0)
					break tableMapLoop;
				String tblMapClassName = content.substring(firstDblQuote + 1,
						secondDblQuote);
				tblMapClassName = tblMapClassName.replace('.', '/');
				if (tblMapClassName != null
						&& !tblMapClassName.equalsIgnoreCase("")) {
					Location tblMapLoc = new Location();
					tblMapLoc.file = file;
					// store tblMapClassName in loc.key
					tblMapLoc.key = tblMapClassName;
					tblMapLoc.charStart = firstDblQuote + 1;
					tblMapLoc.charEnd = secondDblQuote;
					// add this location to collection
					// which is there in the Map.
					mappingCollection.add(tblMapLoc);
				}
				// now decide whether we have to loop
				// through... based on the location of
				// <table tag.
				tableStart = content.indexOf("<table", secondDblQuote);
				startTableMapping = secondDblQuote;
				if (tableStart < queriesStart) {
					continue tableMapLoop;
				} else {
					break tableMapLoop;
				}
			} // end of tableMapLoop: while
		}

		// till here table mapping

		int start = content.indexOf("<queries>");

		// int startHelp = 0;
		int queryStart = -1;
		int idLocation = -1;
		int categoryLocation = -1;
		int serviceTypeLocation = -1;
		int stmtStart = -1;
		int stmtEnd = -1;
		int cDataStart = -1;
		int cDataEnd = -1;
		int equalToLocation = -1;
		int serviceTypeEqLocation = -1;

		int idDoubleQuoteLocation = -1;
		int categoryDblQuoteLocation = -1;
		int ServiceTypeDQuoteLocation = -1;
		int secondQuote = -1;
		int serviceTypesecondQuote = -1;
		int categorySecondDblQuoteLocation = -1;
		int categoryEqLocation = -1;
		if (!(start < 0)) {
			outer: while (!monitor.isCanceled()) {
				// find query location. mark it as
				// loc.charStart
				commentStart = -1;
				commentEnd = -1;
				commentStart = content.indexOf("<!--", start);
				queryStart = content.indexOf("<query", start);
				if (queryStart < 0) {
					break;
				}
				if ((commentStart > 0) && (commentStart < queryStart)) {
					commentEnd = content.indexOf("-->", commentStart);
					start = commentEnd + 3;
					if (content.indexOf("<query", start) < 0)
						break;
					else
						continue outer;
				}
				start = content.indexOf("<query", start);
				if (start < 0)
					break;
				// now we have got the actual <query
				// tag.
				// find the id= location
				idLocation = content.indexOf("id", start);
				serviceTypeLocation = content
						.indexOf("servicetype", start + 12);
				if (idLocation < 0)
					break;
				equalToLocation = content.indexOf("=", idLocation);
				serviceTypeEqLocation = content.indexOf("=",
						serviceTypeLocation);
				if (equalToLocation < 0)
					break;
				idDoubleQuoteLocation = content.indexOf("\"", equalToLocation);
				ServiceTypeDQuoteLocation = content.indexOf("\"",
						serviceTypeEqLocation);
				if (idDoubleQuoteLocation < 0)
					break;
				secondQuote = content.indexOf("\"", idDoubleQuoteLocation + 1);
				serviceTypesecondQuote = content.indexOf("\"",
						ServiceTypeDQuoteLocation + 1);
				if (secondQuote < 0)
					break;

				// get the string inside = and >
				String queryId = content.substring(idDoubleQuoteLocation + 1,
						secondQuote);
				String serviceType = content.substring(
						ServiceTypeDQuoteLocation + 1, serviceTypesecondQuote);

				// Check whether we have the comment
				// for @author, @date.created and
				// @date.modified
				String author = null;
				String dateCreated = null;
				String dateModified = null;
				// commentStart =
				// content.indexOf("<!--", start);
				stmtStart = content.indexOf("<statement>", start);
				stmtEnd = content.indexOf("</statement>", stmtStart);
				int queryEnd = content.indexOf("</query>", stmtEnd);
				String category = "";
				categoryLocation = content.indexOf("category", start);
				if (categoryLocation > -1 && categoryLocation < stmtStart) {
					categoryEqLocation = content.indexOf("=", categoryLocation);
					categoryDblQuoteLocation = content.indexOf("\"",
							categoryEqLocation);
					categorySecondDblQuoteLocation = content.indexOf("\"",
							categoryDblQuoteLocation + 1);
					category = content.substring(categoryDblQuoteLocation + 1,
							categorySecondDblQuoteLocation);
				}

				// this is for Result Mapping

				int tableStart = content.indexOf("<result", stmtEnd);
				if (tableStart < queryEnd && tableStart > -1) {
					int classStart = content.indexOf("class", tableStart);
					int firstDblQuote = content.indexOf("\"", classStart);
					int secondDblQuote = content.indexOf("\"",
							firstDblQuote + 1);
					if (classStart > -1 && firstDblQuote > -1
							&& secondDblQuote > -1) {
						String tblMapClassName = content.substring(
								firstDblQuote + 1, secondDblQuote).replace('.',
								'/');
						if (tblMapClassName != null
								&& !tblMapClassName.equalsIgnoreCase("")) {
							Location tblMapLoc = new Location();
							tblMapLoc.file = file;
							// store tblMapClassName in
							// loc.key
							tblMapLoc.key = tblMapClassName;
							tblMapLoc.charStart = firstDblQuote + 1;
							tblMapLoc.charEnd = secondDblQuote;
							// add this location to
							// collection which is
							// there in the Map.
							mappingCollection.add(tblMapLoc);
						}
					}
				}
				// End of result Mapping

				// if(commentStart > 0 && commentStart
				// < stmtStart){
				// commentEnd = content.indexOf("-->",
				// commentStart);
				// if(commentEnd > 0 && commentEnd <
				// stmtStart){
				int authorStartIndex = content.indexOf(authorString, start);
				int authorEndIndex = -1;
				int dateCreatedEndIndex = -1;
				int dateModifiedEndIndex = -1;
				if (authorStartIndex > -1 && authorStartIndex < stmtEnd) {
					authorStartIndex = authorStartIndex + authorString.length();
					authorEndIndex = content.indexOf("\n", authorStartIndex);
				}
				int dateCreatedStartIndex = content.indexOf(dateCreatedString,
						start);
				if (dateCreatedStartIndex > -1
						&& dateCreatedStartIndex < stmtEnd) {
					dateCreatedStartIndex = dateCreatedStartIndex
							+ dateCreatedString.length();
					dateCreatedEndIndex = content.indexOf("\n",
							dateCreatedStartIndex);
				}
				int dateModifiedStartIndex = content.indexOf(
						dateModifiedString, start);
				if (dateModifiedStartIndex > -1
						&& dateModifiedStartIndex < stmtEnd) {
					dateModifiedStartIndex = dateModifiedStartIndex
							+ dateModifiedString.length();
					dateModifiedEndIndex = content.indexOf("\n",
							dateModifiedStartIndex);
				}
				if (authorStartIndex > 6 && authorEndIndex > authorStartIndex)
					author = content
							.substring(authorStartIndex, authorEndIndex);
				if (dateCreatedStartIndex > 12
						&& dateCreatedEndIndex > dateCreatedStartIndex)
					dateCreated = content.substring(dateCreatedStartIndex,
							dateCreatedEndIndex);
				if (dateModifiedStartIndex > 12
						&& dateModifiedEndIndex > dateModifiedStartIndex)
					dateModified = content.substring(dateModifiedStartIndex,
							dateModifiedEndIndex);

				authorEndIndex = -1;
				dateCreatedEndIndex = -1;
				dateModifiedEndIndex = -1;
				authorStartIndex = -1;
				dateCreatedStartIndex = -1;
				dateModifiedStartIndex = -1;
				// }
				// commentStart = -1;
				// commentEnd = -1;
				// }

				// int authorEndIndex = -1;
				// int dateCreatedEndIndex = -1;
				// int dateModifiedEndIndex = -1;
				//
				//
				// int authorStartIndex =
				// content.indexOf(authorString,
				// stmtStart);
				// if(authorStartIndex > -1 &&
				// authorStartIndex < commentEnd){
				// authorStartIndex = authorStartIndex
				// + authorString.length();
				// authorEndIndex =
				// content.indexOf("@",
				// authorStartIndex);
				// }
				// get the string inside <statement>]

				String query = content.substring(stmtStart + 11, stmtEnd);
				// check whether the statement having
				// CDATA or not
				if (query.indexOf("<![CDATA[") >= 0) {
					cDataStart = content.indexOf("<![CDATA[", start);
					cDataEnd = content.indexOf("]]>", stmtStart);
					query = content.substring(cDataStart + 9, cDataEnd);
				} else {
					stmtStart = content.indexOf("<statement>", start);
					stmtEnd = content.indexOf("</statement>", stmtStart);
					query = content.substring(stmtStart + 11, stmtEnd);
				}

				// trim the strings
				query = query.trim();
				queryId = queryId.trim();
				if (author != null)
					author = author.trim();
				if (dateCreated != null)
					dateCreated = dateCreated.trim();
				if (dateModified != null)
					dateModified = dateModified.trim();

				Location loc = new Location();
				loc.setDuplicate(false);
				String totalQuery = content.substring(start, queryEnd + 8);
				if (serviceType.equalsIgnoreCase("extendedQueryService"))
					loc.serviceType = "extendedQueryService";
				else
					loc.serviceType = "queryService";

				loc.file = file;
				loc.setFilePath(file.getFullPath());
				loc.key = queryId;
				loc.query = query;
				// loc.charStart = start + 1;
				loc.charStart = idDoubleQuoteLocation + 1;
				// loc.charEnd = secondQuote;
				loc.charEnd = loc.charStart + queryId.length();
				loc.setCategory(category);
				loc.setTotalQuery(totalQuery);
				if (author != null)
					loc.setAuthor(author);
				if (dateCreated != null)
					loc.setDateCreated(dateCreated);
				if (dateModified != null)
					loc.setDateModified(dateModified);
				// if this id presents in the duplicate
				// ids map, it means
				// multiple duplicates across files
				// present.
				// so add this id to the map.
				if (duplicateIds.get(loc.key) != null) {
					// this time we are going to add
					// already existing
					// collection.
					// no need to check for null.
					Collection collection = (Collection) duplicateIds
							.get(loc.key);
					collection.add(loc);
					keys.put(loc.key, loc);
				} else if (keys.containsKey(loc.key)) {
					// Collection<Location> collection
					// =
					// duplicateIds.get(loc.key);
					Collection collection = (Collection) duplicateIds
							.get(loc.key);
					Location otherDuplicate = (Location) keys.get(loc.key);
					if (collection != null) {
						collection.add(loc);
						collection.add(otherDuplicate);
					} else {
						// collection = new
						// ArrayList<Location>();
						collection = new ArrayList();
						collection.add(loc);
						collection.add(otherDuplicate);
						duplicateIds.put(loc.key, collection);
					}
					// keys.remove(loc.key);
					// TODO
					if (dupl) {
						int count = 0;
						while (duplicateIdsMap.keySet().contains(
								loc.key + "_duplicate_classification_" + count)) {
							count++;
						}
						keys.put(
								loc.key + "_duplicate_classification_" + count,
								loc);
						duplicateIdsMap.put(loc.key
								+ "_duplicate_classification_" + count, loc);
					} else {
						keys.put(loc.key, loc);
					}
				} else if (allQueryIds.containsKey(loc.key)) {
					// Collection<Location> collection
					// =
					// duplicateIds.get(loc.key);
					Collection collection = (Collection) duplicateIds
							.get(loc.key);
					Location otherDuplicate = (Location) allQueryIds
							.get(loc.key);
					if (collection != null) {
						collection.add(loc);
						collection.add(otherDuplicate);
					} else {
						// collection = new
						// ArrayList<Location>();
						collection = new ArrayList();
						collection.add(loc);
						collection.add(otherDuplicate);
						duplicateIds.put(loc.key, collection);
					}
					allQueryIds.remove(loc.key);
					loc.setDuplicate(true);
					// keys.put(loc.key, loc);
					if (dupl) {
						int count = 0;
						while (duplicateIdsMap.keySet().contains(
								loc.key + "_duplicate_classification_" + count)) {
							count++;
						}
						keys.put(
								loc.key + "_duplicate_classification_" + count,
								loc);
						duplicateIdsMap.put(loc.key
								+ "_duplicate_classification_" + count, loc);
					} else {
						keys.put(loc.key, loc);
					}
				} else {
					keys.put(loc.key, loc);
				}
				start = secondQuote + 1;
				idLocation = -1;
				equalToLocation = -1;
				categoryLocation = -1;
				idDoubleQuoteLocation = -1;
				secondQuote = -1;
			}
		}

		// put the MappingClassNames into
		// mappingClassMap.get(project.getName()
		if (mappingCollection != null && mappingCollection.size() > 0)
			mappingClassMap.put(project.getName(), mappingCollection);
		// HashMap<String, Collection> duplicateMap =
		// BuilderHelper.getInstance().duplicateIds.get(project.getName());
		HashMap duplicateMap = (HashMap) builderHelper.duplicateIds.get(project
				.getName());
		if (duplicateIds != null) {
			if (duplicateMap == null) {
				// duplicateMap=new HashMap<String,
				// Collection>();
				duplicateMap = new HashMap();
				if (duplicateIds.size() > 0)
					duplicateMap.putAll(duplicateIds);
				if (duplicateMap != null && duplicateMap.size() > 0)
					builderHelper.duplicateIds.put(project.getName(),
							duplicateMap);
			} else {
				if (duplicateIds != null && duplicateIds.size() > 0)
					duplicateMap.putAll(duplicateIds);
			}
		}
		return keys;
	}
}
