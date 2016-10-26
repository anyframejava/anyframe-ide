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
package org.anyframe.ide.querymanager.dialogs;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Tree content provider for Property.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class XMLOnlyContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object element) {

		if (element instanceof ArrayList) {
			return ((ArrayList) element).toArray();
		} else if (element instanceof PropertyAddInfo) {
			return ((PropertyAddInfo) element).getChild().toArray();
		} else {
			return new Object[0];
		}
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ArrayList) {
			return true;
		} else if (element instanceof PropertyAddInfo) {
			return true;
		} else {
			return false;
		}
	}

	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
