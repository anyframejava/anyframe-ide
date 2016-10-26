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
import java.util.HashMap;

import org.anyframe.ide.common.util.DialogUtil;
import org.anyframe.ide.common.util.ImageUtil;
import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.parsefile.FilesParser;
import org.anyframe.ide.querymanager.properties.QMPropertiesPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

/**
 * Select mapping-xml files.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class ElementSelectScanDialog extends Dialog {

	public static final String PLUGIN_ID = "org.anyframe.ide.querymanager";
	
	private IProject project;
	private Table table;
	private Button buttonSelectAll;
	private Button buttonDeSelectAll;

	private static ArrayList selectList;
	
	public static ArrayList getSelectList() {
		return selectList;
	}

	public ElementSelectScanDialog(Shell parentShell) {
		super(parentShell);
		project = QMPropertiesPage.getPropertyProject();
	}

	protected void configureShell(Shell newShell) {
		newShell.setText(Message.property_dialog_scan_title);
		super.configureShell(newShell);
		newShell.setBounds(DialogUtil.center(430, 450));
	}

	protected Control createDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(4, true);
		composite.setLayout(gridLayout);
		gridLayout.marginHeight = 10;
		gridLayout.marginWidth = 10;
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label labelInfo = new Label(composite, SWT.NONE);
		GridData layoutData = new GridData(GridData.BEGINNING);
		layoutData.horizontalSpan = 4;
		labelInfo.setLayoutData(layoutData);
		labelInfo
				.setText(Message.property_dialog_scan_message);

		table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		GridData layoutTable = new GridData(GridData.FILL_BOTH);
		layoutTable.horizontalSpan = 4;
		table.setLayoutData(layoutTable);

		createSpacer(composite, 2);

		buttonSelectAll = new QMPropertiesPage()
				.createButton(
						composite,
						Message.property_dialog_scan_selectall_title,
						Message.property_dialog_scan_selectall_tooltip,
						buttonSelectListener);
		GridData layoutSelectButton = new GridData(
				GridData.HORIZONTAL_ALIGN_FILL);
		layoutSelectButton.horizontalSpan = 1;
		buttonSelectAll.setLayoutData(layoutSelectButton);

		buttonDeSelectAll = new QMPropertiesPage()
				.createButton(
						composite,
						Message.property_dialog_scan_deselectall_title,
						Message.property_dialog_scan_deselectall_tooltip,
						buttonDeSelectListener);
		GridData layoutDeselectButton = new GridData(
				GridData.HORIZONTAL_ALIGN_FILL);
		layoutDeselectButton.horizontalSpan = 1;
		buttonDeSelectAll.setLayoutData(layoutDeselectButton);

		settingTable(table);

		table.redraw();

		return parent;
	}

	private void settingTable(final Table table) {

		Job treeViewJob = new Job(Message.dialog_scanmappingxml) { //$NON-NLS-1$

			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask(
						Message.dialog_scanmappingxmlfordialog, 100); //$NON-NLS-1$
				monitor.subTask(Message.dialog_getthefile); //$NON-NLS-1$

				if (monitor.isCanceled()) {
					monitor.done();
				}

				monitor.worked(50);

				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {

						FilesParser parser = new FilesParser();
						parser.parseFiles2(project, new NullProgressMonitor(),
								new HashMap());
						ArrayList list = parser.getXmlFileNames();
						ArrayList fileNameList = new ArrayList();

						for (int i = 0; i < list.size(); i++) {
							String file = (String) list.get(i);
							String filePath = file.substring(project
									.getLocation().toString().length() + 1,
									file.length());
							fileNameList.add(filePath);
						}

						ArrayList xmlList = QMPropertiesPage.xmlList;

						if (xmlList.size() == 0) {
							for (int i = 0; i < list.size(); i++) {
								TableItem item = new TableItem(table, SWT.NONE);
								item.setImage(ImageUtil.getImage(ImageUtil
										.getImageDescriptor(
												PLUGIN_ID,
												Message.image_properties_xmlfile)));
								item.setText(fileNameList.get(i) + ""); //$NON-NLS-1$
								item.setChecked(true);
							}
						} else {
							for (int i = 0; i < fileNameList.size(); i++) {
								for (int j = 0; j < xmlList.size(); j++) {
									if (fileNameList.get(i).equals(
											xmlList.get(j))) {
										break;
									} else {
										if (j == xmlList.size() - 1) {
											TableItem item = new TableItem(
													table, SWT.NONE);
											item.setImage(ImageUtil.getImage(ImageUtil
													.getImageDescriptor(PLUGIN_ID,Message.image_properties_xmlfile)));
											item.setText(fileNameList.get(i)
													+ ""); //$NON-NLS-1$
											item.setChecked(true);
										} else {
										}
									}
								}
							}
						}
					}

				});
				monitor.worked(25);
				monitor.done();
				return Status.OK_STATUS;
			}
		};

		treeViewJob.setSystem(false);
		treeViewJob.setUser(true);

		treeViewJob.schedule();

	}

	@Override
	protected void okPressed() {
		setOkPressed();
		QMPropertiesPage.settingScanTable();
		close();
	}

	@Override
	protected void cancelPressed() {
		close();
	}

	private void setOkPressed() {
		selectList = new ArrayList();
		TableItem[] item = table.getItems();
		for (int i = 0; i < item.length; i++) {
			if (item[i].getChecked()) {
				selectList.add(item[i].getText() + ""); //$NON-NLS-1$
			}
		}
		QMPropertiesPage.scanList = selectList;
	}

	private SelectionListener buttonSelectListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			TableItem[] items = table.getItems();
			for (int i = 0; i < items.length; i++) {
				items[i].setChecked(true);
			}
		}
	};

	private SelectionListener buttonDeSelectListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			TableItem[] items = table.getItems();
			for (int i = 0; i < items.length; i++) {
				items[i].setChecked(false);
			}
		}
	};

	private void createSpacer(Composite parent, int span) {
		Label spacer = new Label(parent, SWT.NONE);
		spacer.setText(""); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		spacer.setLayoutData(gd);
	}

}
