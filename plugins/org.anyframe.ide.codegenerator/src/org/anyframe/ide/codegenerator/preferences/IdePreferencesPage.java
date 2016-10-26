/*   
 * Copyright 2002-2012 the original author or authors.   
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
package org.anyframe.ide.codegenerator.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.PluginUtil;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This is an IdePreferencesPage class.
 * 
 * @author Eunjin Jang
 */
public class IdePreferencesPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public static final String BUILD_TYPE = "buildType";
	public static final String ANT_BUILD_TYPE = "ANT";
	public static final String MAVEN_BUILD_TYPE = "MAVEN";
	public static final String ANYFRAME_HOME = "anyframeHome";
	public static final String MAVEN_HOME = "mavenHome";
	public static final String OFFLINE_MODE = "offlineMode";
	public static final String BASIC_ARCHETYPE = "BASIC_ARCHETYPE";
	public static final String SERVICE_ARCHETYPE = "SERVICE_ARCHETYPE";
	public static final String LOG_LEVEL = "COMMAND_LOG_LEVEL";
	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	IPreferenceStore store;
	Button antBuildTypeRadio;
	Button mavenBuildTypeRadio;
	Group buildTypeGroup;
	Group homeGroup;
	Group archetypeGroup;
	Group logLevelGroup;

	Label homeLabel;
	Text homeText;
	Button offlineCheck;

	Label basicArchetypeLabel;
	Combo basicArchetypeVersionCombo;

	Label logLevelLabel;
	Combo logLevelCombo;

	public IdePreferencesPage() {
		store = CodeGeneratorActivator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	protected Control createContents(Composite parent) {

		Composite preferenceWindow = new Composite(parent, SWT.NULL);
		preferenceWindow.setLayout(new GridLayout());
		preferenceWindow.setLayoutData(new GridData(GridData.FILL_BOTH));

		createProjectBuildGroup(preferenceWindow);
		createHomeGroup(preferenceWindow);
		createArchetypeGroup(preferenceWindow);
		createLogLevelGroup(preferenceWindow);

		initialize();

		return preferenceWindow;
	}

	private void createProjectBuildGroup(Composite parent) {
		buildTypeGroup = new Group(parent, SWT.NONE);
		buildTypeGroup.setText(Message.ide_preferences_buildtype_title);
		buildTypeGroup.setLayout(new GridLayout());
		buildTypeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		mavenBuildTypeRadio = new Button(buildTypeGroup, SWT.RADIO);
		mavenBuildTypeRadio.setText(MAVEN_BUILD_TYPE);
		mavenBuildTypeRadio
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		mavenBuildTypeRadio.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				homeGroup.setText(Message.ide_preferences_mavenhome_title);
				homeLabel.setText(Message.ide_preferences_mavenhome_label);
				homeText.setText(store.getString(MAVEN_HOME));

				if (offlineCheck != null)
					offlineCheck.setVisible(false);

				basicArchetypeVersionCombo.removeAll();
			}
		});

		antBuildTypeRadio = new Button(buildTypeGroup, SWT.RADIO);
		antBuildTypeRadio.setText(ANT_BUILD_TYPE);
		antBuildTypeRadio.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		antBuildTypeRadio.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				homeGroup.setText(Message.ide_preferences_anyframehome_title);
				homeLabel.setText(Message.ide_preferences_anyframehome_label);
				homeText.setText(store.getString(ANYFRAME_HOME));

				if (offlineCheck != null) {
					offlineCheck.setVisible(true);
					offlineCheck.setSelection(store.getBoolean(OFFLINE_MODE));
				}

				basicArchetypeVersionCombo.removeAll();
			}
		});
	}

	private void createHomeGroup(Composite parent) {
		homeGroup = new Group(parent, SWT.NONE);
		homeGroup.setLayout(new GridLayout(3, false));
		homeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		homeLabel = new Label(homeGroup, SWT.NONE);
		homeText = new Text(homeGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 4;
		homeText.setLayoutData(data);
		Button browse = new Button(homeGroup, SWT.PUSH);
		browse.setText(Message.ide_button_browse);
		Image imageSearch = new Image(homeGroup.getDisplay(), getClass()
				.getResourceAsStream(Message.image_serarch));
		browse.setImage(imageSearch);
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleDirBrowseHomeLocation();
			}
		});
		browse.setLayoutData(new GridData());

		offlineCheck = new Button(homeGroup, SWT.CHECK);
		offlineCheck.setText(Message.wizard_offline_check);
		GridData offlineCheckData = new GridData();
		offlineCheckData.horizontalSpan = 3;
		offlineCheck.setLayoutData(offlineCheckData);
		offlineCheck.setSelection(true);
	}

	private void createArchetypeGroup(Composite parent) {
		archetypeGroup = new Group(parent, SWT.NONE);
		archetypeGroup.setLayout(new GridLayout(2, false));
		archetypeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		basicArchetypeLabel = new Label(archetypeGroup, SWT.NONE);

		basicArchetypeVersionCombo = new Combo(archetypeGroup, SWT.SINGLE
				| SWT.BORDER | SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 4;
		basicArchetypeVersionCombo.setLayoutData(data);
		basicArchetypeVersionCombo.addListener(SWT.DROP_DOWN, new Listener() {
			public void handleEvent(Event e) {
				setArchetypeVersionCombo(CommonConstants.ARCHETYPE_BASIC_ARTIFACT_ID);
			}
		});

//		serviceArchetypeLabel = new Label(archetypeGroup, SWT.NONE);
//		serviceArchetypeVersionCombo = new Combo(archetypeGroup, SWT.SINGLE
//				| SWT.BORDER | SWT.READ_ONLY);
//		serviceArchetypeVersionCombo.setLayoutData(data);
//		serviceArchetypeVersionCombo.addListener(SWT.DROP_DOWN, new Listener() {
//			public void handleEvent(Event e) {
//				setArchetypeVersionCombo(CommonConstants.ARCHETYPE_SERVICE_ARTIFACT_ID);
//			}
//		});
	}

	private void createLogLevelGroup(Composite parent) {
		logLevelGroup = new Group(parent, SWT.NONE);
		logLevelGroup.setLayout(new GridLayout(2, false));
		logLevelGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		logLevelLabel = new Label(logLevelGroup, SWT.NONE);

		logLevelCombo = new Combo(logLevelGroup, SWT.SINGLE | SWT.BORDER
				| SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 4;
		logLevelCombo.setLayoutData(data);
		logLevelCombo.add(CommonConstants.LOG_LEVEL_ERROR, 0);
		logLevelCombo.add(CommonConstants.LOG_LEVEL_INFO, 1);
		logLevelCombo.add(CommonConstants.LOG_LEVEL_DEBUG, 2);

		String logLevel = store.getString(LOG_LEVEL);
		if (StringUtils.isEmpty(logLevel))
			logLevelCombo.select(1);
		else
			logLevelCombo.setText(logLevel);
	}

	private void setArchetypeVersionCombo(String archetypeId) {
		if (antBuildTypeRadio.getSelection())
			setVersionText(archetypeId, ANT_BUILD_TYPE.toLowerCase(),
					homeText.getText(), offlineCheck.getSelection());
		else
			setVersionText(archetypeId, MAVEN_BUILD_TYPE.toLowerCase(),
					homeText.getText(), false);
	}

	private void handleDirBrowseHomeLocation() {
		DirectoryDialog dialog = new DirectoryDialog(homeText.getShell());

		String selected = dialog.open();
		if (selected != null) {
			homeText.setText(selected);
		}
	}

	private void initialize() {
		if (store.getString(BUILD_TYPE) != null
				&& store.getString(BUILD_TYPE).equals(ANT_BUILD_TYPE)) {
			antBuildTypeRadio.setSelection(true);
			mavenBuildTypeRadio.setSelection(false);

			offlineCheck.setVisible(true);
			offlineCheck.setSelection(store.getBoolean(OFFLINE_MODE));

			homeGroup.setText(Message.ide_preferences_anyframehome_title);
			homeLabel.setText(Message.ide_preferences_anyframehome_label);
			homeText.setText(store.getString(ANYFRAME_HOME));

		} else {
			antBuildTypeRadio.setSelection(false);
			mavenBuildTypeRadio.setSelection(true);
			homeGroup.setText(Message.ide_preferences_mavenhome_title);
			homeLabel.setText(Message.ide_preferences_mavenhome_label);
			homeText.setText(store.getString(MAVEN_HOME));

			if (offlineCheck != null)
				offlineCheck.setVisible(false);

		}

		logLevelGroup.setText(Message.ide_preferences_loglevel_title);
		logLevelLabel.setText(Message.ide_preferences_loglevel_label);

		archetypeGroup.setText(Message.ide_preferences_archetype_title);
		basicArchetypeLabel
				.setText(Message.ide_preferences_archetype_basic_label);

		String basicStore = store.getString(BASIC_ARCHETYPE);
		if (StringUtils.isNotEmpty(basicStore)) {
			basicArchetypeVersionCombo.add(basicStore);
			basicArchetypeVersionCombo.select(0);
		}
	}

	private void setVersionText(String archetypeId, String buildType,
			String homeText, boolean isOffline) {

		String basicStore = store.getString(BASIC_ARCHETYPE);

		try {
			if (StringUtils.isNotEmpty(homeText)) {
				// set basicArchetypeVersionCombo
				if (archetypeId
						.equals(CommonConstants.ARCHETYPE_BASIC_ARTIFACT_ID))
					setBasicArchetypeVersionCombo(buildType, homeText,
							isOffline, basicStore);
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(ID,
					Message.ide_preferences_exception_error_archetypeversions,
					e);
		}
	}

	private void setBasicArchetypeVersionCombo(String buildType,
			String homeText, boolean isOffline, String basicStore)
			throws Exception {
		List<String> versions = PluginUtil.getArchetypeVersions(
				CommonConstants.ARCHETYPE_GROUP_ID,
				CommonConstants.ARCHETYPE_BASIC_ARTIFACT_ID, buildType,
				homeText, isOffline);

		List<String> comboVersions = new ArrayList<String>();
		for (String comboVersion : basicArchetypeVersionCombo.getItems()) {
			comboVersions.add(comboVersion);
		}

		for (String version : versions) {
			if (!comboVersions.contains(version))
				basicArchetypeVersionCombo.add(version);
		}

	}

	public void init(IWorkbench iworkbench) {
	}

	protected void performDefaults() {
		super.performDefaults();
		initialize();
	}

	public boolean performOk() {

		if (antBuildTypeRadio.getSelection()) {
			if (getHomeLocation() == null || getHomeLocation().length() == 0) {
				if (!MessageDialogUtil
						.confirmMessageDialog(
								Message.ide_message_title,
								Message.ide_preferences_anyframehome_validate_specified)) {
					return false;
				}
			}

			if (!ProjectUtil.existPath(getHomeLocation() + ProjectUtil.SLASH
					+ "bin")
					|| !ProjectUtil.existPath(getHomeLocation()
							+ ProjectUtil.SLASH + "ide")) {
				MessageDialogUtil.openMessageDialog(Message.ide_message_title,
						Message.ide_preferences_anyframehome_validate_correct,
						MessageDialog.ERROR);
				return false;
			}
			store.setValue(BUILD_TYPE, ANT_BUILD_TYPE);
			store.setValue(ANYFRAME_HOME, getHomeLocation());
			store.setValue(OFFLINE_MODE, this.offlineCheck.getSelection());
		} else {
			if (getHomeLocation() == null || getHomeLocation().length() == 0) {
				if (!MessageDialogUtil.confirmMessageDialog(
						Message.ide_message_title,
						Message.ide_preferences_mavenhome_validate_specified)) {
					return false;
				}
			}

			if (!ProjectUtil.existPath(getHomeLocation())
					|| !ProjectUtil.existPath(getHomeLocation()
							+ ProjectUtil.SLASH + "lib")) {
				MessageDialogUtil.openMessageDialog(Message.ide_message_title,
						Message.ide_preferences_mavenhome_validate_correct,
						MessageDialog.ERROR);
				return false;

			} else {
				boolean isValid = false;
				File mavenLib = new File(getHomeLocation() + ProjectUtil.SLASH
						+ "lib");
				if (mavenLib.exists() && mavenLib.isDirectory()
						&& mavenLib.listFiles().length > 0) {
					File[] files = mavenLib.listFiles();
					for (int i = 0; i < files.length; i++) {
						File file = files[i];
						if (file.isFile()
								&& file.getName().endsWith("uber.jar")) {
							isValid = true;
							break;
						}
					}
				}
				if (!isValid) {
					MessageDialogUtil.openMessageDialog(
							Message.ide_message_title,
							Message.ide_preferences_mavenhome_validate_correct,
							MessageDialog.ERROR);
					return isValid;
				}
			}
			store.setValue(BUILD_TYPE, MAVEN_BUILD_TYPE);
			store.setValue(MAVEN_HOME, getHomeLocation());
		}

		store.setValue(BASIC_ARCHETYPE, getBasicArchetypeVersion());
		store.setValue(LOG_LEVEL, getLogLevel());

		return super.performOk();
	}

	public String getHomeLocation() {
		return homeText.getText();
	}

	public String getLogLevel() {
		return this.logLevelCombo.getText();
	}

	public String getBasicArchetypeVersion() {
		return this.basicArchetypeVersionCombo.getText();
	}
}
