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
package org.anyframe.ide.codegenerator.wizards;

import java.util.Set;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.preferences.IdePreferencesPage;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.PluginUtil;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * This is an NewApplicationWizardPage class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 * @author Eunjin Jang
 */
public class NewApplicationWizardPage extends WizardPage {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	private Text pjtNameText;
	private Text locText;

	private Text packageNameText;
	private boolean isModifiedPackageText = false;
	private boolean isModifiedPjtArtifactIdText = false;

	// private Button[] pjtTypeRadios;

	private Text pjtGroupIdText;
	private Text pjtArtifactIdText;
	private Text pjtVersionText;

	private static IPreferenceStore store;
	private String buildType;
	private boolean isAntProject;
	private String anyframeHome;

	private Set<String> pluginNameList;

	private Button templateHome;
	private Text templateHomeText;
	private Button browseTemplateHomeLoc;

	protected NewApplicationWizardPage(String pageName) {
		super(pageName);
		this.setTitle(Message.wizard_application_title);
		this.setDescription(Message.wizard_application_description);
		if (store == null)
			store = CodeGeneratorActivator.getDefault().getPreferenceStore();
	}

	private void initialize() {
		buildType = (store.getString(IdePreferencesPage.BUILD_TYPE).equals("") ? IdePreferencesPage.ANT_BUILD_TYPE
				: store.getString(IdePreferencesPage.BUILD_TYPE)).toLowerCase();

		isAntProject = buildType.equals(CommonConstants.PROJECT_BUILD_TYPE_ANT) ? true
				: false;

		if (isAntProject) {
			anyframeHome = store.getString(IdePreferencesPage.ANYFRAME_HOME);
		}
	}

	public void createControl(Composite parent) {
		initialize();

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createPjtNameFields(composite);
		createContentsGroup(composite);
		if (isAntProject) {
		} else {
			createPjtArtifactGroup(composite);
		}

		setPageComplete(false);
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	private void createPjtNameFields(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(comp, SWT.NONE).setText(Message.wizard_application_name);
		pjtNameText = new Text(comp, SWT.BORDER);
		pjtNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pjtNameText.addListener(SWT.Modify, applicationNameTextModifyListener);
	}

	private void createContentsGroup(Composite parent) {
		// Contents Group
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(Message.wizard_application_contents);
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Application Location
		new Label(group, SWT.NONE).setText(Message.wizard_application_location);
		locText = new Text(group, SWT.BORDER);
		locText.addListener(SWT.Modify, textModifyListener);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 10;
		locText.setLayoutData(gridData);

		if (isAntProject) {
			locText.setText(anyframeHome + ProjectUtil.SLASH + "applications");
		} else {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
					.getRoot();
			locText.setText(workspaceRoot.getLocation().toOSString());
		}

		Button browseAppLoc = new Button(group, SWT.PUSH);
		browseAppLoc.setText(Message.ide_button_browse);
		browseAppLoc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleDirBrowseAppLocation();
			}
		});

		// When project build type is maven, set
		// template home
		templateHome = new Button(group, SWT.CHECK);
		templateHome.setText(Message.wizard_templatehome_location_check);
		GridData templateHomeCheck = new GridData();
		templateHomeCheck.horizontalSpan = 3;
		templateHome.setLayoutData(templateHomeCheck);
		templateHome.setSelection(true);
		templateHome.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (templateHome.getSelection()) {
					templateHomeText.setEnabled(false);
					browseTemplateHomeLoc.setEnabled(false);
					templateHomeText.setText(Constants.DFAULT_TEMPLATE_HOME);
				} else {
					templateHomeText.setEnabled(true);
					browseTemplateHomeLoc.setEnabled(true);
				}
				setPageComplete(isPageComplete());
			}
		});

		// Template home location
		new Label(group, SWT.NONE)
				.setText(Message.wizard_templatehome_location);
		templateHomeText = new Text(group, SWT.BORDER);
		templateHomeText.addListener(SWT.Modify, textModifyListener);
		GridData templateHomegridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 10;
		templateHomeText.setLayoutData(templateHomegridData);
		templateHomeText.setText(Constants.DFAULT_TEMPLATE_HOME);
		templateHomeText.setEnabled(false);

		browseTemplateHomeLoc = new Button(group, SWT.PUSH);
		browseTemplateHomeLoc.setText(Message.ide_button_browse);
		browseTemplateHomeLoc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleDirBrowseTemplateHomeLocation();
			}
		});
		browseTemplateHomeLoc.setEnabled(false);

		// Package
		new Label(group, SWT.NONE)
				.setText(Message.wizard_application_packagename);
		packageNameText = new Text(group, SWT.BORDER);
		packageNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		packageNameText.addListener(SWT.KeyDown, packageModifyListener);
		packageNameText.addListener(SWT.Modify, textModifyListener);
		new Label(group, SWT.NONE).setText("");
	}

	private void createPjtArtifactGroup(Composite parent) {
		// Contents Group
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(Message.wizard_modules_check_pjtartifact_title);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(group, SWT.NONE).setText(Message.wizard_maven_groupid);
		pjtGroupIdText = new Text(group, SWT.BORDER);
		pjtGroupIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pjtGroupIdText.setText(Message.wizard_maven_groupid_default);

		pjtGroupIdText.addListener(SWT.Modify, textModifyListener);

		new Label(group, SWT.NONE).setText(Message.wizard_maven_artifactid);
		pjtArtifactIdText = new Text(group, SWT.BORDER);
		pjtArtifactIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pjtArtifactIdText.addListener(SWT.KeyDown, artifactIdModifyListener);
		pjtArtifactIdText.addListener(SWT.Modify, textModifyListener);

		new Label(group, SWT.NONE).setText(Message.wizard_maven_version);
		pjtVersionText = new Text(group, SWT.BORDER);
		pjtVersionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pjtVersionText.setText(Message.wizard_maven_version_default);
		pjtVersionText.addListener(SWT.Modify, textModifyListener);

	}

	private Listener textModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setPageComplete(isPageComplete());
		}
	};

	private Listener applicationNameTextModifyListener = new Listener() {
		public void handleEvent(Event e) {
			if (!isModifiedPackageText)
				packageNameText.setText(getPjtName());
			if (!isModifiedPjtArtifactIdText && !isAntProject)
				pjtArtifactIdText.setText(getPjtName());
			setPageComplete(isPageComplete());
		}
	};

	private Listener packageModifyListener = new Listener() {
		public void handleEvent(Event e) {
			isModifiedPackageText = true;
		}
	};

	private Listener artifactIdModifyListener = new Listener() {
		public void handleEvent(Event e) {
			isModifiedPjtArtifactIdText = true;
		}
	};

	private void handleDirBrowseAppLocation() {
		DirectoryDialog dialog = new DirectoryDialog(locText.getShell());

		String selected = dialog.open();
		if (selected != null) {
			locText.setText(selected);
		} else {
			locText.setText(selected);
		}
	}

	private void handleDirBrowseTemplateHomeLocation() {
		DirectoryDialog dialog = new DirectoryDialog(
				templateHomeText.getShell());

		String selected = dialog.open();
		if (selected != null) {
			templateHomeText.setText(selected);
		} else {
			// templateHomeText.setText(selected);
		}
		setPageComplete(isPageComplete());
	}

	private void getPluginNameListForValidation() {
		try {
			pluginNameList = PluginUtil.getPluginNameSet(this.buildType,
					anyframeHome, isOfflineChecked());
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, Message.view_exception_getpluginlist, e);
		}
	}

	public String getPjtName() {
		return pjtNameText.getText();
	}

	public String getLocation() {
		return locText.getText();
	}

	public String getTemplateHomeLocation() {
		return templateHomeText.getText();
	}

	public String getPackageName() {
		return packageNameText.getText();
	}

	public String getPjtGroupId() {
		return pjtGroupIdText.getText();
	}

	public String getPjtArtifactId() {
		return pjtArtifactIdText.getText();
	}

	public String getPjtVersion() {
		return pjtVersionText.getText();
	}

	public boolean isAntProject() {
		return isAntProject;
	}

	public String getAnyframeHome() {
		return anyframeHome;
	}

	public boolean useDefaultTemplateHome() {
		return templateHome.getSelection();
	}

	public boolean isOfflineChecked() {
		IPreferenceStore store = CodeGeneratorActivator.getDefault()
				.getPreferenceStore();
		return store.getBoolean(IdePreferencesPage.OFFLINE_MODE);
	}

	@Override
	public boolean isPageComplete() {

		if (getPjtName() == null || getPjtName().length() == 0) {
			setErrorMessage(Message.wizard_application_error_pjtname);
			return false;
		} else if (!ProjectUtil.validateName(getPjtName())) {
			setErrorMessage(Message.wizard_application_validation_pjtname);
			return false;
		}

		if (getLocation() == null || getLocation().length() == 0) {
			setErrorMessage(Message.wizard_application_error_apploc);
			return false;
		} else if (!ProjectUtil.existPath(getLocation())
				|| !ProjectUtil.validatePath(getLocation())) {
			setErrorMessage(Message.wizard_application_validation_apploc);
			return false;
		}

		if (ProjectUtil.existPath(getLocation() + ProjectUtil.SLASH
				+ getPjtName())) {
			setErrorMessage(Message.wizard_application_validation_duplicatedpjtname);
			return false;
		}

		if (getPackageName() == null || getPackageName().length() == 0) {
			setErrorMessage(Message.wizard_application_error_pkgname);
			return false;
		} else if (!ProjectUtil.validatePkgName(getPackageName())) {
			setErrorMessage(Message.wizard_application_validation_pkgname);
			return false;
		}

		if (!isAntProject) {
			if (!templateHome.getSelection()
					&& !getTemplateHomeLocation().equals(
							Constants.DFAULT_TEMPLATE_HOME)) {
				if (getTemplateHomeLocation() == null
						|| getTemplateHomeLocation().length() == 0) {
					setErrorMessage(Message.wizard_application_error_templatehome);
					return false;
				} else if (!ProjectUtil.validatePath(getTemplateHomeLocation())) {
					setErrorMessage(Message.wizard_application_validation_templatehome);
					return false;
				}
			}

			if (getPjtGroupId() == null || getPjtGroupId().length() == 0) {
				setErrorMessage(Message.wizard_application_error_pjtgroupid);
				return false;
			} else if (!ProjectUtil.validateName(getPjtGroupId())) {
				setErrorMessage(Message.wizard_application_validation_pjtgroupid);
				return false;
			}
			if (getPjtArtifactId() == null || getPjtArtifactId().length() == 0) {
				setErrorMessage(Message.wizard_application_error_pjtartifactid);
				return false;
			} else if (!ProjectUtil.validateName(getPjtArtifactId())) {
				setErrorMessage(Message.wizard_application_validation_pjtartifactid);
				return false;
			}
			if (getPjtVersion() == null || getPjtVersion().length() == 0) {
				setErrorMessage(Message.wizard_application_error_pjtversion);
				return false;
			} else if (!ProjectUtil.validateName(getPjtVersion())) {
				setErrorMessage(Message.wizard_application_validation_pjtversion);
				return false;
			}
		}

		setErrorMessage(null);
		return true;
	}

	public boolean canFlipToNextPage() {
		boolean isNext = super.canFlipToNextPage();

		if (isNext)
			return true;
		return false;
	}

}
