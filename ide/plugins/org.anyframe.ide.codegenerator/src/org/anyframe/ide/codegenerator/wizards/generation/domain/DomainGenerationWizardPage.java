/* 
 * Copyright 2008-2013 the original author or authors. 
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
package org.anyframe.ide.codegenerator.wizards.generation.domain;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.model.tree.ITreeModel;
import org.anyframe.ide.codegenerator.model.tree.SimpleTreeNode;
import org.anyframe.ide.codegenerator.model.tree.TreeContentProvider;
import org.anyframe.ide.codegenerator.model.tree.TreeLabelProvider;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.common.databases.JdbcOption;
import org.anyframe.ide.common.util.ConfigXmlUtil;
import org.anyframe.ide.common.util.EDPUtil;
import org.anyframe.ide.common.util.ImageUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.ProjectConfig;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * This is an DomainGenerationWizardPage class.
 * 
 * @author Sujeong Lee
 */
public class DomainGenerationWizardPage extends WizardPage {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	private IProject project;
	private ProjectConfig projectConfig = null;

	private Text packageText;
	private Button refreshButton;
	private CheckboxTreeViewer tableTreeViewer;

	public void setVisible(boolean visible) {
		if (visible) {
			// updateControls();
		}
		super.setVisible(visible);
	}

	protected DomainGenerationWizardPage(IProject project, String pageName) {
		super(pageName);
		this.project = project;

		this.setTitle(pageName);
		this.setDescription(Message.wizard_generation_domain_description);
	}

	public void createControl(Composite parent) {
		init();

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createPackageFields(composite);
		createTableRefreshFields(composite);
		createTables(composite);

		setPageComplete(isPageComplete());
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	private void init() {
		try {
			String configFile = ConfigXmlUtil.getCommonConfigFile(project.getLocation().toOSString());
			projectConfig = ConfigXmlUtil.getProjectConfig(configFile);
		} catch (Exception e) {
			PluginLoggerUtil.warning(ID, Message.wizard_error_properties);
		}
	}
	
	private void createPackageFields(Composite parent) {
		final String basePackage = projectConfig.getPackageName();

		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(3, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(comp, SWT.LEFT).setText(Message.wizard_crud_gen_basepackage);

		packageText = new Text(comp, SWT.BORDER);
		packageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		DomainGenerationWizard wizard = (DomainGenerationWizard)getWizard();
		packageText.setText(wizard.getCurrentPackageName());
		packageText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
			}
		});

		GridData buttonGridData = new GridData(85, 25);
		Button packageEditButton = new Button(comp, SWT.PUSH);
		packageEditButton.setText(Message.ide_button_edit);
		packageEditButton.setLayoutData(buttonGridData);
		packageEditButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionevent) {

				Set<String> packageSet = new TreeSet<String>();
				IJavaProject jpjt = JavaCore.create(project);
				try {
					IPackageFragment[] packages = jpjt.getPackageFragments();
					for (IPackageFragment mypackage : packages) {
						String pName = mypackage.getElementName();
						if (pName.startsWith(basePackage)) {
							packageSet.add(pName);
						}
					}
				} catch (JavaModelException e) {
					e.printStackTrace();
				}

				ElementListSelectionDialog dialog = new ElementListSelectionDialog(comp.getShell(), new PackageLabelProvider());
				dialog.setElements(packageSet.toArray());
				dialog.setTitle(Message.wizard_crud_gen_packageselection);
				DomainGenerationWizard wizard = (DomainGenerationWizard)getWizard();
				dialog.setFilter(wizard.getCurrentPackageName());
				dialog.open();

				Object selectedResult = dialog.getFirstResult();
				if (selectedResult != null) {
					packageText.setText(selectedResult.toString());
				}
			}
		});

	}
	

	private void createTableRefreshFields(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(comp, SWT.LEFT).setText(Message.wizard_generation_domain_selecttables);

		GridData buttonGridData = new GridData(85, 25);
		buttonGridData.horizontalAlignment = SWT.END;
		buttonGridData.grabExcessHorizontalSpace = true;
		refreshButton = new Button(comp, SWT.PUSH);
		refreshButton.setText(Message.ide_button_refresh);
		refreshButton.setLayoutData(buttonGridData);
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionevent) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						try {
							String configFile = ConfigXmlUtil.getCommonConfigFile(project.getLocation().toOSString());
							projectConfig = ConfigXmlUtil.getProjectConfig(configFile);
						} catch (Exception e) {
							PluginLoggerUtil.warning(ID, Message.wizard_error_properties);
						}
						tableTreeViewer.setInput(getTableModel());
						tableTreeViewer.expandAll();
						tableTreeViewer.refresh();
					}
				});
			}
		});
	}

	private void createTables(Composite comp) {
		tableTreeViewer = new CheckboxTreeViewer(comp, SWT.CHECK | SWT.BORDER);
		tableTreeViewer.setContentProvider(new TreeContentProvider());
		tableTreeViewer.setLabelProvider(new TreeLabelProvider());
		tableTreeViewer.setInput(getDefaultTableModel());
		tableTreeViewer.expandAll();
		tableTreeViewer.addCheckStateListener(tableCheckBoxListener);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.heightHint = 220;
		tableTreeViewer.getTree().setLayoutData(data);
	}

	private ITreeModel getDefaultTableModel() {
		String schemaName = "";
		SimpleTreeNode model = null;
		try {
			schemaName = ConfigXmlUtil.getDefaultDatabase(projectConfig).getSchema();
			model = new SimpleTreeNode("catalog", schemaName);
			String rootName = Message.wizard_domain_init_message;
			SimpleTreeNode schemaModel = new SimpleTreeNode(rootName, schemaName);
			model.addChild(schemaModel);
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, Message.view_exception_loadconfig, e);
		}
		return model;
	}

	private ITreeModel getTableModel() {
		String schemaName = "";
		SimpleTreeNode model = null;
		if (schemaName != null) {
			try {
				JdbcOption jdbcOption = ConfigXmlUtil.getDefaultDatabase(projectConfig);
				schemaName = jdbcOption.getSchema();
				model = new SimpleTreeNode("catalog", schemaName);
				String rootName = schemaName.length() == 0 ? "Tables(No Schema)" : schemaName;
				SimpleTreeNode schemaModel = new SimpleTreeNode(rootName, schemaName);
				model.addChild(schemaModel);
				String[] tables = getTables(project, jdbcOption.getDbName(), jdbcOption.getSchema(), jdbcOption.getDbType());
				for (int i = 0; i < tables.length; i++) {
					SimpleTreeNode tableModel = new SimpleTreeNode(tables[i], null);
					schemaModel.addChild(tableModel);
				}
			} catch (Exception e) {
				PluginLoggerUtil.error(ID, Message.view_exception_loadconfig, e);
			}
		} else
			return getProblemNode(schemaName);

		return model;
	}

	private ITreeModel getProblemNode(String schemaName) {
		SimpleTreeNode problemNode = new SimpleTreeNode("problem message", schemaName);
		problemNode.addChild(new SimpleTreeNode(Message.wizard_problem_connection, schemaName));

		return problemNode;
	}

	private ICheckStateListener tableCheckBoxListener = new ICheckStateListener() {
		public void checkStateChanged(CheckStateChangedEvent event) {
			ITreeModel model = (ITreeModel) event.getElement();

			if (model.getParent().getParent() == null) {
				if (event.getChecked()) {
					tableTreeViewer.setSubtreeChecked(model, true);
				} else {
					tableTreeViewer.setSubtreeChecked(model, false);
				}
			} else {
				if (!event.getChecked()) {
					tableTreeViewer.setChecked(model.getParent(), false);
				}
			}

			setPageComplete(isPageComplete());
		}
	};

	public synchronized String[] getTables(IProject project, String dbName, String schemaName, String dbType) throws Exception {
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = EDPUtil.getConnection(project, dbName);
			// ".*", ".BIN"
			String tableNamePattern = null;
			if (dbType.equals("sybase"))
				schemaName = null;
			rs = conn.getMetaData().getTables(conn.getCatalog(), schemaName, tableNamePattern, new String[] { "TABLE" });
			return getRsList(rs, "TABLE_NAME");
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, Message.view_exception_gettable, e);
			throw e;
		} finally {
			EDPUtil.close(conn);
			if (rs != null)
				rs.close();
		}
	}

	private synchronized String[] getRsList(ResultSet rs, String columnName) throws SQLException {
		final ArrayList<String> list = new ArrayList<String>();
		try {
			if (rs != null) {
				while (rs.next()) {
					list.add((String) rs.getString(columnName));
				}
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		return list.toArray(new String[list.size()]);
	}

	public Text getPackageText() {
		return packageText;
	}

	public Object[] getTableNames() {
		Object[] tables = tableTreeViewer.getCheckedElements();
		List<String> tableNames = new ArrayList<String>();
		boolean isSelectAll = false;

		if (tables.length > 0 && ((ITreeModel) tables[0]).getParent().getParent() == null) {
			isSelectAll = true;
		}

		for (int i = 0; i < tables.length; i++) {
			if (isSelectAll) {
				isSelectAll = false;
			} else {
				tableNames.add(((ITreeModel) tables[i]).getName());
			}
		}
		return tableNames.toArray();
	}

	public String getTableNamesWithComma() {
		Object[] tables = tableTreeViewer.getCheckedElements();

		String tableName = "*";
		if (tables.length > 0 && ((ITreeModel) tables[0]).getParent().getParent() == null) {
			tableName = "*";
		} else if (tables.length > 0 && ((ITreeModel) tables[0]).getParent().getParent() == null
				&& ((ITreeModel) tables[0]).getName().equals(Message.wizard_domain_init_message)) {
			tableName = "";
		} else if (tables.length == 1 && ((ITreeModel) tables[0]).getParent().getParent() != null) {
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

	class PackageLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			return ImageUtil.getImageDescriptor(CodeGeneratorActivator.PLUGIN_ID, Message.image_package).createImage();
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

	public boolean isPageComplete() {
		if (getPackageText().getText().length() > 0 && !ProjectUtil.validatePkgName(getPackageText().getText())) {
			setErrorMessage(Message.wizard_application_validation_pkgname);
			return false;
		}

		Object[] tables = tableTreeViewer.getCheckedElements();
		if (tables.length < 1) {
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	public boolean canFlipToNextPage() {
		boolean isNext = super.canFlipToNextPage();

		if (isNext)
			return true;
		return false;
	}
}
