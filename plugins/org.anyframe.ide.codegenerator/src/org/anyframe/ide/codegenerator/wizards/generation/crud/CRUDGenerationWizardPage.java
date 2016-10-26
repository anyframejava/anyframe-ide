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
package org.anyframe.ide.codegenerator.wizards.generation.crud;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.model.table.PluginInfoList;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.codegenerator.util.SearchUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;
import org.anyframe.ide.command.maven.mojo.codegen.Domain;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.util.ConfigXmlUtil;
import org.anyframe.ide.common.util.ImageUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.ProjectConfig;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * This is an CRUDGenerationWizardPage class.
 * 
 * @author Sujeong Lee
 */
public class CRUDGenerationWizardPage extends WizardPage {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	private IProject project;

	private ProjectConfig projectConfig = null;

	private String selectedDomain;

	private Text packageText;
	private Combo templateTypeCombo;

	private Button genWebSourceButton;
	private Button genSampleDataButton;

	private TableViewer modelTableViewer;

	public String getSelectedDomain() {
		return selectedDomain;
	}

	public String getPackage() {
		return packageText.getText();
	}

	public String getTemplateType() {
		return templateTypeCombo.getText();
	}

	public boolean getGenWebSource() {
		return genWebSourceButton.getSelection();
	}

	public boolean getSampleData() {
		return genSampleDataButton.getSelection();
	}

	protected CRUDGenerationWizardPage(IProject project, String pageName) {
		super(pageName);
		this.project = project;

		this.setTitle(pageName);
		this.setDescription(Message.wizard_crud_gen_selectdomainmodelclasses);
	}

	public void createControl(Composite parent) {
		init();

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createInfoSelectAllField(composite);
		createTables(composite);
		createPackageFields(composite);
		createTemplateTypeCombo(composite);
		createOptions(composite);

		setPageComplete(isPageComplete());
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	private void createInfoSelectAllField(Composite comp) {
		new Label(comp, SWT.LEFT).setText(Message.wizard_crud_gen_domainmodelclasses);

		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);
	}

	private void createTables(Composite comp) {
		Table modelTable = new Table(comp, SWT.BORDER);
		modelTableViewer = new TableViewer(modelTable);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 3;
		data.heightHint = 10 * modelTable.getItemHeight(); // maximum 10 line
		modelTableViewer.getTable().setLayoutData(data);

		TableLayout layout = new TableLayout();
		TableColumn projectColumn = new TableColumn(modelTable, SWT.NONE);
		layout.addColumnData(new ColumnWeightData(1, true));
		projectColumn.setResizable(true);
		modelTable.setLayout(layout);

		modelTableViewer.setColumnProperties(new String[] { "a" });
		modelTableViewer.setContentProvider(new ArrayContentProvider());
		modelTableViewer.setLabelProvider(new LabelProvider() {
			public Image getImage(Object o) {
				return ImageUtil.getImageDescriptor(CodeGeneratorActivator.PLUGIN_ID, Message.image_java_class).createImage();
			}

			public String getText(Object o) {
				return o.toString();
			}
		});
		modelTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				selectedDomain = selection.getFirstElement().toString();
				String domainNameWithDot = selectedDomain.substring(selectedDomain.lastIndexOf(".")).toLowerCase();
				packageText.setText(projectConfig.getPackageName() + domainNameWithDot);

				setPageComplete(isPageComplete());
			}
		});
		modelTableViewer.setInput(getDomainList());
	}

	private void createPackageFields(Composite parent) {
		final String basePackage = projectConfig.getPackageName();

		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(3, false));
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 3;
		comp.setLayoutData(data);

		new Label(comp, SWT.NONE).setText(Message.wizard_crud_gen_basepackage);

		packageText = new Text(comp, SWT.BORDER);

		packageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
				dialog.open();

				Object selectedResult = dialog.getFirstResult();
				if (selectedResult != null) {
					packageText.setText(selectedResult.toString());
				}
			}
		});

	}

	private void createTemplateTypeCombo(Composite parent) {
		Group templateGroup = new Group(parent, SWT.NULL);

		templateGroup.setText(Message.wizard_templatetype_group);
		templateGroup.setLayout(new GridLayout(2, false));
		templateGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(templateGroup, SWT.NONE).setText(Message.wizard_templatetype_name);

		templateTypeCombo = new Combo(templateGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		templateTypeCombo.setLayoutData(new GridData(SWT.NULL));
		templateTypeCombo.removeAll();

		File[] templateTypes = FileUtil.dirListByAscAlphabet(new File(projectConfig.getTemplatePath(Constants.PROJECT_NAME_CODE_GENERATOR)));

		PluginInfoList pluginInfoList = new PluginInfoList(projectConfig);
		List<String> installedPluginList = pluginInfoList.getInstalledPluginTypeList();

		List<String> enableTemplates = new ArrayList<String>();
		List<String> disableTemplates = new ArrayList<String>();
		for (File templateType : templateTypes) {
			String templateTypeName = templateType.getName();
			if (!templateTypeName.equals("common") && !templateTypeName.equals("online")) {
				if (installedPluginList.contains(templateTypeName) && !templateTypeName.equals(CommonConstants.DAO_SPRINGJDBC)) {
					enableTemplates.add(templateTypeName);
				} else if (templateTypeName.equals(CommonConstants.DAO_SPRINGJDBC)) {
					enableTemplates.add(CommonConstants.DAO_SPRINGJDBC);
				} else {
					disableTemplates.add(templateTypeName + Constants.TEMPLATE_TYPE_DISABLE_POSTFIX);
				}
			}
		}

		for(String template : enableTemplates){
			templateTypeCombo.add(template);
		}
		
		for(String template : disableTemplates){
			templateTypeCombo.add(template);
		}
		
		templateTypeCombo.select(0);

		templateTypeCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				setPageComplete(isPageComplete());
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	private void createOptions(Composite parent) {
		Group optionsGroup = new Group(parent, SWT.NULL);
		optionsGroup.setText(Message.wizard_crud_gen_otheroptions);
		optionsGroup.setLayout(new GridLayout());
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 3;
		optionsGroup.setLayoutData(data);

		genWebSourceButton = new Button(optionsGroup, SWT.CHECK);
		genWebSourceButton.setText(Message.wizard_crud_genwebsource);
		genWebSourceButton.setSelection(true);

		genSampleDataButton = new Button(optionsGroup, SWT.CHECK);
		genSampleDataButton.setText(Message.wizard_crud_insertsampledata);
		genSampleDataButton.setSelection(true);
	}

	private Object[] getDomainList() {
		Object[] result = getExistDomainList();
		Arrays.sort(result);
		return result;
	}

	private Object[] getExistDomainList() {
		ArrayList<String> result = new ArrayList<String>();

		try {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = javaProject.getPackageFragments();
			for (IPackageFragment mypackage : packages) {
				if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
					for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
						ASTParser parser = ASTParser.newParser(AST.JLS3);
						parser.setResolveBindings(true);
						parser.setSource(unit);
						CompilationUnit cu = (CompilationUnit) parser.createAST(null);
						List typelist = cu.types();
						if (typelist.size() == 0)
							return result.toArray();
						TypeDeclaration td = (TypeDeclaration) typelist.get(0);
						Javadoc jdoc = td.getJavadoc();

						if (jdoc != null) {
							List tags = jdoc.tags();
							for (int i = 0; i < tags.size(); i++) {
								String tag = String.valueOf(tags.get(i));
								if (tag.contains(Domain.TABLE)) {
									result.add(td.resolveBinding().getQualifiedName());
								}
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return result.toArray();
		// ArrayList<String> listAnnotations = new ArrayList<String>();
		// listAnnotations.add("@table");
		// ArrayList<ResolvedSourceType> listClassFile = SearchUtil.search("*",
		// listAnnotations, project);
		//
		// for (ResolvedSourceType classFile : listClassFile) {
		// result.add(classFile.getFullyQualifiedName());
		// }
		// return result.toArray();
	}

	private void init() {
		try {
			String configFile = ConfigXmlUtil.getCommonConfigFile(project.getLocation().toOSString());
			projectConfig = ConfigXmlUtil.getProjectConfig(configFile);
		} catch (Exception e) {
			PluginLoggerUtil.warning(ID, Message.wizard_error_properties);
		}
	}

	public boolean isPageComplete() {
		if (getPackage().length() > 0 && !ProjectUtil.validatePkgName(getPackage())) {
			setErrorMessage(Message.wizard_application_validation_pkgname);
			return false;
		}

		String selected = getSelectedDomain();
		if (selected == null || "".equals(selected)) {
			return false;
		}

		if ("".equals(templateTypeCombo.getText()) || templateTypeCombo.getText().endsWith(Constants.TEMPLATE_TYPE_DISABLE_POSTFIX)) {
			setErrorMessage(Message.wizard_application_validation_templatetype);
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
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
}
