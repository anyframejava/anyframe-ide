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

import org.anyframe.ide.querymanager.build.Location;
import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.model.QueryIdContentProvider;
import org.anyframe.ide.querymanager.model.QueryIdModel;
import org.anyframe.ide.querymanager.model.QueryIdTableContentProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 1. This class is responsible for creating the Dialog on click of the
 * reference column button. 2. Text field to enter the data to be searchd and a
 * button. 3. TableViewer to display the result.
 * 
 * @author Surindhar.Kondoor
 * @author viswa.srikant
 */
public class QueryIdDialog extends Dialog {

	private Composite dialogArea;

	private Table table;

	private TableViewer tableViewer;

	private static final String shelName = Message.dialog_queryidselectiondialog_title;

	private static final String paramName = Message.dialog_queryidhas_message;

	private String projectName;

	private String queryFile;

	private String filePath;

	private String queryId;

	private Location loc;

	public QueryIdDialog(Shell parentShell, String queryId) {

		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.queryId = queryId;

	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		shell.setText(shelName);
		shell.setBounds(500, 200, 450, 450);

	}

	protected Control createDialogArea(Composite parent) {

		dialogArea = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.horizontalSpacing = 20;

		dialogArea.setLayout(layout);

		createTable(dialogArea);

		return dialogArea;

	}

	/**
	 * Creates the table to display the search data.
	 * 
	 * @param composite
	 */
	private void createTable(final Composite composite) {
		Label parameterLabel = new Label(composite, SWT.NULL | SWT.LEFT
				| SWT.WRAP);
		parameterLabel.setText(paramName);

		Label parameterLabel1 = new Label(composite, SWT.NULL | SWT.LEFT);
		parameterLabel1.setText(Message.dialog_queryid + queryId);

		GridData data = new GridData(GridData.FILL, GridData.FILL, true, true,
				2, 10);

		table = new Table(composite, SWT.MULTI | SWT.FULL_SELECTION
				| SWT.BORDER | SWT.V_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setSize(30, 50);
		table.setLayoutData(data);

		TableColumn column = new TableColumn(table, SWT.None);
		column.setText(Message.dialog_projectname);
		column.setWidth(120);

		column = new TableColumn(table, SWT.None);
		column.setText(Message.dialog_queryfile);
		column.setWidth(160);

		column = new TableColumn(table, SWT.None);
		column.setText(Message.dialog_filename);
		column.setWidth(180);

		tableViewer = new TableViewer(table);

		tableViewer.setContentProvider(new QueryIdContentProvider());
		tableViewer.setLabelProvider(new QueryIdTableContentProvider());
		tableViewer.setInput(QueryIdModel.getQueryIdList());
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selected = tableViewer.getSelection();
				Object obj = ((IStructuredSelection) selected)
						.getFirstElement();
				projectName = ((QueryIdModel) obj).getProjectName();
				queryFile = ((QueryIdModel) obj).getQueryFile();
				filePath = ((QueryIdModel) obj).getFilePath();
				loc = ((QueryIdModel) obj).getLoc();
				setReturnCode(OK);
				close();
			}
		});

	}

	public String getProjectName() {
		return projectName;
	}

	public String getQueryFile() {
		return queryFile;
	}

	public String getFilePath() {
		return filePath;
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	protected void okPressed() {
		ISelection selected = tableViewer.getSelection();
		Object obj = ((IStructuredSelection) selected).getFirstElement();
		if (obj == null) {
			MessageDialog
					.openWarning(
							this.getShell(),
							Message.dialog_warning,
							Message.dialog_selectfile_message);
		} else {
			projectName = ((QueryIdModel) obj).getProjectName();
			queryFile = ((QueryIdModel) obj).getQueryFile();
			filePath = ((QueryIdModel) obj).getFilePath();
			loc = ((QueryIdModel) obj).getLoc();
			super.okPressed();
		}
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

}
