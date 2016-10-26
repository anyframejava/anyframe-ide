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
package org.anyframe.ide.common.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * This is ComponentUtil class.
 * 
 * @author Sujeong Lee
 */
public class ComponentUtil {

	public static Label addLabel(Composite composite, String text) {
		Label label = new Label(composite, SWT.NULL | SWT.LEFT);
		label.setText(text);
		return label;
	}

	public static Label addLabel(Composite composite, String text,
			int horizontalSpan, int verticalSpan) {
		GridData gData = new GridData();
		gData.horizontalSpan = horizontalSpan;
		gData.verticalSpan = verticalSpan;
		Label label = new Label(composite, SWT.NULL | SWT.LEFT);
		label.setText(text);
		label.setLayoutData(gData);
		return label;
	}

	public static Button addButton(Composite composite, String text) {
		GridData buttonData = new GridData();
		// buttonData.widthHint = 70;

		Button button = new Button(composite, SWT.PUSH | SWT.CENTER);
		button.setText(text);
		button.setLayoutData(buttonData);
		return button;
	}

	public static Button addButton(Composite composite, String text,
			int horizontalSpan) {
		GridData buttonData = new GridData();
		// buttonData.widthHint = 70;
		buttonData.horizontalSpan = horizontalSpan;
		Button button = new Button(composite, SWT.PUSH | SWT.CENTER);
		button.setText(text);
		button.setLayoutData(buttonData);
		return button;
	}

	public static Combo addCombo(Composite composite) {
		GridData gData = new GridData(GridData.FILL_HORIZONTAL);
		Combo combo = new Combo(composite, SWT.SIMPLE | SWT.DROP_DOWN
				| SWT.READ_ONLY);
		combo.setLayoutData(gData);
		return combo;
	}

	public static Combo addCombo(Composite composite, int horizontalSpan) {
		GridData gData = new GridData(GridData.FILL_HORIZONTAL);
		gData.horizontalSpan = horizontalSpan;
		Combo combo = new Combo(composite, SWT.SIMPLE | SWT.DROP_DOWN
				| SWT.READ_ONLY);
		combo.setLayoutData(gData);
		return combo;
	}

	public static Text addText(Composite composite, String text,
			int horizontalSpan) {

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING
				| GridData.FILL_HORIZONTAL);
		data.horizontalSpan = horizontalSpan;

		Text txt = new Text(composite, SWT.LEFT | SWT.BORDER);

		txt.setText(text);
		txt.setLayoutData(data);

		return txt;
	}

	public static Text addText(Composite composite, String text) {
		return addText(composite, text, 1);
	}

	public static void generateTableColumns(Table table, String[] columnNames,
			int defaultWidth, int[] columnWidthes) {
		if (columnWidthes != null && columnNames.length != columnWidthes.length) {
			columnWidthes = null;
		}
		int widthIdx = 0;
		for (String columnName : columnNames) {
			TableColumn tableColumn = new TableColumn(table, SWT.None);
			tableColumn.setText(columnName);
			if (columnWidthes != null) {
				tableColumn.setWidth(columnWidthes[widthIdx]);
			} else {
				tableColumn.setWidth(defaultWidth);
			}
			++widthIdx;
		}
	}

	public static void createLine(Composite composite, int ncol2) {
		Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL
				| SWT.BOLD);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = ncol2;
		line.setLayoutData(gridData);
	}

	// warning : performance is not considered
	@SuppressWarnings("unchecked")
	public static void moveUpTableViewerSelectedItem(
			final TableViewer tableViewer) {
		List listOfItems = (List) tableViewer.getInput();
		Table table = tableViewer.getTable();
		for (int index = 0; index < table.getItemCount(); index++) {
			if (table.isSelected(index)) {
				if (index > 0) {
					Collections.swap(listOfItems, index, index - 1);
				}
			}
		}
		tableViewer.setInput(listOfItems);
		tableViewer.refresh();
	}

	// warning : performance is not considered
	@SuppressWarnings("unchecked")
	public static void moveDownTableViewerSelectedItem(
			final TableViewer tableViewer) {
		List listOfItems = (List) tableViewer.getInput();
		Table table = tableViewer.getTable();

		for (int index = table.getItemCount() - 1; index >= 0; index--) {
			if (table.isSelected(index)) {
				if (index < table.getItemCount() - 1) {
					Collections.swap(listOfItems, index, index + 1);
				}
			}
		}
		tableViewer.setInput(listOfItems);
		tableViewer.refresh();
	}

	public static void deleteTableViewerSelectedItem(TableViewer tableViewer) {
		if (tableViewer.getSelection().isEmpty()) {
			return;
		}
		IStructuredSelection selection = (IStructuredSelection) tableViewer
				.getSelection();
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			Object itemToDelete = iterator.next();
			List input = (List) tableViewer.getInput();
			input.remove(itemToDelete);
			tableViewer.setInput(input);
			tableViewer.refresh();
		}
	}

	/**
	 * @param eventType
	 * @param control
	 */
	public static void removeListeners(int eventType, Control control) {
		Listener[] listeners = control.getListeners(eventType);
		for (Listener listener : listeners) {
			control.removeListener(eventType, listener);
		}
	}

}
