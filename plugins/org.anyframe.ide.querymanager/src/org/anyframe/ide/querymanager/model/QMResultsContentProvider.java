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
import org.eclipse.jface.viewers.Viewer;

/**
 * This is QMResultsContentProvider class.
 * 
 * @author Junghwan Hong
 */
public class QMResultsContentProvider implements IStructuredContentProvider {

	public Object[] getElements(Object inputElement) {
		ArrayList list = (ArrayList) inputElement;
//		int columnCount = 0;
//		ResultSet resultSet = (ResultSet) inputElement;
//		ResultSetMetaData metaData = null;
//		int counter = 1;
//		try {
//			metaData = resultSet.getMetaData();
//			columnCount = metaData.getColumnCount();
//			while(resultSet.next()){
//				ArrayList rowList = new ArrayList();
//				for(int i = 1; i <= columnCount; i++)
//					rowList.add(resultSet.getString(i));
//				list.add(rowList);
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
		return list.toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
