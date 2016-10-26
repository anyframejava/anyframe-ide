/*
 * Copyright 2002-2012 the original author or authors.
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
package org.anyframe.ide.common.table;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * This is TableViewContentProvider class.
 * 
 * @author Sujeong Lee
 */
public class TableViewContentProvider implements IStructuredContentProvider {

	public TableViewContentProvider() {
		// TODO Auto-generated constructor stub
	}

	public Object[] getElements(Object obj) {
		if (obj instanceof List) {
			return ((List) obj).toArray();
		} else {
			return new Object[0];
		}
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object obj, Object obj1) {
		// TODO Auto-generated method stub

	}

}
