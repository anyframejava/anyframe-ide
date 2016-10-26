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
package org.anyframe.ide.codegenerator.model.table;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * This is a PluginInfoContentProvider class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class PluginInfoContentProvider implements ITreeContentProvider {
	TreeViewer viewer;

	public Object[] getElements(Object pluginInfoList) {
		return ((PluginInfoList) pluginInfoList).getPluginInfoList().values()
				.toArray();
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer) viewer;
	}

	public Object[] getChildren(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getParent(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
