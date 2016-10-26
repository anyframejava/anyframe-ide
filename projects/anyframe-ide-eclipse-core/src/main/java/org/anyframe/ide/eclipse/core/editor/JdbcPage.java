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

import java.util.Map;

import net.miginfocom.swt.MigLayout;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.eclipse.core.CommandExecution;
import org.anyframe.ide.eclipse.core.config.JdbcType;
import org.anyframe.ide.eclipse.core.util.DatabaseUtil;
import org.anyframe.ide.eclipse.core.util.DialogUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * This is an JDBCConfigPage class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class JdbcPage implements Page {
    private CodeGenEditor ideEditor;
    private Combo databaseTypeCombo;
    private Text useNameText;
    private Text passwordText;
    private Text serverText;
    private Text databaseNameText;
    private Text driverJarText;
    private Combo schemaCombo;
    private Combo dialectCombo;
    private Text driverClassNameText;
    private FormToolkit toolkit;

    private Text portText;

    private Label schemaExceptionLabel;
    private boolean isChangedDBConfig = false;

    private Text driverGroupIdText;
    private Text driverArtifactIdText;
    private Text driverVersionText;

    private Button buttonBrowse;
    private Button buttonApply;
    private Button buttonRefresh;

    private java.util.List<JdbcType> jdbcTypes;
    private PropertiesIO pjtProps = null;

    public JdbcPage(CodeGenEditor ideEditor) {
        this.ideEditor = ideEditor;
    }

    private Listener databaseChangeListener = new Listener() {
        public void handleEvent(Event event) {
            isChangedDBConfig = true;
        }
    };

    private void getJdbcTypes() {
        jdbcTypes = this.ideEditor.getJdbcTypes();
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
        getJdbcTypes();

        toolkit = new FormToolkit(parent.getDisplay());
        final ScrolledForm form = toolkit.createScrolledForm(parent);
        form.setText(MessageUtil.getMessage("editor.codegen.jdbc.title"));
        form.getBody().setLayout(new FillLayout());

        Composite container = toolkit.createComposite(form.getBody(), SWT.NONE);
        container.setLayout(new MigLayout("fillx"));

        createJdbcSection(form, container);
        if (pjtProps.readValue(CommonConstants.PROJECT_BUILD_TYPE).equals(
            CommonConstants.PROJECT_BUILD_TYPE_MAVEN))
            createJdbcArtifactSection(form, container);
        createCommandButton(form, container);

        loadConfiguration();

        form.reflow(true);

        return form;
    }

    private void createJdbcSection(final ScrolledForm form,
            final Composite parent) {

        Section section =
            toolkit.createSection(parent, Section.TWISTIE | Section.EXPANDED
                | Section.DESCRIPTION | Section.TITLE_BAR);
        section.setText(MessageUtil
            .getMessage("editor.config.jdbc.section.jdbc"));
        section.setDescription(MessageUtil
            .getMessage("editor.config.jdbc.description"));
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
        container.setLayout(new MigLayout("",
            "[right]10[left, grow]15[right]10[left, grow]15", "20"));
        section.setClient(container);

        // Database Type selection
        toolkit.createLabel(container,
            MessageUtil.getMessage("editor.config.jdbc.typename"), SWT.WRAP);

        databaseTypeCombo =
            new Combo(container, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        databaseTypeCombo.setLayoutData("grow, wmax 350");
        databaseTypeCombo.addSelectionListener(databaseListener);

        // Database Name
        toolkit.createLabel(container,
            MessageUtil.getMessage("editor.config.jdbc.dbname"), SWT.WRAP);
        databaseNameText =
            toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER);
        databaseNameText.setLayoutData("grow, wrap");
        databaseNameText.addListener(SWT.KeyDown, databaseChangeListener);

        // User Name
        toolkit.createLabel(container,
            MessageUtil.getMessage("editor.config.jdbc.username"), SWT.WRAP);
        useNameText =
            toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER);
        useNameText.setLayoutData("grow, wmax 350");
        useNameText.addListener(SWT.KeyDown, databaseChangeListener);

        // HOST
        toolkit.createLabel(container,
            MessageUtil.getMessage("editor.config.jdbc.server"), SWT.WRAP);
        serverText = toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER);
        serverText.setLayoutData("grow, wrap");
        serverText.addListener(SWT.KeyDown, databaseChangeListener);

        // PASSWORD
        toolkit.createLabel(container,
            MessageUtil.getMessage("editor.config.jdbc.password"), SWT.WRAP);
        passwordText =
            toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER
                | SWT.PASSWORD);
        passwordText.setLayoutData("grow, wmax 350");
        passwordText.addListener(SWT.KeyDown, databaseChangeListener);

        // PORT
        toolkit.createLabel(container,
            MessageUtil.getMessage("editor.config.jdbc.port"), SWT.WRAP);
        portText = toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER);
        portText.setLayoutData("grow, wrap");
        portText.addListener(SWT.KeyDown, databaseChangeListener);

        // Dialect
        toolkit.createLabel(container,
            MessageUtil.getMessage("editor.config.jdbc.dialect"), SWT.WRAP);

        dialectCombo =
            new Combo(container, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        dialectCombo.setLayoutData("grow, wmax 350");
        dialectCombo.addListener(SWT.Selection, databaseChangeListener);

        // Driver Class Name
        toolkit.createLabel(container,
            MessageUtil.getMessage("editor.config.jdbc.driverclass"), SWT.WRAP);
        driverClassNameText =
            toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER);
        driverClassNameText.setLayoutData("grow, wrap");
        driverClassNameText.addListener(SWT.KeyDown, databaseChangeListener);

        // Driver Jar Path
        toolkit.createLabel(container,
            MessageUtil.getMessage("editor.config.jdbc.driverjar"), SWT.WRAP);
        driverJarText =
            toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER);
        driverJarText.setLayoutData("grow, wmax 350");
        driverJarText.addListener(SWT.KeyDown, databaseChangeListener);

        buttonBrowse = new Button(container, SWT.PUSH);
        buttonBrowse.setText(MessageUtil.getMessage("ide.button.browse"));
        Image imageSearch =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.serarch")));
        buttonBrowse.setImage(imageSearch);

        buttonBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(parent.getShell(), SWT.OPEN);
                dlg.setFilterExtensions(new String[] {"*.jar;*.zip" });
                String str = dlg.open();
                if (str != null) {
                    isChangedDBConfig = true;
                    driverJarText.setText(str);
                }
            }
        });

        buttonBrowse.setLayoutData("gapleft 1, grow, width 125!, wrap");

        // Schema
        toolkit.createLabel(container,
            MessageUtil.getMessage("editor.config.jdbc.schemaname"), SWT.WRAP);
        schemaCombo =
            new Combo(container, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        schemaCombo.setLayoutData("grow, wmax 350");
        schemaCombo.addListener(SWT.DROP_DOWN, new Listener() {
            public void handleEvent(Event e) {

                if (schemaCombo.getItemCount() == 1 || isChangedDBConfig) {
                    setSchema(driverJarText.getText(),
                        driverClassNameText.getText(),
                        databaseTypeCombo.getText(),
                        databaseNameText.getText(), serverText.getText(),
                        portText.getText(), useNameText.getText(),
                        passwordText.getText());
                } else {
                    Map<Object, Object> result =
                        DatabaseUtil.checkConnection(pjtProps
                            .readValue(CommonConstants.PROJECT_HOME),
                            driverJarText.getText(), driverClassNameText
                                .getText(), DatabaseUtil.getDbUrl(
                                databaseTypeCombo.getText(),
                                databaseNameText.getText(),
                                serverText.getText(), portText.getText()),
                            useNameText.getText(), passwordText.getText());
                    boolean checkResult =
                        (Boolean) result.get(DatabaseUtil.DB_CON_CHK_KEY);
                    if (!checkResult) {
                        setDefaultSchema();
                        schemaExceptionLabel.setText(MessageUtil
                            .getMessage("editor.config.jdbc.checkjdbc")
                            + " "
                            + (String) result.get(DatabaseUtil.DB_CON_MSG_KEY));
                    }
                }
            }
        });

        Label label = new Label(container, SWT.NONE);
        label.setLayoutData("grow, wrap");

        // Schema Exception
        schemaExceptionLabel = toolkit.createLabel(container, "", SWT.NONE);
        schemaExceptionLabel.setLayoutData("grow,  wrap, gaptop 11, span4");
        schemaExceptionLabel.setForeground(new Color(container.getDisplay(),
            255, 0, 0));
    }

    private void createJdbcArtifactSection(final ScrolledForm form,
            final Composite parent) {

        Section section =
            toolkit.createSection(parent, Section.TWISTIE | Section.EXPANDED
                | Section.DESCRIPTION | Section.TITLE_BAR);
        section.setText(MessageUtil
            .getMessage("editor.config.jdbc.section.artifact"));
        section.setDescription(MessageUtil
            .getMessage("editor.config.jdbc.section.artifact.desc"));
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
        container.setLayout(new MigLayout("", "[right]10[left, grow]", "20"));
        section.setClient(container);

        // Driver jar Group Id
        toolkit.createLabel(container,
            MessageUtil.getMessage("wizard.maven.groupid"), SWT.WRAP);

        driverGroupIdText =
            toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER);
        driverGroupIdText.setLayoutData("grow, wrap");
        driverGroupIdText.setText(MessageUtil
            .getMessage("wizard.maven.groupid.default"));

        driverGroupIdText.addListener(SWT.KeyDown, databaseChangeListener);

        // Driver jar Artifact Id
        toolkit.createLabel(container,
            MessageUtil.getMessage("wizard.maven.artifactid"), SWT.WRAP);

        driverArtifactIdText =
            toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER);
        driverArtifactIdText.setLayoutData("grow, wrap");
        driverArtifactIdText.addListener(SWT.KeyDown, databaseChangeListener);

        // Driver jar Version
        toolkit.createLabel(container,
            MessageUtil.getMessage("wizard.maven.version"), SWT.WRAP);
        driverVersionText =
            toolkit.createText(container, "", SWT.SINGLE | SWT.BORDER);
        driverVersionText.setLayoutData("grow, wrap");
        driverVersionText.setText(MessageUtil
            .getMessage("wizard.maven.version.default"));
        driverVersionText.addListener(SWT.KeyDown, databaseChangeListener);
    }

    private void createCommandButton(ScrolledForm form, Composite parent) {

        Label label =
            toolkit.createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData("wrap, gapy 2, growx");

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
        buttonApply.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                savePage();
            }
        });

        Image imageRefresh =
            new Image(container.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.refresh")));
        buttonRefresh =
            toolkit.createButton(container,
                MessageUtil.getMessage("ide.button.refresh"), SWT.PUSH);
        buttonRefresh.setImage(imageRefresh);
        buttonRefresh.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                loadConfiguration();
            }
        });
    }

    protected void setButtonLayoutData(Button button) {
        GridData data = new GridData(SWT.CENTER);
        data.widthHint = 55;
        data.heightHint = 25;

        button.setLayoutData(data);
    }

    private SelectionListener databaseListener = new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e) {
            isChangedDBConfig = true;
            dialectCombo.removeAll();
            setDefaultSchema();
            setDialect(databaseTypeCombo.getSelectionIndex());
            setPort(databaseTypeCombo.getSelectionIndex());
            setDriverClassName(databaseTypeCombo.getSelectionIndex());
            setDriverJarPath(databaseTypeCombo.getSelectionIndex());
            schemaExceptionLabel.setText("");

            if (pjtProps.readValue(CommonConstants.PROJECT_BUILD_TYPE).equals(
                CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
                setDriverPom(databaseTypeCombo.getSelectionIndex());
            }
        }

        public void widgetSelected(SelectionEvent e) {
            widgetDefaultSelected(e);
        }
    };

    private void setDriverJarPath(int index) {
        driverJarText.setText(pjtProps
            .readValue(CommonConstants.DB_DRIVER_PATH));
    }

    private void setDriverClassName(int index) {
        String driverClass = jdbcTypes.get(index).getDriver();
        driverClassNameText.setText(driverClass);
    }

    private void setDriverPom(int index) {
        driverGroupIdText.setText(jdbcTypes.get(index).getDriverGroupId());
        driverArtifactIdText
            .setText(jdbcTypes.get(index).getDriverArtifactId());
        driverVersionText.setText(jdbcTypes.get(index).getDriverVersion());
    }

    private void setDefaultSchema() {
        schemaCombo.removeAll();
        schemaCombo.add(MessageUtil
            .getMessage("editor.config.jdbc.defaultschema"));

        schemaCombo.select(0);
    }

    /*
     * private boolean setSchema() { AnyframeConfig
     * anyframeConfig =
     * CodeGenEditor.getAnyframeConfig(); return
     * setSchema(anyframeConfig); }
     */
    private boolean setSchema(String driverJarPath, String driverClassName,
            String dbType, String dbName, String server, String port,
            String userName, String password) {
        schemaCombo.removeAll();
        schemaCombo.add(MessageUtil
            .getMessage("editor.config.jdbc.defaultschema"));

        Map<Object, Object> result =
            DatabaseUtil.checkConnection(
                pjtProps.readValue(CommonConstants.PROJECT_HOME),
                driverJarPath, driverClassName,
                DatabaseUtil.getDbUrl(dbType, dbName, server, port), userName,
                password);
        boolean checkResult = (Boolean) result.get(DatabaseUtil.DB_CON_CHK_KEY);

        if (checkResult) {

            if (databaseTypeCombo.getText().equals("sybase")) {
                schemaCombo.removeAll();
                schemaCombo.add("No Schema");
            } else {
                String[] schema;
                try {
                    schema =
                        DatabaseUtil
                            .getSchemas(pjtProps
                                .readValue(CommonConstants.PROJECT_HOME),
                                driverJarPath, driverClassName, DatabaseUtil
                                    .getDbUrl(dbType, dbName, server, port),
                                userName, password);
                    if (schema.length == 0) {
                        schemaCombo.removeAll();
                        schemaCombo.add("No Schema");
                    } else {
                        for (int i = 0; i < schema.length; i++) {
                            schemaCombo.add(schema[i]);
                        }
                    }
                } catch (Exception e) {
                    ExceptionUtil.showException(
                        MessageUtil.getMessage("editor.exception.getschema"),
                        IStatus.ERROR, e);
                    schemaExceptionLabel.setText(MessageUtil
                        .getMessage("editor.config.jdbc.checkjdbc")
                        + " "
                        + (String) result.get(DatabaseUtil.DB_CON_MSG_KEY));
                }
            }
            schemaExceptionLabel.setText("");
        } else {
            schemaExceptionLabel.setText(MessageUtil
                .getMessage("editor.config.jdbc.checkjdbc")
                + " "
                + (String) result.get(DatabaseUtil.DB_CON_MSG_KEY));
        }
        if (schemaCombo.getItemCount() == 1)
            schemaCombo.select(0);

        isChangedDBConfig = false;

        return checkResult;
    }

    private void setDialect(int index) {
        String[] dialect = jdbcTypes.get(index).getDialect();

        for (int i = 0; i < dialect.length; i++)
            dialectCombo.add(dialect[i]);

        dialectCombo.select(0);
    }

    private void setPort(int index) {
        String port = jdbcTypes.get(index).getPort();

        portText.setText(port);
    }

    private void loadConfiguration() {

        try {
            pjtProps =
                ProjectUtil.getProjectProperties(ideEditor.getCurrentProject());
        } catch (Exception e) {
            ExceptionUtil.showException(
                MessageUtil.getMessage("editor.exception.error.properties"),
                IStatus.ERROR, e);
        }

        databaseTypeCombo.removeAll();
        for (JdbcType jdbcType : jdbcTypes)
            databaseTypeCombo.add(jdbcType.getType());

        databaseTypeCombo.setText(pjtProps.readValue(CommonConstants.DB_TYPE));

        setDialect(databaseTypeCombo.getSelectionIndex());

        databaseNameText.setText(pjtProps.readValue(CommonConstants.DB_NAME));

        if (pjtProps.readValue(CommonConstants.DB_SCHEMA) == "") {
            setDefaultSchema();
        } else {
            setSchema(pjtProps.readValue(CommonConstants.DB_DRIVER_PATH),
                pjtProps.readValue(CommonConstants.DB_DRIVER_CLASS),
                pjtProps.readValue(CommonConstants.DB_TYPE),
                pjtProps.readValue(CommonConstants.DB_NAME),
                pjtProps.readValue(CommonConstants.DB_SERVER),
                pjtProps.readValue(CommonConstants.DB_PORT),
                pjtProps.readValue(CommonConstants.DB_USERNAME),
                pjtProps.readValue(CommonConstants.DB_PASSWORD));
            schemaCombo.select(schemaCombo.indexOf(pjtProps
                .readValue(CommonConstants.DB_SCHEMA)));
        }

        useNameText.setText(pjtProps.readValue(CommonConstants.DB_USERNAME));
        passwordText.setText(pjtProps.readValue(CommonConstants.DB_PASSWORD));
        serverText.setText(pjtProps.readValue(CommonConstants.DB_SERVER));
        portText.setText(String.valueOf(pjtProps
            .readValue(CommonConstants.DB_PORT)));
        dialectCombo.setText(pjtProps.readValue(CommonConstants.DB_DIALECT));
        setDriverJarPath(databaseTypeCombo.getSelectionIndex());
        driverClassNameText.setText(pjtProps
            .readValue(CommonConstants.DB_DRIVER_CLASS));
        schemaExceptionLabel.setText("");

        if (pjtProps.readValue(CommonConstants.PROJECT_BUILD_TYPE).equals(
            CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
            driverGroupIdText.setText(pjtProps
                .readValue(CommonConstants.DB_GROUPID));
            driverArtifactIdText.setText(pjtProps
                .readValue(CommonConstants.DB_ARTIFACTID));
            driverVersionText.setText(pjtProps
                .readValue(CommonConstants.DB_VERSION));
        }
    }

    public void savePage() {
        if (validateData()) {

            try {
                pjtProps =
                    ProjectUtil.getProjectProperties(ideEditor
                        .getCurrentProject());
            } catch (Exception e) {
                ExceptionUtil
                    .showException(MessageUtil
                        .getMessage("editor.exception.error.properties"),
                        IStatus.ERROR, e);
            }

            try {
                Integer.valueOf(portText.getText());
            } catch (Exception e) {
                portText.setText("-1");
                ExceptionUtil.showException(
                    MessageUtil.getMessage("editor.exception.dbport"),
                    IStatus.INFO, e);
            }

            pjtProps.setProperty(CommonConstants.DB_TYPE,
                databaseTypeCombo.getText());
            pjtProps.setProperty(CommonConstants.DB_NAME,
                databaseNameText.getText());
            pjtProps.setProperty(CommonConstants.DB_SCHEMA, schemaCombo
                .getText().equals("No Schema") ? "" : schemaCombo.getText());
            pjtProps.setProperty(CommonConstants.DB_USERNAME,
                useNameText.getText());
            pjtProps.setProperty(CommonConstants.DB_PASSWORD,
                passwordText.getText());
            pjtProps.setProperty(CommonConstants.DB_SERVER,
                serverText.getText());
            pjtProps.setProperty(CommonConstants.DB_PORT, portText.getText());
            pjtProps.setProperty(CommonConstants.DB_DIALECT,
                dialectCombo.getText());
            pjtProps.setProperty(CommonConstants.DB_DRIVER_CLASS,
                driverClassNameText.getText());
            pjtProps.setProperty(CommonConstants.DB_DRIVER_PATH,
                driverJarText.getText());

            String url =
                DatabaseUtil.getDbUrl(databaseTypeCombo.getText(),
                    databaseNameText.getText(), serverText.getText(),
                    portText.getText());
            pjtProps.setProperty(CommonConstants.DB_URL, url);

            if (pjtProps.readValue(CommonConstants.PROJECT_BUILD_TYPE).equals(
                CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
                pjtProps.setProperty(CommonConstants.DB_GROUPID,
                    driverGroupIdText.getText());
                pjtProps.setProperty(CommonConstants.DB_ARTIFACTID,
                    driverArtifactIdText.getText());
                pjtProps.setProperty(CommonConstants.DB_VERSION,
                    driverVersionText.getText());
            }

            pjtProps.write();

            try {
                // call ant task
                CommandExecution genExecution = new CommandExecution();
                genExecution.changeDBConfig(ideEditor.getCurrentProject()
                    .getLocation().toOSString());

            } catch (Exception e) {
                ExceptionUtil.showException(
                    MessageUtil.getMessage("editor.exception.savedbconfig"),
                    IStatus.ERROR, e);
                DialogUtil.openMessageDialog(
                    MessageUtil.getMessage("ide.message.title"),
                    MessageUtil.getMessage("editor.exception.findconfig"),
                    MessageDialog.ERROR);
            }
        }
    }

    // validation for essential values
    // public boolean validateData(AnyframeConfig
    // anyframeConfig) {
    public boolean validateData() {
        // Database Name, Server, Driver Class Name,
        // Driver Jar Path
        if (databaseNameText.getText() == null
            || databaseNameText.getText().length() == 0) {
            DialogUtil.openDetailMessageDialog(MessageUtil
                .getMessage("ide.message.title"), MessageUtil
                .getMessage("editor.dialog.validation.dbname"), MessageUtil
                .getMessage("editor.dialog.validation.dbname.detail"),
                MessageDialog.ERROR);
            return false;
        } else if (!ProjectUtil.validateName(databaseNameText.getText())) {
            DialogUtil
                .openDetailMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("editor.dialog.validation.error.dbname"),
                    MessageUtil
                        .getMessage("editor.dialog.validation.dbname.detail"),
                    MessageDialog.ERROR);
            return false;
        } else if (StringUtils.isNotEmpty(useNameText.getText())) {
            if (!ProjectUtil.validateName(useNameText.getText())) {
                DialogUtil
                    .openDetailMessageDialog(
                        MessageUtil.getMessage("ide.message.title"),
                        MessageUtil
                            .getMessage("editor.dialog.validation.error.username"),
                        MessageUtil
                            .getMessage("editor.dialog.validation.username.detail"),
                        MessageDialog.ERROR);
                return false;
            }
        } else if (StringUtils.isNotEmpty(passwordText.getText())) {
            if (!ProjectUtil.validateName(passwordText.getText())) {
                DialogUtil
                    .openDetailMessageDialog(
                        MessageUtil.getMessage("ide.message.title"),
                        MessageUtil
                            .getMessage("editor.dialog.validation.error.password"),
                        MessageUtil
                            .getMessage("editor.dialog.validation.password.detail"),
                        MessageDialog.ERROR);
                return false;
            }
        } else if (serverText.getText() == null
            || serverText.getText().length() == 0) {
            DialogUtil.openDetailMessageDialog(MessageUtil
                .getMessage("ide.message.title"), MessageUtil
                .getMessage("editor.dialog.validation.dbserver"), MessageUtil
                .getMessage("editor.dialog.validation.dbserver.detail"),
                MessageDialog.ERROR);
            return false;
        } else if (!ProjectUtil.validateName(serverText.getText())) {
            DialogUtil.openDetailMessageDialog(MessageUtil
                .getMessage("ide.message.title"), MessageUtil
                .getMessage("editor.dialog.validation.error.dbserver"),
                MessageUtil
                    .getMessage("editor.dialog.validation.dbserver.detail"),
                MessageDialog.ERROR);
            return false;
        } else if (StringUtils.isNotEmpty(portText.getText())) {
            if (!ProjectUtil.validateName(portText.getText())) {
                DialogUtil.openDetailMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("editor.dialog.validation.error.port"),
                    MessageUtil
                        .getMessage("editor.dialog.validation.port.detail"),
                    MessageDialog.ERROR);
                return false;
            }
        } else if (driverClassNameText.getText() == null
            || driverClassNameText.getText().length() == 0) {
            DialogUtil.openDetailMessageDialog(MessageUtil
                .getMessage("ide.message.title"), MessageUtil
                .getMessage("editor.dialog.validation.dbclassname"),
                MessageUtil
                    .getMessage("editor.dialog.validation.dbclassname.detail"),
                MessageDialog.ERROR);
            return false;
        } else if (!ProjectUtil.validateName(driverClassNameText.getText())) {
            DialogUtil.openDetailMessageDialog(MessageUtil
                .getMessage("ide.message.title"), MessageUtil
                .getMessage("editor.dialog.validation.error.dbclassname"),
                MessageUtil
                    .getMessage("editor.dialog.validation.dbclassname.detail"),
                MessageDialog.ERROR);
            return false;
        } else if (driverJarText.getText() == null
            || driverJarText.getText().length() == 0) {
            DialogUtil
                .openDetailMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("editor.dialog.validation.dbjar"), MessageUtil
                    .getMessage("editor.dialog.validation.dbjar.detail"),
                    MessageDialog.ERROR);
            return false;
        } else if (!ProjectUtil.existPath(driverJarText.getText())) {
            DialogUtil
                .openDetailMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("editor.dialog.validation.dbjar.valid"),
                    MessageUtil
                        .getMessage("editor.dialog.validation.dbjar.detail"),
                    MessageDialog.ERROR);
            return false;
        }

        Map<Object, Object> validationResult =
            DatabaseUtil.checkConnection(pjtProps
                .readValue(CommonConstants.PROJECT_HOME), driverJarText
                .getText(), driverClassNameText.getText(), DatabaseUtil
                .getDbUrl(databaseTypeCombo.getText(),
                    databaseNameText.getText(), serverText.getText(),
                    portText.getText()), useNameText.getText(), passwordText
                .getText());

        if (!(Boolean) validationResult.get(DatabaseUtil.DB_CON_CHK_KEY)) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.config.jdbc.setjdbc"),
                MessageDialog.ERROR);
            return false;
        }

        // Schema
        if (schemaCombo.getText() != null
            && schemaCombo.getText().trim().length() == 0) {
            schemaCombo.select(0);

            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.dialog.validation.dbschema"),
                MessageDialog.INFORMATION);
            return false;
        } else if (schemaCombo.getText().equals(
            MessageUtil.getMessage("editor.config.jdbc.defaultschema"))
            && schemaCombo.getItemCount() == 1) {
            schemaCombo.select(0);
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.dialog.validation.dbschema"),
                MessageDialog.INFORMATION);
            return false;

        } else if (schemaCombo.getText().equals(
            MessageUtil.getMessage("editor.config.jdbc.defaultschema"))
            && schemaCombo.getItemCount() > 1) {
            DialogUtil.openMessageDialog(
                MessageUtil.getMessage("ide.message.title"),
                MessageUtil.getMessage("editor.dialog.validation.dbschema"),
                MessageDialog.INFORMATION);
            return false;
        }

        if (pjtProps.readValue(CommonConstants.PROJECT_BUILD_TYPE).equals(
            CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
            if (this.driverGroupIdText.getText() == null
                || this.driverGroupIdText.getText().length() == 0) {
                DialogUtil.openMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("wizard.application.error.pjtgroupid"),
                    MessageDialog.ERROR);
                return false;
            } else if (!ProjectUtil.validateName(this.driverGroupIdText
                .getText())) {
                DialogUtil.openMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("wizard.application.validation.pjtgroupid"),
                    MessageDialog.ERROR);
                return false;
            }
            if (this.driverArtifactIdText.getText() == null
                || this.driverArtifactIdText.getText().length() == 0) {
                DialogUtil.openMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("wizard.application.error.pjtartifactid"),
                    MessageDialog.ERROR);
                return false;
            } else if (!ProjectUtil.validateName(this.driverArtifactIdText
                .getText())) {
                DialogUtil.openMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("wizard.application.validation.pjtartifactid"),
                    MessageDialog.ERROR);
                return false;
            }
            if (this.driverVersionText.getText() == null
                || this.driverVersionText.getText().length() == 0) {
                DialogUtil.openMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("wizard.application.error.pjtversion"),
                    MessageDialog.ERROR);
                return false;
            } else if (!ProjectUtil.validateName(this.driverVersionText
                .getText())) {
                DialogUtil.openMessageDialog(MessageUtil
                    .getMessage("ide.message.title"), MessageUtil
                    .getMessage("wizard.application.validation.pjtversion"),
                    MessageDialog.ERROR);
                return false;
            }
        }

        if (!DialogUtil.confirmMessageDialog(
            MessageUtil.getMessage("ide.message.title"),
            MessageUtil.getMessage("editor.dialog.confirm.jdbcconf")))
            return false;
        return true;
    }
}
