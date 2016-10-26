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
package org.anyframe.ide.common.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.databases.DatabasesSettingUtil;
import org.anyframe.ide.common.databases.JdbcOption;
import org.anyframe.ide.common.dialog.DbSettingDialog;
import org.anyframe.ide.common.dialog.IDBSettingDialog;
import org.anyframe.ide.common.messages.Message;
import org.anyframe.ide.common.table.TableViewContentProvider;
import org.anyframe.ide.common.table.TableViewLabelProvider;
import org.anyframe.ide.common.util.ButtonUtil;
import org.anyframe.ide.common.util.ComponentUtil;
import org.anyframe.ide.common.util.ConfigXmlUtil;
import org.anyframe.ide.common.util.ImageUtil;
import org.anyframe.ide.common.util.ListUtil;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.MessageUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.ProjectUtil;
import org.anyframe.ide.common.util.StringUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * This is DataBasesPropertyPage class.
 * 
 * @author Sujeong Lee
 */
public class DataBasesPropertyPage extends PropertyPage {

	private TableViewer viewer;
	private IAction addAction;
	private IAction editAction;
	private IAction deleteAction;
	private IAction defaultAction;
	private IAction moveUpAction;
	private IAction moveDownAction;

	private Button editButton;
	private Button deleteButton;
	private Button defaultButton;

	private final IDBSettingDialog dbSettingDialog;
	private boolean isChangedDBConfig = false;
	private boolean isModifyListConfig = false;

	public static final String PROGRAM_NAME = "Databases";

	public DataBasesPropertyPage() {
		this.dbSettingDialog = CommonActivator.getInstance().getDBSettingDialog();

		noDefaultAndApplyButton();
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, 0);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		createDbSettingsComposite(composite);
		createActionAndMenu();
		loadSettings();

		return composite;
	}

	private void createActionAndMenu() {
		addAction = new Action() {
			public void run() {
				IProject project = (IProject) getElement().getAdapter(IProject.class);
				DbSettingDialog dialog = new DbSettingDialog(Display.getCurrent().getActiveShell(), project, null);
				if (dialog.open() == Dialog.OK) {
					JdbcOption newJdbc = dialog.getJdbcOption();
					if (newJdbc != null) {
						List<JdbcOption> input = (List<JdbcOption>) viewer.getInput();
						if (input == null) {
							input = new ArrayList<JdbcOption>();
						}
						input.add(newJdbc);
						viewer.setInput(input);

						isModifyListConfig = true;
					}
				}
			}
		};
		addAction.setText(Message.ide_button_add);

		editAction = new Action() {
			public void run() {
				if (viewer.getSelection().isEmpty()) {
					return;
				}
				JdbcOption selectedItem = (JdbcOption) ((IStructuredSelection) viewer.getSelection()).getFirstElement();

				IProject project = (IProject) getElement().getAdapter(IProject.class);
				DbSettingDialog dialog = new DbSettingDialog(Display.getCurrent().getActiveShell(), project, selectedItem);
				if (dialog.open() == Dialog.OK) {
					JdbcOption newJdbc = dialog.getJdbcOption();
					if (newJdbc != null) {
						List<JdbcOption> input = (List<JdbcOption>) viewer.getInput();
						if (input == null) {
							input = new ArrayList<JdbcOption>();
						}
						isModifyListConfig = true;
					}
				}
				viewer.refresh();
			}

		};
		editAction.setText(Message.ide_button_edit);

		deleteAction = new Action() {
			public void run() {
				if (viewer.getTable().getSelectionIndex() > -1) {
					if (viewer.getTable().getSelection()[0].getText().endsWith(Constants.DB_SETTING_DEFAULT_POSTFIX)) {
						MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.properties_database_error_delete_default,
								MessageDialog.ERROR);
						return;
					} else {
						ComponentUtil.deleteTableViewerSelectedItem(viewer);
						isModifyListConfig = true;
					}
				}
			}
		};
		deleteAction.setText(Message.ide_button_remove);

		defaultAction = new Action() {
			public void run() {
				if (viewer.getTable().getSelectionIndex() > -1) {
					if (viewer.getSelection().isEmpty()) {
						return;
					}

					List listOfItems = (List) viewer.getInput();
					Table table = viewer.getTable();
					List tempList = new ArrayList();
					List result = new ArrayList();
					boolean selected = false;
					JdbcOption jdbcOption = null;
					for (int index = 0; index < table.getItemCount(); index++) {
						jdbcOption = (JdbcOption) listOfItems.get(index);
						if (table.isSelected(index)) {
							if (index > -1) {
								result.add(jdbcOption);
								result.addAll(tempList);
								jdbcOption.setDefault(true);
								selected = true;
							}
						} else if (selected) {
							result.add(jdbcOption);
							jdbcOption.setDefault(false);
						} else {
							tempList.add(jdbcOption);
							jdbcOption.setDefault(false);
						}
					}
					viewer.setInput(result);
					viewer.refresh();
					isChangedDBConfig = true;
					isModifyListConfig = true;
				}
			}
		};
		defaultAction.setText(Message.ide_button_setdefault);

		moveUpAction = new Action() {
			public void run() {
				ComponentUtil.moveUpTableViewerSelectedItem(viewer);
			}
		};
		moveUpAction.setText(Message.ide_button_moveup);

		moveDownAction = new Action() {
			public void run() {
				ComponentUtil.moveDownTableViewerSelectedItem(viewer);
			}
		};
		moveDownAction.setText(Message.ide_button_movedown);

	}

	private void loadSettings() {
		IProject project = (IProject) getElement().getAdapter(IProject.class);

		File jdbcConfigFile = new File(PropertiesSettingUtil.getJdbcdriversFile(project.getLocation().toOSString()));
		if (!jdbcConfigFile.exists()) {
			URL fileLocation = this.getClass().getProtectionDomain().getCodeSource().getLocation();
			File jarFile = new File(fileLocation.getFile());

			try {
				if (jarFile.isDirectory()) {
					File configFile = new File(fileLocation.getFile() + Constants.FILE_SEPERATOR + Constants.DRIVER_SETTING_XML_FILE);
					if (!configFile.exists()) {
						throw new Exception(Message.properties_jdbc_configuration_notfound);
					}
					copyFile(configFile.getAbsolutePath(), PropertiesSettingUtil.getJdbcdriversFile(project.getLocation().toOSString()));
				} else {
					ZipFile zipFile = new ZipFile(jarFile);
					ZipEntry zipEntry = zipFile.getEntry(Constants.DRIVER_SETTING_XML_FILE);

					InputStream inputStream = zipFile.getInputStream(zipEntry);

					copyFile(inputStream, PropertiesSettingUtil.getJdbcdriversFile(project.getLocation().toOSString()));
				}
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (Exception e) {
				PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, NLS.bind(Message.properties_jdbc_configuration_copy_fail, e.getMessage()));
			}

		}

		List<JdbcOption> jdbcOptionList = new ArrayList();
		List<JdbcOption> result = DatabasesSettingUtil.getDatasourcesByProject(project);
		jdbcOptionList.addAll(result);
		viewer.setInput(jdbcOptionList);
		viewer.refresh();
	}

	private void copyFile(String input, String dest) {
		try {
			InputStream fis = new FileInputStream(input);
			copyFile(fis, dest);
		} catch (Exception e) {
			PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, NLS.bind(Message.properties_jdbc_configuration_copy_fail, e.getMessage()));
		}
	}

	private void copyFile(InputStream input, String dest) {
		try {
			File file = new File(dest);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(dest);

			int data = 0;
			while ((data = input.read()) != -1) {
				fos.write(data);
			}
			input.close();
			fos.close();
		} catch (Exception e) {
			PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, NLS.bind(Message.properties_jdbc_configuration_copy_fail, e.getMessage()));
		}
	}

	private void createDbSettingsComposite(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Message.properties_config_database_description);

		Group group = new Group(parent, SWT.NULL);
		group.setText(Message.properties_database_config_description);
		group.setLayout(new GridLayout(1, false));
		GridData connectionInfoData = new GridData(GridData.FILL_HORIZONTAL);
		connectionInfoData.heightHint = 221;
		group.setLayoutData(connectionInfoData);

		Composite connectionInfoGroup = new Composite(group, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		GridData data = new GridData(GridData.FILL_BOTH);
		connectionInfoGroup.setLayout(layout);
		connectionInfoGroup.setLayoutData(data);

		viewer = new TableViewer(connectionInfoGroup, SWT.BORDER | SWT.SINGLE);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 10));
		// viewer.getTable().setHeaderVisible(true);

		TableViewerColumn column = new TableViewerColumn(viewer, SWT.None);
		column.getColumn().setText(Message.properties_database_datasource_title);
		column.getColumn().setWidth(400);

		viewer.setLabelProvider(new TableViewLabelProvider() {

			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				return ImageUtil.getImage(CommonActivator.PLUGIN_ID, "icons/ball_blue.gif");
				// return afImageCache.getImage(dbImg);
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				JdbcOption jdbcOption = (JdbcOption) element;
				String append = "";
				if (jdbcOption.getDefault()) {
					append = Constants.DB_SETTING_DEFAULT_POSTFIX;
				}
				return jdbcOption.getDbName() + " (" + (jdbcOption.getUrl() + ")" + append);
			}
		});
		viewer.setContentProvider(new TableViewContentProvider() {
			@Override
			public Object[] getElements(Object obj) {
				return ((List) obj).toArray();
			}
		});

		createButtons(connectionInfoGroup);

		MenuManager testMenuMgr = new MenuManager("#PopupMenu");
		testMenuMgr.setRemoveAllWhenShown(true);
		testMenuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(editAction);
				manager.add(deleteAction);
				manager.add(moveUpAction);
				manager.add(moveDownAction);
			}
		});
		Menu menu = testMenuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				editButton.setEnabled(true);
				deleteButton.setEnabled(true);
				defaultButton.setEnabled(true);
			}
		});

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editAction.run();
			}
		});
	}

	private void createButtons(Composite composite) {
		Composite compositeButtonArea = new Composite(composite, SWT.NONE);
		compositeButtonArea.setLayout(new GridLayout());
		compositeButtonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Button addButton = ButtonUtil.createButton(compositeButtonArea, Message.ide_button_add, "");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionevent) {
				addAction.run();
				super.widgetSelected(selectionevent);
			}
		});

		editButton = ButtonUtil.createButton(compositeButtonArea, Message.ide_button_edit, "");
		editButton.setEnabled(false);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionevent) {
				editAction.run();
				super.widgetSelected(selectionevent);
			}
		});

		deleteButton = ButtonUtil.createButton(compositeButtonArea, Message.ide_button_remove, "");
		deleteButton.setEnabled(false);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionevent) {
				deleteAction.run();
				super.widgetSelected(selectionevent);
			}
		});

		defaultButton = ButtonUtil.createButton(compositeButtonArea, Message.ide_button_setdefault, "");

		String projectLocation = ((IProject) getElement().getAdapter(IProject.class)).getLocation().toOSString();

		try {
			String configFile = ConfigXmlUtil.getCommonConfigFile(projectLocation);
			File f = new File(configFile);
			if (f.exists()) {
				defaultButton.setEnabled(false);
				defaultButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent selectionevent) {
						defaultAction.run();
						super.widgetSelected(selectionevent);
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean performOk() {
		List<JdbcOption> settings = (List<JdbcOption>) viewer.getInput();

		if (!isModifyListConfig) {
			return super.performOk();
		}
		if (!MessageDialogUtil.confirmMessageDialog(Message.ide_message_title, Message.properties_confirm_jdbcconf))
			return false;

		if (isDuplicatedDsNames(settings)) {
			return false;
		}

		if (saveSettings(settings)) {
			IProject project = (IProject) getElement().getAdapter(IProject.class);

			for (JdbcOption jdbcOption : settings) {
				if (jdbcOption.getDefault()) {
					if (dbSettingDialog != null && isChangedDBConfig) {
						dbSettingDialog.changeDb(project, jdbcOption);
						break;
					}
				}
			}
			ProjectUtil.refreshProject(project.getName());
			return super.performOk();
		}
		return false;
	}

	private boolean saveSettings(List<JdbcOption> settings) {
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		if (!ListUtil.isEmptyOrNull(settings)) {
			return DatabasesSettingUtil.saveJdbcOptionList(project, settings);
		} else {
			return false;
		}
	}

	private boolean isDuplicatedDsNames(List<JdbcOption> settings) {
		if (ListUtil.isEmptyOrNull(settings)) {
			return false;
		}
		Set<String> foundDsNames = new HashSet<String>();
		for (JdbcOption jdbcOption : settings) {
			String dsName = jdbcOption.getDbName();
			if (foundDsNames.contains(dsName)) {
				MessageUtil.showMessage(NLS.bind(Message.properties_validation_dbname_duplicate, dsName), PROGRAM_NAME);
				return true;
			} else {
				foundDsNames.add(dsName);
			}
		}
		return false;
	}

}
