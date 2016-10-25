/*   
 * Copyright 2002-2010 the original author or authors.   
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
package org.anyframe.ide.eclipse.core.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.eclipse.core.AnyframeIDEPlugin;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.PluginUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
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

    IPreferenceStore store;
    Button antBuildTypeRadio;
    Button mavenBuildTypeRadio;
    Group buildTypeGroup;
    Group homeGroup;
    Group archetypeGroup;

    Label homeLabel;
    Text homeText;
    Button offlineCheck;

    Label basicArchetypeLabel;
    private Combo basicArchetypeVersionCombo;

    Label serviceArchetypeLabel;
    private Combo serviceArchetypeVersionCombo;

    public IdePreferencesPage() {
        store = AnyframeIDEPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    protected Control createContents(Composite parent) {

        Composite preferenceWindow = new Composite(parent, SWT.NULL);
        preferenceWindow.setLayout(new GridLayout());
        preferenceWindow.setLayoutData(new GridData(GridData.FILL_BOTH));

        createProjectBuildGroup(preferenceWindow);
        createHomeGroup(preferenceWindow);
        createArchetypeGroup(preferenceWindow);

        initialize();

        return preferenceWindow;
    }

    private void createProjectBuildGroup(Composite parent) {
        buildTypeGroup = new Group(parent, SWT.NONE);
        buildTypeGroup.setText(MessageUtil
            .getMessage("ide.preferences.buildtype.title"));
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
                homeGroup.setText(MessageUtil
                    .getMessage("ide.preferences.mavenhome.title"));
                homeLabel.setText(MessageUtil
                    .getMessage("ide.preferences.mavenhome.label"));
                homeText.setText(store.getString(MAVEN_HOME));

                if (offlineCheck != null)
                    offlineCheck.setVisible(false);
            }
        });

        antBuildTypeRadio = new Button(buildTypeGroup, SWT.RADIO);
        antBuildTypeRadio.setText(ANT_BUILD_TYPE);
        antBuildTypeRadio.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        antBuildTypeRadio.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent arg0) {
            }

            public void widgetSelected(SelectionEvent arg0) {
                homeGroup.setText(MessageUtil
                    .getMessage("ide.preferences.anyframehome.title"));
                homeLabel.setText(MessageUtil
                    .getMessage("ide.preferences.anyframehome.label"));
                homeText.setText(store.getString(ANYFRAME_HOME));

                if (offlineCheck != null) {
                    offlineCheck.setVisible(true);
                    offlineCheck.setSelection(store.getBoolean(OFFLINE_MODE));
                }
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
        browse.setText(MessageUtil.getMessage("ide.button.browse"));
        Image imageSearch =
            new Image(homeGroup.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.serarch")));
        browse.setImage(imageSearch);
        browse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDirBrowseHomeLocation();
            }
        });
        browse.setLayoutData(new GridData());

        offlineCheck = new Button(homeGroup, SWT.CHECK);
        offlineCheck.setText(MessageUtil.getMessage("wizard.offline.check"));
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

        basicArchetypeVersionCombo =
            new Combo(archetypeGroup, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 4;
        basicArchetypeVersionCombo.setLayoutData(data);
        basicArchetypeVersionCombo.addListener(SWT.DROP_DOWN, new Listener() {
            public void handleEvent(Event e) {
                setArchetypeVersionCombo(CommonConstants.ARCHETYPE_BASIC_ARTIFACT_ID);
            }
        });

        serviceArchetypeLabel = new Label(archetypeGroup, SWT.NONE);
        serviceArchetypeVersionCombo =
            new Combo(archetypeGroup, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        serviceArchetypeVersionCombo.setLayoutData(data);
        serviceArchetypeVersionCombo.addListener(SWT.DROP_DOWN, new Listener() {
            public void handleEvent(Event e) {
                setArchetypeVersionCombo(CommonConstants.ARCHETYPE_SERVICE_ARTIFACT_ID);
            }
        });
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

            homeGroup.setText(MessageUtil
                .getMessage("ide.preferences.anyframehome.title"));
            homeLabel.setText(MessageUtil
                .getMessage("ide.preferences.anyframehome.label"));
            homeText.setText(store.getString(ANYFRAME_HOME));
        } else {
            antBuildTypeRadio.setSelection(false);
            mavenBuildTypeRadio.setSelection(true);
            homeGroup.setText(MessageUtil
                .getMessage("ide.preferences.mavenhome.title"));
            homeLabel.setText(MessageUtil
                .getMessage("ide.preferences.mavenhome.label"));
            homeText.setText(store.getString(MAVEN_HOME));

            if (offlineCheck != null)
                offlineCheck.setVisible(false);
        }

        archetypeGroup.setText(MessageUtil
            .getMessage("ide.preferences.archetype.title"));
        basicArchetypeLabel.setText(MessageUtil
            .getMessage("ide.preferences.archetype.basic.label"));
        serviceArchetypeLabel.setText(MessageUtil
            .getMessage("ide.preferences.archetype.service.label"));

        String basicStore = store.getString(BASIC_ARCHETYPE);
        String serviceStore = store.getString(SERVICE_ARCHETYPE);
        if (StringUtils.isNotEmpty(basicStore)) {
            basicArchetypeVersionCombo.add(basicStore);
            basicArchetypeVersionCombo.select(0);
        }
        if (StringUtils.isNotEmpty(serviceStore)) {
            serviceArchetypeVersionCombo.add(serviceStore);
            serviceArchetypeVersionCombo.select(0);
        }
    }

    private void setVersionText(String archetypeId, String buildType,
            String homeText, boolean isOffline) {

        String basicStore = store.getString(BASIC_ARCHETYPE);
        String serviceStore = store.getString(SERVICE_ARCHETYPE);

        try {
            if (StringUtils.isNotEmpty(homeText)) {
                // set basicArchetypeVersionCombo
                if (archetypeId
                    .equals(CommonConstants.ARCHETYPE_BASIC_ARTIFACT_ID))
                    setBasicArchetypeVersionCombo(buildType, homeText,
                        isOffline, basicStore);

                // set serviceArchetypeVersionCombo
                if (archetypeId
                    .equals(CommonConstants.ARCHETYPE_SERVICE_ARTIFACT_ID))
                    setServiceArchetypeVersionCombo(buildType, homeText,
                        isOffline, serviceStore);
            }
        } catch (Exception e) {
            ExceptionUtil
                .showException(
                    MessageUtil
                        .getMessage("ide.preferences.exception.error.archetypeversions"),
                    IStatus.ERROR, e);
        }
    }

    private void setServiceArchetypeVersionCombo(String buildType,
            String homeText, boolean isOffline, String serviceStore)
            throws Exception {
        List<String> versions =
            PluginUtil.getArchetypeVersions(CommonConstants.ARCHETYPE_GROUP_ID,
                CommonConstants.ARCHETYPE_SERVICE_ARTIFACT_ID, buildType,
                homeText, isOffline);

        List<String> comboVersions = new ArrayList<String>();
        for (String comboVersion : serviceArchetypeVersionCombo.getItems()) {
            comboVersions.add(comboVersion);
        }

        for (String version : versions) {
            if (!comboVersions.contains(version))
                serviceArchetypeVersionCombo.add(version);
        }
    }

    private void setBasicArchetypeVersionCombo(String buildType,
            String homeText, boolean isOffline, String basicStore)
            throws Exception {
        List<String> versions =
            PluginUtil.getArchetypeVersions(CommonConstants.ARCHETYPE_GROUP_ID,
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
                if (!DialogUtil
                    .confirmMessageDialog(
                        MessageUtil.getMessage("ide.message.title"),
                        MessageUtil
                            .getMessage("ide.preferences.anyframehome.validate.specified"))) {
                    return false;
                }
            }

            if (!ProjectUtil.existPath(getHomeLocation() + ProjectUtil.SLASH
                + "templates")
                || !ProjectUtil.existPath(getHomeLocation() + ProjectUtil.SLASH
                    + "ide")) {
                DialogUtil
                    .openMessageDialog(
                        MessageUtil.getMessage("ide.message.title"),
                        MessageUtil
                            .getMessage("ide.preferences.anyframehome.validate.correct"),
                        MessageDialog.ERROR);
                return false;
            }
            store.setValue(BUILD_TYPE, ANT_BUILD_TYPE);
            store.setValue(ANYFRAME_HOME, getHomeLocation());
            store.setValue(OFFLINE_MODE, this.offlineCheck.getSelection());
            store.setValue(BASIC_ARCHETYPE, getBasicArchetypeVersion());
            store.setValue(SERVICE_ARCHETYPE, getServiceArchetypeVersion());
        } else {
            if (getHomeLocation() == null || getHomeLocation().length() == 0) {
                if (!DialogUtil
                    .confirmMessageDialog(
                        MessageUtil.getMessage("ide.message.title"),
                        MessageUtil
                            .getMessage("ide.preferences.mavenhome.validate.specified"))) {
                    return false;
                }
            }

            if (!ProjectUtil.existPath(getHomeLocation())
                || !ProjectUtil.existPath(getHomeLocation() + ProjectUtil.SLASH
                    + "lib")) {
                DialogUtil.openMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("ide.preferences.mavenhome.validate.correct"),
                    MessageDialog.ERROR);
                return false;

            } else {
                boolean isValid = false;
                File mavenLib =
                    new File(getHomeLocation() + ProjectUtil.SLASH + "lib");
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
                    DialogUtil
                        .openMessageDialog(
                            MessageUtil.getMessage("ide.message.title"),
                            MessageUtil
                                .getMessage("ide.preferences.mavenhome.validate.correct"),
                            MessageDialog.ERROR);
                    return isValid;
                }
            }
            store.setValue(BUILD_TYPE, MAVEN_BUILD_TYPE);
            store.setValue(MAVEN_HOME, getHomeLocation());
            store.setValue(BASIC_ARCHETYPE, getBasicArchetypeVersion());
            store.setValue(SERVICE_ARCHETYPE, getServiceArchetypeVersion());

        }

        return super.performOk();
    }

    public String getHomeLocation() {
        return homeText.getText();
    }

    public String getBasicArchetypeVersion() {
        return this.basicArchetypeVersionCombo.getText();
    }

    public String getServiceArchetypeVersion() {
        return this.serviceArchetypeVersionCombo.getText();
    }
}
