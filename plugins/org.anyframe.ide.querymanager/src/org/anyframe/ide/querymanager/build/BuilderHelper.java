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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.parsefile.FilesParser;
import org.anyframe.ide.querymanager.util.AnyframeJarLoader;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * This is a utility Singleton class which helps the DB IO Builder and Marker to
 * find the duplicate ids exist across many query xml files in the same project.
 * This class maintains the Duplicate Ids and All Query Ids of the project in a
 * Singleton Instance.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 * @author Tulasi.m
 */
public class BuilderHelper {
	// logger
	// private static final Log LOGGER = LogFactory.getLog(BuilderHelper.class);

	// Collection of Query Ids in all DAO files
	HashMap allDAOQueryIds = new HashMap();

	// Singleton
	private static BuilderHelper builderHelper = new BuilderHelper();

	// maintains all the queryids for all the projects
	// in the workspace.
	private HashMap queryIds = null;

	private HashMap queryParseResult = null;

	// maintains all the duplicate ids for all projects
	// in the workspace.
	public HashMap duplicateIds = new HashMap();

	private HashMap mappingClassMap = new HashMap();

	private Set usedQueryIDsSet = new HashSet();
	private HashMap usedQueryIDsMap = new HashMap();
	private HashMap usedQueryIDsLocationMap = new HashMap();

	private HashMap duplAliasIdsMap = new HashMap();
	private HashMap allQueryMap = new HashMap();

	public HashMap getDuplAliasIdsMap() {
		return duplAliasIdsMap;
	}

	public HashMap getAllQueryMap() {
		return allQueryMap;
	}

	/**
	 * This utility method helps to search all query ids in a project.
	 * 
	 * @param project
	 *            The project, <code>IProject</code> instance for which all the
	 *            query ids should be found.
	 * @param technicalMap
	 * @return A HashMap object of all the Query Ids in the project.
	 */
	/**
	 * This utility method helps to find all the query ids for a project.
	 * 
	 * @param project
	 *            The project, <code>IProject</code> instance for which all the
	 *            query ids should be found.
	 * @return A HashMap object of all the Query Ids in the project.
	 */
	public HashMap collectAllQueryIdsForBuilder(IProject project) {
		// every time return new map
		if (queryIds != null)
			queryIds.put(project.getName(), null);

		if (queryParseResult != null)
			queryParseResult.put(project.getName(), null);

		// make duplicates also as null;
		if (this.duplicateIds != null)
			this.duplicateIds.put(project.getName(), null);
		// Clear all the mapping classes in the
		// mappingClassMap.

		this.mappingClassMap = new HashMap();

		AnyframeJarLoader loader = new AnyframeJarLoader();
		HashMap technicalMap = loader
				.getRuntimeProjectTechnicalServicesDetails(project);
		FilesParser filesParser = new FilesParser();
		filesParser
				.parseFiles(project, new NullProgressMonitor(), technicalMap);

		HashMap allQueryIdsPerFile = filesParser.getQueryIdsPerFile();
		HashMap allQueryIds = filesParser.getAllQueryIds();
		HashMap daoQueryIds = filesParser.getDaoQueryIds();
		usedQueryIDsSet = filesParser.getDaoQueryIdsSet();
		usedQueryIDsMap.put(project.getName(), usedQueryIDsSet);
		usedQueryIDsLocationMap.put(project.getName(),
				filesParser.getUsedQueryIDLocation());
		duplAliasIdsMap.put(project, filesParser.getDuplAliasIdsMap());

		queryParseResult.put(project.getName(), allQueryIdsPerFile);

		queryIds.put(project.getName(), allQueryIds);
		allDAOQueryIds.put(project.getName(), daoQueryIds);
		allQueryMap.putAll(allQueryIds);
		HashMap returnQryIds = allQueryIds;
		if (this.duplicateIds != null
				&& this.duplicateIds.get(project.getName()) != null) {
			returnQryIds.putAll((HashMap) this.duplicateIds.get(project
					.getName()));
		}
		QMMarkerHelper markerHelper = new QMMarkerHelper();
		markerHelper.markUnUsed(project, usedQueryIDsSet, returnQryIds,
				allDAOQueryIds, new NullProgressMonitor(), true,
				QMMarkerHelper.BUILDER_ERROR_MARKER_ID);
		return allQueryIdsPerFile;
	}

	/**
	 * Utility method to mark problems in DAO for Invalid query ids.
	 * 
	 * @param msg
	 *            message that has to be shown in the error marker.
	 * @param loc
	 *            location object of the marker.
	 * @param violation
	 *            integer
	 * @param isError
	 *            boolean representing whether it is an error or not.
	 * @param markerId
	 * @return true if the marking is successful, false otherwise
	 */
	public boolean markAProblemInDAO(String msg, Location loc, int violation,
			boolean isError, String markerId) {
		try {
			if (loc.file.exists()) {
				IMarker marker = loc.file.createMarker(markerId);
				marker.setAttribute(IMarker.MESSAGE, msg);
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				marker.setAttribute(IMarker.CHAR_START, loc.charStart + 1);
				marker.setAttribute(IMarker.CHAR_END, loc.charEnd);
				// marker.setAttribute(IMarker.LINE_NUMBER,
				// 5);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			// LOGGER.error(" Problem creating a marker .... " + e);
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Problem creating a marker", e);
			return false;

		}
	}

	/**
	 * Utility method to mark problems in resources for duplciate query ids.
	 * 
	 * @param msg
	 *            message that has to be shown in the error marker.
	 * @param loc
	 *            location object of the marker.
	 * @param violation
	 *            integer
	 * @param isError
	 *            boolean representing whether it is an error or not.
	 * @param markerId
	 * @return true if the marking is successful, false otherwise
	 */
	public boolean markAProblem(String msg, Location loc, int violation,
			boolean isError, String markerId) {
		if (loc.key == null || loc.key.equalsIgnoreCase(""))
			return false;
		try {
			if (loc.file.exists()) {
				IMarker marker = loc.file.createMarker(markerId);
				marker.setAttribute(IMarker.MESSAGE, msg + ": " + loc.key);
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				marker.setAttribute(IMarker.CHAR_START, loc.charStart);
				marker.setAttribute(IMarker.CHAR_END, loc.charEnd);
				// marker.setAttribute(IMarker.LINE_NUMBER,
				// 5);
				marker.setAttribute(IMarker.SEVERITY,
						isError ? IMarker.SEVERITY_ERROR
								: IMarker.SEVERITY_WARNING);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			// LOGGER.error(" Problem creating a marker .... " + e);
			return false;
		}
	}

	public HashMap collectAllQueryIdsForProject(IProject project) {
		IProgressMonitor monitor = new NullProgressMonitor();
		monitor.subTask("Analyzing the project cached information.....");
		HashMap returnQryIds = null;
		HashMap allQueryIds = (HashMap) queryIds.get(project.getName());
		HashMap daoQueryIds = (HashMap) allDAOQueryIds.get(project.getName());
		monitor.subTask("Getting the cached information if exist.....");
		if (allQueryIds != null && allQueryIds.size() > 0) {
			monitor.subTask("Retrieved " + allQueryIds.size()
					+ " queries from the cache.");

			returnQryIds = allQueryIds;
			if (this.duplicateIds != null
					&& this.duplicateIds.get(project.getName()) != null) {
				returnQryIds.putAll((HashMap) this.duplicateIds.get(project
						.getName()));
			}
			// clearMarkers(project);
			// createMarkers(project, monitor, returnQryIds);
			return returnQryIds;
		} else {
			AnyframeJarLoader loader = new AnyframeJarLoader();
			HashMap technicalMap = loader
					.getRuntimeProjectTechnicalServicesDetails(project);
			FilesParser filesParser = new FilesParser();
			filesParser.parseFiles(project, monitor, technicalMap);

			allQueryIds = filesParser.getAllQueryIds();
			daoQueryIds = filesParser.getDaoQueryIds();

			queryIds.put(project.getName(), allQueryIds);
			allDAOQueryIds.put(project.getName(), daoQueryIds);
			returnQryIds = allQueryIds;
			if (this.duplicateIds != null
					&& this.duplicateIds.get(project.getName()) != null) {
				returnQryIds.putAll((HashMap) this.duplicateIds.get(project
						.getName()));
			}
			// clearMarkers(project);
			// createMarkers(project, monitor, returnQryIds);
			return returnQryIds;
		}
	}

	/**
	 * This Utility method helps to remove all the query Ids from this Singleton
	 * Instance when the project is disabled from DB IO Nature.
	 * 
	 * @param project
	 *            The project, <code>IProject</code> instance for which DB IO
	 *            nature is disabled.
	 * @return true if
	 */
	public boolean removeAllQueryIds(IProject project) {
		queryIds.put(project.getName(), null);
		return true;
	}

	protected BuilderHelper() {
		queryIds = new HashMap();
		queryParseResult = new HashMap();
	}

	/**
	 * This method helps to get the reference of Singleton Object.
	 * 
	 * @return The Singleton object of BuilderHelper Class.
	 */
	public static BuilderHelper getInstance() {
		return builderHelper;
	}

	public HashMap getAllDAOQueryIds() {
		return allDAOQueryIds;
	}

	public void setAllDAOQueryIds(HashMap allDAOQueryIds) {
		this.allDAOQueryIds = allDAOQueryIds;
	}

	public HashMap getQueryIds() {
		return queryIds;
	}

	public void setQueryIds(HashMap queryIds) {
		this.queryIds = queryIds;
	}

	public HashMap getMappingClassMap() {
		return mappingClassMap;
	}

	public Set getUsedQueryIDsSet() {
		return usedQueryIDsSet;
	}

	public HashMap getUsedQueryIDsMap() {
		return usedQueryIDsMap;
	}

	public HashMap getUsedQueryIDsLocationMap() {
		return usedQueryIDsLocationMap;
	}

	public HashMap getDupllicateIds() {
		return duplicateIds;
	}

	public void markInvalidClassNames(IProject project) {
		QMMarkerHelper markerHelper = new QMMarkerHelper();
		markerHelper.markInvalidClassNames(project,
				this.mappingClassMap.get(project.getName()));
	}
}
