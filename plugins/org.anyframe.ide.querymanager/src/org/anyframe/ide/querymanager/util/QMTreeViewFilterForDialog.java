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

import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filter for Tree Viewer
 * 
 * @author Junghwan Hong
 */
public class QMTreeViewFilterForDialog extends ViewerFilter {
	// private static final Log LOGGER = LogFactory.getLog(QMTreeViewFilterForDialog.class);

	public boolean select(Viewer arg0, Object arg1, Object element) {
		if (element instanceof IPackageFragmentRoot) {
			try {
				return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
			} catch (JavaModelException e) {
				// LOGGER.error(e.getMessage());
				return false;
			}
		}

		Class[] classesToDisplay = new Class[] { IJavaModel.class,
				IPackageFragmentRoot.class, IJavaProject.class };

		for (int i = 0; i < classesToDisplay.length; i++) {
			if (classesToDisplay[i].isInstance(element)) {
				return true;
			}
		}
		return false;
	}

}
