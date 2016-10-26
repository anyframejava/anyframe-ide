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
package org.anyframe.ide.codegenerator.dialogs;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.CommandExecution;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.command.maven.mojo.container.PluginContainer;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.databases.JdbcOption;
import org.anyframe.ide.common.dialog.IDBSettingDialog;
import org.anyframe.ide.common.dialog.JdbcType;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.PropertyUtil;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * This is an DBSettingDialogExtra class.
 * 
 * @author Sujeong Lee
 */
public class DBSettingDialogExtra implements IDBSettingDialog {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	private IProject project;
	private Combo dialectCombo;
	private Text driverGroupIdText;
	private Text driverArtifactIdText;
	private Text driverVersionText;

	private java.util.List<JdbcType> jdbcTypes;
	private PropertiesIO pjtProps = null;

	public void init(IProject project, List<JdbcType> jdbcTypes) {
		this.project = project;
		this.jdbcTypes = jdbcTypes;
	}

	public void createUI(final Composite composite) {
		try {
			pjtProps = ProjectUtil.getProjectProperties(project);
		} catch (Exception e) {
			PluginLoggerUtil.warning(ID, Message.wizard_error_properties);
		}

		ExpandableComposite expander = new ExpandableComposite(composite,
				ExpandableComposite.TWISTIE);

		Composite parent = new Composite(expander, SWT.NONE);
		parent.setLayout(new GridLayout(1, false));
		expander.setClient(parent);
		expander.setText(Message.wizard_jdbcdriver_hidden_title);

		expander.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				composite.getShell().pack();
			}
		});

		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(Message.wizard_jdbcdriver_label);

		GridLayout groupLayout = new GridLayout(3, false);
		groupLayout.marginHeight = 1;
		groupLayout.marginWidth = 1;
		group.setLayout(groupLayout);

		GridData groupGridData = new GridData(SWT.NULL);
		groupGridData.widthHint = 530;

		group.setLayoutData(groupGridData);

		GridData labelWidthData = new GridData(SWT.NULL);
		labelWidthData.widthHint = 110;

		GridData gridData = new GridData(SWT.NULL);
		gridData.widthHint = 300;
		gridData.horizontalSpan = 2;

		// Hibernate Dialect
		Label dialectLabel = new Label(group, SWT.NULL);
		dialectLabel.setText(Message.wizard_jdbc_dialect);
		dialectLabel.setLayoutData(labelWidthData);
		dialectCombo = new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		dialectCombo.setLayoutData(gridData);

		// Group ID
		Label databaseTypeLabel = new Label(group, SWT.NULL);
		databaseTypeLabel.setText(Message.wizard_maven_groupid);
		databaseTypeLabel.setLayoutData(labelWidthData);
		driverGroupIdText = new Text(group, SWT.BORDER);
		driverGroupIdText.setLayoutData(gridData);

		// Artifact ID
		Label driverArtifactIdLabel = new Label(group, SWT.NULL);
		driverArtifactIdLabel.setText(Message.wizard_maven_artifactid);
		driverArtifactIdLabel.setLayoutData(labelWidthData);
		driverArtifactIdText = new Text(group, SWT.BORDER);
		driverArtifactIdText.setLayoutData(gridData);

		// Driver Version
		Label driverVersionLabel = new Label(group, SWT.NULL);
		driverVersionLabel.setText(Message.wizard_maven_version);
		driverVersionLabel.setLayoutData(labelWidthData);
		driverVersionText = new Text(group, SWT.BORDER);
		driverVersionText.setLayoutData(gridData);
	}

	public void loadSettings(JdbcType type) {

		for (int i = 0; i < jdbcTypes.size(); i++) {
			JdbcType jdbcType = jdbcTypes.get(i);
			if (type.getType().equals(jdbcType.getType())) {
				setDialect(i);
				break;
			}
		}

		dialectCombo.setText(type.getDialect()[0] == null ? "" : type
				.getDialect()[0]);
		if (pjtProps.readValue(CommonConstants.PROJECT_BUILD_TYPE).equals(
				CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
			driverGroupIdText.setText(type.getDriverGroupId());
			driverArtifactIdText.setText(type.getDriverArtifactId());
			driverVersionText.setText(type.getDriverVersion());
		}
	}

	public void saveSettings(IProject project, JdbcOption jdbcOption,
			boolean isChangedDBConfig) {

		String projectLocation = project.getLocation().toOSString();
		String metaFile = projectLocation + Constants.METAINF
				+ Constants.METADATA_FILE;
		File f = new File(metaFile);
		if (f.exists()) {
			PropertyUtil propertyUtil = new PropertyUtil(metaFile);
			propertyUtil.setProperty(Constants.DB_TYPE, jdbcOption.getDbType());
			propertyUtil.setProperty(Constants.DB_NAME, jdbcOption.getDbName());
			propertyUtil.setProperty(Constants.DB_SCHEMA,
					jdbcOption.getSchema() == null
							|| jdbcOption.getSchema().equals("No Schema") ? ""
							: jdbcOption.getSchema());
			propertyUtil.setProperty(Constants.DB_USERNAME,
					jdbcOption.getUserName());
			propertyUtil.setProperty(Constants.DB_PASSWORD,
					jdbcOption.getPassword());
			propertyUtil.setProperty(Constants.DB_SERVER, "");
			propertyUtil.setProperty(Constants.DB_PORT, "");
			propertyUtil.setProperty(Constants.DB_DRIVER_CLASS,
					jdbcOption.getDriverClassName());
			propertyUtil.setProperty(Constants.DB_DRIVER_PATH,
					jdbcOption.getDriverJar());
			propertyUtil.setProperty(Constants.DB_URL, jdbcOption.getUrl());

			if (pjtProps.readValue(CommonConstants.PROJECT_BUILD_TYPE).equals(
					CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {

				if (!validateMavenInfo(jdbcOption)) {
					for (int i = 0; i < jdbcTypes.size(); i++) {
						JdbcType jdbcType = jdbcTypes.get(i);
						if (jdbcOption.getDbType().equals(jdbcType.getType())) {

							propertyUtil.setProperty(Constants.DB_DIALECT,
									jdbcType.getDialect()[0]);
							propertyUtil.setProperty(
									CommonConstants.DB_GROUPID,
									jdbcType.getDriverGroupId());
							propertyUtil.setProperty(
									CommonConstants.DB_ARTIFACTID,
									jdbcType.getDriverArtifactId());
							propertyUtil.setProperty(
									CommonConstants.DB_VERSION,
									jdbcType.getDriverVersion());
							break;
						}
					}

				} else {
					propertyUtil.setProperty(Constants.DB_DIALECT,
							jdbcOption.getDialect());
					propertyUtil.setProperty(CommonConstants.DB_GROUPID,
							jdbcOption.getMvnGroupId());
					propertyUtil.setProperty(CommonConstants.DB_ARTIFACTID,
							jdbcOption.getMvnArtifactId());
					propertyUtil.setProperty(CommonConstants.DB_VERSION,
							jdbcOption.getMvnVersion());
				}
			}
			propertyUtil.write();
			
			if (isChangedDBConfig) {
				try {
					// call ant task
					CommandExecution genExecution = new CommandExecution();
					genExecution.changeDBConfig(project.getLocation()
							.toOSString());
				} catch (Exception e) {
					PluginLoggerUtil.error(ID,
							Message.view_exception_savedbconfig, e);
					MessageDialogUtil.openMessageDialog(
							Message.ide_message_title,
							Message.view_exception_findconfig,
							MessageDialog.ERROR);
				}
			}
		}
	}

	private boolean validateMavenInfo(JdbcOption jdbcOption) {
		if (jdbcOption.getDialect() == null) {
			return false;
		}
		if (jdbcOption.getMvnArtifactId() == null) {
			return false;
		}
		if (jdbcOption.getMvnGroupId() == null) {
			return false;
		}
		if (jdbcOption.getMvnVersion() == null) {
			return false;
		}
		return true;
	}

	public void setDatabaseTypeSelectListener(SelectionEvent e) {

		clearUI();

		Combo selected = (Combo) e.getSource();
		JdbcType jdbcType = (JdbcType) this.jdbcTypes.get(selected
				.getSelectionIndex());
		if (!jdbcType.getType().equalsIgnoreCase("Others...")) {
			setDialect(selected.getSelectionIndex());
			if (pjtProps != null) {
				if (pjtProps.readValue(CommonConstants.PROJECT_BUILD_TYPE)
						.equals(CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
					setDriverPom(selected.getSelectionIndex());
				}
			}
		}
	}

	private void setDialect(int index) {
		String[] dialect = jdbcTypes.get(index).getDialect();

		for (int i = 0; i < dialect.length; i++)
			dialectCombo.add(dialect[i]);

		dialectCombo.select(0);
	}

	private void setDriverPom(int index) {
		driverGroupIdText.setText(jdbcTypes.get(index).getDriverGroupId());
		driverArtifactIdText
				.setText(jdbcTypes.get(index).getDriverArtifactId());
		driverVersionText.setText(jdbcTypes.get(index).getDriverVersion());
	}

	private void clearUI() {
		dialectCombo.removeAll();
		driverGroupIdText.setText("");
		driverArtifactIdText.setText("");
		driverVersionText.setText("");
	}

	public Map<String, String> getInputData() {
		Map<String, String> result = new HashMap<String, String>();
		result.put(Constants.XML_TAG_DIALECT, dialectCombo.getText());
		result.put(Constants.XML_TAG_DRIVER_GROUPID,
				driverGroupIdText.getText());
		result.put(Constants.XML_TAG_DRIVER_ARTIFACTID,
				driverArtifactIdText.getText());
		result.put(Constants.XML_TAG_DRIVER_VERSION,
				driverVersionText.getText());
		return result;
	}

	public String getLocalRepositoryPath() {
		PluginContainer container = null;
		try {
			container = new PluginContainer(null);
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, e.getMessage(), e);
		}
		ArchetypeGenerationRequest request = container.getRequest();
		return request.getLocalRepository().getBasedir();
	}

}
