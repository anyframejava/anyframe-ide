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
package org.anyframe.ide.codegenerator.wizards;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.DatabaseUtil;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.codegenerator.util.XmlFileUtil;
import org.anyframe.ide.command.maven.mojo.container.PluginContainer;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.dialog.JdbcType;
import org.anyframe.ide.common.util.ImageUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.StringUtil;
import org.anyframe.ide.common.util.XMLUtil;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
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
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * This is an NewApplicationWizardJDBCPage class.
 * 
 * @author Sooyeon Park
 * @author Eunjin Jang
 */
public class NewApplicationWizardJDBCPage extends WizardPage {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;
	private Combo databaseTypeCombo;
	private Text userNameText;
	private Text passwordText;
	private Text databaseUrlText;
	private Text driverClassNameText;
	private Text driverJarText;

	private Text schemaText;
	private Button schemaButton;

	private Combo dialectCombo;
	private java.util.List<JdbcType> jdbcTypes;
	private String anyframeHomeLocation;
	private String anyframeHome;
	private Label schemaException;
	private boolean isChangedDBConfig = false;

	private Text driverGroupIdText;
	private Text driverArtifactIdText;
	private Text driverVersionText;

	private String[] dbUrls = {
			"jdbc:oracle:thin:@<server>:<port>:<database_name>",
			"jdbc:hsqldb:hsql://<server>/<database_name>",
			"jdbc:mysql://<server>:<port>/<database_name>",
			"jdbc:sybase:Tds:<server>:<port>?ServiceName=<database_name>",
			"jdbc:db2://<server>:<port>/<database_name>",
			"jdbc:sqlserver://<server>:<port>;DatabaseName=<database_name>", "" };

	public void setAnyframeHome(String anyframeHome) {
		this.anyframeHome = anyframeHome;
		// get jdbc types
		getJdbcTypes();
	}

	public String getAnyframeHome() {
		return anyframeHome;
	}
	
	protected NewApplicationWizardJDBCPage(String pageName) {
		super(pageName);
		this.setTitle(Message.wizard_jdbc_title);
		this.setDescription(Message.wizard_jdbc_description);
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
		final Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(Message.wizard_jdbc_label);
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Database Type selection
		new Label(group, SWT.NONE).setText(Message.wizard_jdbc_typename);

		databaseTypeCombo = new Combo(group, SWT.SINGLE | SWT.BORDER
				| SWT.READ_ONLY);
		databaseTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		databaseTypeCombo.addSelectionListener(databaseTypeListener);

		// empty label
		new Label(group, SWT.NONE);

		// Database URL
		new Label(group, SWT.NONE).setText(Message.wizard_jdbc_url);
		databaseUrlText = new Text(group, SWT.BORDER);
		databaseUrlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		databaseUrlText.addListener(SWT.Modify, textModifyListener);
		databaseUrlText.addListener(SWT.KeyDown, databaseChangeListener);

		// empty label
		new Label(group, SWT.NONE);

		// User Name
		new Label(group, SWT.NONE).setText(Message.wizard_jdbc_username);
		userNameText = new Text(group, SWT.BORDER);
		userNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		userNameText.addListener(SWT.KeyDown, databaseChangeListener);

		// empty label
		new Label(group, SWT.NONE);

		// PASSWORD
		new Label(group, SWT.NONE).setText(Message.wizard_jdbc_password);
		passwordText = new Text(group, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		passwordText.addListener(SWT.KeyDown, databaseChangeListener);

		// empty label
		new Label(group, SWT.NONE);

		// Dialect
		new Label(group, SWT.NONE).setText(Message.wizard_jdbc_dialect);
		dialectCombo = new Combo(group, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		dialectCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dialectCombo.addListener(SWT.Selection, databaseChangeListener);

		// empty label
		new Label(group, SWT.NONE);

		// Driver Class
		new Label(group, SWT.NONE).setText(Message.wizard_jdbc_driverclass);
		driverClassNameText = new Text(group, SWT.BORDER);
		driverClassNameText
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		driverClassNameText.addListener(SWT.Modify, textModifyListener);
		driverClassNameText.addListener(SWT.KeyDown, databaseChangeListener);

		// empty label
		new Label(group, SWT.NONE);

		// Driver Jar Path
		new Label(group, SWT.NONE).setText(Message.wizard_jdbc_driverjar);
		driverJarText = new Text(group, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 10;
		driverJarText.setLayoutData(gridData);
		driverJarText.addListener(SWT.Modify, textModifyListener);
		driverJarText.addListener(SWT.KeyDown, databaseChangeListener);

		Button searchButton = new Button(group, SWT.NULL);
		searchButton.setText(Message.ide_button_browse);
		final Shell shell = parent.getShell();
		searchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				FileDialog dlg = new FileDialog(shell, SWT.OPEN);
				dlg.setFilterExtensions(new String[] { "*.jar;*.zip" });
				String str = dlg.open();
				if (str != null) {
					isChangedDBConfig = true;
					driverJarText.setText(str);
				}
			}
		});
		searchButton.setLayoutData(new GridData());

		// Schema
		new Label(group, SWT.NONE).setText(Message.wizard_jdbc_schemaname);

		schemaText = new Text(group, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 10;
		schemaText.setText(Message.wizard_jdbc_defaultschema);
		schemaText.setLayoutData(gridData);
		schemaText.setEnabled(false);

		schemaButton = new Button(group, SWT.NULL);
		schemaButton.setText(Message.ide_button_browse);
		schemaButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Map<Object, Object> result = DatabaseUtil.checkConnection("",
						getDriverJar(), getDriverClassName(), getDatabaseUrl(),
						getUseName(), getPassword());
				boolean checkResult = (Boolean) result
						.get(DatabaseUtil.DB_CON_CHK_KEY);

				if (!checkResult) {
					isChangedDBConfig = true;
					setDefaultSchema();
					schemaException.setText(Message.wizard_jdbc_checkjdbc + " "
							+ (String) result.get(DatabaseUtil.DB_CON_MSG_KEY));
				} else {
					schemaException.setText("");
					
					Set<String> schemaSet = new TreeSet<String>();

					schemaSet = getSchemaSet();

					ElementListSelectionDialog dialog = new ElementListSelectionDialog(
							group.getShell(), new SchemaLabelProvider());
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
		schemaButton.setLayoutData(new GridData());

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
		group.setText(Message.wizard_jdbcdriver_label);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(group, SWT.NONE).setText(Message.wizard_maven_groupid);
		driverGroupIdText = new Text(group, SWT.BORDER);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.minimumWidth = 40;
		driverGroupIdText.setLayoutData(gridData);
		driverGroupIdText.setText(Message.wizard_maven_groupid_default);

		driverGroupIdText.addListener(SWT.Modify, textModifyListener);

		new Label(group, SWT.NONE).setText(Message.wizard_maven_artifactid);
		driverArtifactIdText = new Text(group, SWT.BORDER);
		driverArtifactIdText.setLayoutData(gridData);
		driverArtifactIdText.addListener(SWT.Modify, textModifyListener);

		new Label(group, SWT.NONE).setText(Message.wizard_maven_version);
		driverVersionText = new Text(group, SWT.BORDER);
		driverVersionText.setLayoutData(gridData);
		driverVersionText.setText(Message.wizard_maven_version_default);
		driverVersionText.addListener(SWT.Modify, textModifyListener);
	}

	private void getJdbcTypes() {

		try {
			jdbcTypes = (java.util.List<JdbcType>) XmlFileUtil
					.getObjectFromInputStream(XMLUtil
							.getJdbcConfigFileInputStream());
		} catch (IOException e) {
			PluginLoggerUtil.error(CodeGeneratorActivator.PLUGIN_ID,
					"Can't get jdbc configuration.", e);
		}
	}

	private void loadConfiguration() {
		try {
			// get jdbc types
			getJdbcTypes();

			// clear previous data
			databaseTypeCombo.removeAll();
			schemaText.setText("");
			dialectCombo.removeAll();

			for (JdbcType jdbcType : jdbcTypes)
				databaseTypeCombo.add(jdbcType.getType());

			databaseTypeCombo.setText("hsqldb");
			databaseUrlText.setText("jdbc:hsqldb:hsql://localhost/sampledb");
			userNameText.setText("SA");
			passwordText.setText("");
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
			PluginLoggerUtil
					.error(ID, Message.view_exception_loadjdbcconfig, e);
		}
	}

	private SelectionListener databaseTypeListener = new SelectionListener() {

		public void widgetDefaultSelected(SelectionEvent e) {
			isChangedDBConfig = true;
			dialectCombo.removeAll();

			setDefaultSchema();
			setDialect(databaseTypeCombo.getSelectionIndex());
			setDatabaseUrl(databaseTypeCombo.getSelectionIndex());
			if (((NewApplicationWizardPage) getPreviousPage()).isAntProject()) {
				setDriverJarPath(databaseTypeCombo.getSelectionIndex());
			} else {
				setDriverPom(databaseTypeCombo.getSelectionIndex());
				setUserHomeDriverJarPath(databaseTypeCombo.getSelectionIndex());
			}
			setDriverClassName(databaseTypeCombo.getSelectionIndex());
			userNameText.setText("");
			passwordText.setText("");
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

	private void setDatabaseUrl(int index) {
		databaseUrlText.setText(dbUrls[index]);
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
			String jdbcDriverJarPath = this.anyframeHomeLocation
					+ ProjectUtil.SLASH + "ide" + ProjectUtil.SLASH + "db"
					+ ProjectUtil.SLASH + "lib" + ProjectUtil.SLASH
					+ "hsqldb-2.0.0.jar";
			driverJarText.setText(jdbcDriverJarPath);
		} else {
			driverJarText.setText("");
		}
	}

	public void setAnyframeHomeLocation(String anyframeHomeLocation) {
		this.anyframeHomeLocation = anyframeHomeLocation;
	}

	private void setUserHomeDriverJarPath(int index) {
		PluginContainer container = null;
		try {
			container = new PluginContainer(null);
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, e.getMessage(), e);
		}
		ArchetypeGenerationRequest request = container.getRequest();
		String localRepositoryPath = request.getLocalRepository().getBasedir();

		// hsqldb
		if (index == 1) {
			String jdbcDriverJarPath = localRepositoryPath + ProjectUtil.SLASH
					+ "hsqldb" + ProjectUtil.SLASH + "hsqldb"
					+ ProjectUtil.SLASH + "2.0.0" + ProjectUtil.SLASH
					+ "hsqldb-2.0.0.jar";
			driverJarText.setText(jdbcDriverJarPath);
		} else if (index == 6) {
			driverJarText.setText("");
		} else {
			driverJarText.setText(localRepositoryPath
					+ Constants.FILE_SEPERATOR
					+ jdbcTypes.get(index).getDriverGroupId()
							.replace(".", Constants.FILE_SEPERATOR)
					+ Constants.FILE_SEPERATOR
					+ jdbcTypes.get(index).getDriverArtifactId()
					+ Constants.FILE_SEPERATOR
					+ jdbcTypes.get(index).getDriverVersion()
					+ Constants.FILE_SEPERATOR
					+ jdbcTypes.get(index).getDriverArtifactId() + "-"
					+ jdbcTypes.get(index).getDriverVersion() + ".jar");
		}
	}

	private void setDefaultSchema() {
		schemaText.setText("");
		if (isChangedDBConfig)
			schemaText.setText(Message.wizard_jdbc_defaultschema);
		else
			schemaText.setText("PUBLIC");
		schemaException.setText("");
	}

	private Set<String> getSchemaSet() {
		String[] schema;

		Set<String> result = new HashSet<String>();

		try {
			schema = DatabaseUtil.getSchemas("", getDriverJar(),
					getDriverClassName(), getDatabaseUrl(), getUseName(),
					getPassword());
			if (schema.length == 0) {
				result.add("No Schema");
			}
			for (int i = 0; i < schema.length; i++) {
				result.add(schema[i]);
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, Message.view_exception_getschema, e);
		}

		return result;
	}

	@Override
	public boolean isPageComplete() {
		// Database Name, Server, Driver Class Name,
		// Driver Jar Path
		if (getDatabaseUrl() == null || getDatabaseUrl().length() == 0) {
			setErrorMessage(Message.wizard_validation_dburl);
			return false;
		} else if (getDriverClassName() == null
				|| getDriverClassName().length() == 0) {
			setErrorMessage(Message.wizard_validation_dbclassname);
			return false;
		} else if (getDriverJar() == null || getDriverJar().length() == 0) {
			setErrorMessage(Message.wizard_validation_dbjar);
			return false;
		} else if (!ProjectUtil.existPath(getDriverJar())) {
			setErrorMessage(Message.wizard_validation_dbjar_valid);
			return false;
		} else if (StringUtil.isContainKorean(getDriverJar())) {
			setErrorMessage(Message.wizard_validation_dbjar_valid);
			return false;
		}

		if (!((NewApplicationWizardPage) getPreviousPage()).isAntProject()) {
			if (getDriverGroupId() == null || getDriverGroupId().length() == 0) {
				setErrorMessage(Message.wizard_application_error_pjtgroupid);
				return false;
			} else if (!ProjectUtil.validateName(getDriverGroupId())) {
				setErrorMessage(Message.wizard_application_validation_pjtgroupid);
				return false;
			}
			if (getDriverArtifactId() == null
					|| getDriverArtifactId().length() == 0) {
				setErrorMessage(Message.wizard_application_error_pjtartifactid);
				return false;
			} else if (!ProjectUtil.validateName(getDriverArtifactId())) {
				setErrorMessage(Message.wizard_application_validation_pjtartifactid);
				return false;
			}
			if (getDriverVersion() == null || getDriverVersion().length() == 0) {
				setErrorMessage(Message.wizard_application_error_pjtversion);
				return false;
			} else if (!ProjectUtil.validateName(getDriverVersion())) {
				setErrorMessage(Message.wizard_application_validation_pjtversion);
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
		return userNameText.getText();
	}

	public String getPassword() {
		return passwordText.getText();
	}

	public String getDatabaseUrl() {
		return databaseUrlText.getText();
	}

	public String getDriverClassName() {
		return driverClassNameText.getText();
	}

	public String getDriverJar() {
		return driverJarText.getText();
	}

	public String getSchema() {
		return schemaText.getText();
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

	class SchemaLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			return ImageUtil.getImageDescriptor(
					CodeGeneratorActivator.PLUGIN_ID, Message.image_schema)
					.createImage();
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
}
