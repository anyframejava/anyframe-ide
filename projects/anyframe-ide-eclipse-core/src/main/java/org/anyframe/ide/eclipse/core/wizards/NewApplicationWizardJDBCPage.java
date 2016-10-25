/*   
 * Copyright 2008-2011 the original author or authors.   
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.anyframe.ide.eclipse.core.config.JdbcType;
import org.anyframe.ide.eclipse.core.util.DatabaseUtil;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.anyframe.ide.eclipse.core.util.ProjectUtil;
import org.anyframe.ide.eclipse.core.util.XmlFileUtil;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.anyframe.ide.command.maven.mojo.container.PluginContainer;

/**
 * This is an NewApplicationWizardJDBCPage class.
 * @author Sooyeon Park
 * @author Eunjin Jang
 */
public class NewApplicationWizardJDBCPage extends WizardPage {

    private Combo databaseTypeCombo;
    private Text useNameText;
    private Text passwordText;
    private Text serverText;
    private Text portText;
    private Text databaseNameText;
    private Text driverClassNameText;
    private Text driverJarText;
    private Combo schemaCombo;
    private Combo dialectCombo;
    private java.util.List<JdbcType> jdbcTypes;
    private String anyframeHomeLocation;
    private String anyframeHome;
    private Label schemaException;
    private boolean isChangedDBConfig = false;

    private Text driverGroupIdText;
    private Text driverArtifactIdText;
    private Text driverVersionText;

    public void setAnyframeHome(String anyframeHome) {
        this.anyframeHome = anyframeHome;
        // get jdbc types
        getJdbcTypes();
    }

    public String getAnyframeHome() {
        return anyframeHome;
    }

    public Combo getSchemaCombo() {
        return schemaCombo;
    }

    public void setSchemaCombo(Combo schemaCombo) {
        this.schemaCombo = schemaCombo;
    }

    protected NewApplicationWizardJDBCPage(String pageName) {
        super(pageName);
        this.setTitle(MessageUtil.getMessage("wizard.jdbc.title"));
        this.setDescription(MessageUtil.getMessage("wizard.jdbc.description"));
    }

    private Listener textModifyListener = new Listener() {
        public void handleEvent(Event event) {
            setPageComplete(isPageComplete());
        }

    };

    private Listener databaseChangeListener = new Listener() {
        public void handleEvent(Event event) {
            isChangedDBConfig = true;
        }
    };

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createJdbcGroup(composite);

        if (!((NewApplicationWizardPage) getPreviousPage()).isAntProject()) {
            createDriverArtifactGroup(composite);
        }
        loadConfiguration();

        setPageComplete(isPageComplete());
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
    }

    private void createJdbcGroup(Composite parent) {
        // creaste jdbc group
        // Contents Group
        Group group = new Group(parent, SWT.SHADOW_NONE);
        group.setText(MessageUtil.getMessage("wizard.jdbc.label"));
        group.setLayout(new GridLayout(3, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Database Type selection
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("editor.config.jdbc.typename"));

        databaseTypeCombo =
            new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        databaseTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        databaseTypeCombo.addSelectionListener(databaseTypeListener);

        // empty label
        new Label(group, SWT.NONE);

        // Database Name
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("editor.config.jdbc.dbname"));
        databaseNameText = new Text(group, SWT.BORDER);
        databaseNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        databaseNameText.addListener(SWT.Modify, textModifyListener);
        databaseNameText.addListener(SWT.KeyDown, databaseChangeListener);

        // empty label
        new Label(group, SWT.NONE);

        // User Name
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("editor.config.jdbc.username"));
        useNameText = new Text(group, SWT.BORDER);
        useNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        useNameText.addListener(SWT.KeyDown, databaseChangeListener);

        // empty label
        new Label(group, SWT.NONE);

        // PASSWORD
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("editor.config.jdbc.password"));
        passwordText = new Text(group, SWT.BORDER | SWT.PASSWORD);
        passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        passwordText.addListener(SWT.KeyDown, databaseChangeListener);

        // empty label
        new Label(group, SWT.NONE);

        // HOST
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("editor.config.jdbc.server"));
        serverText = new Text(group, SWT.BORDER);
        serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        serverText.addListener(SWT.Modify, textModifyListener);
        serverText.addListener(SWT.KeyDown, databaseChangeListener);

        // empty label
        new Label(group, SWT.NONE);

        // PORT
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("editor.config.jdbc.port"));
        portText = new Text(group, SWT.BORDER);
        portText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        portText.addListener(SWT.KeyDown, databaseChangeListener);

        // empty label
        new Label(group, SWT.NONE);

        // Dialect
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("editor.config.jdbc.dialect"));
        dialectCombo =
            new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        dialectCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dialectCombo.addListener(SWT.Selection, databaseChangeListener);

        // empty label
        new Label(group, SWT.NONE);

        // Driver Class
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("editor.config.jdbc.driverclass"));
        driverClassNameText = new Text(group, SWT.BORDER);
        driverClassNameText
            .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        driverClassNameText.addListener(SWT.Modify, textModifyListener);
        driverClassNameText.addListener(SWT.KeyDown, databaseChangeListener);

        // empty label
        new Label(group, SWT.NONE);

        // Driver Jar Path
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("editor.config.jdbc.driverjar"));
        driverJarText = new Text(group, SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = 10;
        driverJarText.setLayoutData(gridData);
        driverJarText.addListener(SWT.Modify, textModifyListener);
        driverJarText.addListener(SWT.KeyDown, databaseChangeListener);

        Button searchButton = new Button(group, SWT.NULL);
        searchButton.setText(MessageUtil.getMessage("ide.button.browse"));
        Image imageSearch =
            new Image(group.getDisplay(), getClass().getResourceAsStream(
                MessageUtil.getMessage("image.serarch")));
        searchButton.setImage(imageSearch);
        final Shell shell = parent.getShell();
        searchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                FileDialog dlg = new FileDialog(shell, SWT.OPEN);
                dlg.setFilterExtensions(new String[] {"*.jar;*.zip" });
                String str = dlg.open();
                if (str != null) {
                    isChangedDBConfig = true;
                    driverJarText.setText(str);
                }
            }
        });
        searchButton.setLayoutData(new GridData());

        // Schema
        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("editor.config.jdbc.schemaname"));
        schemaCombo = new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        schemaCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        schemaCombo.addListener(SWT.DROP_DOWN, new Listener() {
            public void handleEvent(Event e) {
                if (schemaCombo.getItemCount() == 1 || isChangedDBConfig) {
                    setSchema();
                } else {
                    Map<Object, Object> result =
                        DatabaseUtil.checkConnection("", getDriverJar(),
                            getDriverClassName(), DatabaseUtil.getDbUrl(
                                getDatabaseType(), getDatabaseName(),
                                getServer(), getPort()), getUseName(),
                            getPassword());
                    boolean checkResult =
                        (Boolean) result.get(DatabaseUtil.DB_CON_CHK_KEY);

                    if (!checkResult) {
                        isChangedDBConfig = true;
                        setDefaultSchema();
                        schemaException.setText(MessageUtil
                            .getMessage("editor.config.jdbc.checkjdbc")
                            + " "
                            + (String) result.get(DatabaseUtil.DB_CON_MSG_KEY));
                    }
                }
            }
        });

        // empty label
        new Label(group, SWT.NONE);

        schemaException = new Label(group, SWT.NONE);
        schemaException.setLayoutData(new GridData(GridData.FILL,
            GridData.FILL_VERTICAL, true, false, 3, 1));
        schemaException.setForeground(new Color(group.getDisplay(), 255, 0, 0));
    }

    private void createDriverArtifactGroup(Composite parent) {
        // Set jdbc driver artifact
        Group group = new Group(parent, SWT.SHADOW_NONE);
        group.setText(MessageUtil.getMessage("wizard.jdbcdriver.label"));
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("wizard.maven.groupid"));
        driverGroupIdText = new Text(group, SWT.BORDER);

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 40;
        driverGroupIdText.setLayoutData(gridData);
        driverGroupIdText.setText(MessageUtil
            .getMessage("wizard.maven.groupid.default"));

        driverGroupIdText.addListener(SWT.Modify, textModifyListener);

        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("wizard.maven.artifactid"));
        driverArtifactIdText = new Text(group, SWT.BORDER);
        driverArtifactIdText.setLayoutData(gridData);
        driverArtifactIdText.addListener(SWT.Modify, textModifyListener);

        new Label(group, SWT.NONE).setText(MessageUtil
            .getMessage("wizard.maven.version"));
        driverVersionText = new Text(group, SWT.BORDER);
        driverVersionText.setLayoutData(gridData);
        driverVersionText.setText(MessageUtil
            .getMessage("wizard.maven.version.default"));
        driverVersionText.addListener(SWT.Modify, textModifyListener);
    }

    private void getJdbcTypes() {
        File jdbcConfigFile =
            new File(anyframeHome + ProjectUtil.SLASH + "ide"
                + ProjectUtil.SLASH + "db" + ProjectUtil.SLASH + "jdbc.config");
        if (jdbcConfigFile.exists()) {
            try {
                jdbcTypes =
                    (java.util.List<JdbcType>) XmlFileUtil
                        .getObjectFromInputStream(new FileInputStream(
                            jdbcConfigFile));
            } catch (FileNotFoundException e) {
                ExceptionUtil.showException(MessageUtil
                    .getMessage("editor.exception.loadjdbcconfig"),
                    IStatus.ERROR, e);
            }
        } else {
            jdbcTypes =
                (java.util.List<JdbcType>) XmlFileUtil
                    .getObjectFromInputStream(this.getClass().getClassLoader()
                        .getResourceAsStream("jdbc.config"));
        }
    }

    private void loadConfiguration() {
        try {
            // get jdbc types
            getJdbcTypes();

            // clear previous data
            databaseTypeCombo.removeAll();
            schemaCombo.removeAll();
            dialectCombo.removeAll();

            for (JdbcType jdbcType : jdbcTypes)
                databaseTypeCombo.add(jdbcType.getType());

            databaseTypeCombo.setText("hsqldb");
            databaseNameText.setText("sampledb");
            useNameText.setText("SA");
            passwordText.setText("");
            serverText.setText("localhost");
            setPort(databaseTypeCombo.getSelectionIndex());
            setDialect(databaseTypeCombo.getSelectionIndex());
            setDriverClassName(databaseTypeCombo.getSelectionIndex());

            if (((NewApplicationWizardPage) getPreviousPage()).isAntProject()) {
                setAnyframeHomeLocation(((NewApplicationWizardPage) getPreviousPage())
                    .getAnyframeHome());
                setDriverJarPath(databaseTypeCombo.getSelectionIndex());
            } else {
                setUserHomeDriverJarPath(databaseTypeCombo.getSelectionIndex());
                setDriverPom(databaseTypeCombo.getSelectionIndex());
            }

            setDefaultSchema();
        } catch (Exception e) {
            ExceptionUtil.showException(MessageUtil
                .getMessage("editor.exception.loadjdbcconfig"), IStatus.ERROR,
                e);
        }
    }

    private SelectionListener databaseTypeListener = new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e) {
            isChangedDBConfig = true;
            dialectCombo.removeAll();
            setDefaultSchema();
            setDialect(databaseTypeCombo.getSelectionIndex());
            setPort(databaseTypeCombo.getSelectionIndex());
            if (((NewApplicationWizardPage) getPreviousPage()).isAntProject()) {
                setDriverJarPath(databaseTypeCombo.getSelectionIndex());
            } else {
                setDriverPom(databaseTypeCombo.getSelectionIndex());
                setUserHomeDriverJarPath(databaseTypeCombo.getSelectionIndex());
            }
            setDriverClassName(databaseTypeCombo.getSelectionIndex());
        }

        public void widgetSelected(SelectionEvent e) {
            widgetDefaultSelected(e);
        }
    };

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

    private void setDriverClassName(int index) {
        driverClassNameText.setText(jdbcTypes.get(index).getDriver());
    }

    private void setDriverPom(int index) {
        driverGroupIdText.setText(jdbcTypes.get(index).getDriverGroupId());
        driverArtifactIdText
            .setText(jdbcTypes.get(index).getDriverArtifactId());
        driverVersionText.setText(jdbcTypes.get(index).getDriverVersion());
    }

    private void setDriverJarPath(int index) {
        // hsqldb
        if (index == 1) {
            String jdbcDriverJarPath =
                this.anyframeHomeLocation + ProjectUtil.SLASH + "ide"
                    + ProjectUtil.SLASH + "db" + ProjectUtil.SLASH + "lib"
                    + ProjectUtil.SLASH + "hsqldb-2.0.0.jar";
            driverJarText.setText(jdbcDriverJarPath);
        } else {
            driverJarText.setText("");
        }
    }

    public void setAnyframeHomeLocation(String anyframeHomeLocation) {
        this.anyframeHomeLocation = anyframeHomeLocation;
    }

    private void setUserHomeDriverJarPath(int index) {
        // hsqldb
        if (index == 1) {
            PluginContainer container = null;
            try {
                container = new PluginContainer(null);
            } catch (Exception e) {
                ExceptionUtil.showException(e.getMessage(), IStatus.ERROR, e);
            }
            ArchetypeGenerationRequest request = container.getRequest();
            String jdbcDriverJarPath =
                request.getLocalRepository().getBasedir() + ProjectUtil.SLASH
                    + "hsqldb" + ProjectUtil.SLASH + "hsqldb"
                    + ProjectUtil.SLASH + "2.0.0" + ProjectUtil.SLASH
                    + "hsqldb-2.0.0.jar";
            driverJarText.setText(jdbcDriverJarPath);
        } else {
            driverJarText.setText("");
        }
    }

    private void setDefaultSchema() {
        schemaCombo.removeAll();
        if (isChangedDBConfig)
            schemaCombo.add(MessageUtil
                .getMessage("editor.config.jdbc.defaultschema"));
        else
            schemaCombo.add("PUBLIC");

        schemaCombo.select(0);
        schemaException.setText("");
    }

    public boolean setSchema() {
        schemaCombo.removeAll();
        schemaCombo.add(MessageUtil
            .getMessage("editor.config.jdbc.defaultschema"));

        Map<Object, Object> result =
            DatabaseUtil.checkConnection("", getDriverJar(), getDriverClassName(),
                DatabaseUtil.getDbUrl(getDatabaseType(), getDatabaseName(),
                    getServer(), getPort()), getUseName(), getPassword());
        boolean checkResult = (Boolean) result.get(DatabaseUtil.DB_CON_CHK_KEY);

        if (checkResult) {
            if (getDatabaseType().equals("sybase")) {
                schemaCombo.removeAll();
                schemaCombo.add("No Schema");
            } else {
                String[] schema;
                try {
                    schema =
                        DatabaseUtil.getSchemas("", getDriverJar(),
                            getDriverClassName(), DatabaseUtil.getDbUrl(
                                getDatabaseType(), getDatabaseName(),
                                getServer(), getPort()), getUseName(),
                            getPassword());
                    if (schema.length == 0) {
                        schemaCombo.removeAll();
                        schemaCombo.add("No Schema");
                    }
                    for (int i = 0; i < schema.length; i++) {
                        schemaCombo.add(schema[i]);
                    }
                } catch (Exception e) {
                    ExceptionUtil.showException(MessageUtil
                        .getMessage("editor.exception.getschema"),
                        IStatus.ERROR, e);
                    schemaException.setText(MessageUtil
                        .getMessage("editor.config.jdbc.checkjdbc")
                        + " "
                        + (String) result.get(DatabaseUtil.DB_CON_MSG_KEY));
                }
            }
            schemaException.setText("");
        } else {
            schemaException.setText(MessageUtil
                .getMessage("editor.config.jdbc.checkjdbc")
                + " " + (String) result.get(DatabaseUtil.DB_CON_MSG_KEY));
        }
        if (schemaCombo.getItemCount() == 1)
            schemaCombo.select(0);

        isChangedDBConfig = false;

        return checkResult;
    }

    @Override
    public boolean isPageComplete() {
        // Database Name, Server, Driver Class Name,
        // Driver Jar Path
        if (getDatabaseName() == null || getDatabaseName().length() == 0) {
            setErrorMessage(MessageUtil
                .getMessage("editor.dialog.validation.dbname"));
            return false;
        } else if (getServer() == null || getServer().length() == 0) {
            setErrorMessage(MessageUtil
                .getMessage("editor.dialog.validation.dbserver"));
            return false;
        } else if (getDriverClassName() == null
            || getDriverClassName().length() == 0) {
            setErrorMessage(MessageUtil
                .getMessage("editor.dialog.validation.dbclassname"));
            return false;
        } else if (getDriverJar() == null || getDriverJar().length() == 0) {
            setErrorMessage(MessageUtil
                .getMessage("editor.dialog.validation.dbjar"));
            return false;
        } else if (!ProjectUtil.existPath(getDriverJar())) {
            setErrorMessage(MessageUtil
                .getMessage("editor.dialog.validation.dbjar.valid"));
            return false;
        }

        if (!((NewApplicationWizardPage) getPreviousPage()).isAntProject()) {
            if (getDriverGroupId() == null || getDriverGroupId().length() == 0) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.error.pjtgroupid"));
                return false;
            } else if (!ProjectUtil.validateName(getDriverGroupId())) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.validation.pjtgroupid"));
                return false;
            }
            if (getDriverArtifactId() == null
                || getDriverArtifactId().length() == 0) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.error.pjtartifactid"));
                return false;
            } else if (!ProjectUtil.validateName(getDriverArtifactId())) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.validation.pjtartifactid"));
                return false;
            }
            if (getDriverVersion() == null || getDriverVersion().length() == 0) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.error.pjtversion"));
                return false;
            } else if (!ProjectUtil.validateName(getDriverVersion())) {
                setErrorMessage(MessageUtil
                    .getMessage("wizard.application.validation.pjtversion"));
                return false;
            }
        }

        setErrorMessage(null);
        return true;
    }

    public String getDatabaseType() {
        return databaseTypeCombo.getText();
    }

    public String getUseName() {
        return useNameText.getText();
    }

    public String getPassword() {
        return passwordText.getText();
    }

    public String getServer() {
        return serverText.getText();
    }

    public String getPort() {
        return portText.getText();
    }

    public String getDatabaseName() {
        return databaseNameText.getText();
    }

    public String getDriverClassName() {
        return driverClassNameText.getText();
    }

    public String getDriverJar() {
        return driverJarText.getText();
    }

    public String getSchema() {
        return schemaCombo.getText();
    }

    public void setSchema(String schema) {
        schemaCombo.setText(schema);
    }

    public String getDialect() {
        return dialectCombo.getText();
    }

    public String getDriverGroupId() {
        return driverGroupIdText.getText();
    }

    public String getDriverArtifactId() {
        return driverArtifactIdText.getText();
    }

    public String getDriverVersion() {
        return driverVersionText.getText();
    }
}
