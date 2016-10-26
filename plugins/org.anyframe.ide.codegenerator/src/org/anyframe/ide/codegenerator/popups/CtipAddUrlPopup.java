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

import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.views.CtipView;
import org.anyframe.ide.common.util.DialogUtil;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This is a CtipAddUrlPopup class.
 * 
 * @author Soungmin Joo
 */
public class CtipAddUrlPopup extends Dialog {
	
	private final CtipView view;
	private final CtipManageUrlPopup managePopup;

	private Text nameText;
	private Text locationText;
	private String name;
	private String location;
	private String oldLocationStr;

	private final boolean isModify;

	public CtipAddUrlPopup(Shell parent, CtipManageUrlPopup managePopup,
			CtipView view) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.view = view;
		this.managePopup = managePopup;
		this.isModify = false;

	}

	public CtipAddUrlPopup(Shell parent, CtipManageUrlPopup managePopup,
			CtipView view, String name, String location) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.view = view;
		this.managePopup = managePopup;
		this.isModify = true;

		this.name = name;
		this.location = location;

		oldLocationStr = name + " - " + location;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Message.view_ctip_addserverpopup_title);
		shell.setBounds(DialogUtil.center(300, 150));
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		dialogArea.setLayout(layout);

		createLocationInfo(dialogArea);

		return dialogArea;
	}

	private void createLocationInfo(Composite dialogArea) {
		Composite composite = new Composite(dialogArea, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label addPopupName = new Label(composite, SWT.NONE);
		addPopupName.setText(Message.view_ctip_addserverpopup_name);

		nameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		nameText.setLayoutData(layoutData);

		Label addPopupLoc = new Label(composite, SWT.NONE);
		addPopupLoc.setText(Message.view_ctip_addserverpopup_location);

		locationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		locationText.setText("http://");
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		locationText.setLayoutData(layoutData);

		if (name != null)
			nameText.setText(name);
		if (location != null)
			locationText.setText(location);
	}

	protected void okPressed() {
		if (saveChanges()) {
			close();
		}
	}

	protected void cancelPressed() {
		close();
	}

	private boolean saveChanges() {
		if (nameText.getText().trim().equals("")) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_addserverpopup_valid_server,
					MessageDialog.WARNING);
			return false;
		}

		if (!locationText.getText().trim().toLowerCase().startsWith("http://")) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_addserverpopup_valid_location_prefix,
					MessageDialog.WARNING);
			return false;
		}

		if (locationText.getText().trim().equals("http://")) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_addserverpopup_valid_location,
					MessageDialog.WARNING);
			return false;
		}

		if (locationText.getText().trim().endsWith("/")) {
			String text = locationText.getText().trim();
			locationText.setText(text.substring(0, text.length() - 1));
		}

		if (isModify) {
			view.updateCtipServerToUrl(nameText.getText().trim(), locationText
					.getText().trim(), oldLocationStr);
			managePopup.redrawTableData();
		} else {
			// check duplication name
			boolean isName = managePopup.isName(nameText.getText().trim());

			if (!isName) {
				view.addCtipServerToUrl(nameText.getText().trim(), locationText
						.getText().trim());

				managePopup.setFilterDefaultValue();
				managePopup.redrawTableData();
			} else {
				MessageDialogUtil.openMessageDialog(Message.ide_message_title,
						Message.view_ctip_addserverpopup_valid_name,
						MessageDialog.WARNING);
				return false;
			}
		}
		return true;
	}
}
