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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.anyframe.ide.querymanager.build.Location;
import org.anyframe.ide.querymanager.preferences.AnyframePreferencePage;
import org.anyframe.ide.querymanager.preferences.PreferencesHelper;
import org.anyframe.ide.querymanager.util.BuilderUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This is JavaFileParser class.
 * 
 * @author Surindhar.Kondoor
 * @author viswa.srikant
 */
public class JavaFileParser {

	private Set usedQueryID = new HashSet();
	private Set usedQueryIDLocation = new HashSet();

	public Set getUsedQueryID() {
		return usedQueryID;
	}

	public Set getUsedQueryIDLocation() {
		return usedQueryIDLocation;
	}

	public Map collectDaoQueryIdsFromProject(IProject project, String filePath,
			HashMap allQueryIds, IProgressMonitor monitor, HashMap technicalMap) {
		IFile file = project.getFile(filePath);

		if (file.exists()) {

			return collectDAOQueryIds(project, file, allQueryIds, monitor,
					technicalMap);

		}
		monitor.worked(50);
		return null;
	}

	/**
	 * This utility method returns collection of Query ids used in DAO file
	 * 
	 * @param project
	 *            The project, <code>IProject</code> instance for which all the
	 *            query ids should be found.
	 * @param technicalMap
	 * @param filePath
	 *            One file, <code>IFile</code> instance of DAO file.
	 * @return Map of All Query Ids which are used in DAO.
	 */
	private Map collectDAOQueryIds(IProject project, IResource resource,
			HashMap allQueryIds, IProgressMonitor monitor, HashMap technicalMap) {
		if (resource == null)
			return null;
		// HashMap<String, Location> keys = new
		// HashMap<String, Location>();
		HashMap keys = new HashMap();
		IFile file = (IFile) resource;
		int num = file.getName().indexOf(".");
		String justFileName = file.getName().substring(0, num);

		if (file.exists()) {
			String content = null;

			content = BuilderUtil.readFile(file);

			// if isQS == true then it refers that this
			// file is a QS DAO.
			// boolean isQS = true;
			// boolean isNTS = false;
			if (content == null) {
				return null;
			}

			HashMap preferencesMap = null;
			HashMap abstractDAOMethodMap;

			if (content.indexOf("org.anyframe.query.QueryService") > -1) {
				HashMap methodMap = new HashMap();
				PreferencesHelper preferencesHelper = PreferencesHelper
						.getPreferencesHelper();
				preferencesMap = new HashMap();
				preferencesMap = preferencesHelper
						.populateHashMapWithPreferences(preferencesMap);
				abstractDAOMethodMap = preferencesHelper
						.populateHashMapWithAbstractDAOMethodNames();
				methodMap = abstractDAOMethodMap;

				if (content.indexOf("org.anyframe.query.dao.AbstractDao") > -1) {
					collectDAOQueriesForMethods2(methodMap, monitor, content,
							false, file, false, preferencesMap, keys, project,
							allQueryIds);
				} else if (content
						.indexOf("org.anyframe.generic.dao.query.GenericDaoQuery") > -1) {
					collectDAOQueriesForMethods1(methodMap, monitor, content,
							false, file, false, preferencesMap, keys, project,
							allQueryIds);
				} else if (content
						.indexOf("org.anyframe.query.dao.QueryServiceDaoSupport") > -1) {
					collectDAOQueriesForMethods1(methodMap, monitor, content,
							false, file, false, preferencesMap, keys, project,
							allQueryIds);
				} else if (justFileName.toUpperCase().endsWith("DAO")) {
					collectDAOQueriesForMethods1(methodMap, monitor, content,
							false, file, false, preferencesMap, keys, project,
							allQueryIds);
				} else if (isExtendDao(content)) {
					collectDAOQueriesForMethods1(methodMap, monitor, content,
							false, file, false, preferencesMap, keys, project,
							allQueryIds);
				} else {
					collectDAOQueriesForMethods1(methodMap, monitor, content,
							false, file, false, preferencesMap, keys, project,
							allQueryIds);
				}
			} else {
			}

			HashMap classMethodsMap = technicalMap;
			String qsVar = "";
			if (classMethodsMap != null && classMethodsMap.size() > 0) {
				Iterator classMethodMapIterator = classMethodsMap.keySet()
						.iterator();
				classMethodLoop: while (classMethodMapIterator.hasNext()) {
					// String className = classMethodMapIterator.next().toString();
					final String importClassFullyQualName = classMethodMapIterator
							.next().toString();
					if (importClassFullyQualName != null) {
						try {
							final String className = importClassFullyQualName
									.substring(importClassFullyQualName
											.lastIndexOf('.') + 1);
							if (content.indexOf(className) > -1) {
								qsVar = BuilderUtil.getVarName(content,
										importClassFullyQualName, className);
								if (qsVar != null) {
									HashMap methodMap3 = new HashMap();
									Collection methodCollection = (Collection) classMethodsMap
											.get(importClassFullyQualName);
									Iterator methodCollectionItr = methodCollection
											.iterator();
									while (methodCollectionItr.hasNext()) {
										String methName = methodCollectionItr
												.next().toString();
										methodMap3.put(methName, methName);
									}
									methodMap3 = BuilderUtil
											.appendVarToMethods(methodMap3,
													qsVar + ".");
									// To consider ClassName.VariableName in
									// DAOs add className to methodMap3.
									methodMap3.put(className + ".", className
											+ ".");
								}
							}
						} catch (Exception unkonown) {
							// This scenario should not and will not happen. If
							// for some reasons it happens,
							// this unknown reason should not affect the flow of
							// finding the DAO Queries.
							continue classMethodLoop;
						}
					}
				}
				// got the class and methods.
			}
			return keys;
		} else {
			// DAO File doesnt exist
			return null;
		}
	}

	private boolean isExtendDao(String content) {
		int extendsIndex = content.indexOf("extends");
		int nextBlankIndex = content.indexOf(" ", extendsIndex + 8);
		String extendsClassName = content.substring(extendsIndex + 8,
				nextBlankIndex);

		if (extendsClassName.toUpperCase().indexOf("DAO") > -1) {
			return true;
		} else {
			return false;
		}
	}

	private void collectDAOQueriesForMethods1(HashMap methodMap,
			IProgressMonitor monitor, String content, boolean isNTS,
			IFile file, boolean isQS, Map preferencesMap, HashMap keys,
			IProject project, HashMap allQueryIds) {

		if (methodMap == null)
			return;
		Iterator methodMapIterator = methodMap.keySet().iterator();
		// for (int i = 0; i < methodArr.length; i++) {
		while (methodMapIterator.hasNext()) {
			String key = methodMapIterator.next().toString();
			String value = methodMap.get(key).toString();
			if (monitor.isCanceled()) {
				break;
			}

			boolean endContent = true;

			int index1 = 0;
			int index2 = 0;

			while (endContent) {

				int methodQuotIndex = content
						.indexOf(value + "(\"", index1 + 1);
				int methodLineIndex = content.indexOf(value + "(\r\n",
						index2 + 1);

				int num1 = (value + "(\"").length();
				int num2 = (value + "(\n").length();

				int index = -1;
				if (methodQuotIndex != -1) {
					index = methodQuotIndex + num1;
				} else if (methodLineIndex != -1) {
					int tempQuot = content
							.indexOf("\"", methodLineIndex + num2);
					index = tempQuot + 1;
				} else {
				}

				int lastIndex = -1;

				if (index > 0) {
					lastIndex = content.indexOf("\"", index);

					Location loc = new Location();
					loc.setKey(content.substring(index, lastIndex));
					loc.setFilePath(file.getLocation());
					loc.setFile(file);
					loc.setCharStart(index);
					loc.setCharEnd(lastIndex);

					usedQueryID.add(content.substring(index, lastIndex));
					usedQueryIDLocation.add(loc);
				}
				index1 = methodQuotIndex;
				index2 = methodLineIndex;

				if (methodQuotIndex != -1 || methodLineIndex != -1) {
					endContent = true;
				} else {
					endContent = false;
				}
			}
		}
	}

	private void collectDAOQueriesForMethods2(HashMap methodMap,
			IProgressMonitor monitor, String content, boolean isNTS,
			IFile file, boolean isQS, Map preferencesMap, HashMap keys,
			IProject project, HashMap allQueryIds) {

		if (methodMap == null)
			return;
		Iterator preferencesMapIterator = preferencesMap.keySet().iterator();
		// for (int i = 0; i < methodArr.length; i++) {
		while (preferencesMapIterator.hasNext()) {
			String key = preferencesMapIterator.next().toString();
			String value = methodMap.get(key).toString();
			if (monitor.isCanceled()) {
				break;
			}

			boolean endContent = true;

			int index1 = 0;
			int index2 = 0;

			while (endContent) {

				int methodQuotIndex = content
						.indexOf(value + "(\"", index1 + 1);
				int methodLineIndex = content.indexOf(value + "(\r\n",
						index2 + 1);

				int num1 = (value + "(\"").length();
				int num2 = (value + "(\r\n").length();

				int index = -1;
				if (methodQuotIndex != -1) {
					index = methodQuotIndex + num1;
				} else if (methodLineIndex != -1) {
					int tempQuot = content
							.indexOf("\"", methodLineIndex + num2);
					index = tempQuot + 1;
				} else {
				}

				int lastIndex = -1;

				if (index > 0) {
					lastIndex = content.indexOf("\"", index);
					// data class name
					String queryIDwithoutFix = content.substring(index,
							lastIndex);

					Location loc = new Location();
					loc.setFilePath(file.getLocation());
					loc.setFile(file);

					loc.setKey(content.substring(index, lastIndex));
					loc.setCharStart(index);
					loc.setCharEnd(lastIndex);

					if (key.equals(AnyframePreferencePage.CREATE_PREFIX_ID)) {
						usedQueryID.add(value + queryIDwithoutFix);

						loc.setKey(value + queryIDwithoutFix);
						loc.setCharStart(index);
						loc.setCharEnd(index + queryIDwithoutFix.length());
						usedQueryIDLocation.add(loc);

					} else if (key
							.equals(AnyframePreferencePage.FIND_PREFIX_ID)) {
						usedQueryID.add(value + queryIDwithoutFix);

						loc.setKey(value + queryIDwithoutFix);
						loc.setCharStart(index);
						loc.setCharEnd(index + queryIDwithoutFix.length());
						usedQueryIDLocation.add(loc);

					} else if (key
							.equals(AnyframePreferencePage.REMOVE_PREFIX_ID)) {
						usedQueryID.add(value + queryIDwithoutFix);

						loc.setKey(value + queryIDwithoutFix);
						loc.setCharStart(index);
						loc.setCharEnd(index + queryIDwithoutFix.length());
						usedQueryIDLocation.add(loc);

					} else if (key
							.equals(AnyframePreferencePage.UPDATE_PREFIX_ID)) {
						usedQueryID.add(value + queryIDwithoutFix);

						loc.setKey(value + queryIDwithoutFix);
						loc.setCharStart(index);
						loc.setCharEnd(index + queryIDwithoutFix.length());
						usedQueryIDLocation.add(loc);

					} else if (key
							.equals(AnyframePreferencePage.FIND_BYPK_SUFFIX_ID)) {
						String prefix = preferencesMap.get(
								AnyframePreferencePage.FIND_PREFIX_ID)
								.toString();
						String suffix = value.substring(prefix.length());
						usedQueryID.add(prefix + queryIDwithoutFix + suffix);

						loc.setKey(prefix + queryIDwithoutFix + suffix);
						loc.setCharStart(index);
						loc.setCharEnd(index + queryIDwithoutFix.length());
						usedQueryIDLocation.add(loc);

					} else if (key
							.equals(AnyframePreferencePage.FIND_LIST_SUFFIX_ID)) {
						String prefix = preferencesMap.get(
								AnyframePreferencePage.FIND_PREFIX_ID)
								.toString();
						String suffix = value.substring(prefix.length());
						usedQueryID.add(prefix + queryIDwithoutFix + suffix);

						loc.setKey(prefix + queryIDwithoutFix + suffix);
						loc.setCharStart(index);
						loc.setCharEnd(index + queryIDwithoutFix.length());
						usedQueryIDLocation.add(loc);

					} else if (key
							.equals(AnyframePreferencePage.FIND_LIST_WITH_PAGING_SUFFIX_ID)) {
						value = methodMap.get(
								AnyframePreferencePage.FIND_LIST_SUFFIX_ID)
								.toString();
						String prefix = preferencesMap.get(
								AnyframePreferencePage.FIND_PREFIX_ID)
								.toString();
						String suffix = value.substring(prefix.length());
						usedQueryID.add(prefix + queryIDwithoutFix + suffix);

						loc.setKey(prefix + queryIDwithoutFix + suffix);
						loc.setCharStart(index);
						loc.setCharEnd(index + queryIDwithoutFix.length());
						usedQueryIDLocation.add(loc);
					} else {
					}
				}

				index1 = methodQuotIndex;
				index2 = methodLineIndex;

				if (methodQuotIndex != -1 || methodLineIndex != -1) {
					endContent = true;
				} else {
					endContent = false;
				}
			}
		}
	}

	private void collectDAOQueriesForMethods(HashMap methodMap,
			IProgressMonitor monitor, String content, boolean isNTS,
			IFile file, boolean isQS, Map preferencesMap, HashMap keys,
			IProject project, HashMap allQueryIds) {
		int start = 0;
		int commentStart = -1;
		int singlecommentStart = -1;
		int singlecommentEnd = -1;
		int commentEnd = -1;
		int queryStart = -1;

		if (methodMap == null)
			return;
		Iterator methodMapIterator = methodMap.keySet().iterator();
		// for (int i = 0; i < methodArr.length; i++) {
		while (methodMapIterator.hasNext()) {
			String key = methodMapIterator.next().toString();
			String value = methodMap.get(key).toString();
			if (monitor.isCanceled()) {
				break;
			}
			start = 0;
			outer: while (true) {
				if (monitor.isCanceled()) {
					break;
				}
				commentStart = -1;
				commentEnd = -1;
				singlecommentStart = -1;
				singlecommentEnd = -1;
				commentStart = content.indexOf("/*", start);
				singlecommentStart = content.indexOf("//", start);
				if (isNTS) {
					queryStart = content.indexOf(value, start);
					if (queryStart < 0)
						break outer;
					queryStart = content.indexOf("\"", start);
				} else {
					queryStart = content.indexOf(value + "(\"");
				}

				if (queryStart < 0) {
					break outer;
				} else {
				}

				if ((singlecommentStart > 0)
						&& (singlecommentStart < queryStart)) {
					singlecommentEnd = content
							.indexOf('\n', singlecommentStart);
					start = singlecommentEnd + 1;
					if (isNTS) {
						if (content.indexOf(value, start + 1) < 0)
							break;
						else
							continue outer;
					} else {
						if (content.indexOf(value + "(\"", start + 1) < 0)
							break;
						else
							continue outer;
					}
				}

				if ((commentStart > 0) && (commentStart < queryStart)) {
					commentEnd = content.indexOf("*/", commentStart);
					start = commentEnd + 2;
					if (isNTS) {
						if (content.indexOf(value, start + 1) < 0)
							break;
						else
							continue outer;
					} else {
						if (content.indexOf(value + "(\"", start + 1) < 0)
							break;
						else
							continue outer;
					}
				}

				// Find query id location which is
				// used as queryservice
				// method parameter.
				// mark it as loc.charStart
				if (!isQS) {
					int checkPos = content.indexOf(value, start);
					if (content.charAt(checkPos - 1) == '.'
							|| content.charAt(checkPos - 2) == '.'
							|| content.charAt(checkPos - 3) == '.') {
						start = start + 5;
						continue outer;
					}
				}
				if (isNTS) {
					start = content.indexOf(value, start);
					if (start > 0)
						start = content.indexOf("\"", start) + 1;
				} else
					start = content.indexOf(value + "(\"", start);

				if (start < 0)
					break;
				if (!isNTS)
					start = start + value.length() + 2;
				// Get the end position of query
				// id.
				int end = content.indexOf('"', start);

				if (end < 0)
					break;
				Location loc = new Location();
				loc.file = file;
				// Get the Query ID.
				// int daoQueryIdStart=start-24;
				// int daoQueryIdEnd=start-7;
				String temp = content.substring(start, end).trim();

				if (isQS || isNTS)
					loc.key = temp;
				else {
					// if create, find, remove and update key will be
					// preferencesMap.get(key)
					// if findByPK and findByList key should be replaced with...
					// preferencesMap.get(FIND_PREFIX_ID) + temp +
					// preferencesMap.get(FIND_BYPK_SUFFIX_ID) ||
					// preferencesMap.get(FIND_LIST_SUFFIX_ID)
					if (key.equalsIgnoreCase(AnyframePreferencePage.FIND_BYPK_SUFFIX_ID)) {
						loc.key = preferencesMap
								.get(AnyframePreferencePage.FIND_PREFIX_ID)
								.toString()
								.concat(temp)
								.concat(preferencesMap
										.get(AnyframePreferencePage.FIND_BYPK_SUFFIX_ID)
										.toString());
					} else if (key
							.equalsIgnoreCase(AnyframePreferencePage.FIND_LIST_SUFFIX_ID)
							|| key.equalsIgnoreCase(AnyframePreferencePage.FIND_LIST_WITH_PAGING_SUFFIX_ID)) {
						loc.key = preferencesMap
								.get(AnyframePreferencePage.FIND_PREFIX_ID)
								.toString()
								.concat(temp)
								.concat(preferencesMap
										.get(AnyframePreferencePage.FIND_LIST_SUFFIX_ID)
										.toString());
					} else {
						loc.key = preferencesMap.get(key).toString()
								.concat(temp);
					}

				}
				loc.charStart = start - 1;
				loc.charEnd = end;

				// if this id presents in the keys
				// ids map, it means
				// multiple duplicates across files
				// present.
				// so add this id to the map.
				// HashMap projectDaoMap =
				// (HashMap)allDAOQueryIds.get(project.getName());
				if (keys.get(loc.key) instanceof Collection
						&& keys.containsKey(loc.key)) {
					// this time we are going to
					// add already existing
					// collection.
					// no need to check for null.
					Collection collection = (Collection) keys.get(loc.key);

					if (collection != null) {
						collection.add(loc);
					} else {
						collection = new ArrayList();
						collection.add(loc);
						keys.put(loc.key, collection);
					}
				} else if (allQueryIds.get(loc.key) instanceof Collection
						&& allQueryIds.containsKey(loc.key)) {

					Collection collection = (Collection) allQueryIds
							.get(loc.key);

					if (collection != null) {
						collection.add(loc);
					} else {
						collection = new ArrayList();
						collection.add(loc);
						keys.put(loc.key, collection);
					}

				} else {
					// First occurance of query id
					// in DAO
					Collection collection = new ArrayList();
					collection.add(loc);
					keys.put(loc.key, collection);
				}

				start = end + 1;
			} // end of while loop

		} // end while itr loop
			// returns Map of All Query Ids which are
			// used in DAO.
	}

}
