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
package org.anyframe.ide.querymanager.model;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for Query Explorer view.
 *
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class QueryExplorerViewContentProvider implements
		IStructuredContentProvider, ITreeContentProvider {

	String projectName = "";
	String fileName = "";

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {

		return getChildren(parent);
	}

	public Object getParent(Object child) {
		return null;
	}

	public Object[] getChildren(Object parent) {

		if (parent instanceof ArrayList) {
			return ((ArrayList) parent).toArray();
		} else if (parent instanceof QueryTreeVO) {
			projectName = ((QueryTreeVO) parent).getProject();
			return ((QueryTreeVO) parent).getFileList().toArray();
		} else if (parent instanceof FileInfoVO) {
			return ((FileInfoVO) parent).getQueryId().keySet().toArray();
		} else {
			return new Object[0];
		}
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof QueryTreeVO)
			return true;
		if (parent instanceof FileInfoVO)
			return true;
		return false;
	}
}
