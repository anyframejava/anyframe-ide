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
package org.anyframe.ide.eclipse.core.popup;

import java.util.List;

import net.miginfocom.swt.MigLayout;

import org.anyframe.ide.eclipse.core.editor.CodeGenEditor;
import org.anyframe.ide.eclipse.core.editor.CtipPage;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.HudsonRemoteAPI;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.SwtGenUtil;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


/**
 * This is a CtipManageUrlPopup class.
 * @author Soungmin Joo
 */
public class CtipManageUrlPopup {

    private HudsonRemoteAPI hudson = new HudsonRemoteAPI();
    private String DEFAULT_FILTER_STR = "type filter text";

    private Shell shell;
    private CodeGenEditor ideEditor;

    private Text filterText;
    private Table urlTable;
    private TableViewer taskViewer;
    private Button addButton;
    private Button editButton;
    private Button removeButton;
    private Button testButton;
    private Button closeButton;

    public CtipManageUrlPopup(Shell parent, CodeGenEditor ideEditor) {

        this.ideEditor = ideEditor;

        this.shell = new Shell(parent, SWT.DIALOG_TRIM);
        shell.setLayout(new MigLayout("fill", "[grow]10[110]",
            "[20]5[20]5[20]5[20]5[20]25[20]5[grow]10"));

        createFilterText();

        urlTable = createTable(shell);
        urlTable.setLayoutData("grow, spany 6");
        redrawTableData();

        addButton =
            createButtonWithListenerAndLineWrap(shell, "ide.button.add",
                "image.new");
        editButton =
            createButtonWithListenerAndLineWrap(shell, "ide.button.edit",
                "image.edit");
        removeButton =
            createButtonWithListenerAndLineWrap(shell, "ide.button.remove",
                "image.remove");
        testButton =
            createButtonWithListenerAndLineWrap(shell,
                "editor.ctip.button.testconnection", "image.testcon");
        closeButton =
            createButtonWithListenerAndLineWrap(shell, "ide.button.close",
                "image.close");

        shell.setText(MessageUtil.getMessage("editor.ctip.popup.title"));
        shell.setSize(460, 300);

        int screenWidth =
            shell.getDisplay().getPrimaryMonitor().getBounds().width;
        int screenHeight =
            shell.getDisplay().getPrimaryMonitor().getBounds().height;

        shell.setLocation((screenWidth - 460) / 2, (screenHeight - 490) / 3);
    }

    private void createFilterText() {
        filterText = new Text(shell, SWT.BORDER);
        filterText.setText(DEFAULT_FILTER_STR);
        filterText.setLayoutData("growx, wrap");

        filterText.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                if (!(filterText.getText().equals(DEFAULT_FILTER_STR))) {
                    redrawTableData();
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
            new Table(container, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER
                | SWT.V_SCROLL);
        taskTable.setHeaderVisible(true);
        taskTable.setLinesVisible(true);

        taskViewer = new TableViewer(taskTable);

        TableViewerColumn taskName =
            new TableViewerColumn(taskViewer, SWT.None);
        taskName.getColumn().setText("Name");
        taskName.getColumn().setWidth(100);

        TableViewerColumn buildstatus =
            new TableViewerColumn(taskViewer, SWT.None);
        buildstatus.getColumn().setText("URL");
        buildstatus.getColumn().setWidth(200);

        return taskTable;
    }

    public void setFilterDefaultValue() {
        filterText.setText(DEFAULT_FILTER_STR);
    }

    public void redrawTableData() {
        String filterStr = filterText.getText().trim();
        if (filterStr.equals(DEFAULT_FILTER_STR)) {
            filterStr = "";
        }

        List<String> urlList =
            this.ideEditor.getAnyframeConfig().getCtipUrlList();
        urlTable.setItemCount(urlList.size());

        int count = 0;
        for (int i = 0; i < urlList.size(); i++) {
            String row = urlList.get(i);
            String name = row.substring(0, row.indexOf(" - "));
            String url = row.substring(row.toLowerCase().indexOf("http://"));

            if (name.indexOf(filterStr) == -1) {
                continue;
            }

            urlTable.getItem(count).setText(0, name);
            urlTable.getItem(count).setText(1, url);
            count++;
        }
        urlTable.setItemCount(count);
    }

    public void open() {
        this.shell.open();
    }

    private Button createButtonWithListenerAndLineWrap(Composite container,
            String name, String imageKey) {
        Button button =
            SwtGenUtil.createButton(container, name, "", buttonListener);
        Image image =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage(imageKey)));
        button.setImage(image);
        button.setLayoutData("growx, wrap");
        return button;
    }

    private CtipManageUrlPopup getInstance() {
        return this;
    }

    private SelectionListener buttonListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == addButton) {
                CtipAddUrlPopup addUrl =
                    new CtipAddUrlPopup(addButton.getParent().getShell(),
                        getInstance(), ideEditor);
                addUrl.open();

            } else if (e.getSource() == editButton) {
                onEditButtonClicked();

            } else if (e.getSource() == removeButton) {
                onRemoveButtonClicked();

            } else if (e.getSource() == testButton) {
                onTestConnectionButtonClicked();

            } else if (e.getSource() == closeButton) {
                shell.dispose();
            }
        }

    };

    private void onEditButtonClicked() {
        if (urlTable.getSelectionCount() == 0) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.popup.edit.notselected"),
                MessageDialog.WARNING);
            return;
        }

        TableItem item = urlTable.getItem(urlTable.getSelectionIndex());
        CtipAddUrlPopup addUrl =
            new CtipAddUrlPopup(addButton.getParent().getShell(),
                getInstance(), this.ideEditor);
        addUrl.openEditMode(item.getText(0).trim(), item.getText(1).trim());
    }

    private void onRemoveButtonClicked() {
        if (urlTable.getSelectionCount() == 0) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.popup.remove.notselected"),
                MessageDialog.WARNING);
            return;
        }
        if (this.ideEditor.getAnyframeConfig().getCtipUrlList().size() == 1) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.popup.remove.notremoved"),
                MessageDialog.WARNING);
            return;
        }

        if (!DialogUtil.confirmMessageDialog(
            MessageUtil.getMessage("ide.message.title"),
            MessageUtil.getMessage("editor.ctip.popup.remove.confirm"))) {
            return;
        }
        TableItem item = urlTable.getItem(urlTable.getSelectionIndex());

        ((CtipPage) this.ideEditor.getMapPages().get(5))
            .removeCtipServerToUrlCombo(item.getText(0), item.getText(1));

        redrawTableData();
    }

    private void onTestConnectionButtonClicked() {
        if (urlTable.getSelectionCount() == 0) {
            DialogUtil.openMessageDialog(MessageUtil
                .getMessage("ide.message.title"), MessageUtil
                .getMessage("editor.ctip.popup.testconnection.notselected"),
                MessageDialog.WARNING);
            return;
        }

        String url = urlTable.getItem(urlTable.getSelectionIndex()).getText(1);
        hudson.setHudsonURL(url);
        try {
            hudson.getJobList();
            DialogUtil.openMessageDialog(MessageUtil
                .getMessage("ide.message.title"), MessageUtil
                .getMessage("editor.ctip.popup.testconnection.success"),
                MessageDialog.INFORMATION);

        } catch (Exception ex) {
            DialogUtil
                .openMessageDialog(MessageUtil.getMessage("ide.message.title"),
                    MessageUtil
                        .getMessage("editor.ctip.popup.testconnection.fail"),
                    MessageDialog.WARNING);
        }
    }
}
