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
package org.anyframe.ide.common.dialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.databases.DatabasesSettingUtil;
import org.anyframe.ide.common.databases.JdbcOption;
import org.anyframe.ide.common.messages.Message;
import org.anyframe.ide.common.properties.DataBasesPropertyPage;
import org.anyframe.ide.common.util.ButtonUtil;
import org.anyframe.ide.common.util.ConnectionUtil;
import org.anyframe.ide.common.util.ImageUtil;
import org.anyframe.ide.common.util.MessageUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.StringUtil;
import org.anyframe.ide.common.util.XMLUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * This is DbSettingDialog class.
 * 
 * @author Sujeong Lee
 */
public class DbSettingDialog extends Dialog {

	private Combo databaseTypeCombo;
	private Text dataSourceText;
	private Text driverText;
	private Text driverClassNameText;
	private Text urlText;
	private Text userIdText;
	private Text passwordText;
	private Text schemaText;
	private Label schemaException;
	private IProject project;

	private JdbcOption jdbc;

	private List<JdbcType> jdbcTypes;
	private IDBSettingDialog dbSettingDialogInstance;
	private boolean isChangedDBConfig = false;

	private static final String STR_KEY_VALID = "VALID";
	private static final String STR_KEY_MSG = "MSG";

	private String localRepositoryPath = "";

	public DbSettingDialog(Shell parentShell, IProject project, JdbcOption jdbc) {
		super(parentShell);
		this.project = project;
		this.jdbc = jdbc;

		this.dbSettingDialogInstance = CommonActivator.getInstance()
				.getDBSettingDialog();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		File jdbcConfigFile = new File(project.getLocation()
				+ Constants.FILE_SEPERATOR + Constants.SETTING_HOME
				+ Constants.FILE_SEPERATOR + Constants.DRIVER_SETTING_XML_FILE);
		if (jdbcConfigFile.exists()) {
			try {
				jdbcTypes = (java.util.List<JdbcType>) XMLUtil
						.getObjectFromInputStream(new FileInputStream(
								jdbcConfigFile));
			} catch (FileNotFoundException e) {
				PluginLoggerUtil.error(CommonActivator.PLUGIN_ID,
						"Fail to find jdbc configuration file.", e);
			}
		} else {
			PluginLoggerUtil.warning(CommonActivator.PLUGIN_ID,
					Message.exception_load_jdbcconfig);

			jdbcTypes = (java.util.List<JdbcType>) XMLUtil
					.getObjectFromInputStream(this
							.getClass()
							.getClassLoader()
							.getResourceAsStream(
									Constants.DRIVER_SETTING_XML_FILE));
		}

		if (dbSettingDialogInstance != null) {
			dbSettingDialogInstance.init(project, jdbcTypes);
		}

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		createDBConnectionComposite(dialogArea);
		createDDBonnectionExtraComposite(dialogArea);
		loadConfiguration();
		fillJdbcOptionComposite();
		return dialogArea;
	}

	private void loadConfiguration() {
		init();
		for (JdbcType jdbcType : jdbcTypes)
			databaseTypeCombo.add(jdbcType.getType());
	}

	private void fillJdbcOptionComposite() {
		if (jdbc != null) {
			String schema = jdbc.getSchema() == null
					|| jdbc.getSchema().equals("") ? Constants.DB_NO_SCHEMA
					: jdbc.getSchema();

			databaseTypeCombo.setText(StringUtil.null2str(jdbc.getDbType()));
			dataSourceText.setText(StringUtil.null2str(jdbc.getDbName()));
			driverText.setText(StringUtil.null2str(jdbc.getDriverJar()));
			driverClassNameText.setText(StringUtil.null2str(jdbc
					.getDriverClassName()));
			urlText.setText(StringUtil.null2str(jdbc.getUrl()));
			userIdText.setText(StringUtil.null2str(jdbc.getUserName()));
			passwordText.setText(StringUtil.null2str(jdbc.getPassword()));
			schemaText.setText(StringUtil.null2str(schema));

			if (dbSettingDialogInstance != null) {
				JdbcType type = new JdbcType();
				String[] dialect = { jdbc.getDialect() };
				type.setType(StringUtil.null2str(jdbc.getDbType()));
				type.setDialect(dialect);
				type.setDriverGroupId(jdbc.getMvnGroupId() == null ? "" : jdbc
						.getMvnGroupId());
				type.setDriverArtifactId(jdbc.getMvnArtifactId() == null ? ""
						: jdbc.getMvnArtifactId());
				type.setDriverVersion(jdbc.getMvnVersion() == null ? "" : jdbc
						.getMvnVersion());
				dbSettingDialogInstance.loadSettings(type);
			}
		}
	}

	private void createDDBonnectionExtraComposite(Composite dialogArea) {
		if (dbSettingDialogInstance == null) {
			return;
		}
		dbSettingDialogInstance.createUI(dialogArea);
	}

	protected void isSuccessConnectionTest() {
		Map<String, String> result = runToConnectionTest();
		if (result.get(STR_KEY_VALID).equalsIgnoreCase(Boolean.TRUE.toString())) {
			MessageUtil.showMessage(Message.properties_connection_success,
					DataBasesPropertyPage.PROGRAM_NAME);
		} else {
			String append = "";
			if (!result.get(STR_KEY_MSG).equals("")) {
				append = "\n" + result.get(STR_KEY_MSG);
			}
			MessageUtil.showMessage(Message.properties_connection_fail + append,
					DataBasesPropertyPage.PROGRAM_NAME);
		}
	}

	protected Map<String, String> runToConnectionTest() {
		Map<String, String> result = new HashMap<String, String>();
		Connection conn = null;
		String validResult = "";
		try {
			validResult = validateInputedJdbcOption();
			if ("".equals(validResult)) {
				JdbcOption jdbcOptionForTest = new JdbcOption();
				fillJdbcOption(jdbcOptionForTest);
				conn = getConnection(jdbcOptionForTest);
			} else {
				result.put(STR_KEY_VALID, Boolean.FALSE.toString());
				result.put(STR_KEY_MSG, validResult);
				return result;
			}
		} catch (Exception e) {
			PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, validResult);
		} finally {
			if (conn != null) {
				try {
					conn.close();
					result.put(STR_KEY_VALID, Boolean.TRUE.toString());
					result.put(STR_KEY_MSG, "");
					return result;
				} catch (SQLException e) {
					result.put(STR_KEY_VALID, Boolean.FALSE.toString());
					result.put(STR_KEY_MSG, e.getMessage());
					return result;
				}
			}
		}
		result.put(STR_KEY_VALID, Boolean.FALSE.toString());
		result.put(STR_KEY_MSG, "");
		return result;
	}

	private Connection getConnection(JdbcOption jdbcOption) throws Exception {
		Properties connectionProps = new Properties();
		connectionProps.put("user", jdbcOption.getUserName());
		connectionProps.put("password", jdbcOption.getPassword());

		try {
			Driver driver = ConnectionUtil.getDriverFromPath(
					jdbcOption.getDriverJar(), jdbcOption.getDriverClassName());
			return driver.connect(jdbcOption.getUrl(), connectionProps);
		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID,
					Message.exception_getconnection, e);
			throw e;
		}
	}

	private void createDBConnectionComposite(final Composite parent) {
		Composite tableComposite = new Composite(parent, SWT.NULL);

		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 1;
		layout.marginWidth = 1;
		tableComposite.setLayout(layout);

		final Group connectionInfoGroup = new Group(tableComposite, SWT.NULL);
		connectionInfoGroup.setText(Message.properties_database_config_description);

		GridLayout connectionInfoLayout = new GridLayout(3, false);
		connectionInfoLayout.marginHeight = 1;
		connectionInfoLayout.marginWidth = 1;
		connectionInfoGroup.setLayout(connectionInfoLayout);

		GridData connectionInfoData = new GridData(SWT.NULL);
		connectionInfoData.widthHint = 530;
		connectionInfoGroup.setLayoutData(connectionInfoData);

		Label databaseTypeLabel = new Label(connectionInfoGroup, SWT.NULL);
		databaseTypeLabel.setText(Message.wizard_jdbc_typename);
		databaseTypeCombo = new Combo(connectionInfoGroup, SWT.SINGLE
				| SWT.BORDER | SWT.READ_ONLY);
		GridData databaseTypeCombotData = new GridData(SWT.NULL);
		databaseTypeCombotData.widthHint = 280;
		databaseTypeCombotData.horizontalSpan = 2;
		databaseTypeCombo.setLayoutData(databaseTypeCombotData);
		databaseTypeCombo.addSelectionListener(databaseTypeListener);

		Label dataSourceLabel = new Label(connectionInfoGroup, SWT.NULL);
		dataSourceLabel.setText(Message.properties_database_profile);
		dataSourceText = new Text(connectionInfoGroup, SWT.LEFT | SWT.BORDER);
		dataSourceText.setText("");
		GridData dataSourceTextData = new GridData(SWT.NULL);
		dataSourceTextData.widthHint = 300;
		dataSourceTextData.horizontalSpan = 2;
		dataSourceText.setLayoutData(dataSourceTextData);

		Label driverLabel = new Label(connectionInfoGroup, SWT.NULL);
		driverLabel.setText(Message.wizard_jdbc_driverjar);
		driverText = new Text(connectionInfoGroup, SWT.LEFT | SWT.BORDER);
		driverText.setText("");
		GridData driverTextData = new GridData(SWT.NULL);
		driverTextData.widthHint = 300;
		driverText.setLayoutData(driverTextData);

		driverText.setLayoutData(driverTextData);
		Button driverSearchButton = ButtonUtil.createButton(
				connectionInfoGroup, Message.ide_button_browse, "");
		driverSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				isChangedDBConfig = true;
				FileDialog dlg = new FileDialog(parent.getShell(), SWT.OPEN);
				dlg.setFilterExtensions(new String[] { "*.jar;*.zip" });
				String str = dlg.open();
				if (str != null) {
					driverText.setText(str);

					if (StringUtil.isContainKorean(str)) {
						schemaException
								.setText(Message.properties_validation_dbjar_valid
										+ " : Korean");
					}
				}
			}
		});

		Label driverClassNameLabel = new Label(connectionInfoGroup, SWT.NULL);
		driverClassNameLabel.setText(Message.wizard_jdbc_driverclass);
		driverClassNameText = new Text(connectionInfoGroup, SWT.LEFT
				| SWT.BORDER);
		driverClassNameText.setText("");
		GridData driverClassNameTextData = new GridData(SWT.NULL);
		driverClassNameTextData.widthHint = 300;
		driverClassNameTextData.horizontalSpan = 2;
		driverClassNameText.setLayoutData(driverClassNameTextData);

		Label urlLabel = new Label(connectionInfoGroup, SWT.NULL);
		urlLabel.setText(Message.wizard_jdbc_url);
		urlText = new Text(connectionInfoGroup, SWT.LEFT | SWT.BORDER);
		urlText.setText("");
		GridData urlTextData = new GridData(SWT.NULL);
		urlTextData.widthHint = 300;
		urlTextData.horizontalSpan = 2;
		urlText.setLayoutData(urlTextData);

		Label userIdLabel = new Label(connectionInfoGroup, SWT.NULL);
		userIdLabel.setText(Message.wizard_jdbc_username);
		userIdText = new Text(connectionInfoGroup, SWT.LEFT | SWT.BORDER);
		userIdText.setText("");
		GridData userIdTextData = new GridData(SWT.NULL);
		userIdTextData.widthHint = 300;
		userIdTextData.horizontalSpan = 2;
		userIdText.setLayoutData(userIdTextData);

		Label passwordLabel = new Label(connectionInfoGroup, SWT.NULL);
		passwordLabel.setText(Message.wizard_jdbc_password);
		passwordText = new Text(connectionInfoGroup, SWT.LEFT | SWT.BORDER
				| SWT.PASSWORD);
		passwordText.setText("");
		GridData passwordTextData = new GridData(SWT.NULL);
		passwordTextData.widthHint = 300;
		passwordTextData.horizontalSpan = 2;
		passwordText.setLayoutData(passwordTextData);

		Label schemaLabel = new Label(connectionInfoGroup, SWT.NULL);
		schemaLabel.setText(Message.wizard_jdbc_schemaname);
		schemaText = new Text(connectionInfoGroup, SWT.LEFT | SWT.BORDER);
		schemaText.setText(Message.properties_jdbc_defaultschema);
		GridData schemaPatternTextData = new GridData(SWT.NULL);
		schemaPatternTextData.widthHint = 300;
		schemaText.setLayoutData(schemaPatternTextData);
		schemaText.setEnabled(false);

		Button schemaButton = ButtonUtil.createButton(connectionInfoGroup,
				Message.ide_button_browse, "");
		schemaButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Map<Object, Object> result = checkConnection("", driverText.getText(),
								driverClassNameText.getText(),
								urlText.getText(), userIdText.getText(),
								passwordText.getText());
				boolean checkResult = (Boolean) result
						.get(Constants.DB_CON_CHK_KEY);

				if (!checkResult) {
					isChangedDBConfig = true;
					setDefaultSchema();
					settingConnectionResultErrorMessage(Message.wizard_jdbc_checkjdbc + " "
							+ (String) result.get(Constants.DB_CON_MSG_KEY));
				} else {
					schemaException.setText("");
					Set<String> schemaSet = new TreeSet<String>();

					schemaSet = getSchemaSet();

					ElementListSelectionDialog dialog = new ElementListSelectionDialog(
							connectionInfoGroup.getShell(),
							new SchemaLabelProvider());
					dialog.setElements(schemaSet.toArray());
					dialog.setTitle("Schema Selection");
					dialog.open();

					Object selectedResult = dialog.getFirstResult();
					if (selectedResult != null) {
						schemaText.setText(selectedResult.toString());
					}
				}
			}
		});

		schemaException = new Label(connectionInfoGroup, SWT.NONE);
		schemaException.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL_VERTICAL, true, false, 3, 1));
		schemaException.setForeground(new Color(connectionInfoGroup
				.getDisplay(), 255, 0, 0));
	}

	protected void fillJdbcOption(JdbcOption jdbcOptionToFill) {
		jdbcOptionToFill.setDbType(databaseTypeCombo.getText());
		jdbcOptionToFill.setDbName(dataSourceText.getText());
		jdbcOptionToFill.setDriverJar(driverText.getText());
		jdbcOptionToFill.setDriverClassName(driverClassNameText.getText());
		jdbcOptionToFill.setUrl(urlText.getText());
		jdbcOptionToFill.setUserName(userIdText.getText());
		jdbcOptionToFill.setPassword(passwordText.getText());
		jdbcOptionToFill.setSchema(schemaText.getText());

		jdbcOptionToFill.setUseDbSpecific(false);
		jdbcOptionToFill.setRunExplainPaln(false);

		if (dbSettingDialogInstance != null) {
			Map<String, String> mvnInputs = dbSettingDialogInstance
					.getInputData();
			jdbcOptionToFill.setDialect(mvnInputs
					.get(Constants.XML_TAG_DIALECT));
			jdbcOptionToFill.setMvnGroupId(mvnInputs
					.get(Constants.XML_TAG_DRIVER_GROUPID));
			jdbcOptionToFill.setMvnArtifactId(mvnInputs
					.get(Constants.XML_TAG_DRIVER_ARTIFACTID));
			jdbcOptionToFill.setMvnVersion(mvnInputs
					.get(Constants.XML_TAG_DRIVER_VERSION));
		}
	}

	private String validateInputedJdbcOption() {
		String msg = "";
		if (StringUtil.isEmptyOrNull(dataSourceText.getText())) {
			if (!"".equals(msg))
				msg += ", ";
			msg += Message.properties_database_profile;
		}

		if (StringUtil.isEmptyOrNull(driverText.getText())) {
			if (!"".equals(msg))
				msg += ", ";
			msg += Message.wizard_jdbc_driverjar;
		}

		if (StringUtil.isContainKorean(driverText.getText())) {
			if (!"".equals(msg))
				msg += ", ";
			msg += Message.wizard_jdbc_driverclass;
		}

		if (StringUtil.isEmptyOrNull(driverClassNameText.getText())) {
			if (!"".equals(msg))
				msg += ", ";
			msg += Message.wizard_jdbc_driverclass;
		}
		if (StringUtil.isEmptyOrNull(urlText.getText())) {
			if (!"".equals(msg))
				msg += ", ";
			msg += Message.wizard_jdbc_url;
		}

		if (StringUtil.isEmptyOrNull(userIdText.getText())) {
			if (!"".equals(msg))
				msg += ", ";
			msg += Message.wizard_jdbc_username;
		}

		if (!databaseTypeCombo.getText().equals("sybase")) {
			if (StringUtil.isEmptyOrNull(schemaText.getText())
					|| schemaText.getText().equals(
							Message.wizard_jdbc_defaultschema)) {
				if (!"".equals(msg))
					msg += ", ";
				msg += Message.wizard_jdbc_defaultschema;
			}
		}

		if (!"".equals(msg)) {
			return "Please fill missing items [" + msg + "]";
		}
		return "";
	}

	public JdbcOption getJdbcOption() {
		return jdbc;
	}

	private final SelectionListener databaseTypeListener = new SelectionListener() {

		public void widgetDefaultSelected(SelectionEvent e) {
			isChangedDBConfig = true;

			if (dbSettingDialogInstance != null) {
				dbSettingDialogInstance.setDatabaseTypeSelectListener(e);
				localRepositoryPath = dbSettingDialogInstance
						.getLocalRepositoryPath();
			}

			setDefaultSettingByType(databaseTypeCombo.getSelectionIndex());
		}

		private void setDefaultSettingByType(int index) {

			String url = "";
			if (jdbcTypes.get(index).getType().equalsIgnoreCase("hsqldb")) {
				url = Constants.DB_HSQL_SERVER_URL;
			} else if (jdbcTypes.get(index).getType()
					.equalsIgnoreCase("oracle")) {
				url = Constants.DB_ORACLE_URL;
			} else if (jdbcTypes.get(index).getType().equalsIgnoreCase("mysql")) {
				url = Constants.DB_MYSQL_URL;
			} else if (jdbcTypes.get(index).getType()
					.equalsIgnoreCase("sybase")) {
				url = Constants.DB_SYBASE_URL;
			} else if (jdbcTypes.get(index).getType().equalsIgnoreCase("db2")) {
				url = Constants.DB_DB2_URL;
			} else if (jdbcTypes.get(index).getType().equalsIgnoreCase("mssql")) {
				url = Constants.DB_MSSQL_URL;
			}

			String driverClassName = jdbcTypes.get(index).getDriver();
			if (driverClassName == null) {
				driverClassName = "";
			}

			urlText.setText(url);
			driverClassNameText.setText(driverClassName);
			dataSourceText.setText("");
			userIdText.setText("");
			passwordText.setText("");
			schemaText.setText("");
			driverText.setText("");

			if (dbSettingDialogInstance != null) {
				Map<String, String> mvnInputs = dbSettingDialogInstance
						.getInputData();

				if (jdbcTypes.get(index).getType()
						.equals(Constants.XML_DRIVER_CONFIG_OTHERS)) {
					driverText.setText("");
				} else {
					driverText.setText(localRepositoryPath
							+ Constants.FILE_SEPERATOR
							+ mvnInputs.get(Constants.XML_TAG_DRIVER_GROUPID)
									.replace(".", Constants.FILE_SEPERATOR)
							+ Constants.FILE_SEPERATOR
							+ mvnInputs
									.get(Constants.XML_TAG_DRIVER_ARTIFACTID)
							+ Constants.FILE_SEPERATOR
							+ mvnInputs.get(Constants.XML_TAG_DRIVER_VERSION)
							+ Constants.FILE_SEPERATOR
							+ mvnInputs
									.get(Constants.XML_TAG_DRIVER_ARTIFACTID)
							+ "-"
							+ mvnInputs.get(Constants.XML_TAG_DRIVER_VERSION)
							+ ".jar");
				}
			}
		}

		public void widgetSelected(SelectionEvent e) {
			widgetDefaultSelected(e);
		}
	};

	private void init() {
		databaseTypeCombo.removeAll();
		dataSourceText.setText("");
		driverText.setText("");
		driverClassNameText.setText("");
		urlText.setText("");
		userIdText.setText("");
		passwordText.setText("");
		schemaText.setText("");
	}

	private void setDefaultSchema() {
		schemaText.setText("");
		if (isChangedDBConfig)
			schemaText.setText(Message.properties_jdbc_defaultschema);
		else
			schemaText.setText("PUBLIC");
		schemaException.setText("");
	}

	private Set<String> getSchemaSet() {
		String[] schema;

		Set<String> result = new HashSet<String>();

		try {
			schema = DatabasesSettingUtil.getSchemas("", driverText.getText(),
					driverClassNameText.getText(), urlText.getText(),
					userIdText.getText(), passwordText.getText());
			if (schema.length == 0) {
				result.add(Constants.DB_NO_SCHEMA);
			}
			for (int i = 0; i < schema.length; i++) {
				result.add(schema[i]);
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID,
					Message.exception_getschema, e);
		}

		return result;
	}

	@Override
	protected void okPressed() {
		if (!validateInputedJdbcOption().equals("")) {
			return;
		}
		if (jdbc == null) {
			jdbc = new JdbcOption();
		}
		fillJdbcOption(jdbc);
		if (jdbc.toString().contains("<") || jdbc.toString().contains(">")) {
			MessageUtil.showMessage(
					"Database information can't contains \"<\" or \">\".",
					DataBasesPropertyPage.PROGRAM_NAME);
			return;
		}
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Message.properties_database_config_description);
	}

	class SchemaLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			return ImageUtil.getImageDescriptor(CommonActivator.PLUGIN_ID,
					Message.image_schema).createImage();
		}

		public String getText(Object element) {
			return (String) element;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}

	private Map<Object, Object> checkConnection(String projectHome,
			String driverJarName, String dbDriver, String dbUrl,
			String userName, String password) {
		Map<Object, Object> result = new HashMap<Object, Object>();
		result.put(Constants.DB_CON_CHK_KEY, Constants.DB_CON_CHK);
		result.put(Constants.DB_CON_MSG_KEY, Constants.DB_CON_MSG);
		Connection connection = null;
		try {
			connection = DatabasesSettingUtil.getConnection(projectHome, driverJarName, dbDriver,
					dbUrl, userName, password);
		} catch (Exception e) {
			result.put(Constants.DB_CON_MSG_KEY, e.getMessage());
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID,
					Message.exception_getconnection, e);
		} finally {
			if (connection != null){
				result.put(Constants.DB_CON_CHK_KEY, true);
				DatabasesSettingUtil.close(connection);
			}else{
				settingConnectionResultErrorMessage(Message.exception_getconnection);
			}
		}
		return result;
	}
	
	private void settingConnectionResultErrorMessage(String resultMessage){
		schemaException.setText(resultMessage);
	}
}
