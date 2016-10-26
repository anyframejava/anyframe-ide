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
package org.anyframe.ide.eclipse.core.editor;

import java.util.ArrayList;

import net.miginfocom.swt.MigLayout;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.command.maven.mojo.codegen.SourceCodeChecker;
import org.anyframe.ide.eclipse.core.CommandExecution;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.anyframe.ide.eclipse.core.util.SearchUtil;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.hibernate.tool.hbm2x.StringUtils;

/**
 * This is an CRUDGenPage class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class CRUDGenPage implements Page {
    private CodeGenEditor ideEditor;
    private Button buttonBuild;
    private Button buttonRefresh;
    private Button webProjectCheck;
    private Button insertSampleDataCheck;

    private List domainObjectList;
    private Text serviceNameText;
    private Text packageNameText;
    private Text servicePjtNameText;
    private FormToolkit toolkit;

    private String packageName = "";
    private String webProjectName = "";
    private PropertiesIO projectMF;

    private String projectName = "";
    private String projectType = "";

    public CRUDGenPage(CodeGenEditor ideEditor) {
        this.ideEditor = ideEditor;
    }

    public Composite getPage(Composite parent) {
        toolkit = new FormToolkit(parent.getDisplay());
        final ScrolledForm form = toolkit.createScrolledForm(parent);
        form.setText(MessageUtil.getMessage("editor.codegen.crud.title"));
        form.getBody().setLayout(new FillLayout());

        Composite container = toolkit.createComposite(form.getBody(), SWT.NONE);
        container.setLayout(new MigLayout("fillx"));

        createDomainSection(form, container);
        createProjectSection(form, container);
        loadListClass("javax.persistence.Entity");
        setProject();

        createCommandButton(form, container);

        form.reflow(true);

        return form;
    }

    private void createDomainSection(final ScrolledForm form, Composite parent) {
        Section section =
            toolkit.createSection(parent, Section.TWISTIE | Section.EXPANDED
                | Section.DESCRIPTION | Section.TITLE_BAR);
        section.setText(MessageUtil.getMessage("editor.crud.section.domain"));
        section.setDescription(MessageUtil
            .getMessage("editor.crud.description"));
        section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });
        section.setLayout(new FillLayout());
        section.setLayoutData("growx,wrap");

        Composite container = toolkit.createComposite(section, SWT.NONE);
        container.setLayout(new MigLayout("fillx", "[]"));
        section.setClient(container);

        domainObjectList =
            new List(container, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);

        domainObjectList.addListener(SWT.Selection, listener);
        domainObjectList.setLayoutData("hmin 150px, hmax 150px, growx, wrap");

    }

    private void createProjectSection(final ScrolledForm form, Composite parent) {
        // get properties
        try {
            // get default package
            projectMF =
                ProjectUtil.getProjectProperties(this.ideEditor
                    .getCurrentProject());
            packageName = projectMF.readValue(CommonConstants.PACKAGE_NAME);
            webProjectName = projectMF.readValue(CommonConstants.PROJECT_NAME);
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.pjtsection"),
                IStatus.ERROR, e);
        }

        // UI
        Section section =
            toolkit.createSection(parent, Section.TWISTIE | Section.EXPANDED
                | Section.DESCRIPTION | Section.TITLE_BAR);
        section.setText(MessageUtil.getMessage("editor.crud.section.project"));
        section.setDescription(MessageUtil
            .getMessage("editor.curd.description.crud"));
        section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });
        section.setLayout(new FillLayout());
        section.setLayoutData("growx,wrap");

        Composite container = toolkit.createComposite(section, SWT.NONE);
        container.setLayout(new MigLayout("fillx",
            "5%[left][left,25%]push[left][left,25%][2,left]10%"));
        container.setLayoutData("wrap");

        section.setClient(container);

        // Service Name
        Label label =
            toolkit.createLabel(container,
                MessageUtil.getMessage("editor.crud.servicename"), SWT.WRAP);
        serviceNameText =
            toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER);
        serviceNameText.setLayoutData("growx");
        serviceNameText.setEnabled(false);

        // Package
        toolkit.createLabel(container,
            MessageUtil.getMessage("editor.crud.packagename"));
        label = toolkit.createLabel(container, packageName + ".");
        label.setLayoutData("split 2");
        packageNameText = toolkit.createText(container, "", SWT.SINGLE);
        packageNameText.setLayoutData("growx, wrap, gapleft 0");

        // Service Project
        label =
            toolkit.createLabel(container,
                MessageUtil.getMessage("editor.crud.servicepjtname"),
                SWT.SINGLE);
        servicePjtNameText =
            toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER);
        servicePjtNameText.setLayoutData("growx");
        servicePjtNameText.setEnabled(false);

        // Web Module
        webProjectCheck = new Button(container, SWT.CHECK);
        webProjectCheck.setText(MessageUtil
            .getMessage("editor.crud.genwebsource"));
        webProjectCheck.setLayoutData("growx, span 3, wrap");

        // check Web Type
        if (webProjectName == null || webProjectName.length() == 0)
            webProjectCheck.setSelection(false);
        else
            webProjectCheck.setSelection(true);
        
        // Sample Data
        insertSampleDataCheck = new Button(container, SWT.CHECK);
        insertSampleDataCheck.setText(MessageUtil
            .getMessage("editor.crud.insertsampledata"));
        insertSampleDataCheck.setLayoutData("growx, span 3");

        // check whether input sample data to database
        insertSampleDataCheck.setSelection(true);
    }

    private void createCommandButton(ScrolledForm form, Composite parent) {

        Label label =
            toolkit.createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData("growx,gapy 20,wrap");

        Composite container = toolkit.createComposite(parent, SWT.NONE);
        Image imageBuild =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.build")));
        container.setLayout(new MigLayout("fillx"));

        buttonBuild =
            toolkit.createButton(container,
                MessageUtil.getMessage("ide.button.build"), SWT.PUSH);
        buttonBuild.setImage(imageBuild);
        buttonBuild.addListener(SWT.Selection, listener);

        Image imageRefresh =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.refresh")));
        buttonRefresh = toolkit.createButton(container, "", SWT.PUSH);
        buttonRefresh.setImage(imageRefresh);
        buttonRefresh.setText(MessageUtil.getMessage("ide.button.refresh"));
        buttonRefresh.addListener(SWT.Selection, listener);

    }

    private void setProject() {
        try {
            this.projectMF =
                ProjectUtil.getProjectProperties(this.ideEditor
                    .getCurrentProject());
            projectName =
                this.projectMF.readValue(CommonConstants.PROJECT_NAME);
            projectType =
                this.projectMF.readValue(CommonConstants.PROJECT_TYPE);

            if (projectType.equals(CommonConstants.PROJECT_TYPE_SERVICE)) {
                webProjectCheck.setEnabled(false);
                webProjectCheck.setSelection(false);
            }

        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.loadconfig"),
                IStatus.ERROR, e);
        }
    }

    private void loadListClass(String annotation) {
        ArrayList<String> listAnnotations = new ArrayList<String>();
        domainObjectList.removeAll();
        listAnnotations.add(annotation);
        ArrayList<ResolvedSourceType> listClassFile =
            SearchUtil.search("*", listAnnotations,
                this.ideEditor.getCurrentProject());

        for (ResolvedSourceType classFile : listClassFile) {
            domainObjectList.add(classFile.getFullyQualifiedName());
        }

        serviceNameText.setText("");
        packageNameText.setText("");
        servicePjtNameText.setText("");
    }

    private Listener listener = new Listener() {
        public void handleEvent(Event event) {

            // get domainClassName
            String domainClassName = "";

            if (domainObjectList.getSelectionIndex() > -1) {
                domainClassName =
                    domainObjectList.getItem(domainObjectList
                        .getSelectionIndex());
            }

            if (event.widget == buttonBuild) {
                CommandExecution commandExecution = new CommandExecution();
                // 1.serviceNameText, webProjectCheck
                if (domainObjectList.getSelection().length <= 0) {
                    DialogUtil.openMessageDialog(
                        MessageUtil.getMessage("ide.message.title"),
                        MessageUtil.getMessage("editor.dialog.crud.choose"),
                        MessageDialog.WARNING);
                    return;
                }

                if (packageNameText.getText() == null
                    || packageNameText.getText().length() == 0) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("wizard.application.error.pkgname"),
                        MessageDialog.ERROR);
                    return;
                }

                if (!ProjectUtil.validatePkgName(packageNameText.getText())) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("wizard.application.validation.pkgname"),
                        MessageDialog.ERROR);
                    return;
                }

                // check ConfigPage DAO Frameworks
                // selection
                try {
                    projectMF =
                        ProjectUtil.getProjectProperties(ideEditor
                            .getCurrentProject());
                } catch (Exception e) {
                    ExceptionUtil.showException(
                        MessageUtil.getMessage("editor.exception.pjtsection"),
                        IStatus.ERROR, e);
                }
                String projectHome =
                    projectMF.readValue(CommonConstants.PROJECT_HOME);
                String templateType =
                    projectMF.readValue(CommonConstants.APP_TEMPLATE_TYPE);
                String basePackage =
                    projectMF.readValue(CommonConstants.PACKAGE_NAME);
                String daoframework =
                    projectMF.readValue(CommonConstants.APP_DAOFRAMEWORK_TYPE);
                String templateHome =
                    projectMF.readValue(CommonConstants.PROJECT_TEMPLATE_HOME);

                ConfigPage configPage =
                    (ConfigPage) ideEditor.getMapPages().get(new Integer(2));
                if (!configPage.useHibernate() && !configPage.useQueryService()
                    && !configPage.useIBatis2() && !configPage.useMyBatis() && !configPage.useSpringJdbc()) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.dialog.crud.daoframework"),
                        MessageDialog.WARNING);
                    return;
                } else if (StringUtils.isEmpty(daoframework)) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.dialog.crud.daoframework.save"),
                        MessageDialog.WARNING);
                    return;
                }

                if (!DialogUtil.confirmMessageDialog(
                    MessageUtil.getMessage("ide.message.title"),
                    MessageUtil.getMessage("editor.dialog.confirm.crud")))
                    return;

                String domainName =
                    domainClassName
                        .substring(domainClassName.lastIndexOf(".") + 1);
                String scope = "all";
                if (!webProjectCheck.getSelection()) {
                    scope = CommonConstants.PROJECT_TYPE_SERVICE;
                }

                try {
                    SourceCodeChecker sourceCodeChecker =
                        new SourceCodeChecker();
                    String errorMessage =
                        sourceCodeChecker.checkExistingCrud(false, null,
                            templateType, templateHome, projectHome,
                            basePackage,
                            packageName + "." + packageNameText.getText(),
                            domainName, scope, daoframework);
                    // If existing file has found.
                    if (errorMessage != null) {
                        if (!DialogUtil
                            .confirmMessageDialog(
                                MessageUtil.getMessage("ide.message.title"),
                                errorMessage
                                    + "\n"
                                    + MessageUtil
                                        .getMessage("editor.dialog.confirm.overwrite")))
                            return;
                    }
                } catch (Exception e) {
                    ExceptionUtil.showException(MessageUtil
                        .getMessage("editor.exception.checkoverwrite"),
                        IStatus.ERROR, e);
                }

                try {
                    if (packageNameText.getText().length() != 0)
                        basePackage =
                            basePackage + "." + packageNameText.getText();
                    commandExecution.createCRUD(domainClassName, basePackage,
                        servicePjtNameText.getText(),
                        webProjectCheck.getSelection(), insertSampleDataCheck.getSelection(), ideEditor
                            .getCurrentProject().getLocation().toOSString());
                } catch (Exception e) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.dialog.codegen.createcode"),
                        MessageDialog.ERROR);
                    ExceptionUtil.showException(MessageUtil
                        .getMessage("editor.dialog.codegen.createcode"),
                        IStatus.ERROR, e);
                }

            }
            if (event.widget == domainObjectList
                && (domainObjectList.getSelectionIndex() > -1)) {
                int index = domainClassName.lastIndexOf(".");
                if (index >= 0)
                    domainClassName = domainClassName.substring(index + 1);

                serviceNameText.setText(domainClassName + "Service");
                packageNameText.setText(domainClassName.toLowerCase());

                if (servicePjtNameText != null)
                    servicePjtNameText.setText(projectName);
            }

            if (event.widget == buttonRefresh) {
                onRefreshClicked();
            }
        }

    };

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void onRefreshClicked() {
        loadListClass("javax.persistence.Entity");
    }
}
