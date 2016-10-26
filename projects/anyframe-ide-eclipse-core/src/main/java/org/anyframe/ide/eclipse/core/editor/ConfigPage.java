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

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.miginfocom.swt.MigLayout;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.command.maven.mojo.codegen.AnyframeTemplateData;
import org.anyframe.ide.eclipse.core.model.table.PluginInfoList;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.FileUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.annotations.Annotations;

/**
 * This is an JDBCConfigPage class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class ConfigPage implements Page {
    private CodeGenEditor ideEditor;
    private Text appHomeText;

    private FormToolkit toolkit;

    private Button[] daoRadios;
    private Button buttonApply;
    private Button buttonRefresh;
    private Combo templateCombo;

    private Text templateHomeText;
    private Button browseTemplateHomeLoc;

    private PluginInfoList pluginInfoList;
    private List<String> pList;
    private PropertiesIO pjtProps = null;
    private String templateHomePath = "";

    // in case of dao framework name is same with
    // plugin name

    // mip-query, query plugin name
    private String pluginMipQuery = CommonConstants.MIP_QUERY_PLUGIN;
    private String pluginXPQuery = CommonConstants.XP_QUERY_PLUGIN;
    private String pluginQuery = CommonConstants.QUERY_PLUGIN;

    // miplatform, map template name
    private String templateMiplatform = "miplatform";
    private String templateXPlatform = "xplatform";
    private String templateMap = "map";
    
    public boolean useQueryService() {
        return daoRadios[1].getEnabled() && daoRadios[1].getSelection();
    }

    public boolean useHibernate() {
        return daoRadios[2].getEnabled() && daoRadios[2].getSelection();
    }

    public boolean useIBatis2() {
        return daoRadios[3].getEnabled() && daoRadios[3].getSelection();
    }
    
    public boolean useMyBatis() {
        return daoRadios[4].getEnabled() && daoRadios[4].getSelection();
    }

    public boolean useSpringJdbc() {
        return daoRadios[0].getEnabled() && daoRadios[0].getSelection();
    }

    public Combo getTemplateCombo() {
        return templateCombo;
    }

    public void setTemplateCombo(Combo templateCombo) {
        this.templateCombo = templateCombo;
    }

    public String getTemplateType() {
        return templateCombo.getText();
    }

    public ConfigPage(CodeGenEditor ideEditor) {
        this.ideEditor = ideEditor;
    }

    public Composite getPage(Composite parent) {
        try {
            pjtProps =
                ProjectUtil.getProjectProperties(ideEditor.getCurrentProject());
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.error.properties"),
                IStatus.ERROR, e);
        }

        pluginInfoList = new PluginInfoList(pjtProps);
        pList = pluginInfoList.getInstalledPluginTypeList();

        toolkit = new FormToolkit(parent.getDisplay());
        final ScrolledForm form = toolkit.createScrolledForm(parent);
        form.setText(MessageUtil.getMessage("editor.codegen.config.title"));
        form.getBody().setLayout(new FillLayout());

        Composite container = toolkit.createComposite(form.getBody(), SWT.NONE);
        container.setLayout(new MigLayout("fillx"));

        createConfigSection(form, container);

        createCommandButton(form, container);

        loadConfiguration();
        setEnabledByCombo();

        form.reflow(true);
        return form;
    }

    private void createConfigSection(final ScrolledForm form,
            final Composite parent) {

        Section section =
            toolkit.createSection(parent, Section.TWISTIE | Section.EXPANDED
                | Section.DESCRIPTION | Section.TITLE_BAR);
        section.setText(MessageUtil.getMessage("editor.config.config.section"));
        section.setDescription(MessageUtil
            .getMessage("editor.config.config.description"));
        section
            .setActiveToggleColor(new Color(Display.getDefault(), 255, 0, 0));
        section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });
        section.setLayout(new FillLayout());
        section.setLayoutData("growx,wrap");

        // Application Home
        Composite container = toolkit.createComposite(section, SWT.NONE);
        container
            .setLayout(new MigLayout("",
                "[right]10[left]10[left]10[left]10[left, grow]10[left, grow]",
                "20"));
        section.setClient(container);

        Label label = null;
        label =
            toolkit.createLabel(container,
                MessageUtil.getMessage("editor.config.apphome.section"), // Project
                                                                         // Location
                SWT.NONE);
        label.setFont(new Font(Display.getDefault(), "\uad74\ub9bc", 9,
            SWT.BOLD));
        label.setLayoutData("split, span, gaptop 10");

        label =
            toolkit.createLabel(container, null, SWT.SEPARATOR | SWT.HORIZONTAL
                | SWT.WRAP);
        label.setLayoutData("grow, wrap, gaptop 10");

        label =
            toolkit.createLabel(container,
                MessageUtil.getMessage("editor.config.apphome.description"), // Check
                                                                             // the
                                                                             // current
                                                                             // Project
                                                                             // Location.
                SWT.NONE);
        label.setLayoutData("grow, span 6 , wrap, gaptop 10, gap 20");

        label =
            toolkit.createLabel(container,
                MessageUtil.getMessage("editor.config.apphome"), SWT.NONE); // Project
                                                                            // Location:
        label.setLayoutData("grow, gap 20");
        appHomeText = toolkit.createText(container, null, SWT.SINGLE);
        appHomeText.setLayoutData("grow, span4, wrap"); // text
                                                        // box
        appHomeText.setEnabled(false);

        
        // Templates Group
        label =
            toolkit.createLabel(container,
                MessageUtil.getMessage("wizard.module.template.selection"),
                SWT.NONE);
        label.setLayoutData("split, span, gaptop 10");
        label.setFont(new Font(Display.getDefault(), "\uad74\ub9bc", 9,
            SWT.BOLD));

        label =
            toolkit.createLabel(container, null, SWT.SEPARATOR | SWT.HORIZONTAL
                | SWT.WRAP);
        label.setLayoutData("grow, wrap, gaptop 10");

        if (pjtProps.readValue(CommonConstants.PROJECT_BUILD_TYPE).equals(
            CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
            // Template Home
            label =
                toolkit.createLabel(container,
                    MessageUtil.getMessage("wizard.module.template.home"),
                    SWT.WRAP);
            label.setLayoutData("grow, gap 20, gaptop 10");

            templateHomeText = toolkit.createText(container, null, SWT.SINGLE);
            templateHomeText.setLayoutData("grow, span 4, gaptop 10");
            templateHomeText.setText(pjtProps
                .readValue(CommonConstants.PROJECT_TEMPLATE_HOME));
            templateHomeText.addListener(SWT.Modify, new Listener() {

                public void handleEvent(Event arg0) {
                    File templateDir = new File(templateHomeText.getText());
                    checkAndSetTemplateLocation(templateDir);
                }
            });

            browseTemplateHomeLoc =
                toolkit.createButton(container,
                    MessageUtil.getMessage("ide.button.browse"), SWT.PUSH);
            Image imageSearchTemplateHome =
                new Image(container.getDisplay(), getClass()
                    .getResourceAsStream(
                        MessageUtil.getMessage("image.serarch")));
            browseTemplateHomeLoc.setImage(imageSearchTemplateHome);
            browseTemplateHomeLoc.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    handleDirBrowseTemplateHomeLocation();
                }
            });
            browseTemplateHomeLoc.setLayoutData("gapleft 1, gaptop 10, wrap");
        }

        // Template Combo
        label =
            toolkit.createLabel(container,
                MessageUtil.getMessage("wizard.module.template"), SWT.WRAP);
        label.setLayoutData("grow, gap 20, gaptop 10");
        templateCombo =
            new Combo(container, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        templateCombo.setLayoutData("grow, wrap, gaptop 10");
        templateCombo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }

            public void widgetSelected(SelectionEvent arg0) {
                setEnabledByCombo();
                setSelectionByCombo();
            }
        });
     // DAO Framework
        label =
            toolkit.createLabel(container,
                MessageUtil.getMessage("wizard.application.daoframeworks"),
                SWT.NONE);
        label.setLayoutData("split, span, gaptop 10");
        label.setFont(new Font(Display.getDefault(), "\uad74\ub9bc", 9,
            SWT.BOLD));

        label =
            toolkit.createLabel(container, null, SWT.SEPARATOR | SWT.HORIZONTAL
                | SWT.WRAP);
        label.setLayoutData("grow, wrap, gaptop 10");

        daoRadios = new Button[5];
        daoRadios[0] = new Button(container, SWT.RADIO);
        daoRadios[0].setText(MessageUtil
            .getMessage("wizard.application.daoframeworks.springjdbc"));
        daoRadios[0].setLayoutData("grow, gap 20, gaptop 10");

        daoRadios[1] = new Button(container, SWT.RADIO);
        daoRadios[1].setText(MessageUtil
            .getMessage("wizard.application.daoframeworks.queryservice"));
        daoRadios[1].setLayoutData("grow, gaptop 10");

        daoRadios[2] = new Button(container, SWT.RADIO);
        daoRadios[2].setText(MessageUtil
            .getMessage("wizard.application.daoframeworks.hibernate"));
        daoRadios[2].setLayoutData("grow, gaptop 10");
        
        daoRadios[3] = new Button(container, SWT.RADIO);
        daoRadios[3].setText(MessageUtil
            .getMessage("wizard.application.daoframeworks.ibatis2"));
        daoRadios[3].setLayoutData("grow, gaptop 10");
        
        daoRadios[4] = new Button(container, SWT.RADIO);
        daoRadios[4].setText(MessageUtil
            .getMessage("wizard.application.daoframeworks.mybatis"));
        daoRadios[4].setLayoutData("grow, gaptop 10");
       
    }

    private void handleDirBrowseTemplateHomeLocation() {
        DirectoryDialog dialog =
            new DirectoryDialog(templateHomeText.getShell());

        String selected = dialog.open();

        if (selected != null)
            templateHomeText.setText(selected);
    }

    private void checkAndSetTemplateLocation(File templateDir) {
        try {
            String propertyTemplateType =
                pjtProps.readValue(CommonConstants.APP_TEMPLATE_TYPE);

            if (templateDir.exists()) {

                File[] templateTypes =
                    FileUtil.dirListByAscAlphabet(templateDir);

                Combo templateCombo = getTemplateCombo();
                templateCombo.removeAll();

                for (File templateType : templateTypes) {
                    if (FileUtil.validateTemplatePath(templateType)) {
                        templateCombo.add(templateType.getName());
                    }
                }

                if (propertyTemplateType == "") {
                    templateCombo.select(0);
                } else {
                    templateCombo.setText(propertyTemplateType);
                }

                // check whether miplatform plugin is
                // installed or not
                if (templateCombo.getItemCount() > 0) {
                    if (!pList.contains(pluginMipQuery))
                        templateCombo.remove(templateMiplatform);
                    if (!pList.contains(pluginQuery))
                        templateCombo.remove(templateMap);
                    if (!pList.contains(pluginXPQuery))
                        templateCombo.remove(templateXPlatform);
                }
            } else {
                Combo templateCombo = getTemplateCombo();
                templateCombo.removeAll();
                ExceptionUtil.showException(
                    MessageUtil.getMessage("editor.dialog.error.template"),
                    IStatus.ERROR, new Exception());
            }
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.error.properties"),
                IStatus.ERROR, e);
        }
    }

    private void setEnabledByCombo() {
        List<AnyframeTemplateData> templates = null;
        XStream xstream = new XStream();
        xstream.processAnnotations(AnyframeTemplateData.class);
        xstream.setMode(XStream.NO_REFERENCES);

        FileInputStream templateConfigFile = null;
        String templateConfigFilePath =
            templateHomePath + ProjectUtil.SLASH + getTemplateType()
                + ProjectUtil.SLASH + "source" + ProjectUtil.SLASH;
        try {
            templateConfigFile =
                new FileInputStream(templateConfigFilePath
                    + CommonConstants.TEMPLATE_CONFIG_FILE);
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.error.templateconfig")
                    + templateConfigFilePath, IStatus.ERROR, e);
        }
        templates =
            (java.util.List<AnyframeTemplateData>) xstream
                .fromXML(templateConfigFile);

        Set<String> daoSet = new HashSet<String>();
        for (AnyframeTemplateData template : templates) {
            if (template.getDao() != null) {
                daoSet.add(template.getDao());
            }
        }

        // template.config 내 dao tag 중 해당 dao
        // framework이 존재하면 enable
        
        if (daoSet.contains(CommonConstants.DAO_QUERY)) {
            if (pList.contains(CommonConstants.QUERY_PLUGIN))
                daoRadios[1].setEnabled(true);
            else
                daoRadios[1].setEnabled(false);
        } else
            daoRadios[1].setEnabled(false);

        if (daoSet.contains(CommonConstants.DAO_HIBERNATE)) {
            if (pList.contains(CommonConstants.HIBERNATE_PLUGIN))
                daoRadios[2].setEnabled(true);
            else
                daoRadios[2].setEnabled(false);
        } else
            daoRadios[2].setEnabled(false);

        if (daoSet.contains(CommonConstants.DAO_IBATIS2)) {
            if (pList.contains(CommonConstants.IBATIS2_PLUGIN))
                daoRadios[3].setEnabled(true);
            else
                daoRadios[3].setEnabled(false);
        } else
            daoRadios[3].setEnabled(false);
        
        if (daoSet.contains(CommonConstants.DAO_MYBATIS)) {
            if (pList.contains(CommonConstants.MYBATIS_PLUGIN))
                daoRadios[4].setEnabled(true);
            else
                daoRadios[4].setEnabled(false);
        } else
            daoRadios[4].setEnabled(false);

        if (daoSet.contains(CommonConstants.DAO_SPRINGJDBC)) {
            if (pList.contains(CommonConstants.CORE_PLUGIN))
                daoRadios[0].setEnabled(true);
            else
                daoRadios[0].setEnabled(false);
        } else
            daoRadios[0].setEnabled(false);
    }

    //select default value among the enabled radio buttons.
    private void setSelectionByCombo() {
        boolean ckExistSelected = false;
        
        for(int i=0; i<daoRadios.length; i++){
            if(daoRadios[i].isEnabled() && ckExistSelected==false){
               daoRadios[i].setSelection(true);
               ckExistSelected = true;
            }
            else
                daoRadios[i].setSelection(false);
        }
    }

    private void createCommandButton(ScrolledForm form, Composite parent) {

        Label label =
            toolkit.createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData("wrap, gapy 25, growx");

        // create buttons
        Composite container = toolkit.createComposite(parent, SWT.NULL);
        container.setLayout(new MigLayout("", "[left][left]"));

        Image imageApply =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.apply")));

        buttonApply =
            toolkit.createButton(container,
                MessageUtil.getMessage("ide.button.apply"), SWT.PUSH);
        buttonApply.setImage(imageApply);
        buttonApply.addListener(SWT.Selection, listener);

        Image imageRefresh =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.refresh")));
        buttonRefresh =
            toolkit.createButton(container,
                MessageUtil.getMessage("ide.button.refresh"), SWT.PUSH);
        buttonRefresh.setImage(imageRefresh);
        buttonRefresh.addListener(SWT.Selection, listener);
    }

    //
    /**
     * Listener of the buttons of the view.
     */
    private Listener listener = new Listener() {
        public void handleEvent(Event event) {

            if (event.widget == buttonApply) {
                savePage();
            } else if (event.widget == buttonRefresh) {
                refreshAction();
            }
        }
    };

    public void refreshAction() {
        try {
            pjtProps =
                ProjectUtil.getProjectProperties(ideEditor.getCurrentProject());
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.error.properties"),
                IStatus.ERROR, e);
        }
        pList = pluginInfoList.getInstalledPluginTypeList();
        loadConfiguration();
        setEnabledByCombo();
    }

    protected void setButtonLayoutData(Button button) {
        GridData data = new GridData(SWT.CENTER);
        data.widthHint = 55;
        data.heightHint = 25;

        button.setLayoutData(data);
    }

    public void loadConfiguration() {
        String appHomeLocVariableValue = "";
        String daoFramwork = "";
        try {
            appHomeLocVariableValue =
                pjtProps.readValue(CommonConstants.PROJECT_HOME);
            if (appHomeLocVariableValue != null)
                appHomeText.setText(appHomeLocVariableValue);
            daoFramwork =
                pjtProps.readValue(CommonConstants.APP_DAOFRAMEWORK_TYPE);
            if (daoFramwork.equals(CommonConstants.DAO_HIBERNATE)) {
                daoRadios[0].setSelection(false);
                daoRadios[2].setSelection(true);
                daoRadios[1].setSelection(false);
                daoRadios[3].setSelection(false);
                daoRadios[4].setSelection(false);
            } else if (daoFramwork.equals(CommonConstants.DAO_QUERY)) {
                daoRadios[1].setSelection(true);
                daoRadios[0].setSelection(false);
                daoRadios[2].setSelection(false);
                daoRadios[3].setSelection(false);
                daoRadios[4].setSelection(false);
            } else if (daoFramwork.equals(CommonConstants.DAO_IBATIS2)) {
                daoRadios[3].setSelection(true);
                daoRadios[0].setSelection(false);
                daoRadios[1].setSelection(false);
                daoRadios[2].setSelection(false);
                daoRadios[4].setSelection(false);
            } else if (daoFramwork.equals(CommonConstants.DAO_MYBATIS)) {
                daoRadios[4].setSelection(true);
                daoRadios[0].setSelection(false);
                daoRadios[1].setSelection(false);
                daoRadios[2].setSelection(false);
                daoRadios[3].setSelection(false);
            } else if (daoFramwork.equals(CommonConstants.DAO_SPRINGJDBC)) {
                daoRadios[0].setSelection(true);
                daoRadios[1].setSelection(false);
                daoRadios[2].setSelection(false);
                daoRadios[3].setSelection(false);
                daoRadios[4].setSelection(false);
            } else {
                daoRadios[0].setSelection(false);
                daoRadios[1].setSelection(false);
                daoRadios[2].setSelection(false);
                daoRadios[3].setSelection(false);
                daoRadios[4].setSelection(false);
            }

        } catch (Exception e1) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.loadconfig"),
                IStatus.ERROR, e1);
        }

        // set template type list
        File templateDir;
        String projectBuildType =
            pjtProps.readValue(CommonConstants.PROJECT_BUILD_TYPE);

        if (projectBuildType.equals(CommonConstants.PROJECT_BUILD_TYPE_ANT)) {
            String templateHome =
                pjtProps.readValue(CommonConstants.ANYFRAME_HOME)
                    + ProjectUtil.SLASH + "templates";
            templateDir = new File(templateHome);
            templateHomePath = templateHome;
        } else {
            String templateHome =
                pjtProps.readValue(CommonConstants.PROJECT_TEMPLATE_HOME);
            templateHomeText.setText(templateHome);
            templateDir = new File(templateHome);
            templateHomePath = templateHome;
        }
        checkAndSetTemplateLocation(templateDir);
    }

    public void savePage() {
        if (pjtProps.readValue(CommonConstants.PROJECT_BUILD_TYPE).equals(
            CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
            if (templateHomeText.getText() == null
                || templateHomeText.getText().length() == 0) {
                DialogUtil.openMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("wizard.application.error.templatehome"),
                    MessageDialog.ERROR);
                return;
            } else if (!ProjectUtil.existPath(templateHomeText.getText())
                || !ProjectUtil.validatePath(templateHomeText.getText())) {
                DialogUtil.openMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("wizard.application.validation.templatehome"),
                    MessageDialog.ERROR);
                return;
            } else {
                File templateDir = new File(templateHomeText.getText());
                if (templateDir.exists()) {
                    File[] templateTypes =
                        FileUtil.dirListByAscAlphabet(templateDir);

                    if (templateTypes != null && templateTypes.length != 0) {

                        for (File templateType : templateTypes) {
                            if (!FileUtil.validateTemplatePath(templateType)) {
                                DialogUtil
                                    .openMessageDialog(
                                        MessageUtil
                                            .getMessage("ide.message.title"),
                                        MessageUtil
                                            .getMessage("wizard.application.validation.templatehome.check"),
                                        MessageDialog.ERROR);
                                return;
                            } else {
                                pjtProps.setProperty(
                                    CommonConstants.PROJECT_TEMPLATE_HOME,
                                    templateDir.getAbsolutePath());
                            }
                        }
                    } else {
                        DialogUtil
                            .openMessageDialog(
                                MessageUtil.getMessage("ide.message.title"),
                                MessageUtil
                                    .getMessage("wizard.application.validation.template.null"),
                                MessageDialog.ERROR);
                        return;
                    }
                }
            }
        }

        if (getTemplateType() != null && !"".equals(getTemplateType())) {
            try {
                pjtProps.setProperty(CommonConstants.APP_TEMPLATE_TYPE,
                    getTemplateType());
            } catch (Exception e) {
                ExceptionUtil.showException(
                    MessageUtil.getMessage("editor.exception.loadconfig"),
                    IStatus.ERROR, e);
            }
        } else {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.dialog.error.choose"),
                MessageDialog.WARNING);
            return;
        }

        try {
            String daoframework = "";
            if (useQueryService())
                daoframework = CommonConstants.DAO_QUERY;
            else if (useHibernate())
                daoframework = CommonConstants.DAO_HIBERNATE;
            else if (useIBatis2())
                daoframework = CommonConstants.DAO_IBATIS2;
            else if (useMyBatis())
                daoframework = CommonConstants.DAO_MYBATIS;
            else if (useSpringJdbc())
                daoframework = CommonConstants.DAO_SPRINGJDBC;

            pjtProps.setProperty(CommonConstants.APP_DAOFRAMEWORK_TYPE,
                daoframework);
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.loadconfig"),
                IStatus.ERROR, e);
        }

        if (!DialogUtil.confirmMessageDialog(
            MessageUtil.getMessage("ide.message.title"),
            MessageUtil.getMessage("editor.dialog.confirm.conf")))
            return;

        pjtProps.write();
    }
}
