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
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.ProjectUtil;
import org.anyframe.ide.common.util.PropertyUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
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
 * This is ConfigPropertyPage class.
 * 
 * @author Sujeong Lee
 */
public class ConfigPropertyPage extends org.eclipse.ui.dialogs.PropertyPage implements
		IWorkbenchPropertyPage {

	private Text templateLocText;
	private Text databasesLocText;
	private Text jdbcdriversLogText;

	private IProject project;

	private IPropertyPage thisPropertyPage;
	private boolean isChangedConfig = false;

	/**
	 * the constructor
	 * 
	 */
	public ConfigPropertyPage() {
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
		label.setText(Message.properties_config_template_description);

		Composite composite = new Composite(parent, 0);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		createTemplateGroup(composite);
		createJdbcGroup(composite);
		createDatabasesGroup(composite);

		loadSettings();

		return composite;
	}

	@Override
	public boolean performOk() {
		if (isChangedConfig) {
			// temp - project.mf
			saveProperties();
			saveSettings();
		}
		return super.performOk();
	}

	private void createTemplateGroup(final Composite parent) {
		// Group
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(Message.properties_config_template_section);
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Template Location
		new Label(group, SWT.NONE)
				.setText(Message.properties_templatehome_location);
		templateLocText = new Text(group, SWT.BORDER);
		templateLocText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event arg0) {
				isChangedConfig = true;
			}
		});
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 10;
		templateLocText.setLayoutData(gridData);

		GridData templateLocSearchButtonData = new GridData(SWT.NULL);
		templateLocSearchButtonData.widthHint = 90;
		Button templateLocSearchButton = new Button(group, SWT.BUTTON1);
		templateLocSearchButton.setText(Message.ide_button_browse);
		templateLocSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(parent.getShell(),
						SWT.OPEN);
				dlg.setMessage(Message.properties_template_location_info);
				String str = dlg.open();
				if (str != null) {
					templateLocText.setText(str);
					isChangedConfig = true;
				}
			}
		});
		templateLocSearchButton.setLayoutData(templateLocSearchButtonData);
	}

	private void createDatabasesGroup(final Composite parent) {
		// Group
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(Message.properties_config_databases_section);
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Databases Location
		new Label(group, SWT.NONE)
				.setText(Message.properties_databases_configuartion_location);
		databasesLocText = new Text(group, SWT.BORDER);
		databasesLocText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event arg0) {
				isChangedConfig = true;
			}
		});
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 10;
		databasesLocText.setLayoutData(gridData);

		GridData databasesLocSearchButtonData = new GridData(SWT.NULL);
		databasesLocSearchButtonData.widthHint = 90;
		Button databasesLocSearchButton = new Button(group, SWT.BUTTON1);
		databasesLocSearchButton.setText(Message.ide_button_browse);
		databasesLocSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(parent.getShell(),
						SWT.OPEN);
				dlg.setMessage(Message.properties_databases_location_info);
				String str = dlg.open();
				if (str != null) {
					databasesLocText.setText(str);
					isChangedConfig = true;
				}
			}
		});
		databasesLocSearchButton.setLayoutData(databasesLocSearchButtonData);
	}

	private void createJdbcGroup(final Composite parent) {
		// Group
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(Message.properties_config_jdbcdrivers_section);
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Template Location
		new Label(group, SWT.NONE)
				.setText(Message.properties_jdbc_configuration_location);
		jdbcdriversLogText = new Text(group, SWT.BORDER);
		jdbcdriversLogText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event arg0) {
				isChangedConfig = true;
			}
		});
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 10;
		jdbcdriversLogText.setLayoutData(gridData);

		GridData jdbcLocSearchButtonData = new GridData(SWT.NULL);
		jdbcLocSearchButtonData.widthHint = 90;
		Button jdbcLocSearchButton = new Button(group, SWT.BUTTON1);
		jdbcLocSearchButton.setText(Message.ide_button_browse);
		jdbcLocSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(parent.getShell(),
						SWT.OPEN);
				dlg.setMessage(Message.properties_jdbcdrivers_location_info);
				String str = dlg.open();
				if (str != null) {
					jdbcdriversLogText.setText(str);
					isChangedConfig = true;
				}
			}
		});
		jdbcLocSearchButton.setLayoutData(jdbcLocSearchButtonData);
	}

	private void loadSettings(){
		// AS-IS project.mf
		/*
		 * String projectLocation = project.getLocation().toOSString(); String
		 * metaFile = projectLocation + Constants.METAINF +
		 * Constants.METADATA_FILE; File f = new File(metaFile); if (f.exists())
		 * { PropertyUtil propertyUtil = new PropertyUtil(metaFile);
		 * templateLocText.setText(propertyUtil
		 * .getProperty(Constants.PROJECT_TEMPLATE_HOME)); }
		 */
		// TO-BE org.anyframe.ide.common.config.xml
		String projectLocation = project.getLocation().toOSString();

		PropertiesSettingUtil.parsing(projectLocation);
		ProjectUtil.refreshProject(project.getName());

		templateLocText.setText(PropertiesSettingUtil.TEMPLATE_LOC);
		jdbcdriversLogText.setText(PropertiesSettingUtil.JDBCDRIVERS_LOC);
		databasesLocText.setText(PropertiesSettingUtil.DATABASES_LOC);
	}

	private void saveProperties() {
		String projectLocation = project.getLocation().toOSString();
		String metaFile = projectLocation + Constants.METAINF
				+ Constants.METADATA_FILE;
		File f = new File(metaFile);
		if (f.exists()) {
			PropertyUtil propertyUtil = new PropertyUtil(metaFile);
			propertyUtil.setProperty(Constants.PROJECT_TEMPLATE_HOME,
					templateLocText.getText());
			propertyUtil.write();
		}
		
		ProjectUtil.refreshProject(project.getName());
	}

	private void saveSettings() {
		String projectLocation = project.getLocation().toOSString();
		
		CommonPropertiesVO vo = new CommonPropertiesVO();
		vo.setTemplateLoc(templateLocText.getText());
		vo.setJdbcdriversLoc(jdbcdriversLogText.getText());
		vo.setDatabasesLoc(databasesLocText.getText());

		if(!PropertiesSettingUtil.save(projectLocation, vo)){
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID,
					Message.exception_create_config_xml, new Exception());
			MessageDialogUtil.openMessageDialog(
					Message.ide_message_title,
					Message.exception_create_config_xml,
					MessageDialog.ERROR);
		}
	}

	public IPropertyPage getThisPropertyPage() {
		return thisPropertyPage;
	}

	public void setThisPropertyPage(IPropertyPage thisPropertyPage) {
		this.thisPropertyPage = thisPropertyPage;
	}
}
