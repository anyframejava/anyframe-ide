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
package org.anyframe.ide.codegenerator.wizards.generation.crud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.codegenerator.util.SearchUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.common.util.ImageUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
	private PropertiesIO pjtProps = null;
	
	private String selectedDomain;

	private Text packageText;

	private Button genWebSourceButton;
	private Button genSampleDataButton;

	private TableViewer modelTableViewer;

	public String getSelectedDomain() {
		return selectedDomain;
	}

	public String getPackage() {
		return packageText.getText();
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
		this.setDescription("Select Domain Model Classes.");
	}

	public void createControl(Composite parent) {
		init();

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createInfoSelectAllField(composite);
		createTables(composite);
		createPackageFields(composite);
		createOptions(composite);

		setPageComplete(false);
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	private void createInfoSelectAllField(Composite comp) {
		new Label(comp, SWT.LEFT).setText("Domain Model Classes.");

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
				return ImageUtil.getImageDescriptor(
						CodeGeneratorActivator.PLUGIN_ID,
						Message.image_java_class).createImage();
			}

			public String getText(Object o) {
				return o.toString();
			}
		});
		modelTableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(
							final SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) event
								.getSelection();
						selectedDomain = selection.getFirstElement()
								.toString();
						String domainNameWithDot = selectedDomain.substring(
								selectedDomain.lastIndexOf(".")).toLowerCase();
						packageText.setText(pjtProps
								.readValue(CommonConstants.PACKAGE_NAME)
								+ domainNameWithDot);
					}
				});
		modelTableViewer.setInput(getDomainList());
	}

	private void createPackageFields(Composite parent) {
		final String basePackage = pjtProps
				.readValue(CommonConstants.PACKAGE_NAME);

		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(3, false));
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 3;
		comp.setLayoutData(data);

		new Label(comp, SWT.NONE).setText("Base Package :");

		packageText = new Text(comp, SWT.BORDER);
		packageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		packageText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				setPageComplete(isPageComplete());
			}
		});

		GridData buttonGridData = new GridData(85, 25);
		Button packageEditButton = new Button(comp, SWT.PUSH);
		packageEditButton.setText("Edit");
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

				ElementListSelectionDialog dialog = new ElementListSelectionDialog(
						comp.getShell(), new PackageLabelProvider());
				dialog.setElements(packageSet.toArray());
				dialog.setTitle("Package Selection");
				dialog.open();

				Object selectedResult = dialog.getFirstResult();
				if (selectedResult != null) {
					packageText.setText(selectedResult.toString());
				}
			}
		});

	}

	private void createOptions(Composite parent) {
		Group optionsGroup = new Group(parent, SWT.NULL);
		optionsGroup.setText("Other options");
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

		ArrayList<String> listAnnotations = new ArrayList<String>();
		listAnnotations.add("javax.persistence.Entity");
		ArrayList<ResolvedSourceType> listClassFile = SearchUtil.search("*",
				listAnnotations, project);

		for (ResolvedSourceType classFile : listClassFile) {
			result.add(classFile.getFullyQualifiedName());
		}
		return result.toArray();
	}

	private void init() {
		try {
			pjtProps = ProjectUtil.getProjectProperties(project);
		} catch (Exception e) {
			PluginLoggerUtil.warning(ID, Message.wizard_error_properties);
		}
	}

	public boolean isPageComplete() {
		if (getPackage().length() > 0
				&& !ProjectUtil.validatePkgName(getPackage())) {
			setErrorMessage(Message.wizard_application_validation_pkgname);
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	class PackageLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			return ImageUtil.getImageDescriptor(
					CodeGeneratorActivator.PLUGIN_ID, Message.image_package)
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