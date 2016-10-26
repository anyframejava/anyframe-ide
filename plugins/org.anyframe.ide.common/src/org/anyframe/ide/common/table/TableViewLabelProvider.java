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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * This is TableViewLabelProvider class.
 * 
 * @author Sujeong Lee
 */
public class TableViewLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public TableViewLabelProvider(Object element) {
		fields = element.getClass().getDeclaredFields();
	}

	public TableViewLabelProvider() {
	}

	private Field[] fields;

	private int getColumnIndex(int columnIndex) {
		return fields.length > columnIndex ? columnIndex : fields.length;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {

		Object obj;
		int idx = getColumnIndex(columnIndex);

		String fieldName = fields[idx].getName().substring(0, 1).toUpperCase()
				+ fields[idx].getName().substring(1,
						fields[idx].getName().length());

		Method method;
		try {
			method = element.getClass().getMethod("get" + fieldName,
					new Class[] {});
			obj = method.invoke(element, new Object[] {});
			return String.valueOf(obj);
		} catch (Exception e) {
			// e.printStackTrace();
			PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, e.getMessage());
		} 
		return "";
	}

}
