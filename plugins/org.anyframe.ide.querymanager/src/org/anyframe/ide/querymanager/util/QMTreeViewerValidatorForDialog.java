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
package org.anyframe.ide.querymanager.util;

import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * This is QMTreeViewerValidatorForDialog class.
 * 
 * @author Junghwan Hong
 */
public class QMTreeViewerValidatorForDialog implements
		ISelectionStatusValidator {

	// private static final Log LOGGER =
	// LogFactory.getLog(QMTreeViewFilterForDialog.class);

	IStatus okStatus = new Status(IStatus.OK, QueryManagerActivator.PLUGIN_ID,
			IStatus.OK, "", null);
	IStatus errorStatus = new Status(IStatus.ERROR,
			QueryManagerActivator.PLUGIN_ID, IStatus.ERROR, "", null);

	private Class[] fAcceptedTypes = new Class[] { IPackageFragmentRoot.class };;
	private boolean allowMultipleSelection = false;

	public boolean isSelectedValid(Object element) {
		try {
			if (element instanceof IJavaProject) {
				IJavaProject jproject = (IJavaProject) element;
				// if(!(project.getFullPath().equals(jproject.getProject().getFullPath())))
				// return false;

				IPath path = jproject.getProject().getFullPath();
				return (jproject.findPackageFragmentRoot(path) != null);
			} else if (element instanceof IPackageFragmentRoot) {
				return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
			}
			return true;
		} catch (JavaModelException e) {
			// LOGGER.error(e.getMessage());

		}
		return false;
	}

	public IStatus validate(Object[] elements) {

		if (isValid(elements)) {
			return okStatus;
		}
		return errorStatus;

	}

	private boolean isValid(Object[] selection) {
		if (selection.length == 0) {
			return false;
		}

		if (!allowMultipleSelection && selection.length != 1) {
			return false;
		}

		for (int i = 0; i < selection.length; i++) {
			Object o = selection[i];
			if (!isOfAcceptedType(o)) {
				return false;
			}
		}
		return true;
	}

	private boolean isOfAcceptedType(Object o) {
		for (int i = 0; i < fAcceptedTypes.length; i++) {
			if (fAcceptedTypes[i].isInstance(o)) {
				return true;
			}
		}
		return false;
	}

}
