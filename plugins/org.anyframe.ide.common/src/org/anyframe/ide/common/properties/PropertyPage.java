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
package org.anyframe.ide.common.properties;

import java.io.File;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.messages.Message;
import org.anyframe.ide.common.util.ProjectUtil;
import org.anyframe.ide.common.util.PropertyUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;

/**
 * This is PropertyPage class.
 * 
 * @author Sujeong Lee
 */
public class PropertyPage extends org.eclipse.ui.dialogs.PropertyPage implements
		IWorkbenchPropertyPage {

	private Text configLocText;

	private IProject project;

	private IPropertyPage thisPropertyPage;
	private boolean isChangedConfig = false;

	/**
	 * the constructor
	 * 
	 */
	public PropertyPage() {
		noDefaultAndApplyButton();

		setThisPropertyPage(CommonActivator.getInstance().getPropertyPage());
	}

	/**
	 * @param parent
	 *            .
	 */
	protected Control createContents(Composite parent) {
		project = (IProject) getElement().getAdapter(IProject.class);

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Message.properties_config_xml_description);

		Composite composite = new Composite(parent, 0);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		createTemplateGroup(composite);

		loadSettings();

		return composite;
	}

	@Override
	public boolean performOk() {
		if (isChangedConfig) {
			saveSettings();
		}
		return super.performOk();
	}

	private void createTemplateGroup(final Composite parent) {
		// Group
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(Message.properties_config_xml_section);
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Template Location
		new Label(group, SWT.NONE)
				.setText(Message.properties_xml_configuration_location);
		configLocText = new Text(group, SWT.BORDER);
		configLocText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event arg0) {
				isChangedConfig = true;
			}
		});
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 10;
		configLocText.setLayoutData(gridData);

		GridData xmlLocSearchButtonData = new GridData(SWT.NULL);
		xmlLocSearchButtonData.widthHint = 90;
		Button xmlLocSearchButton = new Button(group, SWT.BUTTON1);
		xmlLocSearchButton.setText(Message.ide_button_browse);
		xmlLocSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(parent.getShell(),
						SWT.OPEN);
				dlg.setMessage(Message.properties_xml_location_info);
				String str = dlg.open();
				if (str != null) {
					configLocText.setText(str);
					isChangedConfig = true;
				}
			}
		});
		xmlLocSearchButton.setLayoutData(xmlLocSearchButtonData);
	}

	private void loadSettings() {
		String propertyFile = PropertiesSettingUtil.getPrefs(project
				.getLocation().toOSString());
		PropertyUtil propertyUtil = new PropertyUtil(propertyFile);
		configLocText.setText(propertyUtil
				.getProperty(Constants.COMMON_CONFIG_PREFS_KEY));
	}

	private void saveSettings() {
		String fileName = project.getLocation().toOSString()
				+ Constants.FILE_SEPERATOR + Constants.COMMON_CONFIG_PREFS_FILE;
		File f = new File(fileName);
		if (f.exists()) {
			PropertyUtil propertyUtil = new PropertyUtil(fileName);
			propertyUtil.setProperty(Constants.COMMON_CONFIG_PREFS_KEY,
					configLocText.getText());
			propertyUtil.write();
		}

		ProjectUtil.refreshProject(project.getName());
	}

	public IPropertyPage getThisPropertyPage() {
		return thisPropertyPage;
	}

	public void setThisPropertyPage(IPropertyPage thisPropertyPage) {
		this.thisPropertyPage = thisPropertyPage;
	}
}
