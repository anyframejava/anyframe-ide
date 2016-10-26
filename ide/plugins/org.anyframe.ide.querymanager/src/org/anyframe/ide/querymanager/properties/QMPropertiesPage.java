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
package org.anyframe.ide.querymanager.properties;

import java.util.ArrayList;

import org.anyframe.ide.common.util.ImageUtil;
import org.anyframe.ide.querymanager.dialogs.ElementSelectAddDialog;
import org.anyframe.ide.querymanager.dialogs.ElementSelectScanDialog;
import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.views.QMExplorerView;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Query Manager Property page. If nature is setting, add mapping-xml filse to
 * see in Query Explorer View.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class QMPropertiesPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	private static IProject project;

	private static Table tableList;
	private Button buttonAdd;

	private static Button buttonRemove;

	private Button buttonScan;
	public static ArrayList addList;
	public static ArrayList scanList;
	public static ArrayList xmlList;

	private Button checkComment;
	private boolean boolComment;

	private Button checkDupl;
	private boolean boolDupl;

	private Button checkUsedUnused;
	private boolean boolUsedUnused;

	private ElementSelectAddDialog addDialog;
	private ElementSelectScanDialog scanDialog;

	private QMPropertiesXMLUtil util = new QMPropertiesXMLUtil();
	
	public static final String PLUGIN_ID = "org.anyframe.ide.querymanager";
	
	public QMPropertiesPage() {
		noDefaultAndApplyButton();
	}
	
	private SelectionListener buttonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	private void setPropertyProject() {
		this.project = (IProject) getElement().getAdapter(IResource.class);
	}

	public static IProject getPropertyProject() {
		return project;
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		initData();

		setPropertyProject();
		
		Composite composite = new Composite(parent, 0);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		createContentsGroup(composite);
		
//		Composite panel = new Composite(parent, SWT.NONE);
//		GridLayout layout = new GridLayout();
//		layout.marginHeight = 0;
//		layout.marginWidth = 0;
//		panel.setLayout(layout);

		
		return composite;
	}

	private void createContentsGroup(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Message.property_dialog_text);

		// Group
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
		fillPropertiesPage(group);

		checkComment = new Button(group, SWT.CHECK);
		checkComment.setText(Message.property_enablequerycommentappend);
		checkComment.setToolTipText("");
		checkComment.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolComment = checkComment.getSelection();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		checkDupl = new Button(group, SWT.CHECK);
		checkDupl.setText(Message.property_enablequeryidduplicationchecking);
		checkDupl.setToolTipText("");
		checkDupl.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolDupl = checkDupl.getSelection();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		checkUsedUnused = new Button(group, SWT.CHECK);
		checkUsedUnused.setText(Message.property_enablequeryidcheck);
		checkUsedUnused.setToolTipText("");
		checkUsedUnused.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolUsedUnused = checkUsedUnused.getSelection();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		load();
		settingScanTable();
		checkRemoveButton();

		boolComment = checkComment.getSelection();
		boolDupl = checkDupl.getSelection();
		boolUsedUnused = checkUsedUnused.getSelection();
	}

	private void initData() {
		tableList = null;
		addList = null;
		scanList = null;
		xmlList = null;
		ArrayList list = ElementSelectScanDialog.getSelectList();
		list = null;
	}

	private void fillPropertiesPage(Composite panel) {
		Composite composite = new Composite(panel, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayout(layout);
		composite.setLayoutData(data);

		tableList = new Table(composite, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.MULTI);
		GridData gridTable = new GridData(GridData.FILL_BOTH);
		gridTable.horizontalSpan = 1;
		gridTable.heightHint = 285;
		gridTable.widthHint = 390;
		tableList.setLayoutData(gridTable);

		Composite compositeButtonArea = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		compositeButtonArea.setLayout(layout);
		compositeButtonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		buttonAdd = createButton(compositeButtonArea,
				Message.property_add_button, "", //$NON-NLS-2$ //$NON-NLS-1$
				buttonListener);
		buttonRemove = createButton(compositeButtonArea,
				Message.property_dialog_remove_button,
				"", //$NON-NLS-2$ //$NON-NLS-1$
				buttonListener);
		buttonScan = createButton(compositeButtonArea,
				Message.property_dialog_scan_button, "", //$NON-NLS-2$ //$NON-NLS-1$
				buttonListener);

	}

	public static void settingScanTable() {

		ArrayList tableResult = new ArrayList();
		ArrayList scanResult = new ArrayList();

		TableItem[] temp = tableList.getItems();
		for (int i = 0; i < temp.length; i++) {
			tableResult.add(temp[i].getText() + ""); //$NON-NLS-1$
		}

		if (scanList != null) {
			if (scanList.size() != 0) {
				for (int i = 0; i < scanList.size(); i++) {
					scanResult.add(scanList.get(i));
				}
			}

			for (int i = 0; i < scanResult.size(); i++) {
				TableItem item = new TableItem(tableList, SWT.NONE);
				item.setImage(ImageUtil.getImage(ImageUtil
						.getImageDescriptor(PLUGIN_ID , Message.image_properties_xmlfile)));
				item.setText(scanResult.get(i) + ""); //$NON-NLS-1$
			}
		}
		tableResult.addAll(scanResult);
		xmlList = tableResult;
	}

	public Button createButton(Composite compositeButtonArea, String title,
			String toolTip, SelectionListener listener) {
		Button button = new Button(compositeButtonArea, SWT.PUSH);
		button.setText(title);
		button.setToolTipText(toolTip);
		button.addSelectionListener(listener);

		FontMetrics fontMetrics = getFontMetrics(button);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
				IDialogConstants.BUTTON_WIDTH);
		gd.widthHint = Math.max(widthHint,
				button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(gd);
		return button;
	}

	public static FontMetrics getFontMetrics(Control control) {
		FontMetrics fontMetrics = null;
		GC gc = new GC(control);
		try {
			gc.setFont(control.getFont());
			fontMetrics = gc.getFontMetrics();
		} finally {
			gc.dispose();
		}
		return fontMetrics;
	}

	private void handleButtonPressed(Button button) {
		if (button == buttonAdd) {
			handleAddButtonPressed();
		} else if (button == buttonRemove) {
			handleRemoveButtonPressed();
		} else if (button == buttonScan) {
			handleScanButtonPressed();
		}
		checkRemoveButton();
	}

	private void handleAddButtonPressed() {
		addDialog = new ElementSelectAddDialog(Display.getCurrent()
				.getActiveShell());
		addDialog.open();
	}

	private void handleScanButtonPressed() {
		scanDialog = new ElementSelectScanDialog(Display.getCurrent()
				.getActiveShell());
		scanDialog.open();
	}

	private void handleRemoveButtonPressed() {

		TableItem[] selectItem = tableList.getSelection();
		if (selectItem.length != -1) {
			for (int i = 0; i < selectItem.length; i++) {
				tableList.remove(tableList.indexOf(selectItem[i]));
			}

			ArrayList tableResult = new ArrayList();
			TableItem[] temp = tableList.getItems();
			for (int i = 0; i < temp.length; i++) {
				tableResult.add(temp[i].getText() + ""); //$NON-NLS-1$
			}
			xmlList = tableResult;
		}

	}

	private static void checkRemoveButton() {
		if (xmlList.size() == 0) {
			buttonRemove.setEnabled(false);
		} else {
			buttonRemove.setEnabled(true);
		}
	}

	public boolean performOk() {
		save();
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
			QMExplorerView explorerView = (QMExplorerView) PlatformUI
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getPartService()
					.getActivePart()
					.getSite()
					.getPage()
					.showView(
							"org.anyframe.ide.querymanager.views.QMExplorerView");
			explorerView.refresh();
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.performOk();
	}

	public void load() {
		ArrayList list = util.loadPropertiesXML();
		for (int i = 0; i < list.size(); i++) {
			TableItem item = new TableItem(tableList, SWT.NONE);
			item.setImage(ImageUtil.getImage(ImageUtil
					.getImageDescriptor(PLUGIN_ID , Message.image_properties_xmlfile)));
			item.setText(list.get(i).toString());
		}

		Boolean bool1 = util.getPropertiesComment();
		Boolean bool2 = util.getPropertiesDupl();
		Boolean bool3 = util.getPropertiesUsed();
		if (bool1) {
			checkComment.setSelection(true);
		} else {
			checkComment.setSelection(false);
		}
		if (bool2) {
			checkDupl.setSelection(true);
		} else {
			checkDupl.setSelection(false);
		}
		if (bool3) {
			checkUsedUnused.setSelection(true);
		} else {
			checkUsedUnused.setSelection(false);
		}
	}

	public void save() {
		util.savePropertiesXML(xmlList, boolComment, boolDupl, boolUsedUnused);
	}

}
