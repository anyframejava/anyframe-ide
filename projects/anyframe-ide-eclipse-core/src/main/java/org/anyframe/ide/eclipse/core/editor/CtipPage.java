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

import hudson.scheduler.CronTab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.miginfocom.swt.MigLayout;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.eclipse.core.popup.CtipManageUrlPopup;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.HudsonRemoteAPI;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.anyframe.ide.eclipse.core.util.SwtGenUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * This is an CtipPage class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class CtipPage implements Page {
    private CodeGenEditor ideEditor;
    private Composite parent;

    private HudsonRemoteAPI hudson = new HudsonRemoteAPI();
    private boolean isTaskListRefreshFailed;
    private String DEFAULT_FILTER_STR = "type filter text";

    private Image imageRed;
    private Image imageBlue;
    private Image imageGray;
    private Cursor waitCursor;

    private Element jobConfigElement;

    private Combo urlCombo;
    private Button addUrlButton;

    private Text homeText;
    private Text hudsonLinkUrlText;
    private Button saveAdminConfigButton;

    private Text filterText;
    private Table taskTable;
    private TableViewer taskViewer;
    private Button newTaskButton;
    private Button runTaskButton;
    private Button removeTaskButton;

    private Composite detailSection;
    private Text taskNameText;
    private Text workspaceText;
    private Button buildTypeCheck;
    private Button reportTypeCheck;
    private Combo scmTypeCombo;
    private Text scmUrlText;
    private Text scheduleText;
    private Text childProjectText;
    private Button saveTaskButton;

    private Button refreshButton;

    private PropertiesIO appProps;
    private String homeTextCode;
    private String projectName;
    private String projectBuild;
    private String anyframeVersion;
    private boolean isChangedChildProject = false;
    private String childProject;

    private ArrayList<String> jobNameList = new ArrayList<String>();

    public CtipPage(CodeGenEditor ideEditor) {
        this.ideEditor = ideEditor;
    }

    public boolean isPageOpened() {
        return parent != null;
    }

    public Composite getPage(Composite parent) {
        this.parent = parent;

        appProps = loadBuildPropertiesFile();
        projectBuild = appProps.readValue(CommonConstants.PROJECT_BUILD_TYPE);

        final ScrolledForm form =
            SwtGenUtil.createScrolledForm(parent, "editor.codegen.ctip.title");

        loadSwtResources(parent);
        loadSwtPageLayoutInsideOfForm(parent, form);
        loadInitialPageData();

        form.reflow(true);
        return form;
    }

    /*********************************************************************************************************
     * 1. Load SWT Resources (Image, Cursor)
     */

    private void loadSwtResources(Composite parent) {
        waitCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);

        imageRed =
            new Image(parent.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.status.red")));
        imageBlue =
            new Image(parent.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.status.blue")));
        imageGray =
            new Image(parent.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.status.grey")));
    }

    /*********************************************************************************************************
     * 2. Load SWT page layout
     */

    private void loadSwtPageLayoutInsideOfForm(Composite parent,
            final ScrolledForm form) {
        FormToolkit toolkit = new FormToolkit(parent.getDisplay());

        Composite container = toolkit.createComposite(form.getBody(), SWT.NONE);
        container.setLayout(new MigLayout("fillx", "[40%][grow]"));

        createUrlSection(form, container);
        createAdminSection(form, container);
        createTaskListSection(form, container);
        createTaskDetailSection(form, container);
        createCommandButtonSection(form, container);
    }

    private void createUrlSection(final ScrolledForm form, Composite parent) {
        String titleCode = "editor.ctip.url.section.title";
        String descCode = "editor.ctip.url.section.desc";
        Composite container =
            SwtGenUtil.createSectionAndGetInnerContainer(form, parent,
                titleCode, descCode, "span 2,growx,wrap", true);
        container.setLayout(new MigLayout("fillx",
            "[left]10[left, grow]20[right]", "[]10"));

        SwtGenUtil.createLabel(container, "editor.ctip.url");

        urlCombo =
            SwtGenUtil.createCombo(container, SWT.SINGLE | SWT.BORDER
                | SWT.READ_ONLY, "grow, gaptop 3, alignx left");
        urlCombo.addSelectionListener(comboListener);

        addUrlButton =
            SwtGenUtil.createButton(container,
                "editor.ctip.url.section.configure", "image.serarch",
                buttonListener);
        addUrlButton.setLayoutData("wmin 100px");
    }

    private void createAdminSection(final ScrolledForm form, Composite parent) {
        String titleCode = "editor.ctip.admin.section.title";
        String descCode = "editor.ctip.admin.section.desc";
        Composite container =
            SwtGenUtil.createSectionAndGetInnerContainer(form, parent,
                titleCode, descCode, "span 2,growx,wrap", false);
        container.setLayout(new MigLayout("fillx", "[left]10[left, grow]",
            "[][][]"));

        homeTextCode = "editor.ctip.admin.anthome";
        if (projectBuild.equals(CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
            homeTextCode = "editor.ctip.admin.mavenhome";
        }
        homeText =
            SwtGenUtil.createLabelAndTextAndWrapLine(container, homeTextCode);
        hudsonLinkUrlText =
            SwtGenUtil.createLabelAndTextAndWrapLine(container,
                "editor.ctip.admin.hudsonurl");
        saveAdminConfigButton =
            SwtGenUtil.createButton(container, "ide.button.save",
                "image.apply", buttonListener);
        saveAdminConfigButton.setLayoutData("span 2, alignx right, wmin 100px");
    }

    private void createTaskListSection(final ScrolledForm form, Composite parent) {
        String titleCode = "editor.ctip.task.section.title";
        String descCode = "editor.ctip.task.section.desc";
        Composite container =
            SwtGenUtil.createSectionAndGetInnerContainer(form, parent,
                titleCode, descCode, "aligny top,grow", true);
        container.setLayout(new MigLayout("fillx", "[left]10[left]push",
            "10[20]10[20][20][20][94]"));

        createFilterText(container);

        taskTable = createTable(container);
        taskTable.setLayoutData("wmax 255px, hmax 194px, spany 4, grow");
        taskTable.addSelectionListener(tableListener);

        newTaskButton =
            SwtGenUtil.createButton(container, "ide.button.newtask", "",
                buttonListener);
        Image imageNewTask =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.new")));

        newTaskButton.setImage(imageNewTask);
        newTaskButton.setLayoutData("wmin 100px, wrap");

        removeTaskButton =
            SwtGenUtil.createButton(container, "ide.button.remove", "",
                buttonListener);
        Image imageRemoveTask =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.remove")));

        removeTaskButton.setImage(imageRemoveTask);
        removeTaskButton.setLayoutData("wmin 100px, wrap");

        runTaskButton =
            SwtGenUtil.createButton(container, "ide.button.run", "image.build",
                buttonListener);
        Image imageRunTask =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.run")));

        runTaskButton.setImage(imageRunTask);
        runTaskButton.setLayoutData("wmin 100px");
    }

    private void createFilterText(Composite container) {
        filterText = new Text(container, SWT.LEFT | SWT.BORDER);
        filterText.setText(DEFAULT_FILTER_STR);
        filterText.setLayoutData("wmax 252px, span 2, grow, wrap");

        filterText.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                if (!(filterText.getText().equals(DEFAULT_FILTER_STR))) {
                    getTaskListFromHudson(urlCombo.getItem(0));
                }
            }
        });
        filterText.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent event) {
                if (filterText.getText().equals(DEFAULT_FILTER_STR)) {
                    filterText.setSelection(0, 16);
                }
            }
        });
    }

    private Table createTable(Composite container) {
        Table taskTable =
            new Table(container, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        taskTable.setHeaderVisible(true);
        taskTable.setLinesVisible(true);

        taskViewer = new TableViewer(taskTable);

        TableViewerColumn taskName =
            new TableViewerColumn(taskViewer, SWT.None);
        taskName.getColumn().setText(
            MessageUtil.getMessage("editor.ctip.task.title"));
        taskName.getColumn().setWidth(178);

        TableViewerColumn buildstatus =
            new TableViewerColumn(taskViewer, SWT.None);
        buildstatus.getColumn().setText(
            MessageUtil.getMessage("editor.ctip.task.status"));
        buildstatus.getColumn().setWidth(52);

        return taskTable;
    }

    private void createTaskDetailSection(final ScrolledForm form,
            Composite parent) {

        String titleCode = "editor.ctip.taskdetail.section.title";
        String descCode = "editor.ctip.taskdetail.section.desc";
        Composite container =
            SwtGenUtil.createSectionAndGetInnerContainer(form, parent,
                titleCode, descCode, "aligny top,growx,wrap", true);
        container.setLayout(new MigLayout("fillx", "[left]10[left, grow]",
            "20[]10[]10[]10[]10[]10[]10[]15[]"));

        createTaskTypeCheckBoxes(container);

        taskNameText =
            SwtGenUtil.createLabelAndTextAndWrapLine(container,
                "editor.ctip.taskdetail.taskname");
        taskNameText.addListener(SWT.Modify, new Listener() {

            public void handleEvent(Event arg0) {
                if (!isChangedChildProject && reportTypeCheck.getSelection()
                    && buildTypeCheck.getSelection())
                    childProjectText.setText(taskNameText.getText()
                        + MessageUtil
                            .getMessage("editor.ctip.taskname.build.postfix"));
            }
        });
        workspaceText =
            SwtGenUtil.createLabelAndTextAndWrapLine(container,
                "editor.ctip.taskdetail.workspace");

        createScmTypeCombo(container);

        scmUrlText =
            SwtGenUtil.createLabelAndTextAndWrapLine(container,
                "editor.ctip.taskdetail.scm.url");
        scheduleText =
            SwtGenUtil.createLabelAndTextAndWrapLine(container,
                "editor.ctip.taskdetail.schedule");
        childProjectText =
            SwtGenUtil.createLabelAndTextAndWrapLine(container,
                "editor.ctip.taskdetail.childproject");
        childProjectText.addListener(SWT.KeyDown, new Listener() {

            public void handleEvent(Event arg0) {
                childProject = childProjectText.getText();
                isChangedChildProject = true;
            }

        });

        saveTaskButton =
            SwtGenUtil.createButton(container, "ide.button.save",
                "image.apply", buttonListener);
        saveTaskButton.setLayoutData("wmin 100px, span 2, alignx right");

        setDefaultDetailText();

        detailSection = container.getParent();
        detailSection.setVisible(false);
    }

    private void createTaskTypeCheckBoxes(Composite container) {
        SwtGenUtil.createLabel(container, "editor.ctip.taskdetail.type");
        buildTypeCheck = new Button(container, SWT.CHECK);
        buildTypeCheck.setText(MessageUtil
            .getMessage("editor.ctip.taskdetail.type.build"));
        buildTypeCheck.setLayoutData("growx, split 2, gap rel");
        buildTypeCheck.addSelectionListener(checkboxListener);

        reportTypeCheck = new Button(container, SWT.CHECK);
        reportTypeCheck.setText(MessageUtil
            .getMessage("editor.ctip.taskdetail.type.report"));
        reportTypeCheck.setLayoutData("growx, wrap");
        reportTypeCheck.addSelectionListener(checkboxListener);
    }

    private void createScmTypeCombo(Composite container) {
        SwtGenUtil.createLabel(container, "editor.ctip.taskdetail.scm.type");
        scmTypeCombo =
            SwtGenUtil.createCombo(container, SWT.SINGLE | SWT.BORDER
                | SWT.READ_ONLY, "grow, gaptop 1, alignx left");
        scmTypeCombo.addSelectionListener(comboListener);
        scmTypeCombo.add("subversion", 0);
        scmTypeCombo.add("cvs", 1);
        scmTypeCombo.add("none", 2);
        scmTypeCombo.select(0);
        scmTypeCombo.setLayoutData("growx, wrap");
    }

    private void setDefaultDetailText() {
        buildTypeCheck.setSelection(true);
        reportTypeCheck.setSelection(true);

        appProps = loadBuildPropertiesFile();
        if (appProps == null)
            return;

        projectName = appProps.readValue(CommonConstants.PROJECT_NAME);

        taskNameText.setText(projectName);
        String applicationHome =
            MessageUtil.getMessage("editor.ctip.apphome.prefix")
                + ProjectUtil.SLASH + ProjectUtil.SLASH
                + MessageUtil.getMessage("editor.ctip.anyframe.postfix");
        if (projectBuild.equals(CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
            applicationHome =
                MessageUtil.getMessage("editor.ctip.apphome.prefix")
                    + ProjectUtil.SLASH
                    + MessageUtil.getMessage("editor.ctip.anyframe.postfix");
        }
        workspaceText.setText(applicationHome);
        scmTypeCombo.select(0);
        scmUrlText.setText(MessageUtil.getMessage("editor.ctip.scmurl.init")
            + projectName);
        scmUrlText.setEnabled(true);
        scheduleText.setText(MessageUtil
            .getMessage("editor.ctip.schedule.init"));
        childProjectText.setText(projectName
            + MessageUtil.getMessage("editor.ctip.taskname.build.postfix"));
    }

    private PropertiesIO loadBuildPropertiesFile() {
        PropertiesIO projectMF = null;
        try {
            projectMF =
                ProjectUtil.getProjectProperties(ideEditor.getCurrentProject());
        } catch (Exception e) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.exception.error.properties"),
                MessageDialog.WARNING);
        }
        return projectMF;
    }

    private void createCommandButtonSection(ScrolledForm form, Composite parent) {
        Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData("span 2,growx,wrap");

        FormToolkit toolkit = new FormToolkit(parent.getDisplay());
        Composite container = toolkit.createComposite(parent, SWT.NONE);
        container.setLayout(new MigLayout("fillx"));

        Image imageRefresh =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.refresh")));
        refreshButton = toolkit.createButton(container, "", SWT.PUSH);
        refreshButton.setImage(imageRefresh);
        refreshButton.setText(MessageUtil.getMessage("ide.button.refresh"));
        refreshButton.addSelectionListener(buttonListener);
        refreshButton.setLayoutData("gap left 15, wmin 100px, span 2");
    }

    /*********************************************************************************************************
     * 3. Initial data load
     */

    private void loadInitialPageData() {
        loadServerUrlListToUrlCombo();
        loadHudsonConfigInitInfo();
    }

    private void loadServerUrlListToUrlCombo() {
        List<String> urlList =
            this.ideEditor.getAnyframeConfig().getCtipUrlList();
        for (int i = 0; i < urlList.size(); i++) {
            urlCombo.add(urlList.get(i), i);
        }

        if (urlCombo.getItemCount() > 0) {
            urlCombo.select(0);
            getInitTaskListFromHudson(urlCombo.getItem(0));
        }
        isTaskListRefreshFailed = true;
    }

    private void loadHudsonConfigInitInfo() {
        homeText.setText(MessageUtil.getMessage("editor.ctip.hometext.init"));
        homeText.setEditable(false);
        hudsonLinkUrlText.setText(MessageUtil
            .getMessage("editor.ctip.hudsonlink.init"));
        hudsonLinkUrlText.setEditable(false);
        saveAdminConfigButton.setVisible(false);
    }

    private void loadHudsonConfigInfo() {
        try {
            Element config = hudson.getHudsonConfig();

            homeText.setText(config.getChildText(projectBuild + "Home"));
            hudsonLinkUrlText.setText(config.getChildText("hudsonURL"));
            homeText.setEditable(true);
            hudsonLinkUrlText.setEditable(true);
            saveAdminConfigButton.setVisible(true);
        } catch (Exception e) {
            loadHudsonConfigInitInfo();
        }
    }

    /*********************************************************************************************************
     * 4. Public exturnal API (Called in other classes
     * - ex. CtipAddUrlPopup.java)
     */
    public void addCtipServerToUrlCombo(String name, String url) {
        for (int i = 0; i < urlCombo.getItemCount(); i++) {
            if (urlCombo.getItem(i).equals(name + " - " + url)) {
                urlCombo.select(i);
                DialogUtil.openMessageDialog(
                    MessageUtil.getMessage("ide.message.title"),
                    MessageUtil.getMessage("editor.ctip.warn.url.duplicated"),
                    MessageDialog.WARNING);
                return;
            }
        }
        urlCombo.add(name + " - " + url);
        urlCombo.select(urlCombo.getItemCount() - 1);

        List<String> urlList =
            this.ideEditor.getAnyframeConfig().getCtipUrlList();
        urlList.add(name + " - " + url);
        this.ideEditor.getAnyframeConfig().setCtipUrlList(urlList);
        this.ideEditor.saveOnlyAnyframeConfigFile();
    }

    public void removeCtipServerToUrlCombo(String name, String url) {
        if (urlCombo.getItem(urlCombo.getSelectionIndex()).equals(
            name + " - " + url)) {
            urlCombo.remove(name + " - " + url);
            urlCombo.select(0);
            getTaskListFromHudson(urlCombo.getItem(0));

        } else {
            urlCombo.remove(name + " - " + url);
        }

        List<String> urlList =
            this.ideEditor.getAnyframeConfig().getCtipUrlList();
        urlList.remove(name + " - " + url);
        this.ideEditor.getAnyframeConfig().setCtipUrlList(urlList);
        this.ideEditor.saveOnlyAnyframeConfigFile();
    }

    public void updateCtipServerToUrlCombo(String name, String newUrl,
            String oldKey) {
        if (urlCombo.indexOf(name + " - " + newUrl) != -1) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.warn.url.duplicated"),
                MessageDialog.WARNING);
            return;
        }

        if (urlCombo.getItem(urlCombo.getSelectionIndex()).equals(oldKey)) {
            urlCombo.setItem(urlCombo.getSelectionIndex(), name + " - "
                + newUrl);
            getTaskListFromHudson(newUrl);

        } else {
            int index = urlCombo.indexOf(oldKey);
            urlCombo.setItem(index, name + " - " + newUrl);
        }

        List<String> urlList =
            this.ideEditor.getAnyframeConfig().getCtipUrlList();
        int index = urlList.indexOf(oldKey);
        urlList.set(index, name + " - " + newUrl);
        this.ideEditor.getAnyframeConfig().setCtipUrlList(urlList);
        this.ideEditor.saveOnlyAnyframeConfigFile();
    }

    public void setFilterDefaultValue() {
        filterText.setText(DEFAULT_FILTER_STR);
    }

    public void getInitTaskListFromHudson(String hudsonUrl) {
        String filterStr = filterText.getText().trim();
        if (filterStr.equals(DEFAULT_FILTER_STR)) {
            filterStr = "";
        }

        hudsonUrl =
            hudsonUrl.substring(hudsonUrl.toLowerCase().indexOf("http://"));
        hudson.setHudsonURL(hudsonUrl);

        saveAdminConfigButton.setVisible(false);

        taskTable.setItemCount(1);
        taskTable.getItem(0).setText(0,
            MessageUtil.getMessage("editor.ctip.init.message"));
        taskTable.getItem(0).setImage(1, imageGray);
    }

    public boolean getTaskListFromHudson(String hudsonUrl) {
        String filterStr = filterText.getText().trim();
        if (filterStr.equals(DEFAULT_FILTER_STR)) {
            filterStr = "";
        }

        hudsonUrl =
            hudsonUrl.substring(hudsonUrl.toLowerCase().indexOf("http://"));
        hudson.setHudsonURL(hudsonUrl);

        try {
            jobNameList.clear();
            List<Element> jobList = hudson.getJobList();
            taskTable.setItemCount(jobList.size());

            int count = 0;
            for (int i = 0; i < jobList.size(); i++) {
                Element elem = jobList.get(i);

                if (elem.getChildText("name").indexOf(filterStr) == -1) {
                    continue;
                }
                jobNameList.add(elem.getChildText("name"));

                taskTable.getItem(count).setText(0, elem.getChildText("name"));
                setStatusImage(count, elem.getChildText("color"));
                count++;
            }
            taskTable.setItemCount(count);

            saveAdminConfigButton.setVisible(true);
            isTaskListRefreshFailed = false;
            loadHudsonConfigInfo();
            return true;

        } catch (Exception e) {
            saveAdminConfigButton.setVisible(false);
            isTaskListRefreshFailed = true;

            taskTable.setItemCount(1);
            taskTable.getItem(0).setText(0,
                MessageUtil.getMessage("editor.ctip.getpjtlist.warn"));
            taskTable.getItem(0).setImage(1, imageGray);

            return false;
        }
    }

    private void setStatusImage(int index, String status) {
        taskTable.getItem(index).setText(1, "");

        if (status.equals("red")) {
            taskTable.getItem(index).setImage(1, imageRed);

        } else if (status.equals("blue")) {
            taskTable.getItem(index).setImage(1, imageBlue);

        } else if (status.endsWith("_anime")) {
            taskTable.getItem(index).setImage(1, null);
            taskTable.getItem(index).setText(1, "building");

        } else {
            taskTable.getItem(index).setImage(1, imageGray);
        }
    }

    /**********************************************************************************************************************************
     * 5. Combo Listener Class section
     */
    private SelectionListener comboListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == urlCombo) {
                String url = urlCombo.getItem(urlCombo.getSelectionIndex());
                setFilterDefaultValue();
                getTaskListFromHudson(url);

            } else if (e.getSource() == scmTypeCombo) {
                // selected "none" SCM Server Type
                if (scmTypeCombo.getSelectionIndex() == 2) {
                    scmUrlText.setText("");
                    scmUrlText.setEnabled(false);
                    scheduleText.setText("");
                    scheduleText.setEnabled(false);
                } else {
                    scmUrlText.setEnabled(true);
                    scheduleText.setEnabled(true);
                }
            }
        }
    };

    /**********************************************************************************************************************************
     * 6. Table Listener Class section
     */
    private SelectionListener tableListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == taskTable) {
                onTaskTableSelected();
            }
        }
    };

    private void onTaskTableSelected() {
        if (isTaskListRefreshFailed) {
            return;
        }

        String jobName = taskTable.getSelection()[0].getText(0);
        buildTypeCheck.setEnabled(false);
        buildTypeCheck.setSelection(jobName.endsWith(MessageUtil
            .getMessage("editor.ctip.taskname.build.postfix")));
        reportTypeCheck.setEnabled(false);
        reportTypeCheck.setSelection(jobName.endsWith(MessageUtil
            .getMessage("editor.ctip.taskname.report.postfix")));
        taskNameText.setEditable(false);

        try {
            jobConfigElement = hudson.getJobConfigXml(jobName);

            taskNameText.setText(jobName);
            workspaceText.setText(hudson.getCustomWorkspace(jobConfigElement));
            scmTypeCombo.select(hudson.getScmTypeMapping(jobConfigElement));
            scmUrlText.setText(hudson.getScmURL(jobConfigElement));
            scheduleText.setText(hudson.getSchedule(jobConfigElement));
            childProjectText.setText(hudson.getChildProject(jobConfigElement));

            enableFieldsIfReportType();

            detailSection.setVisible(true);

        } catch (Exception ex) {
            detailSection.setVisible(false);
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.hudsonlink.init"),
                MessageDialog.WARNING);
        }
    }

    /**********************************************************************************************************************************
     * 7. Checkbox Listener Class section
     */
    private SelectionListener checkboxListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            if (!reportTypeCheck.getSelection()
                && !buildTypeCheck.getSelection()) {
                taskNameText.setEnabled(false);
                workspaceText.setEnabled(false);
                scmTypeCombo.setEnabled(false);
                scmUrlText.setEnabled(false);
                scheduleText.setEnabled(false);
                childProjectText.setEnabled(false);
            } else {
                taskNameText.setEnabled(true);
                workspaceText.setEnabled(true);
                scmTypeCombo.setEnabled(true);
                // not selected "none" SCM Server Type
                if (scmTypeCombo.getSelectionIndex() != 2) {
                    scmUrlText.setEnabled(true);
                } else {
                    scmUrlText.setText("");
                    scmUrlText.setEnabled(false);
                }
                scheduleText.setEnabled(true);
                childProjectText.setEnabled(true);

                if (reportTypeCheck.getSelection()
                    && buildTypeCheck.getSelection()) {
                    if (isChangedChildProject)
                        childProjectText.setText(childProject);
                    else
                        childProjectText
                            .setText(taskNameText.getText()
                                + MessageUtil
                                    .getMessage("editor.ctip.taskname.build.postfix"));
                } else {
                    childProjectText.setText("");
                }
            }
        }
    };

    private void enableFieldsIfReportType() {
        scmTypeCombo.setEnabled(true);
        // not selected "none" SCM Server Type
        if (scmTypeCombo.getSelectionIndex() != 2) {
            scmUrlText.setEnabled(true);
            scheduleText.setEnabled(true);
        } else {
            scmUrlText.setText("");
            scmUrlText.setEnabled(false);
            scheduleText.setText("");
            scheduleText.setEnabled(false);
        }
        childProjectText.setEnabled(true);
    }

    /**********************************************************************************************************************************
     * 8. Button Listener Class section
     */
    private SelectionListener buttonListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == addUrlButton) {
                CtipManageUrlPopup addUrl =
                    new CtipManageUrlPopup(addUrlButton.getParent().getShell(),
                        ideEditor);
                addUrl.open();

            } else if (e.getSource() == newTaskButton) {
                onClickNewTaskButton();

            } else if (e.getSource() == removeTaskButton) {
                onClickRemoveTaskButton();

            } else if (e.getSource() == runTaskButton) {
                onClickRunTaskButton();

            } else if (e.getSource() == saveTaskButton) {
                onClickSaveTaskButton();

            } else if (e.getSource() == saveAdminConfigButton) {
                onClickSaveAdminConfigButton();

            } else if (e.getSource() == refreshButton) {
                String url = urlCombo.getItem(urlCombo.getSelectionIndex());
                getTaskListFromHudson(url);
                if (detailSection.isVisible()) {
                    if (jobConfigElement == null) {
                        onClickNewTaskButton();
                    } else {
                        onTaskTableSelected();
                    }
                }

            }
        }
    };

    private void onClickNewTaskButton() {
        if (isTaskListRefreshFailed) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.init.message"),
                MessageDialog.WARNING);
            return;
        }

        taskTable.deselectAll();
        taskNameText.setFocus();
        setDefaultDetailText();
        enableFieldsIfReportType();
        buildTypeCheck.setEnabled(true);
        reportTypeCheck.setEnabled(true);
        taskNameText.setEditable(true);
        detailSection.setVisible(true);

        jobConfigElement = null;
    }

    private void onClickRemoveTaskButton() {
        if (isTaskListRefreshFailed) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.init.message"),
                MessageDialog.WARNING);
            return;
        }

        if (taskTable.getSelectionCount() == 0) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.remove.warn.getpjt"),
                MessageDialog.WARNING);
            return;
        }

        String jobName = taskTable.getSelection()[0].getText(0);
        if (!DialogUtil.confirmMessageDialog(
            MessageUtil.getMessage("ide.message.title"),
            MessageUtil.getMessage("editor.ctip.remove.warn.confirm") + " \""
                + jobName + "\"?")) {
            return;
        }

        try {
            hudson.deleteJob(jobName);
            detailSection.setVisible(false);
            taskTable.remove(taskTable.getSelectionIndex());

        } catch (Exception ex) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.remove.warn"),
                MessageDialog.WARNING);
        }
    }

    private void onClickRunTaskButton() {
        if (isTaskListRefreshFailed) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.init.message"),
                MessageDialog.WARNING);
            return;
        }

        if (taskTable.getSelectionCount() == 0) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.run.warn.getpjt"),
                MessageDialog.WARNING);
            return;
        }

        String jobName = taskTable.getSelection()[0].getText(0);
        if (!DialogUtil.confirmMessageDialog(
            MessageUtil.getMessage("ide.message.title"),
            MessageUtil.getMessage("editor.ctip.run.warn.confirm") + " \""
                + jobName + "\"?")) {
            return;
        }

        Cursor oldCursor = parent.getCursor();
        try {
            reqeustBuildAndWaitForFirstResponse(jobName);

        } catch (Exception ex) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.run.warn.fail"),
                MessageDialog.WARNING);
        }
        parent.setCursor(oldCursor);
    }

    private void reqeustBuildAndWaitForFirstResponse(String jobName)
            throws JDOMException, IOException, Exception {
        parent.setCursor(waitCursor);

        Element detail = hudson.getJobDetail(jobName);
        String lastBuildNum = "";
        if (detail.getChild("lastBuild") != null) {
            lastBuildNum = detail.getChild("lastBuild").getChildText("number");
        }
        hudson.executeBuild(jobName);

        while (true) {
            detail = hudson.getJobDetail(jobName);
            // if new build started exit while
            // loop.(Wait until new build starts.)
            if (detail.getChild("lastBuild") != null
                && !detail.getChild("lastBuild").getChildText("number")
                    .equals(lastBuildNum)) {
                taskTable.getItem(taskTable.getSelectionIndex()).setImage(1,
                    null);
                taskTable.getItem(taskTable.getSelectionIndex()).setText(1,
                    "building");
                break;
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
    }

    private void onClickSaveTaskButton() {
        String taskName = taskNameText.getText().trim();
        boolean result = false;

        if (jobConfigElement == null) {
            // create job
            if (!buildTypeCheck.getSelection()
                && !reportTypeCheck.getSelection()) {
                DialogUtil
                    .openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.ctip.taskdetail.type.check"),
                        MessageDialog.WARNING);
                return;
            } else {
                if (taskName.equals("")) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.ctip.taskdetail.taskname.check"),
                        MessageDialog.WARNING);
                    return;
                } else if (!ProjectUtil.validateName(taskName)) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.ctip.taskdetail.taskname.valid"),
                        MessageDialog.WARNING);
                    return;
                }

                if (reportTypeCheck.getSelection()) {
                    if (jobNameList.contains(taskName
                        + MessageUtil
                            .getMessage("editor.ctip.taskname.report.postfix"))) {
                        DialogUtil
                            .openMessageDialog(
                                MessageUtil.getMessage("ide.message.title"),
                                MessageUtil
                                    .getMessage("editor.ctip.taskdetail.taskname.duplicated"),
                                MessageDialog.WARNING);
                        return;
                    }
                }
                if (buildTypeCheck.getSelection()) {
                    if (jobNameList.contains(taskName
                        + MessageUtil
                            .getMessage("editor.ctip.taskname.build.postfix"))) {
                        DialogUtil
                            .openMessageDialog(
                                MessageUtil.getMessage("ide.message.title"),
                                MessageUtil
                                    .getMessage("editor.ctip.taskdetail.taskname.duplicated"),
                                MessageDialog.WARNING);
                        return;
                    }
                }

                if (workspaceText.getText().indexOf(
                    MessageUtil.getMessage("editor.ctip.apphome.prefix")) != -1) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.ctip.apphome.warn.change"),
                        MessageDialog.WARNING);
                    return;
                }

                if (buildTypeCheck.getSelection()
                    && reportTypeCheck.getSelection()) {

                    if (!childProjectText
                        .getText()
                        .equals(
                            taskName
                                + MessageUtil
                                    .getMessage("editor.ctip.taskname.build.postfix"))) {
                        if (!jobNameList.contains(childProjectText.getText())) {
                            DialogUtil
                                .openMessageDialog(
                                    MessageUtil.getMessage("ide.message.title"),
                                    MessageUtil
                                        .getMessage("editor.ctip.new.warn.notexistpjt"),
                                    MessageDialog.WARNING);
                            return;
                        }

                    }
                } else {
                    if (childProjectText
                        .getText()
                        .equals(
                            taskName
                                + MessageUtil
                                    .getMessage("editor.ctip.taskname.build.postfix"))) {
                        if (!jobNameList.contains(childProjectText.getText())) {
                            DialogUtil
                                .openMessageDialog(
                                    MessageUtil.getMessage("ide.message.title"),
                                    MessageUtil
                                        .getMessage("editor.ctip.new.warn.notexistpjt"),
                                    MessageDialog.WARNING);
                            return;
                        }
                    }
                }
            }

            if (!scmTypeCombo.getText().equals("none")) {
                if (StringUtils.isEmpty(this.scmUrlText.getText())) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.ctip.new.warn.emptyscmurl"),
                        MessageDialog.WARNING);
                    return;
                }
            }

            String scmSchedule = this.scheduleText.getText();
            if (StringUtils.isNotEmpty(scmSchedule)) {
                if (StringUtils.isNotEmpty(this.scmUrlText.getText())) {
                    try {
                        new CronTab(scmSchedule);
                    } catch (Exception e) {
                        DialogUtil
                            .openDetailMessageDialog(
                                MessageUtil.getMessage("ide.message.title"),
                                MessageUtil
                                    .getMessage("editor.ctip.new.warn.invalidschedule"),
                                e.getClass().getCanonicalName() + ": "
                                    + e.getMessage(), MessageDialog.WARNING);

                        ExceptionUtil
                            .showException(
                                MessageUtil
                                    .getMessage("editor.ctip.new.warn.invalidschedule"),
                                IStatus.ERROR, e);
                        return;
                    }
                } else {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.ctip.new.warn.invalidscmurl"),
                        MessageDialog.WARNING);
                    return;
                }
            }

            if (!DialogUtil.confirmMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.save.warn.confirm") + " \""
                    + taskName + "\"?")) {
                return;
            }

            result = createNewJob(taskName);

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            }

            String url = urlCombo.getItem(urlCombo.getSelectionIndex());
            getTaskListFromHudson(url);
            taskTable.select(getNewTaskIndex(taskName));

        } else {
            // update job
            if (!childProjectText.getText().equals("")) {
                if (buildTypeCheck.getSelection()) {
                    if (childProjectText.getText().equals(taskName)) {
                        DialogUtil.openMessageDialog(MessageUtil
                            .getMessage("ide.message.title"), MessageUtil
                            .getMessage("editor.ctip.new.warn.notexistpjt"),
                            MessageDialog.WARNING);
                        return;
                    }
                }

                if (!jobNameList.contains(childProjectText.getText())) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.ctip.new.warn.notexistpjt"),
                        MessageDialog.WARNING);
                    return;
                }
            }

            if (!scmTypeCombo.getText().equals("none")) {
                if (StringUtils.isEmpty(this.scmUrlText.getText())) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.ctip.new.warn.emptyscmurl"),
                        MessageDialog.WARNING);
                    return;
                }
            }

            String scmSchedule = this.scheduleText.getText();
            if (StringUtils.isNotEmpty(scmSchedule)) {
                if (StringUtils.isNotEmpty(this.scmUrlText.getText())) {
                    try {
                        new CronTab(scmSchedule);
                    } catch (Exception e) {
                        DialogUtil
                            .openDetailMessageDialog(
                                MessageUtil.getMessage("ide.message.title"),
                                MessageUtil
                                    .getMessage("editor.ctip.new.warn.invalidschedule"),
                                e.getClass().getCanonicalName() + ": "
                                    + e.getMessage(), MessageDialog.WARNING);

                        ExceptionUtil
                            .showException(
                                MessageUtil
                                    .getMessage("editor.ctip.new.warn.invalidschedule"),
                                IStatus.ERROR, e);
                        return;
                    }
                } else {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.ctip.new.warn.invalidscmurl"),
                        MessageDialog.WARNING);
                    return;
                }
            }

            if (!DialogUtil.confirmMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.save.warn.confirm") + " \""
                    + taskName + "\"?")) {
                return;
            }

            result = updateExistingJob(taskName);
        }
        if (result) {
            onTaskTableSelected();

            DialogUtil.openMessageDialog(MessageUtil
                .getMessage("ide.message.title"), MessageUtil
                .getMessage("editor.ctip.taskdetail.taskname.success"),
                MessageDialog.INFORMATION);
        }
    }

    private int getNewTaskIndex(String taskName) {
        for (int i = 0; i < taskTable.getItemCount(); i++) {
            if (taskTable
                .getItem(i)
                .getText(0)
                .equals(
                    taskName
                        + MessageUtil
                            .getMessage("editor.ctip.taskname.report.postfix"))) {
                return i;
            }
        }
        return 0;
    }

    private boolean createNewJob(String taskName) {
        try {
            Context context = getParamContext();

            if (reportTypeCheck.getSelection()) {
                hudson.createJob(
                    taskName
                        + MessageUtil
                            .getMessage("editor.ctip.taskname.report.postfix"),
                    "report", context);
            }
            if (buildTypeCheck.getSelection()) {
                String type = "onlybuild";

                if (reportTypeCheck.getSelection()) {
                    type = "build";

                    if (!scmTypeCombo.getText().equals("none")) {
                        context.put("customWorkspace",
                            context.get("customWorkspace") + "/" + taskName);
                    }
                }

                hudson.createJob(
                    taskName
                        + MessageUtil
                            .getMessage("editor.ctip.taskname.build.postfix"),
                    type, context);
            }

            return true;

        } catch (Exception e) {
            DialogUtil.openDetailMessageDialog(MessageUtil
                .getMessage("ide.message.title"), MessageUtil
                .getMessage("editor.ctip.taskdetail.taskname.fail"),
                MessageUtil
                    .getMessage("editor.ctip.taskdetail.taskname.fail.detail"),
                MessageDialog.WARNING);
            return false;
        }
    }

    private Context getParamContext() {
        Context context = new VelocityContext();
        context.put("customWorkspace", workspaceText.getText().trim());
        context.put("scmType",
            scmTypeCombo.getItem(scmTypeCombo.getSelectionIndex()));
        context.put("scmUrl", scmUrlText.getText().trim());
        context.put("triggerSchedule", scheduleText.getText().trim());
        context.put("childProject", childProjectText.getText().trim());
        context.put("projectBuild", this.projectBuild);
        return context;
    }

    private boolean updateExistingJob(String taskName) {
        try {
            Context context = getParamContext();
            hudson.updateJob(taskName, context);

            return true;
        } catch (Exception e) {
            DialogUtil.openDetailMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.update.warn"),
                MessageUtil.getMessage("editor.ctip.update.warn.detail"),
                MessageDialog.WARNING);
            return false;
        }
    }

    private void onClickSaveAdminConfigButton() {
        String homeTextTrim = homeText.getText().trim();
        homeTextTrim = homeTextTrim.replaceAll("\\\\", "/");
        String hudsonURL = hudsonLinkUrlText.getText().trim();

        if (homeTextTrim.equals("")) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage(homeTextCode).replace(":", "")
                    + MessageUtil.getMessage("editor.ctip.warn.empty"),
                MessageDialog.WARNING);
            return;
        } else if (!ProjectUtil.existPath(homeTextTrim)
            || !ProjectUtil.validatePath(homeTextTrim)) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage(homeTextCode).replace(":", "")
                    + MessageUtil.getMessage("editor.ctip.warn.valid"),
                MessageDialog.WARNING);
            return;
        }

        try {
            if (projectBuild.equals(CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
                hudson.saveHudsonConfig("KEEP_AS_IS", homeTextTrim, hudsonURL);
            } else {
                hudson.saveHudsonConfig(homeTextTrim, "KEEP_AS_IS", hudsonURL);
            }
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.config.success"),
                MessageDialog.INFORMATION);

        } catch (Exception e) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.hudsonlink.init"),
                MessageDialog.WARNING);
        }
    }
}
