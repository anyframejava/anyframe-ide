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

import net.miginfocom.swt.MigLayout;

import org.anyframe.ide.eclipse.core.editor.CodeGenEditor;
import org.anyframe.ide.eclipse.core.editor.CtipPage;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.SwtGenUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * This is a CtipAddUrlPopup class.
 * @author Soungmin Joo
 */
public class CtipAddUrlPopup {
    private CodeGenEditor ideEditor;
    private CtipPage ctipPage;
    private CtipManageUrlPopup managePopup;

    private Shell shell;
    private Text nameText;
    private Text locationText;
    private Button buttonAddUrl;
    private Button cancelButton;
    private String oldLocationStr;

    public CtipAddUrlPopup(Shell parent, CtipManageUrlPopup managePopup,
            CodeGenEditor ideEditor) {
        this.ideEditor = ideEditor;
        this.managePopup = managePopup;
        this.ctipPage = (CtipPage) this.ideEditor.getMapPages().get(5);

        this.shell = new Shell(parent, SWT.DIALOG_TRIM);
        shell.setLayout(new MigLayout("fill", "5[70][grow]10", "15[][]5[]20"));

        SwtGenUtil.createLabel(shell, "editor.ctip.addpopup.name");
        nameText = new Text(shell, SWT.SINGLE | SWT.BORDER);
        nameText.setLayoutData("growx, wrap");

        SwtGenUtil.createLabel(shell, "editor.ctip.addpopup.location");
        locationText = new Text(shell, SWT.SINGLE | SWT.BORDER);
        locationText.setText("http://");
        locationText.setLayoutData("growx, wrap");
        locationText.addKeyListener(keyListener);

        buttonAddUrl =
            SwtGenUtil.createButton(shell, "ide.button.ok", "", buttonListener);
        buttonAddUrl.setLayoutData("wmin 60px, span 2, alignx right, split 2");
        cancelButton =
            SwtGenUtil.createButton(shell, "ide.button.cancel", "",
                buttonListener);
        cancelButton.setLayoutData("wmin 60px");

        shell.setText(MessageUtil.getMessage("editor.ctip.addpopup.title"));
        shell.setSize(300, 130);

        int screenWidth =
            shell.getDisplay().getPrimaryMonitor().getBounds().width;
        int screenHeight =
            shell.getDisplay().getPrimaryMonitor().getBounds().height;

        shell.setLocation((screenWidth - 300) / 2, (screenHeight - 130) / 3);
    }

    public void open() {
        oldLocationStr = null;
        this.shell.open();
    }

    public void openEditMode(String name, String location) {
        nameText.setText(name);
        locationText.setText(location);
        oldLocationStr = name + " - " + location;

        this.shell.open();
    }

    private boolean isEditMode() {
        return oldLocationStr != null;
    }

    private KeyListener keyListener = new KeyAdapter() {
        public void keyReleased(KeyEvent e) {
            if (e.keyCode == '\r') {
                onAddButtonClicked();

            }
        }
    };

    private SelectionListener buttonListener = new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == buttonAddUrl) {
                onAddButtonClicked();

            } else if (e.getSource() == cancelButton) {
                shell.close();
            }
        }

    };

    private void onAddButtonClicked() {
        if (nameText.getText().trim().equals("")) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.addpopup.valid.server"),
                MessageDialog.WARNING);
            return;
        }

        if (!locationText.getText().trim().toLowerCase().startsWith("http://")) {
            DialogUtil.openMessageDialog(MessageUtil
                .getMessage("ide.message.title"), MessageUtil
                .getMessage("editor.ctip.addpopup.valid.location.prefix"),
                MessageDialog.WARNING);
            return;
        }

        if (locationText.getText().trim().equals("http://")) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.ctip.addpopup.valid.location"),
                MessageDialog.WARNING);
            return;
        }

        if (locationText.getText().trim().endsWith("/")) {
            String text = locationText.getText().trim();
            locationText.setText(text.substring(0, text.length() - 1));
        }

        if (isEditMode()) {
            ctipPage.updateCtipServerToUrlCombo(nameText.getText().trim(),
                locationText.getText().trim(), oldLocationStr);
            managePopup.redrawTableData();
            shell.close();

        } else {
            ctipPage.setFilterDefaultValue();
            boolean isSuccess =
                ctipPage.getTaskListFromHudson(locationText.getText().trim());
            if (isSuccess) {
                ctipPage.addCtipServerToUrlCombo(nameText.getText().trim(),
                    locationText.getText().trim());

                managePopup.setFilterDefaultValue();
                managePopup.redrawTableData();
                shell.close();
            } else {
                DialogUtil.openMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("editor.ctip.addpopup.valid.connection"),
                    MessageDialog.WARNING);
            }
        }
    }
}
