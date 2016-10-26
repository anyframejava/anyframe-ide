/*   
 * Copyright 2002-2013 the original author or authors.   
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
package org.anyframe.ide.codegenerator.popups;

import java.util.List;

import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.HudsonRemoteAPI;
import org.anyframe.ide.codegenerator.views.CtipView;
import org.anyframe.ide.common.util.ButtonUtil;
import org.anyframe.ide.common.util.DialogUtil;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * This is a CtipManageUrlPopup class.
 * 
 * @author Soungmin Joo
 */
public class CtipManageUrlPopup extends Dialog {

	private HudsonRemoteAPI hudson = new HudsonRemoteAPI();
	private String DEFAULT_FILTER_STR = "type filter text";

	private Composite dialogArea;

	private CtipView view;

	private Text filterText;
	private Table urlTable;
	private TableViewer taskViewer;
	private Button addButton;
	private Button editButton;
	private Button removeButton;
	private Button testButton;

	public CtipManageUrlPopup(Shell parent, CtipView view) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.view = view;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Message.view_ctip_popup_title);
		shell.setBounds(DialogUtil.center(460, 400));

	}

	protected Control createDialogArea(Composite parent) {
		dialogArea = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		dialogArea.setLayout(layout);

		createFilterText(dialogArea);
		createTable(dialogArea);

		return dialogArea;
	}

	private void createFilterText(Composite dialogArea) {
		Composite composite = new Composite(dialogArea, SWT.NONE);
		GridLayout gridLayout = new GridLayout(4, true);
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		filterText = new Text(composite, SWT.BORDER);
		filterText.setText(DEFAULT_FILTER_STR);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 3;
		filterText.setLayoutData(layoutData);

		// empty label
		new Label(composite, SWT.NONE);

		filterText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				if (!(filterText.getText().equals(DEFAULT_FILTER_STR))) {
					redrawTableData();
				}
			}
		});
		filterText.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				if (filterText.getText().equals(DEFAULT_FILTER_STR)) {
					filterText.setSelection(0, 16);
				}
			}
		});
	}

	private void createTable(Composite dialogArea) {

		Composite composite = new Composite(dialogArea, SWT.NONE);
		composite.setLayout(new GridLayout(4, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		urlTable = createTableContents(composite);
		GridData gridTable = new GridData(GridData.FILL_BOTH);
		gridTable.horizontalSpan = 3;
		urlTable.setLayoutData(gridTable);
		redrawTableData();

		Composite compositeButtonArea = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		compositeButtonArea.setLayout(layout);
		compositeButtonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		addButton = ButtonUtil.createButton(compositeButtonArea,
				Message.ide_button_add, Message.ide_button_add);
		addButton.addSelectionListener(buttonListener);

		editButton = ButtonUtil.createButton(compositeButtonArea,
				Message.ide_button_edit, Message.ide_button_edit);
		editButton.addSelectionListener(buttonListener);

		removeButton = ButtonUtil.createButton(compositeButtonArea,
				Message.ide_button_remove, Message.ide_button_remove);
		removeButton.addSelectionListener(buttonListener);

		testButton = ButtonUtil.createButton(compositeButtonArea,
				Message.view_ctip_button_testconnection,
				Message.view_ctip_button_testconnection);
		testButton.addSelectionListener(buttonListener);

	}

	private Table createTableContents(Composite container) {
		Table taskTable = new Table(container, SWT.SINGLE | SWT.FULL_SELECTION
				| SWT.BORDER | SWT.V_SCROLL);
		taskTable.setHeaderVisible(true);
		taskTable.setLinesVisible(true);

		taskViewer = new TableViewer(taskTable);

		TableViewerColumn taskName = new TableViewerColumn(taskViewer, SWT.None);
		taskName.getColumn().setText(Message.view_ctip_urlpopop_columnname);
		taskName.getColumn().setWidth(100);

		TableViewerColumn buildstatus = new TableViewerColumn(taskViewer,
				SWT.None);
		buildstatus.getColumn().setText(Message.view_ctip_urlpopop_columnurl);
		buildstatus.getColumn().setWidth(200);

		return taskTable;
	}

	public void setFilterDefaultValue() {
		filterText.setText(DEFAULT_FILTER_STR);
	}

	public void redrawTableData() {
		String filterStr = filterText.getText().trim();
		if (filterStr.equals(DEFAULT_FILTER_STR)) {
			filterStr = "";
		}

		List<String> urlList = view.getAnyframeConfig().getCtipUrlList();
		urlTable.setItemCount(urlList.size());

		int count = 0;
		for (int i = 0; i < urlList.size(); i++) {
			String row = urlList.get(i);
			String name = row.substring(0, row.indexOf(" - "));
			String url = row.substring(row.toLowerCase().indexOf("http://"));

			if (name.indexOf(filterStr) == -1) {
				continue;
			}

			urlTable.getItem(count).setText(0, name);
			urlTable.getItem(count).setText(1, url);
			count++;
		}
		urlTable.setItemCount(count);
	}

	private CtipManageUrlPopup getInstance() {
		return this;
	}

	private SelectionListener buttonListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource() == addButton) {
				CtipAddUrlPopup addUrl = new CtipAddUrlPopup(addButton
						.getParent().getShell(), getInstance(), view);
				addUrl.open();

			} else if (e.getSource() == editButton) {
				onEditButtonClicked();

			} else if (e.getSource() == removeButton) {
				onRemoveButtonClicked();

			} else if (e.getSource() == testButton) {
				onTestConnectionButtonClicked();
			}
		}

	};

	private void onEditButtonClicked() {
		if (urlTable.getSelectionCount() == 0) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_popup_edit_notselected,
					MessageDialog.WARNING);
			return;
		}

		TableItem item = urlTable.getItem(urlTable.getSelectionIndex());
		CtipAddUrlPopup modifyUrl = new CtipAddUrlPopup(addButton.getParent()
				.getShell(), getInstance(), this.view, item.getText(0).trim(),
				item.getText(1).trim());
		modifyUrl.open();
	}

	private void onRemoveButtonClicked() {
		if (urlTable.getSelectionCount() == 0) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_popup_remove_notselected,
					MessageDialog.WARNING);
			return;
		}
		if (this.view.getAnyframeConfig().getCtipUrlList().size() == 1) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_popup_remove_notremoved,
					MessageDialog.WARNING);
			return;
		}

		if (!MessageDialogUtil.confirmMessageDialog(Message.ide_message_title,
				Message.view_ctip_popup_remove_confirm)) {
			return;
		}
		TableItem item = urlTable.getItem(urlTable.getSelectionIndex());

		view.removeCtipServerToUrl(item.getText(0), item.getText(1));

		redrawTableData();
	}

	private void onTestConnectionButtonClicked() {
		if (urlTable.getSelectionCount() == 0) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_popup_testconnection_notselected,
					MessageDialog.WARNING);
			return;
		}

		String url = urlTable.getItem(urlTable.getSelectionIndex()).getText(1);
		hudson.setHudsonURL(url);
		try {
			hudson.getJobList();
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_popup_testconnection_success,
					MessageDialog.INFORMATION);

		} catch (Exception ex) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_popup_testconnection_fail,
					MessageDialog.WARNING);
		}
	}

	public boolean isName(String name) {

		for (int i = 0; i < urlTable.getItemCount(); i++) {
			TableItem item = urlTable.getItem(i);
			if (name.equals(item.getText()))
				return true;
		}

		return false;
	}

	protected void okPressed() {
		close();
	}

	protected void cancelPressed() {
		close();
	}
}
