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
package org.anyframe.ide.querymanager.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.build.AnyframeNature;
import org.anyframe.ide.querymanager.build.BuilderHelper;
import org.anyframe.ide.querymanager.build.QMMarkerHelper;
import org.anyframe.ide.querymanager.messages.Message;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This class helps to remove the Query Manager Nature from a Project in
 * Eclipse.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class DisableNatureAction implements IObjectActionDelegate {
	// logger
	// private static final Log LOGGER =
	// LogFactory.getLog(DisableNatureAction.class);

	private ISelection iselection;

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 * @param action
	 *            the IAction object
	 */
	public void run(IAction action) {
		// LOGGER.debug("Start disabling.....");
		if (iselection instanceof IStructuredSelection) {
			IStructuredSelection stSel = (IStructuredSelection) iselection;
			for (Iterator it = stSel.iterator(); it.hasNext();) {
				Object element = it.next();
				IProject pjt = null;
				if (element instanceof IProject) {
					pjt = (IProject) element;
				} else if (element instanceof IAdaptable) {
					pjt = (IProject) ((IAdaptable) element)
							.getAdapter(IProject.class);
				}
				if (pjt != null) {
					// LOGGER.debug(" calling disable nature,....");
					disableNature(pjt, stSel.size() == 1);
					// QueryManagerPlugin.getDefault().getQMExplorerView(false).refresh();
				}
			}
		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction iaction, ISelection iselection) {
		this.iselection = iselection;
	}

	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction iaction, IWorkbenchPart itargetPart) {
	}

	/**
	 * This utility method helps to remove the Nature from the project. It also
	 * helps to remove all the error markers if present from the project.
	 * 
	 * @param project
	 *            The <code>IProject</code> instance of the project for which
	 *            the nature has to be disabled.
	 * @param isSingle
	 *            boolean
	 */
	private void disableNature(IProject project, boolean isSingle) {
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			// LOGGER.debug(" Natures before removing..." + natures.length);
			ArrayList newNatures = new ArrayList();
			for (int i = 0; i < natures.length; ++i) {
				if (!AnyframeNature.NATURERID.equals(natures[i])) {
					newNatures.add(natures[i]);
				}
			}
			description.setNatureIds((String[]) newNatures
					.toArray(new String[newNatures.size()]));
			project.setDescription(description, null);
			// LOGGER.debug(" Natures after removing..."
			// + project.getDescription().getNatureIds().length);
			project.deleteMarkers(QMMarkerHelper.BUILDER_ERROR_MARKER_ID,
					false, IResource.DEPTH_INFINITE);
			project.deleteMarkers(
					QMMarkerHelper.QUERYNAV_DBLCLK_BOOK_MARKER_ID, false,
					IResource.DEPTH_INFINITE);
			project.deleteMarkers(QMMarkerHelper.SEARCH_UNUSED_MARKER_ID,
					false, IResource.DEPTH_INFINITE);
			HashMap dups = (HashMap) BuilderHelper.getInstance().duplicateIds
					.get(project.getName());
			// LOGGER.debug(" Dups from disabling b4 ...." + dups);
			BuilderHelper.getInstance().duplicateIds.put(project.getName(),
					null);
			BuilderHelper.getInstance().removeAllQueryIds(project);
			// LOGGER.debug(" Dups from disabling after ...."
			// + BuilderHelper.getInstance().duplicateIds.get(project
			// .getName()));
		} catch (CoreException ex) {
			// LOGGER.error("Can't disable nature " + ex.toString());
			// LOGGER.error(ex);
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					Message.exception_disablenatureaction, ex);
		}
	}

}
