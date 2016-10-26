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

import java.util.Iterator;
import java.util.Map;

import net.miginfocom.swt.MigLayout;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.eclipse.core.CommandExecution;
import org.anyframe.ide.eclipse.core.model.table.PluginInfoContentProvider;
import org.anyframe.ide.eclipse.core.model.table.PluginInfoLabelProvider;
import org.anyframe.ide.eclipse.core.model.table.PluginInfoList;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.PluginUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Anyframe Plugins may be added or removed on this
 * page.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class InstallationPage implements Page {
    private CodeGenEditor ideEditor;
    private Table table;
    private TableViewer tableViewer;
    private Button buttonInstall;
    private Button buttonUninstall;
    private Button buttonRefresh;
    private Button sampleCheck;
    private Button buttonUpdateCatalog;
    private PropertiesIO pio = null;

    public InstallationPage(CodeGenEditor ideEditor) {
        this.ideEditor = ideEditor;
    }

    public Composite getPage(final Composite parent) {
        FormToolkit toolkit = new FormToolkit(parent.getDisplay());
        ScrolledForm form = toolkit.createScrolledForm(parent);
        form.setText(MessageUtil
            .getMessage("editor.codegen.installation.title"));
        form.getBody().setLayout(new FillLayout());

        Composite container = toolkit.createComposite(form.getBody(), SWT.NONE);
        container.setLayout(new MigLayout("fillx"));
        createPluginSection(toolkit, form, container);
        createCommandButton(toolkit, container);

        form.reflow(true);

        return form;
    }

    private void createCommandButton(FormToolkit toolkit, Composite parent) {

        Label label =
            toolkit.createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData("wrap, gapy 2, growx");

        // create buttons
        Composite container = toolkit.createComposite(parent, SWT.NULL);
        container.setLayout(new MigLayout("", "[left][left]"));

        Image imageInstall =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.install")));
        buttonInstall =
            toolkit.createButton(container,
                MessageUtil.getMessage("ide.button.install"), SWT.PUSH);
        buttonInstall.setImage(imageInstall);
        buttonInstall.addListener(SWT.Selection, listener);

        Image imageUninstall =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.uninstall")));
        buttonUninstall =
            toolkit.createButton(container,
                MessageUtil.getMessage("ide.button.uninstall"), SWT.PUSH);
        buttonUninstall.setImage(imageUninstall);
        buttonUninstall.addListener(SWT.Selection, listener);

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
            if (event.widget == buttonInstall) {
                PluginInfoList pluginInfoList =
                    (PluginInfoList) tableViewer.getInput();
                Map<String, PluginInfo> pList =
                    pluginInfoList.getPluginInfoList();

                CommandExecution commandExecution = new CommandExecution();
                StringBuffer pluginNamesBuffer = new StringBuffer();
                Iterator<String> itr = pList.keySet().iterator();
                boolean hasInstalled = false;
                while (itr.hasNext()) {
                    String pluginName = (String) itr.next();
                    PluginInfo info = pList.get(pluginName);

                    if (info.isChecked()) {
                        pluginNamesBuffer.append(pluginName + ",");
                        if (info.isInstalled())
                            hasInstalled = true;
                    }
                }

                String pluginNames = pluginNamesBuffer.toString();
                if (!checkPluginNames(pluginNames))
                    return;

                if (hasInstalled) {
                    if (!DialogUtil.confirmMessageDialog(MessageUtil
                        .getMessage("ide.message.title"), MessageUtil
                        .getMessage("editor.dialog.plugintarget.install.info")))
                        return;
                } else {
                    if (!DialogUtil
                        .confirmMessageDialog(MessageUtil
                            .getMessage("ide.message.title"), MessageUtil
                            .getMessage("editor.dialog.confirm.install")))
                        return;
                }

                commandExecution.installPlugins(pluginNames,
                    sampleCheck.getSelection(), ideEditor.getCurrentProject()
                        .getLocation().toOSString(), ideEditor);

            } else if (event.widget == buttonUninstall) {
                PluginInfoList pluginInfoList =
                    (PluginInfoList) tableViewer.getInput();
                Map<String, PluginInfo> pList =
                    pluginInfoList.getPluginInfoList();

                CommandExecution commandExecution = new CommandExecution();
                StringBuffer pluginNamesBuffer = new StringBuffer();
                Iterator<String> itr = pList.keySet().iterator();
                while (itr.hasNext()) {
                    String pluginName = (String) itr.next();
                    PluginInfo info = pList.get(pluginName);

                    if (info.isChecked()) {
                        pluginNamesBuffer.append(pluginName + ",");
                    }
                }

                String pluginNames = pluginNamesBuffer.toString();
                if (!checkPluginNames(pluginNames))
                    return;

                if (!DialogUtil.confirmMessageDialog(
                    MessageUtil.getMessage("ide.message.title"),
                    MessageUtil.getMessage("editor.dialog.confirm.uninstall")))
                    return;

                commandExecution.uninstallPlugins(pluginNames,
                    sampleCheck.getSelection(), ideEditor.getCurrentProject()
                        .getLocation().toOSString(), ideEditor);
            } else if (event.widget == buttonRefresh) {
                PropertiesIO projectMF = null;
                try {
                    projectMF =
                        ProjectUtil.getProjectProperties(ideEditor
                            .getCurrentProject());

                    checkOfflineMode(projectMF);
                } catch (Exception e) {
                    ExceptionUtil.showException(e.getMessage(), IStatus.ERROR,
                        e);
                }
                PluginInfoList pluginInfoList = new PluginInfoList(projectMF);
                tableViewer.setInput(pluginInfoList);
                tableViewer.refresh();
            } else if (event.widget == buttonUpdateCatalog) {
                CommandExecution genExecution = new CommandExecution();
                genExecution.updateCatalog(ideEditor.getCurrentProject()
                    .getLocation().toOSString());
            }
        }

    };

    private boolean checkPluginNames(String pluginNames) {
        boolean checkResult = true;
        if (StringUtils.isEmpty(pluginNames)) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.dialog.plugin.check"),
                MessageDialog.WARNING);
            checkResult = false;
        }
        return checkResult;
    }

    private void createPluginSection(FormToolkit toolkit,
            final ScrolledForm form, Composite parent) {
        try {
            Section section =
                toolkit.createSection(parent, Section.TWISTIE
                    | Section.EXPANDED | Section.DESCRIPTION
                    | Section.TITLE_BAR);
            section.setText(MessageUtil
                .getMessage("editor.installation.section"));
            section.setDescription(MessageUtil
                .getMessage("editor.installation.description"));

            section.setActiveToggleColor(new Color(Display.getDefault(), 255,
                0, 0));
            section.addExpansionListener(new ExpansionAdapter() {
                public void expansionStateChanged(ExpansionEvent e) {
                    form.reflow(true);
                }
            });
            section.setLayout(new FillLayout());
            section.setLayoutData("growx,wrap");

            Composite container = toolkit.createComposite(section, SWT.NONE);
            container.setLayout(new MigLayout("wrap 2", "[center][right]", ""));
            section.setClient(container);

            Label label =
                toolkit.createLabel(container,
                    MessageUtil.getMessage("editor.installation.plugin"));
            label.setLayoutData("grow, gaptop5");

            buttonUpdateCatalog = new Button(container, SWT.PUSH);
            buttonUpdateCatalog.setText(MessageUtil
                .getMessage("editor.installation.updatecatalog"));
            Image imageSearch =
                new Image(container.getDisplay(), getClass()
                    .getResourceAsStream(
                        MessageUtil.getMessage("image.update.small")));
            buttonUpdateCatalog.setImage(imageSearch);
            buttonUpdateCatalog.setLayoutData("width 110!, wrap, right");
            buttonUpdateCatalog.addListener(SWT.Selection, listener);

            createTable(container);
            createTableViewer();
            tableViewer.setContentProvider(new PluginInfoContentProvider());
            tableViewer.setLabelProvider(new PluginInfoLabelProvider());

            pio =
                ProjectUtil.getProjectProperties(this.ideEditor
                    .getCurrentProject());
            PluginInfoList pluginInfoList = new PluginInfoList(pio);
            tableViewer.setInput(pluginInfoList);

            tableViewer.getControl().setLayoutData(
                "growx, hmin 200px, hmax 200px, span2");

            sampleCheck = new Button(container, SWT.CHECK);
            sampleCheck.setLayoutData("grow, wrap, gaptop 11, span 2");
            sampleCheck.setText(MessageUtil
                .getMessage("editor.installation.sample"));

            if (pio.readValue(CommonConstants.PROJECT_TYPE).equals(
                CommonConstants.PROJECT_TYPE_SERVICE)) {
                sampleCheck.setEnabled(false);
                sampleCheck.setSelection(false);
            } else {
                sampleCheck.setEnabled(true);
                sampleCheck.setSelection(true);
            }
            checkOfflineMode(pio);

        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.loadpjttype"),
                IStatus.ERROR, e);
        }
    }

    private void checkOfflineMode(PropertiesIO pio) {
        // check offline
        boolean enabled = true;

        if (pio.readValue(CommonConstants.OFFLINE) != null) {
            boolean isOffline =
                new Boolean(pio.readValue(CommonConstants.OFFLINE))
                    .booleanValue();
            if (isOffline)
                enabled = false;
        }
        buttonUpdateCatalog.setEnabled(enabled);
    }

    private void createTable(Composite parent) {
        int style =
            SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

        table = new Table(parent, style);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.addListener(SWT.MeasureItem, new Listener() {
            public void handleEvent(Event event) {
                event.height = 20;
            }
        });
        table.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();

                PluginInfo pluginInfo =
                    (PluginInfo) tableViewer.getElementAt(index);
                String checked = pluginInfo.isChecked() ? "false" : "true";
                pluginInfo.setChecked(checked);
                tableViewer.refresh();
            }
        });

        TableColumn column = new TableColumn(table, SWT.CENTER, 0);
        column.setText("!");
        column.setWidth(30);

        column = new TableColumn(table, SWT.LEFT, 1);
        column.setText("Plugin Name");
        column.setWidth(100);

        column = new TableColumn(table, SWT.LEFT, 2);
        column.setText("Group Id");
        column.setWidth(140);

        column = new TableColumn(table, SWT.LEFT, 3);
        column.setText("Artifact Id");
        column.setWidth(160);

        column = new TableColumn(table, SWT.LEFT, 4);
        column.setText("Latest");
        column.setWidth(100);

        column = new TableColumn(table, SWT.CENTER, 5);
        column.setText("Installed");
        column.setWidth(100);

    }

    private void createTableViewer() {
        tableViewer = new TableViewer(table);
        tableViewer.setUseHashlookup(true);
    }

    public Listener getListener() {
        return listener;
    }

    public Button getButtonRefresh() {
        return buttonRefresh;
    }

}
