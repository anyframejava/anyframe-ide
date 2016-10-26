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
package org.anyframe.ide.eclipse.core.wizards;

import java.util.Set;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.eclipse.core.AnyframeIDEPlugin;
import org.anyframe.ide.eclipse.core.preferences.IdePreferencesPage;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.PluginUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * This is an NewApplicationWizardPage class.
 * @author Changje Kim
 * @author Sooyeon Park
 * @author Eunjin Jang
 */
public class NewApplicationWizardPage extends WizardPage {

    private Text pjtNameText;
    private Text locText;

    private Text packageNameText;
    private boolean isModifiedPackageText = false;
    private boolean isModifiedPjtArtifactIdText = false;

    private Button[] pjtTypeRadios;

    private Text pjtGroupIdText;
    private Text pjtArtifactIdText;
    private Text pjtVersionText;

    private static IPreferenceStore store;
    private String buildType;
    private boolean isAntProject;
    private String anyframeHome;
    private Combo packagingCombo;

    private Set<String> pluginNameList;

    private Button templateHome;
    private Text templateHomeText;
    private Button browseTemplateHomeLoc;

    protected NewApplicationWizardPage(String pageName) {
        super(pageName);
        this.setTitle(MessageUtil.getMessage("wizard.application.title"));
        this.setDescription(MessageUtil
            .getMessage("wizard.application.description"));
        if (store == null)
            store = AnyframeIDEPlugin.getDefault().getPreferenceStore();
    }

    private void initialize() {
        buildType =
            (store.getString(IdePreferencesPage.BUILD_TYPE).equals("")
                ? IdePreferencesPage.ANT_BUILD_TYPE : store
                    .getString(IdePreferencesPage.BUILD_TYPE)).toLowerCase();

        isAntProject =
            buildType.equals(CommonConstants.PROJECT_BUILD_TYPE_ANT)
                ? true : false;

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
            createPjtTypeGroup(composite);
        } else {
            createPjtArtifactGroup(composite);
        }

        //getPluginNameListForValidation();

        setPageComplete(false);
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
    }

    private void createPjtNameFields(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayout(new GridLayout(2, false));
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(comp, SWT.NONE).setText(MessageUtil
            .getMessage("wizard.application.name"));
        pjtNameText = new Text(comp, SWT.BORDER);
        pjtNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pjtNameText.addListener(SWT.Modify, applicationNameTextModifyListener);
    }

    private void createContentsGroup(Composite parent) {
        // Contents Group
        Group group = new Group(parent, SWT.SHADOW_NONE);
        group.setText(MessageUtil.getMessage("wizard.application.contents"));
        group.setLayout(new GridLayout(3, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Application Location
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("wizard.application.location"));
        locText = new Text(group, SWT.BORDER);
        locText.addListener(SWT.Modify, textModifyListener);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = 10;
        locText.setLayoutData(gridData);

        if (isAntProject) {
            locText.setText(anyframeHome + ProjectUtil.SLASH + "applications");
        } else {
            IWorkspaceRoot workspaceRoot =
                ResourcesPlugin.getWorkspace().getRoot();
            locText.setText(workspaceRoot.getLocation().toOSString());
        }

        Button browseAppLoc = new Button(group, SWT.PUSH);
        browseAppLoc.setText(MessageUtil.getMessage("ide.button.browse"));
        Image imageSearchApp =
            new Image(group.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.serarch")));
        browseAppLoc.setImage(imageSearchApp);
        browseAppLoc.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDirBrowseAppLocation();
            }
        });

        // When project build type is maven, set
        // template home
        if (!isAntProject) {
            templateHome = new Button(group, SWT.CHECK);
            templateHome.setText(MessageUtil
                .getMessage("wizard.templatehome.location.check"));
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
                        templateHomeText.setText(System.getProperty("user.home")
                            + ProjectUtil.SLASH + ".anyframe" + ProjectUtil.SLASH
                            + "templates");
                    } else {
                        templateHomeText.setEnabled(true);
                        browseTemplateHomeLoc.setEnabled(true);
                    }
                    setPageComplete(isPageComplete());
                }
            });

            // Template home location
            new Label(group, SWT.NONE).setText(MessageUtil
                .getMessage("wizard.templatehome.location"));
            templateHomeText = new Text(group, SWT.BORDER);
            templateHomeText.addListener(SWT.Modify, textModifyListener);
            GridData templateHomegridData =
                new GridData(GridData.FILL_HORIZONTAL);
            gridData.widthHint = 10;
            templateHomeText.setLayoutData(templateHomegridData);
            templateHomeText.setText(System.getProperty("user.home")
                + ProjectUtil.SLASH + ".anyframe" + ProjectUtil.SLASH
                + "templates");
            templateHomeText.setEnabled(false);

            browseTemplateHomeLoc = new Button(group, SWT.PUSH);
            browseTemplateHomeLoc.setText(MessageUtil
                .getMessage("ide.button.browse"));
            Image imageSearchTemplateHome =
                new Image(group.getDisplay(), getClass().getResourceAsStream(
                    MessageUtil.getMessage("image.serarch")));
            browseTemplateHomeLoc.setImage(imageSearchTemplateHome);
            browseTemplateHomeLoc.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    handleDirBrowseTemplateHomeLocation();
                }
            });
            browseTemplateHomeLoc.setEnabled(false);
        }

        // Package
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("wizard.application.packagename"));
        packageNameText = new Text(group, SWT.BORDER);
        packageNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        packageNameText.addListener(SWT.KeyDown, packageModifyListener);
        packageNameText.addListener(SWT.Modify, textModifyListener);
        new Label(group, SWT.NONE).setText("");
    }

    private void createPjtTypeGroup(Composite parent) {
        // Contents Group
        Group group = new Group(parent, SWT.SHADOW_NONE);
        group.setText(MessageUtil
            .getMessage("wizard.modules.check.pjttype.title"));
        group.setLayout(new GridLayout(3, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pjtTypeRadios = new Button[2];
        pjtTypeRadios[0] = new Button(group, SWT.RADIO);
        pjtTypeRadios[0].setText(MessageUtil
            .getMessage("wizard.modules.check.pjttype.web"));
        pjtTypeRadios[0].setSelection(true);
        // pjtTypeRadios[0].addListener(SWT.Selection,
        // pluginNameListener);

        pjtTypeRadios[1] = new Button(group, SWT.RADIO);
        pjtTypeRadios[1].setText(MessageUtil
            .getMessage("wizard.modules.check.pjttype.service"));
        // pjtTypeRadios[1].addListener(SWT.Selection,
        // pluginNameListener);
    }

    private void createPjtArtifactGroup(Composite parent) {
        // Contents Group
        Group group = new Group(parent, SWT.SHADOW_NONE);
        group.setText(MessageUtil
            .getMessage("wizard.modules.check.pjtartifact.title"));
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("wizard.maven.groupid"));
        pjtGroupIdText = new Text(group, SWT.BORDER);
        pjtGroupIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pjtGroupIdText.setText(MessageUtil
            .getMessage("wizard.maven.groupid.default"));

        pjtGroupIdText.addListener(SWT.Modify, textModifyListener);

        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("wizard.maven.artifactid"));
        pjtArtifactIdText = new Text(group, SWT.BORDER);
        pjtArtifactIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pjtArtifactIdText.addListener(SWT.KeyDown, artifactIdModifyListener);
        pjtArtifactIdText.addListener(SWT.Modify, textModifyListener);

        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("wizard.maven.version"));
        pjtVersionText = new Text(group, SWT.BORDER);
        pjtVersionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pjtVersionText.setText(MessageUtil
            .getMessage("wizard.maven.version.default"));
        pjtVersionText.addListener(SWT.Modify, textModifyListener);

        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("wizard.maven.packaging"));

        packagingCombo =
            new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        packagingCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        packagingCombo
            .add(MessageUtil.getMessage("wizard.maven.packaging.war"));
        packagingCombo
            .add(MessageUtil.getMessage("wizard.maven.packaging.jar"));
        packagingCombo.select(0);
        // packagingCombo.addListener(SWT.Selection,
        // pluginNameListener);
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

    // private Listener pluginNameListener = new
    // Listener() {
    // public void handleEvent(Event e) {
    // getPluginNameListForValidation();
    // }
    // };

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
        DirectoryDialog dialog =
            new DirectoryDialog(templateHomeText.getShell());

        String selected = dialog.open();
        if (selected != null) {
            templateHomeText.setText(selected);
        } else {
            templateHomeText.setText(selected);
        }
        setPageComplete(isPageComplete());
    }

    private void getPluginNameListForValidation() {
        try {
            pluginNameList =
                PluginUtil.getPluginNameSet(this.buildType, anyframeHome,
                    isOfflineChecked());
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.getpluginlist"),
                IStatus.ERROR, e);
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

    public boolean useWebProject() {
        if (isAntProject)
            return pjtTypeRadios[0].getSelection();
        else
            return packagingCombo.getText().equals(
                MessageUtil.getMessage("wizard.maven.packaging.war"));
    }

    public boolean useServiceProject() {
        if (isAntProject)
            return pjtTypeRadios[1].getSelection();
        else
            return packagingCombo.getText().equals(
                MessageUtil.getMessage("wizard.maven.packaging.jar"));
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
        IPreferenceStore store =
            AnyframeIDEPlugin.getDefault().getPreferenceStore();
        return store.getBoolean(IdePreferencesPage.OFFLINE_MODE);
    }

    @Override
    public boolean isPageComplete() {

        if (getPjtName() == null || getPjtName().length() == 0) {
            setErrorMessage(MessageUtil
                .getMessage("wizard.application.error.pjtname"));
            return false;
        } else if (!ProjectUtil.validateName(getPjtName())) {
            setErrorMessage(MessageUtil
                .getMessage("wizard.application.validation.pjtname"));
            return false;
        }

        if (getLocation() == null || getLocation().length() == 0) {
            setErrorMessage(MessageUtil
                .getMessage("wizard.application.error.apploc"));
            return false;
        } else if (!ProjectUtil.existPath(getLocation())
            || !ProjectUtil.validatePath(getLocation())) {
            setErrorMessage(MessageUtil
                .getMessage("wizard.application.validation.apploc"));
            return false;
        }

        if (ProjectUtil.existPath(getLocation() + ProjectUtil.SLASH
            + getPjtName())) {
            setErrorMessage(MessageUtil
                .getMessage("wizard.application.validation.duplicatedpjtname"));
            return false;
        }

        if (getPackageName() == null || getPackageName().length() == 0) {
            setErrorMessage(MessageUtil
                .getMessage("wizard.application.error.pkgname"));
            return false;
        } else if (!ProjectUtil.validatePkgName(getPackageName())) {
            setErrorMessage(MessageUtil
                .getMessage("wizard.application.validation.pkgname"));
            return false;
        } 
//        else {
//            try {
//                if (pluginNameList.contains(getPackageName())) {
//                    setErrorMessage(MessageUtil
//                        .getMessage("wizard.application.error.pkgname.valid"));
//                    return false;
//                }
//            } catch (Exception e) {
//                ExceptionUtil.showException(
//                    MessageUtil.getMessage("editor.exception.getpluginlist"),
//                    IStatus.ERROR, e);
//            }
//        }

        if (!isAntProject) {
            if (!templateHome.getSelection()) {
                if (getTemplateHomeLocation() == null
                    || getTemplateHomeLocation().length() == 0) {
                    setErrorMessage(MessageUtil
                        .getMessage("wizard.application.error.templatehome"));
                    return false;
                } else if (!ProjectUtil.existPath(getTemplateHomeLocation())
                    || !ProjectUtil.validatePath(getTemplateHomeLocation())) {
                    setErrorMessage(MessageUtil
                        .getMessage("wizard.application.validation.templatehome"));
                    return false;
                }
            }

            if (getPjtGroupId() == null || getPjtGroupId().length() == 0) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.error.pjtgroupid"));
                return false;
            } else if (!ProjectUtil.validateName(getPjtGroupId())) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.validation.pjtgroupid"));
                return false;
            }
            if (getPjtArtifactId() == null || getPjtArtifactId().length() == 0) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.error.pjtartifactid"));
                return false;
            } else if (!ProjectUtil.validateName(getPjtArtifactId())) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.validation.pjtartifactid"));
                return false;
            }
            if (getPjtVersion() == null || getPjtVersion().length() == 0) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.error.pjtversion"));
                return false;
            } else if (!ProjectUtil.validateName(getPjtVersion())) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.validation.pjtversion"));
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
