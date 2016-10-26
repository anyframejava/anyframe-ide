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
package org.anyframe.ide.querymanager.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.anyframe.ide.querymanager.actions.EnableNatureAction;
import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.parsefile.ParserHelper;
import org.anyframe.ide.querymanager.properties.QMPropertiesXMLUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This is QMMarkerHelper class.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 * @author Tulasi.m
 */
public class QMMarkerHelper {
	// private static final Log LOGGER = LogFactory.getLog(QMMarkerHelper.class);
	public boolean isReturned;
	public static final String BUILDER_ERROR_MARKER_ID = "org.anyframe.ide.querymanager.builderErrorMarker";
	public static final String SEARCH_UNUSED_MARKER_ID = "org.anyframe.ide.querymanager.searchUnUsedMarker";
	public static final String QUERYNAV_DBLCLK_BOOK_MARKER_ID = "org.anyframe.ide.querymanager.queryNavigatorDblClickBookMarker";

	public void markUnUsed(IProject project, Set usedQueryId, HashMap queryIds,
			HashMap allDAOQueryIds, IProgressMonitor monitor, boolean mark,
			String markerId) {
		if (mark && monitor.isCanceled()) {
			monitor.done();
			return;
		}
		try {
			if (!project.isOpen()
					|| !project
							.hasNature("org.anyframe.ide.querymanager.build.AnyframeNature"))
				return;
		} catch (CoreException e) {
			return;
		}
		// if(mark){QueryMgrMarkerHelper.SEARCH_UNUSED_MARKER_ID
		try {
			project.deleteMarkers(markerId, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// Do not do anything.
		}
		// If this method is being called from Builder,
		// and as Builder is also responsible for
		// marking warnings,
		// we have to clean the warnings also and then
		// create.
		// so delete warning markers in Query Manager.
		if (markerId.equalsIgnoreCase(QMMarkerHelper.BUILDER_ERROR_MARKER_ID)) {
			try {
				project.deleteMarkers(QMMarkerHelper.SEARCH_UNUSED_MARKER_ID,
						false, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				// Do not do anything.
			}
		}
		// }
		// project.deleteMarkers(AnyframeProjectBuilder.MARKER_ID,
		// arg1, arg2)
		// for project "project", compare xml query ids
		// and dao query ids
		// and mark unused query ids as "UnUsed" using
		// Location object, for the purpose of use in Query Manager View.

		// get query ids for project
		BuilderHelper builderHelper = BuilderHelper.getInstance();
		HashMap allQueryIds = queryIds;

		// get dao query ids for project

		HashMap daoQueryIds = (HashMap) allDAOQueryIds.get(project.getName());

		Set tempSet = usedQueryId;

		// iterate through allQueryIds.
		Iterator allQueryIdsItr = null;
		// if(allQueryIds!=null && allQueryIds.size() >
		// 0)
		if (allQueryIds == null) {
			return;
		}
		allQueryIdsItr = allQueryIds.keySet().iterator();
		// while (allQueryIdsItr.hasNext() && !monitor.isCanceled()) {
		while (allQueryIdsItr.hasNext()) {
			if (mark && monitor.isCanceled()) {
				monitor.done();
				isReturned = true;
				break;
			}
			String queryId = allQueryIdsItr.next().toString();
			boolean used = false;
			if (tempSet.contains(queryId))
				used = true;
			else
				used = false;
			// based on flag used, mark all the query
			// ids with the same name or unique query id.
			Object queryIdContent = allQueryIds.get(queryId);
			QMPropertiesXMLUtil util = new QMPropertiesXMLUtil();

			if (queryIdContent instanceof ArrayList) {
				Collection col = (ArrayList) queryIdContent;
				Iterator colItr = col.iterator();
				while (colItr.hasNext()) {
					if (mark && monitor.isCanceled()) {
						monitor.done();
						isReturned = true;
						break;
					}
					Location loc = (Location) colItr.next();
					if (used && loc.getKey().length() > 0)
						loc.setUsed("Used");
					else {
						if (util.getPropertiesUsed2(loc.getFile())) {
							loc.setUsed("UnUsed");
							if (mark)
								builderHelper
										.markAProblem(
												"Query Id \""
														+ loc.key
														+ Message.build_queryidisnotused_message
														+ project.getName(),
												(Location) loc,
												1,
												false,
												QMMarkerHelper.SEARCH_UNUSED_MARKER_ID);
						} else {
							loc.setUsed("Used");
						}

					}
				}
			} else {
				if (mark && monitor.isCanceled()) {
					monitor.done();
					isReturned = true;
					break;
				}
				Location loc = (Location) queryIdContent;
				if (used && loc.getKey().length() > 0)
					loc.setUsed("Used");
				else {

					if (util.getPropertiesUsed2(loc.getFile())) {
						loc.setUsed("UnUsed");

						if (mark && loc.getUsed() == "UnUsed") {
							if (monitor.isCanceled()) {
								monitor.done();
								break;
							}

							monitor.subTask(Message.build_createmarkersfor 
									+ loc.getFile().getLocation());
							builderHelper
									.markAProblem(
											"Query Id \""
													+ loc.key
													+ Message.build_queryidisnotused_message
													+ project.getName(),
											(Location) loc,
											1,
											false,
											QMMarkerHelper.SEARCH_UNUSED_MARKER_ID);
							monitor.worked(10);
						}
					} else {
						loc.setUsed("Used");
					}
				}
			}
		}
		if (monitor.isCanceled()) {
			monitor.done();
			isReturned = true;
			return;
		}
		// mark invalid queries used in daos.
		// should be marked only when "AnyFrame Nature"
		// is added to this project.
		if (EnableNatureAction.hasQueryMgrNature(project)) {
			if (monitor.isCanceled()) {
				monitor.done();
				isReturned = true;
				return;
			}
			boolean valid = false;
			HashMap dupQueryIds = (HashMap) builderHelper.duplicateIds
					.get(project.getName());
			if (dupQueryIds == null)
				dupQueryIds = new HashMap();
			String eqsMethodArr[] = new String[] { "create", "update",
					"findList", "findByPk", "remove", "find" };
			// iterate through allQueryIds.
			Iterator allDaoQueryIdsItr = null;
			if (daoQueryIds != null && daoQueryIds.size() > 0) {
				allDaoQueryIdsItr = daoQueryIds.keySet().iterator();
				whileLoop: while (allDaoQueryIdsItr.hasNext()) {
					if (monitor.isCanceled()) {
						monitor.done();
						return;
					}

					String allDaoQueryId = allDaoQueryIdsItr.next().toString();
					valid = false;
					for (int i = 0; i < eqsMethodArr.length; i++) {
						if (monitor.isCanceled()) {
							monitor.done();
							return;
						}
						if (allDaoQueryId.equalsIgnoreCase(eqsMethodArr[i])
								|| allDaoQueryId.equalsIgnoreCase("")) {
							markErrorInDAO(daoQueryIds, allDaoQueryId, valid,
									markerId, true);
							continue whileLoop;
						}

					}
					if (allQueryIds.containsKey(allDaoQueryId)
							|| dupQueryIds.containsKey(allDaoQueryId))
						valid = true;
					else
						valid = false;
					if (valid)
						continue;
					if (monitor.isCanceled()) {
						monitor.done();
						return;
					}
					markErrorInDAO(daoQueryIds, allDaoQueryId, valid, markerId,
							false);
				}
			}
		}
	}

	private void markErrorInDAO(HashMap daoQueryIds, String allDaoQueryId,
			boolean valid, String markerId, boolean notDefined) {
		Object queryIdContent = daoQueryIds.get(allDaoQueryId);
		if (queryIdContent instanceof ArrayList) {
			Collection col = (ArrayList) queryIdContent;
			Iterator colItr = col.iterator();
			while (colItr.hasNext()) {
				Location loc = (Location) colItr.next();
				if (!valid) {
					if (notDefined)
						BuilderHelper
								.getInstance()
								.markAProblemInDAO(
										Message.exception_queryidisnotdefined,
										(Location) loc, 1, true, markerId);
					else
						BuilderHelper
								.getInstance()
								.markAProblemInDAO(
										"Query Id \""
												+ loc.key
												+ Message.build_queryidisnotexistinxmlfile_message,
										(Location) loc, 1, true, markerId);
				}
			}
		} else {
			Location loc = (Location) queryIdContent;
			if (!valid) {
				if (notDefined)
					BuilderHelper
							.getInstance()
							.markAProblemInDAO(
									Message.exception_queryidisnotdefined,
									(Location) loc, 1, true, markerId);
				else
					BuilderHelper
							.getInstance()
							.markAProblemInDAO(
									"Query Id \""
											+ loc.key
											+ Message.build_queryidisnotexistinxmlfile_message,
									(Location) loc, 1, true, markerId);
			}
		}
	}

	public void markInvalidClassNames(IProject project, Object object) {
		// 1. Get the class Names used in Mappings
		Collection mappingClassNames = (Collection) object;
		// 2. Get the classes from project
		ParserHelper parserHelper = ParserHelper.getInstance();
		Collection allClasses = getAllClasses(project, parserHelper);
		// 3. Compare and identify all the invalid classes
		if (mappingClassNames != null) {
			Iterator mappingClassNamesItr = mappingClassNames.iterator();
			while (mappingClassNamesItr.hasNext()) {
				Location loc = (Location) mappingClassNamesItr.next();
				String mappingClassName = loc.getKey();
				Iterator allClassesItr = allClasses.iterator();
				boolean found = false;
				innerLoop: while (allClassesItr.hasNext()) {
					String className = allClassesItr.next().toString();
					if (className.indexOf(mappingClassName) > -1) {
						// 4.mark them.
						found = true;
						break innerLoop;
					}
				}
				if (!found)
					BuilderHelper
							.getInstance()
							.markAProblem(
									Message.exception_resultclassisnotset,
									loc, 1, false,
									QMMarkerHelper.BUILDER_ERROR_MARKER_ID);
			}
		}
	}

	private Collection getAllClasses(IProject project,
			ParserHelper builderHelper) {
		Collection allClasses = (Collection) builderHelper.getAllFileInfo(
				project, project.getLocation());
		return allClasses;
	}

}
