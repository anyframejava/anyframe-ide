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
package org.anyframe.ide.querymanager.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.build.AnyframeNature;
import org.anyframe.ide.querymanager.build.BuilderHelper;
import org.anyframe.ide.querymanager.build.Location;
import org.anyframe.ide.querymanager.build.QMMarkerHelper;
import org.anyframe.ide.querymanager.properties.QMPropertiesXMLUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This class helps to add the Query Manager Nature for a Project in Eclipse.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class EnableNatureAction implements IObjectActionDelegate {
	// logger
	// private static final Log LOGGER =
	// LogFactory.getLog(EnableNatureAction.class);

	private ISelection iselection;

	/**
	 * This is a call back method which will be called when "Enable Nature" is
	 * selected from the project context menu.
	 */
	public void run(IAction action) {
		if (iselection instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) iselection;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object element = it.next();
				IProject anyFrameProject = null;
				if (element instanceof IProject) {
					anyFrameProject = (IProject) element;
				} else if (element instanceof IAdaptable) {
					anyFrameProject = (IProject) ((IAdaptable) element)
							.getAdapter(IProject.class);
				}
				if (anyFrameProject != null) {
					enableNature(anyFrameProject);
					// QueryManagerPlugin.getDefault().getQMExplorerView(false).refresh();
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection iselection) {
		this.iselection = iselection;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * This method helps to add the Nature for the project. It also helps to
	 * collect all the error markers if present for the project.
	 * 
	 * @param anyframeProject
	 *            The <code>IProject</code> instance of the project for which
	 *            the nature has to be enabled.
	 */
	private void enableNature(IProject anyframeProject) {
		try {
			if (!anyframeProject.isOpen())
				return;
			// Get the Builder instance.
			BuilderHelper builder = BuilderHelper.getInstance();
			// LOGGER.debug("From EnableNatureAction ====== ");
			ArrayList queryMgrNatures = new ArrayList();
			// add java nature
			queryMgrNatures.add(JavaCore.NATURE_ID);
			// add Anyframe Nature.
			queryMgrNatures.add(AnyframeNature.NATURERID);

			IProjectDescription desc = anyframeProject.getDescription();
			String[] oldNatures = desc.getNatureIds();
			for (int i = 0; i < oldNatures.length; ++i) {
				String oldId = oldNatures[i];
				if (!AnyframeNature.NATURERID.equals(oldId)
						&& !JavaCore.NATURE_ID.equals(oldNatures[i])) {
					queryMgrNatures.add(oldNatures[i]);
				}
			}
			desc.setNatureIds((String[]) queryMgrNatures
					.toArray(new String[queryMgrNatures.size()]));
			anyframeProject.setDescription(desc, null);

			QMPropertiesXMLUtil util = new QMPropertiesXMLUtil();
			if (util.getPropertiesDupl2(anyframeProject)) {
				builder.collectAllQueryIdsForBuilder(anyframeProject);
				HashMap duplicateIds = new HashMap();
				if (builder.duplicateIds.containsKey(anyframeProject.getName())) {
					duplicateIds = (HashMap) builder.duplicateIds
							.get(anyframeProject.getName());
				}
				// If dupicate Ids found, mark it.
				if (duplicateIds != null && duplicateIds.size() > 0) {
					Iterator duplicatesItr = duplicateIds.keySet().iterator();
					while (duplicatesItr.hasNext()) {
						String queryId = (String) duplicatesItr.next();
						Collection col = (Collection) duplicateIds.get(queryId);
						if (col != null && col.size() > 0) {
							Iterator colItr = col.iterator();
							while (colItr.hasNext()) {
								Location loc = (Location) colItr.next();
								if (!builder.markAProblem(
										"Repitition of Query Id",
										(Location) loc, 1, true,
										QMMarkerHelper.BUILDER_ERROR_MARKER_ID)) {
									// if markAProblem
									// returns false, file
									// may not
									// be
									// existing......remove
									// it from
									// duplicateIds.
									// colItr.remove();
								} else {
								}
							}
						}
					}
					builder.markInvalidClassNames(anyframeProject);
				} else {
				}
			}
			// Now Mark the invalid mapping class
			// names in all the XML Files.
		} catch (CoreException ex) {
			// LOGGER
			// .error("could not enable Anyframe Nature :" + ex.getMessage());
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"could not enable Anyframe Nature", ex);

		}
	}

	/**
	 * This method helps us to find whether a project is enabled with Anyframe
	 * Nature.
	 * 
	 * @param anyframeProject
	 *            the project to check for Anyframe Nature.
	 * @return true if Anyframe Nature is enabled, false otherwise.
	 */
	public static boolean hasQueryMgrNature(IProject anyframeProject) {
		IProjectDescription desc = null;
		try {
			desc = anyframeProject.getDescription();
		} catch (CoreException e) {
			return false;
		}
		if (desc == null)
			return false;
		String[] natures = desc.getNatureIds();
		for (int i = 0; i < natures.length; ++i) {
			String id = natures[i];
			// Find whether the nature id matches with
			// Anyframe Nature.
			// if matches return true.
			if (AnyframeNature.NATURERID.equals(id)) {
				return true;
			}
		}
		// oops... no anyframe nature. return false.
		return false;
	}
}
