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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.properties.QMPropertiesXMLUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This is a builder class which helps in Building the project when the Query
 * Manager Nature is Enabled for the project.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class AnyframeProjectBuilder extends IncrementalProjectBuilder {
	// private static final Log LOGGER = LogFactory.getLog(AnyframeProjectBuilder.class);

	public static final String BUILDERID = "org.anyframe.ide.querymanager.anyframeProjectBuilder";

	// public static final String MARKER_ID = "anyframe.querymanager.eclipse.core.quryMgrMarker";

	/**
	 * Defaukt constructor.
	 */
	public AnyframeProjectBuilder() {
	}

	/**
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param kind
	 *            integer
	 * @param args
	 *            map of arguments.
	 * @param monitor
	 *            IProgressMonitor object
	 * @throws CoreException
	 *             when something goes wrong.
	 * @return IProject [] array of IProject objects.
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		final IProject project = getProject();
		try {
			project.deleteMarkers(QMMarkerHelper.BUILDER_ERROR_MARKER_ID,
					false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// LOGGER.error("Error while removing existing markers for project "
			// + project.getName() + "  ", e);
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Error while removing existing markers for project "
							+ project.getName() + "  ", e);
		}
		if (kind == AUTO_BUILD || kind == INCREMENTAL_BUILD) {
			BuilderHelper builderHelper = BuilderHelper.getInstance();
			IResourceDelta delta = super.getDelta(project);
			if (delta != null && project.isOpen()) {

				// Collect all the Query Ids for the project and also update the cache.
				QMPropertiesXMLUtil util = new QMPropertiesXMLUtil();
				if (util.getPropertiesDupl2(project)) {

					builderHelper.collectAllQueryIdsForBuilder(project);
					// If the project do not contain the AnyFrame Nature.......
					// Return as no need to mark the duplicates and invalid
					// class references in XMLs.

					if (project.getNature(AnyframeNature.NATURERID) == null)
						return null;

					// If the project contains AnyFrame Nature.......
					// proceed and mark duplicates and invalid class
					// references....

					HashMap duplicateIds = new HashMap();
					if (builderHelper.duplicateIds.containsKey(project
							.getName())) {
						duplicateIds = (HashMap) builderHelper.duplicateIds
								.get(project.getName());
					}
					if (duplicateIds != null && duplicateIds.size() > 0) {
						Iterator duplicatesItr = duplicateIds.keySet()
								.iterator();
						while (duplicatesItr.hasNext()) {
							String queryId = (String) duplicatesItr.next();
							Collection col = (Collection) duplicateIds
									.get(queryId);
							if (col != null && col.size() > 0) {
								Iterator colItr = col.iterator();
								while (colItr.hasNext()) {
									Location loc = (Location) colItr.next();
									if (!builderHelper
											.markAProblem(
													"Repitition of Query Id",
													(Location) loc,
													1,
													true,
													QMMarkerHelper.BUILDER_ERROR_MARKER_ID)) {
										// if markAProblem
										// returns false,
										// file may
										// not
										// be
										// existing......remove
										// it from
										// duplicateIds.
										// colItr.remove();
									}
								}
							}
						}
						builderHelper.markInvalidClassNames(project);
					}
				} else {
				}

				// Now Mark the invalid mapping class
				// names in all the XML Files.
			}
		}
		return null;
	}
}
