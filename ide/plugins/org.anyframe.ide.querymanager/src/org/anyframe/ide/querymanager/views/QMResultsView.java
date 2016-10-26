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
package org.anyframe.ide.querymanager.views;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.StringUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.model.QMDetails;
import org.anyframe.ide.querymanager.model.QMResultsContentProvider;
import org.anyframe.ide.querymanager.model.QMResultsLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

/**
 * This is QMResultsView class
 * 
 * @author Junghwan Hong
 */
public class QMResultsView extends ViewPart {

	public static final String ID = QueryManagerActivator.PLUGIN_ID
			+ ".views.QMResultsView";

	// private static final Log LOGGER =
	// LogFactory.getLog(QMResultsView.class);

	private Composite parent;

//	private Composite childComposite;

	private ResultSet resultSet;

//	private Text searchText;

//	private Button searchButton;

//	private Button modifyButton;

//	private Label querrySearchView;

//	private Action modifyQuerry;

//	private Action doubleClickAction;

//	private Table table;

	private Table messagesTable;

	TabItem tabItem;

	private TabFolder tabFolder;

	public TabFolder getTabFolder() {
		return tabFolder;
	}

	Composite composite;

	TableViewer tableViewer;

	private HashMap queryIds = null;

	public HashMap duplicateIds = new HashMap();

	QMDetails empDetails = new QMDetails();

	Label queryLabel = null;

	Listener sortListener;

	public QMResultsView() {

	}

	public void createPartControl(Composite parent) {
		this.parent = parent;
		createMessageTab();
	}

	private void createMessageTab() {

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		parent.setLayout(layout);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 20;

//		queryLabel = new Label(parent, SWT.NONE);
//		queryLabel.setText("");
//
//		queryLabel.setLayoutData(data);

		tabFolder = new TabFolder(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		GridData data1 = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(data1);
		TabItem messagesTab = new TabItem(tabFolder, SWT.NONE);
		messagesTab.setText(Message.view_explorer_messages);

		messagesTable = new Table(tabFolder, SWT.SINGLE | SWT.BORDER
				| SWT.FULL_SELECTION);
		messagesTab.setControl(messagesTable);
		messagesTable.setLinesVisible(true);
		messagesTable.setHeaderVisible(true);

		TableColumn col = new TableColumn(messagesTable, SWT.NONE);
		col.setText(Message.view_explorer_status);
		col.pack();

		col = new TableColumn(messagesTable, SWT.NONE);
		col.setText(Message.view_explorer_location);
		col.pack();

		col = new TableColumn(messagesTable, SWT.NONE);
		col.setText(Message.view_explorer_sql);
		col.pack();

		col = new TableColumn(messagesTable, SWT.NONE);
		col.setText(Message.view_explorer_text);
		col.pack();

		tabFolder.setSelection(messagesTab);

	}

	public void setFocus() {

	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	public void setResults(ResultSet rs, final String query) {
		this.resultSet = rs;
		ResultSetMetaData metaData = null;
		int columnCount = 0;

		try {
			metaData = this.resultSet.getMetaData();
			columnCount = metaData.getColumnCount();
		} catch (SQLException e) {
			// LOGGER.error(e.getMessage());
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					e.getMessage(), e);
		}

		if (metaData == null)
			return;

		if (!query.equals("")) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					setContentDescription(StringUtil.replace(query.trim(),
							"\r", " "));
				}

			});

			// String label = StringUtil.replace(query.trim(), "\r", " ");
			// queryLabel.setText(StringUtil.replace(label.trim(), "\n", " "));
		}
		if (tabItem != null)
			tabItem.dispose();

		tabItem = new TabItem(tabFolder, SWT.CLOSE);
		tabItem.setText(Message.view_explorer_queryresult);

		Composite composite = new Composite(tabFolder, SWT.NONE);
		tabItem.setControl(composite);

		GridLayout gLayout = new GridLayout();
		gLayout.numColumns = 2;
		gLayout.marginLeft = 0;
		gLayout.horizontalSpacing = 0;
		gLayout.verticalSpacing = 0;
		gLayout.marginWidth = 0;
		gLayout.marginHeight = 0;
		composite.setLayout(gLayout);

		GridData data = new GridData(GridData.FILL_BOTH);

		tableViewer = new TableViewer(composite, SWT.BORDER);

		tableViewer.setContentProvider(new QMResultsContentProvider());
		tableViewer.setLabelProvider(new QMResultsLabelProvider());
		tableViewer.setComparator(new ViewerComparator());

		createListener();

		Table table = tableViewer.getTable();

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(data);

		for (int i = 0; i < columnCount; i++) {

			TableColumn tableColumn = new TableColumn(table, SWT.CENTER);

			tableColumn.setWidth(100);
			tableColumn.addListener(SWT.Selection, sortListener);

			try {
				tableColumn.setText(metaData.getColumnName(i + 1));

			} catch (SQLException e) {

				// LOGGER.error(e.getMessage());
				PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
						e.getMessage(), e);
			}

		}

		int counter = 1;
		ArrayList rowsList = new ArrayList();
		try {
			while (resultSet.next()) {
				ArrayList rowList = new ArrayList();
				for (int i = 1; i <= columnCount; i++)
					rowList.add(resultSet.getString(i));
				rowsList.add(rowList);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		tableViewer.setInput(rowsList);

		tabFolder.setSelection(tabItem);
	}

	public void createListener() {

		sortListener = new Listener() {
			public void handleEvent(Event e) {
				// determine new sort column and direction
				TableColumn sortColumn = tableViewer.getTable().getSortColumn();
				TableColumn currentColumn = (TableColumn) e.widget;
				int dir = tableViewer.getTable().getSortDirection();
				if (sortColumn == currentColumn) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					tableViewer.getTable().setSortColumn(currentColumn);
					dir = SWT.UP;
				}

				TableColumn[] columns = tableViewer.getTable().getColumns();
				int index = 0;
				for (int i = 0; i < columns.length; i++) {
					if (columns[i].equals(currentColumn)) {
						index = i;
						break;
					}
				}
				tableViewer.getTable().setSortDirection(dir);
				tableViewer.setSorter(new QMViewSorter(index, dir));
			}
		};
	}

	class QMViewSorter extends ViewerSorter {

		int dir;
		int columnIndex;

		public QMViewSorter(int index, int dir) {
			this.dir = dir;
			this.columnIndex = index;
		}

		public int compare(Viewer viewer, Object o1, Object o2) {
			ArrayList firstList = (ArrayList) o1;
			ArrayList secondList = (ArrayList) o2;

			String inFirstList = (String) firstList.get(columnIndex);
			String inSecondList = (String) secondList.get(columnIndex);

			if (inFirstList == null) {
				inFirstList = "";
			}
			if (inSecondList == null) {
				inSecondList = "";
			}

			if (this.dir == SWT.DOWN) {

				return collator.compare(inFirstList, inSecondList) * -1;
			}
			return collator.compare(inFirstList, inSecondList);
		}
	}

}
