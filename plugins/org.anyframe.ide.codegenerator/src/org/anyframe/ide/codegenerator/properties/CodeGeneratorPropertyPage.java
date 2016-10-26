/*
 * Copyright 2007-2012 Samsung SDS Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.anyframe.ide.codegenerator.properties;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.model.table.PluginInfoList;
import org.anyframe.ide.codegenerator.util.FileUtil;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.command.maven.mojo.codegen.AnyframeTemplateData;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.PropertyUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.thoughtworks.xstream.XStream;

/**
 * This is an CodeGeneratorPropertyPage class.
 * 
 * @author Sujeong Lee
 */
public class CodeGeneratorPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	private Combo templateTypeCombo;
	private Combo daoFrameworkTypeCombo;

	private Label validationResult;

	private static IProject project;
	private boolean isChangedConfig = false;

	PropertyUtil propertyUtil = null;

	private List<String> pList;

	// mip-query, query plugin name
	private final String pluginMipQuery = CommonConstants.MIP_QUERY_PLUGIN;
	private final String pluginXPQuery = CommonConstants.XP_QUERY_PLUGIN;
	// private final String pluginQuery = CommonConstants.QUERY_PLUGIN;

	// miplatform, map template name
	private final String templateMiplatform = "miplatform";
	private final String templateXPlatform = "xplatform";

	// private final String templateMap = "map";

	public CodeGeneratorPropertyPage() {
		noDefaultAndApplyButton();
	}

	public static IProject getPropertyProject() {
		return project;
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		init();
		Composite composite = new Composite(parent, 0);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		createContentsGroup(composite);
		loadSettings();

		return composite;
	}

	private void init() {
		project = (IProject) getElement().getAdapter(IResource.class);
		PropertiesIO pjtProps = null;
		try {
			pjtProps = ProjectUtil.getProjectProperties(project);
		} catch (Exception e) {
			PluginLoggerUtil.warning(ID, Message.wizard_error_properties);
		}
		PluginInfoList pluginInfoList = new PluginInfoList(pjtProps);
		pList = pluginInfoList.getInstalledPluginTypeList();

		String projectLocation = project.getLocation().toOSString();
		String metaFile = projectLocation + Constants.METAINF
				+ Constants.METADATA_FILE;
		propertyUtil = new PropertyUtil(metaFile);
	}

	private void createContentsGroup(final Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Message.wizard_config_description);

		// Group
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(Message.wizard_config_section);
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Template Location
		new Label(group, SWT.NONE).setText(Message.wizard_module_template);
		templateTypeCombo = new Combo(group, SWT.SINGLE | SWT.BORDER
				| SWT.READ_ONLY);
		templateTypeCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				setEnableDaoFrameworkTypeCombo(false);
				isChangedConfig = true;
			}
		});
		GridData templateTypeComboData = new GridData(SWT.NULL);
		templateTypeComboData.widthHint = 150;
		templateTypeCombo.setLayoutData(templateTypeComboData);

		// empty label
		new Label(group, SWT.NONE);

		// Template Location
		new Label(group, SWT.NONE)
				.setText(Message.wizard_application_daoframeworks);
		daoFrameworkTypeCombo = new Combo(group, SWT.SINGLE | SWT.BORDER
				| SWT.READ_ONLY);
		daoFrameworkTypeCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				isChangedConfig = true;
			}
		});
		GridData daoFrameworkTypeComboData = new GridData(SWT.NULL);
		daoFrameworkTypeComboData.widthHint = 150;
		daoFrameworkTypeCombo.setLayoutData(daoFrameworkTypeComboData);

		// empty label
		new Label(group, SWT.NONE);

		validationResult = new Label(group, SWT.NONE);
		validationResult.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL_VERTICAL, true, false, 3, 1));
		validationResult
				.setForeground(new Color(group.getDisplay(), 255, 0, 0));
	}

	private void loadSettings() {
		String templateLoc = propertyUtil
				.getProperty(Constants.PROJECT_TEMPLATE_HOME);
		File templateLocDir = new File(templateLoc);
		checkAndSetTemplateLocation(templateLocDir);
		checkAndSetDaoFrameworkType();
	}

	private void setEnableDaoFrameworkTypeCombo(boolean isLoadPropertyFile) {
		daoFrameworkTypeCombo.removeAll();

		String projectLocation = project.getLocation().toOSString();
		String metaFile = projectLocation + Constants.METAINF
				+ Constants.METADATA_FILE;
		PropertyUtil propertyUtil = new PropertyUtil(metaFile);

		List<AnyframeTemplateData> templates = null;
		XStream xstream = new XStream();
		xstream.processAnnotations(AnyframeTemplateData.class);
		xstream.setMode(XStream.NO_REFERENCES);

		FileInputStream templateConfigFile = null;
		String templateConfigFilePath = propertyUtil
				.getProperty(Constants.PROJECT_TEMPLATE_HOME)
				+ ProjectUtil.SLASH
				+ getTemplateType()
				+ ProjectUtil.SLASH
				+ "source" + ProjectUtil.SLASH;
		try {
			templateConfigFile = new FileInputStream(templateConfigFilePath
					+ CommonConstants.TEMPLATE_CONFIG_FILE);
			templates = (java.util.List<AnyframeTemplateData>) xstream
					.fromXML(templateConfigFile);
		} catch (Exception e) {
			PluginLoggerUtil.error(ID, Message.wizard_error_templateconfig
					+ templateConfigFilePath, e);
		}

		Set<String> daoSet = new HashSet<String>();
		if (templates != null) {
			for (AnyframeTemplateData template : templates) {
				if (template.getDao() != null) {
					daoSet.add(template.getDao());
				}
			}
		}

		if (templateTypeCombo.getText().equals(Constants.TEMPLATE_TYPE_ONLINE)) {
			daoFrameworkTypeCombo.add(CommonConstants.DAO_SPRINGJDBC);
		} else {
			if (daoSet.contains(CommonConstants.DAO_QUERY)
					&& pList.contains(CommonConstants.QUERY_PLUGIN)) {
				daoFrameworkTypeCombo.add(CommonConstants.DAO_QUERY);
			}

			if (daoSet.contains(CommonConstants.DAO_HIBERNATE)
					&& pList.contains(CommonConstants.HIBERNATE_PLUGIN)) {
				daoFrameworkTypeCombo.add(CommonConstants.DAO_HIBERNATE);
			}

			if (daoSet.contains(CommonConstants.DAO_IBATIS2)
					&& pList.contains(CommonConstants.IBATIS2_PLUGIN)) {
				daoFrameworkTypeCombo.add(CommonConstants.DAO_IBATIS2);
			}

			if (daoSet.contains(CommonConstants.DAO_MYBATIS)
					&& pList.contains(CommonConstants.MYBATIS_PLUGIN)) {
				daoFrameworkTypeCombo.add(CommonConstants.DAO_MYBATIS);
			}

			if (daoSet.contains(CommonConstants.DAO_SPRINGJDBC)
					&& pList.contains(CommonConstants.CORE_PLUGIN)) {
				daoFrameworkTypeCombo.add(CommonConstants.DAO_SPRINGJDBC);
			}
		}

		if (isLoadPropertyFile) {
			daoFrameworkTypeCombo.setText(propertyUtil
					.getProperty(Constants.APP_DAOFRAMEWORK_TYPE));
		} else {
			daoFrameworkTypeCombo.select(0);
		}

		if (daoFrameworkTypeCombo.getItemCount() < 1) {
			validationResult
					.setText("There is no selected Template or DAO Framework. You should select Template and DAO Framework.");
		} else {
			validationResult.setText("");
		}
	}

	private void checkAndSetTemplateLocation(File templateDir) {
		try {
			String propertyTemplateType = propertyUtil
					.getProperty(Constants.APP_TEMPLATE_TYPE);
			if (templateDir.exists()) {

				File[] templateTypes = FileUtil
						.dirListByAscAlphabet(templateDir);

				templateTypeCombo.removeAll();

				for (File templateType : templateTypes) {
					if (FileUtil.validateTemplatePath(templateType)) {
						templateTypeCombo.add(templateType.getName());
					}
				}

				List<String> templatesList = Arrays.asList(templateTypeCombo
						.getItems());
				if ("".equals(propertyTemplateType)) {
					templateTypeCombo.select(0);
				} else if (templatesList.indexOf(propertyTemplateType) < 0) {
					templateTypeCombo.select(0);
					PluginLoggerUtil
							.info(CodeGeneratorActivator.PLUGIN_ID,
									Message.properties_error_template_pluginlist_notmatch);
				} else {
					templateTypeCombo.setText(propertyTemplateType);
				}

				// check whether plugin is installed or not
				if (templateTypeCombo.getItemCount() > 0) {
					if (!pList.contains(pluginMipQuery)
							&& templatesList.indexOf(templateMiplatform) > -1)
						templateTypeCombo.remove(templateMiplatform);
					// map type template
					// if (!pList.contains(pluginQuery))
					// templateTypeCombo.remove(templateMap);
					if (!pList.contains(pluginXPQuery)
							&& templatesList.indexOf(templateXPlatform) > -1)
						templateTypeCombo.remove(templateXPlatform);
					if (!pList.contains(Constants.TEMPLATE_TYPE_ONLINE)
							&& templatesList
									.indexOf(Constants.TEMPLATE_TYPE_ONLINE) > -1)
						templateTypeCombo
								.remove(Constants.TEMPLATE_TYPE_ONLINE);
				}
			} else {
				templateTypeCombo.removeAll();
				PluginLoggerUtil.error(ID, Message.wizard_error_template,
						new Exception());
			}

			if (templateTypeCombo.getItemCount() < 1) {
				validationResult
						.setText("There is no selected Template or DAO Framework. You should select Template and DAO Framework.");
			} else {
				validationResult.setText("");
			}
		} catch (Exception e) {
			PluginLoggerUtil.warning(ID, Message.wizard_error_properties);
		}
	}

	private void checkAndSetDaoFrameworkType() {
		setEnableDaoFrameworkTypeCombo(true);
	}

	@Override
	public boolean performOk() {
		if (validationResult.getText().equals("")) {
			if (isChangedConfig) {
				saveProperties();
			}
			return super.performOk();
		} else {
			return false;
		}
	}

	private void saveProperties() {
		String projectLocation = project.getLocation().toOSString();
		String metaFile = projectLocation + Constants.METAINF
				+ Constants.METADATA_FILE;
		File f = new File(metaFile);
		if (f.exists()) {
			PropertyUtil propertyUtil = new PropertyUtil(metaFile);
			propertyUtil.setProperty(Constants.APP_TEMPLATE_TYPE,
					templateTypeCombo.getText());
			propertyUtil.setProperty(Constants.APP_DAOFRAMEWORK_TYPE,
					daoFrameworkTypeCombo.getText());
			propertyUtil.write();
		}
	}

	public Combo getTemplateTypeCombo() {
		return templateTypeCombo;
	}

	public String getTemplateType() {
		return templateTypeCombo.getText();
	}

	public String getDaoFrameworkType() {
		return daoFrameworkTypeCombo.getText();
	}

}
