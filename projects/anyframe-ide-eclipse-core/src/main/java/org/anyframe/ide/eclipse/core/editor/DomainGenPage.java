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
import java.util.Map;

import net.miginfocom.swt.MigLayout;

import org.anyframe.ide.eclipse.core.CommandExecution;
import org.anyframe.ide.eclipse.core.model.tree.ITreeModel;
import org.anyframe.ide.eclipse.core.model.tree.SimpleTreeNode;
import org.anyframe.ide.eclipse.core.model.tree.TreeContentProvider;
import org.anyframe.ide.eclipse.core.model.tree.TreeLabelProvider;
import org.anyframe.ide.eclipse.core.util.DatabaseUtil;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.anyframe.ide.eclipse.core.util.SearchUtil;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.command.maven.mojo.codegen.SourceCodeChecker;

/**
 * This is a DomainGenPage class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class DomainGenPage implements Page {
    private TreeViewer packageViewer;
    private CheckboxTreeViewer tableViewer;
    private Button buttonBuild;
    private Button buttonRefresh;
    private CodeGenEditor ideEditor;
    private String rootName;

    private PropertiesIO pjtProps = null;

    public DomainGenPage(CodeGenEditor ideEditor) {
        this.ideEditor = ideEditor;
    }

    public Composite getPage(final Composite parent) {
        try {
            pjtProps =
                ProjectUtil.getProjectProperties(ideEditor.getCurrentProject());
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.error.properties"),
                IStatus.ERROR, e);
        }

        FormToolkit toolkit = new FormToolkit(parent.getDisplay());
        ScrolledForm form = toolkit.createScrolledForm(parent);
        form.setText(MessageUtil.getMessage("editor.codegen.domain.title"));
        form.getBody().setLayout(new FillLayout());

        Composite container = toolkit.createComposite(form.getBody(), SWT.NONE);
        container.setLayout(new MigLayout("fillx"));
        createMappingSection(toolkit, form, container);
        createCommandButton(toolkit, container);

        form.reflow(true);

        return form;
    }

    private void createCommandButton(FormToolkit toolkit, Composite parent) {

        Label label =
            toolkit.createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData("wrap, gapy 45, growx");

        // create buttons
        Composite container = toolkit.createComposite(parent, SWT.NULL);
        container.setLayout(new MigLayout("", "[left][left]"));

        Image imageBuild =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.build")));
        buttonBuild =
            toolkit.createButton(container,
                MessageUtil.getMessage("ide.button.build"), SWT.PUSH);
        buttonBuild.setImage(imageBuild);
        buttonBuild.addListener(SWT.Selection, listener);

        Image imageRefresh =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.refresh")));
        buttonRefresh =
            toolkit.createButton(container,
                MessageUtil.getMessage("ide.button.refresh"), SWT.PUSH);
        buttonRefresh.setImage(imageRefresh);
        buttonRefresh.addListener(SWT.Selection, listener);
    }

    private void createMappingSection(FormToolkit toolkit,
            final ScrolledForm form, Composite parent) {
        Section section =
            toolkit.createSection(parent, Section.TWISTIE | Section.EXPANDED
                | Section.DESCRIPTION | Section.TITLE_BAR);
        section.setText(MessageUtil.getMessage("editor.domain.section.domain"));
        section.setDescription(MessageUtil
            .getMessage("editor.domain.description"));

        section
            .setActiveToggleColor(new Color(Display.getDefault(), 255, 0, 0));
        section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });
        section.setLayout(new FillLayout());
        section.setLayoutData("growx,wrap");

        Composite container = toolkit.createComposite(section, SWT.NONE);
        container.setLayout(new MigLayout("fillx, wrap 2", "[40%]10[40%]", ""));
        section.setClient(container);

        Label label = null;
        label =
            toolkit.createLabel(container,
                MessageUtil.getMessage("editor.domain.table"));

        label =
            toolkit.createLabel(container,
                MessageUtil.getMessage("editor.domain.package"));
        label.setLayoutData("wrap, growx");

        tableViewer =
            new CheckboxTreeViewer(toolkit.createTree(container, SWT.CHECK));
        tableViewer.setContentProvider(new TreeContentProvider());
        tableViewer.setLabelProvider(new TreeLabelProvider());
        tableViewer.setInput(getDefaultTableModel());
        tableViewer.expandAll();
        tableViewer.getControl().setLayoutData("growx, hmin 200px, hmax 200px");
        tableViewer.addCheckStateListener(tableCheckBoxListener);

        packageViewer = new TreeViewer(container);
        packageViewer.setContentProvider(new TreeContentProvider());
        packageViewer.setLabelProvider(new TreeLabelProvider());
        String sourcePath =
            SearchUtil.getSourcePath(this.ideEditor.getCurrentProject());
        packageViewer.setInput(getSourcePackageModel(sourcePath, true));
        packageViewer.expandAll();
        packageViewer.getControl().setLayoutData(
            "growx, hmin 200px, hmax 200px,wrap");
    }

    private ICheckStateListener tableCheckBoxListener =
        new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                ITreeModel model = (ITreeModel) event.getElement();

                if (model.getParent().getParent() == null) {
                    if (event.getChecked()) {
                        // tableViewer.setAllChecked(true);
                        tableViewer.setSubtreeChecked(model, true);
                    } else {
                        // tableViewer.setAllChecked(false);
                        tableViewer.setSubtreeChecked(model, false);
                    }
                } else {
                    if (!event.getChecked()) {
                        tableViewer.setChecked(model.getParent(), false);
                    }

                }

            }
        };

    private ITreeModel getSourcePackageModel(String sourceFolder,
            boolean topNode) {
        if (sourceFolder == null) {
            return new SimpleTreeNode("It needs more than one source package.");
        }

        File sourceFolderFile = new File(sourceFolder);
        SimpleTreeNode model =
            new SimpleTreeNode(sourceFolderFile.getName(), sourceFolderFile);

        File[] childFiles = sourceFolderFile.listFiles();

        for (int i = 0; i < childFiles.length; i++) {
            File childFile = childFiles[i];
            if (childFile.isDirectory() && !childFile.getName().startsWith(".")) {
                model.addChild(getSourcePackageModel(
                    childFile.getAbsolutePath(), false));
            }
        }

        return model;
    }

    private ITreeModel getDefaultTableModel() {
        String schemaName = pjtProps.readValue(CommonConstants.DB_SCHEMA);
        SimpleTreeNode model = new SimpleTreeNode("catalog", schemaName);
        try {
            rootName = MessageUtil.getMessage("editor.domain.init.message");
            SimpleTreeNode schemaModel =
                new SimpleTreeNode(rootName, schemaName);
            model.addChild(schemaModel);
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.loadconfig"),
                IStatus.ERROR, e);
        }
        return model;
    }

    private ITreeModel getTableModel() {
        String schemaName = pjtProps.readValue(CommonConstants.DB_SCHEMA);
        SimpleTreeNode model = new SimpleTreeNode("catalog", schemaName);
        if (schemaName != null) {
            try {
                rootName =
                    schemaName.length() == 0 ? "Tables(No Schema)" : schemaName;
                SimpleTreeNode schemaModel =
                    new SimpleTreeNode(rootName, schemaName);
                model.addChild(schemaModel);
                String[] tables =
                    DatabaseUtil.getTables(
                        pjtProps.readValue(CommonConstants.PROJECT_HOME),
                        pjtProps.readValue(CommonConstants.DB_DRIVER_PATH),
                        pjtProps.readValue(CommonConstants.DB_DRIVER_CLASS),
                        pjtProps.readValue(CommonConstants.DB_URL),
                        pjtProps.readValue(CommonConstants.DB_USERNAME),
                        pjtProps.readValue(CommonConstants.DB_PASSWORD),
                        pjtProps.readValue(CommonConstants.DB_TYPE),
                        pjtProps.readValue(CommonConstants.DB_SCHEMA));
                for (int i = 0; i < tables.length; i++) {
                    SimpleTreeNode tableModel =
                        new SimpleTreeNode(tables[i], null);
                    schemaModel.addChild(tableModel);
                }
            } catch (Exception e) {
                ExceptionUtil.showException(
                    MessageUtil.getMessage("editor.exception.loadconfig"),
                    IStatus.ERROR, e);
            }
        } else
            return getProblemNode(schemaName);

        return model;
    }

    private ITreeModel getProblemNode(String schemaName) {
        SimpleTreeNode problemNode =
            new SimpleTreeNode("problem message", schemaName);
        problemNode.addChild(new SimpleTreeNode(MessageUtil
            .getMessage("editor.dialog.problem.connection"), schemaName));

        return problemNode;
    }

    /**
     * Listener of the buttons of the view.
     */
    private Listener listener = new Listener() {
        public void handleEvent(Event event) {

            if (event.widget == buttonBuild) {
                if (rootName.equals(MessageUtil
                    .getMessage("editor.domain.init.message"))) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.dialog.error.init.message"),
                        MessageDialog.WARNING);
                    return;
                }

                Object[] tables = tableViewer.getCheckedElements();
                Object[] srcPackage = getSelectedNodeList(packageViewer);

                Map<Object, Object> result =
                    DatabaseUtil.checkConnection(
                        pjtProps.readValue(CommonConstants.PROJECT_HOME),
                        pjtProps.readValue(CommonConstants.DB_DRIVER_PATH),
                        pjtProps.readValue(CommonConstants.DB_DRIVER_CLASS),
                        pjtProps.readValue(CommonConstants.DB_URL),
                        pjtProps.readValue(CommonConstants.DB_USERNAME),
                        pjtProps.readValue(CommonConstants.DB_PASSWORD));

                if (!(Boolean) result.get(DatabaseUtil.DB_CON_CHK_KEY)) {
                    DialogUtil.openMessageDialog(
                        MessageUtil.getMessage("ide.message.title"),
                        MessageUtil.getMessage("editor.dialog.error.checkdb"),
                        MessageDialog.WARNING);
                    return;
                }

                if (tables.length == 0) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.dialog.error.choosemodel"),
                        MessageDialog.WARNING);
                    return;
                }

                if (!DialogUtil.confirmMessageDialog(
                    MessageUtil.getMessage("ide.message.title"),
                    MessageUtil.getMessage("editor.dialog.confirm.domain")))
                    return;

                // check package
                String basepackage = "";

                if (srcPackage.length == 0) {
                    if (!DialogUtil
                        .confirmMessageDialog(
                            MessageUtil.getMessage("ide.message.title"),
                            MessageUtil
                                .getMessage("editor.dialog.error.choosepackage")
                                + pjtProps
                                    .readValue(CommonConstants.PACKAGE_NAME)
                                + MessageUtil
                                    .getMessage("editor.dialog.error.choosepackagesub"))) {
                        return;
                    } else {
                        basepackage =
                            pjtProps.readValue(CommonConstants.PACKAGE_NAME)
                                + ".domain";
                    }
                } else {
                    basepackage = ((ITreeModel) srcPackage[0]).getPath();
                }

                CommandExecution commandExecution = new CommandExecution();
                String tableName = getTableNames(tables);

                try {
                    SourceCodeChecker sourceCodeChecker =
                        new SourceCodeChecker();

                    String errorMessage =
                        sourceCodeChecker.checkExistingModel(false, null,
                            pjtProps,
                            pjtProps.readValue(CommonConstants.PROJECT_HOME),
                            basepackage, tableName);

                    if (errorMessage != null) {
                        if (!DialogUtil
                            .confirmMessageDialog(
                                MessageUtil.getMessage("ide.message.title"),
                                errorMessage
                                    + "\n"
                                    + MessageUtil
                                        .getMessage("editor.dialog.confirm.domain.overwrite")))
                            return;
                    }
                } catch (Exception e) {
                    ExceptionUtil.showException(MessageUtil
                        .getMessage("editor.exception.domain.checkoverwrite"),
                        IStatus.ERROR, e);
                }

                try {
                    commandExecution.createModel(tableName, basepackage,
                        ideEditor.getCurrentProject().getLocation()
                            .toOSString());
                } catch (Exception e) {
                    DialogUtil.openMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.dialog.error.createmodel"),
                        MessageDialog.ERROR);
                    ExceptionUtil.showException(MessageUtil
                        .getMessage("editor.dialog.error.createmodel"),
                        IStatus.ERROR, e);
                }
            } else if (event.widget == buttonRefresh) {
                refreshTree();
            }
        }
    };

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public String getTableNames(Object[] tables) {
        String tableName = "*";
        if (tables.length > 0
            && ((ITreeModel) tables[0]).getParent().getParent() == null) {
            tableName = "*";
        } else if (tables.length == 1
            && ((ITreeModel) tables[0]).getParent().getParent() != null) {
            tableName = ((ITreeModel) tables[0]).getName();
        } else {
            String tableNameTemp = "";
            for (int i = 0; i < tables.length; i++) {
                if (tableNameTemp.length() > 0)
                    tableNameTemp += "," + ((ITreeModel) tables[i]).getName();
                else
                    tableNameTemp += ((ITreeModel) tables[i]).getName();
            }
            tableName = tableNameTemp;
        }

        return tableName;
    }

    /**
     * Refresh java source treeviewer.
     */
    public void refreshTree() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                try {
                    pjtProps =
                        ProjectUtil.getProjectProperties(ideEditor
                            .getCurrentProject());
                } catch (Exception e) {
                    ExceptionUtil.showException(MessageUtil
                        .getMessage("editor.exception.error.properties"),
                        IStatus.ERROR, e);
                }
                tableViewer.setInput(getTableModel());
                tableViewer.expandAll();
                tableViewer.refresh();
                packageViewer.setInput(getSourcePackageModel(
                    SearchUtil.getSourcePath(ideEditor.getCurrentProject()),
                    true));
                packageViewer.expandAll();
                packageViewer.refresh();

            }
        });
    }

    public Object[] getSelectedNodeList(TreeViewer viewer) {
        TreeSelection treeSelection = (TreeSelection) viewer.getSelection();
        if (treeSelection != null)
            return treeSelection.toList().toArray();

        return null;
    }
}
