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
package org.anyframe.ide.querymanager.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.anyframe.ide.common.util.VersionUtil;
import org.anyframe.ide.querymanager.messages.MessagePropertiesLoader;
import org.anyframe.ide.querymanager.parsefile.FilesParser;
import org.anyframe.ide.querymanager.properties.QMPropertiesPage;
import org.anyframe.ide.querymanager.properties.QMPropertiesXMLUtil;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Select mapping-xml files in tree.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class ElementSelectAddDialog extends Dialog {

	private IProject project;
	protected static FilteredTree filter;
	public static TreeViewer viewer;

	ArrayList inputDataList = new ArrayList();

	private static ArrayList selectList;

	public static ArrayList getSelectList() {
		return selectList;
	}

	private QMPropertiesXMLUtil util = new QMPropertiesXMLUtil();

	public ElementSelectAddDialog(Shell parentShell) {
		super(parentShell);
		project = QMPropertiesPage.getPropertyProject();
	}

	protected void configureShell(Shell newShell) {
		newShell.setText(MessagePropertiesLoader.property_dialog_add_title);
		super.configureShell(newShell);

		Monitor mon = Display.getDefault().getMonitors()[0];
		Rectangle rect = mon.getBounds();
		int width = 430;
		int height = 450;
		newShell.setBounds(rect.width / 2 - width / 2, rect.height / 2 - height
				/ 2, width, height);
	}

	protected Control createDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		composite.setLayout(gridLayout);
		gridLayout.marginHeight = 10;
		gridLayout.marginWidth = 10;
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label labelInfo = new Label(composite, SWT.NONE);
		GridData layoutData = new GridData(GridData.BEGINNING);
		labelInfo.setLayoutData(layoutData);
		labelInfo
				.setText(MessagePropertiesLoader.property_dialog_add_message);

//		new VersionUtil();
		String platformVer = VersionUtil.getPlatformVersion();
		String productVer = VersionUtil.getProductVersion();

		PatternFilter patternFilter = new PatternFilter();
		patternFilter
				.setPattern(MessagePropertiesLoader.property_dialog_add_xmlfile_extention);
		if (platformVer.substring(0, 3).equals("3.5") //$NON-NLS-1$
				|| productVer.substring(0, 3).equals("3.5")) { //$NON-NLS-1$
			filter = new FilteredTree(composite, SWT.NONE | SWT.H_SCROLL
					| SWT.V_SCROLL | SWT.BORDER | SWT.MULTI, patternFilter,
					true);
		} else {
			filter = new FilteredTree(composite, SWT.NONE | SWT.H_SCROLL
					| SWT.V_SCROLL | SWT.BORDER | SWT.MULTI, patternFilter);
		}

		viewer = filter.getViewer();

		viewer.setContentProvider(new XMLOnlyContentProvider());
		viewer.setLabelProvider(new XMLOnlyLabelProvider());
		viewer.setSorter(new ViewerSorter());

		getFiles();

		return parent;
	}

	private void getFiles() {
		Job treeViewJob = new Job("Searching mapping-xml files") {

			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask("Searching the mapping-xml files for Dialog",
						100);
				monitor.subTask("Getting the Files....");

				monitor.worked(25);
				settingTree(viewer);
				monitor.worked(50);

				if (monitor.isCanceled()) {
					monitor.done();
				}

				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						viewer.setInput(inputDataList);
					}
				});
				monitor.worked(25);
				monitor.done();
				return Status.OK_STATUS;
			}
		};

		treeViewJob.setSystem(false);
		treeViewJob.setUser(true);
		treeViewJob.schedule();
	}

	private void settingTree(TreeViewer viewer) {
		ArrayList resultList = new ArrayList();

		FilesParser parser = new FilesParser();
		parser.parseFiles2(project, new NullProgressMonitor(), new HashMap());
		ArrayList list = parser.getXmlFileNames();
		ArrayList fileNameList = new ArrayList();
		ArrayList packageList = new ArrayList();

		for (int i = 0; i < list.size(); i++) {
			String file = (String) list.get(i);
			String filePath = file.substring(project.getLocation().toString()
					.length() + 1, file.length());
			fileNameList.add(filePath);
		}

		Object element = null;
		try {
			if (project
					.hasNature("org.anyframe.ide.querymanager.build.AnyframeNature")) {
				element = JavaCore.create(project);
			} else {
				element = new Object[0];
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		Object[] o1;
		try {
			o1 = ((IJavaProject) element).getChildren();

			for (int i = 0; i < o1.length; i++) {
				if (o1[i] instanceof JarPackageFragmentRoot) {
				} else if (o1[i] instanceof PackageFragmentRoot) {
					Object object = ((PackageFragmentRoot) o1[i]).getResource();

					if (object instanceof Folder) {
						String tempProject = project.getName();
						int n = object.toString().lastIndexOf(tempProject)
								+ tempProject.length();
						String packageName = object.toString().substring(n);
						packageList.add(packageName);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		inputDataList = getPackageAndFiles(fileNameList, packageList);
	}

	private ArrayList getPackageAndFiles(ArrayList fullFileList,
			ArrayList packageList) {

		ArrayList inXMLFileList = util.loadPropertiesXML();
		ArrayList filteredFileNameList = new ArrayList();
		ArrayList xmlList = QMPropertiesPage.xmlList;

		for (int i = 0; i < fullFileList.size(); i++) {
			if (xmlList.contains(fullFileList.get(i))) {
			} else {
				filteredFileNameList.add(fullFileList.get(i));
			}
		}

		ArrayList resultList = new ArrayList();
		ArrayList list = new ArrayList();

		for (int i = 0; i < packageList.size(); i++) {
			for (int j = 0; j < filteredFileNameList.size(); j++) {
				String file = "/" + filteredFileNameList.get(j).toString(); //$NON-NLS-1$
				String packageName = packageList.get(i).toString();
				if (file.contains(packageName)) {
					list.add(getTempList(file, packageName));
				}
			}
		}

		for (int num = 0; num < packageList.size(); num++) {
			String packageName = packageList.get(num).toString();

			Set folderSet = new HashSet();
			Map folderFileMap = new HashMap();

			ArrayList infoList = new ArrayList();
			PropertyAddInfo vo = new PropertyAddInfo();
			for (int i = 0; i < list.size(); i++) {
				ArrayList fileList = (ArrayList) list.get(i);
				if (packageName.equals(fileList.get(0))) {
					PropertyAddInfo info = new PropertyAddInfo();
					ArrayList folderList = new ArrayList();
					for (int j = 1; j < fileList.size() - 1; j++) {
						folderList.add(fileList.get(j));
					}
					folderSet.add(folderList);
					String file = fileList.get(fileList.size() - 1).toString();

					ArrayList temp = (ArrayList) folderFileMap.get(folderList);
					if (temp != null) {
						temp.add(file);
					} else {
						temp = new ArrayList();
						temp.add(file);
					}

					folderFileMap.put(folderList, temp);

					String folderName = ""; //$NON-NLS-1$
					for (int j = 0; j < folderList.size(); j++) {
						folderName = folderName + "/" + folderList.get(j); //$NON-NLS-1$
					}

					info.setType(MessagePropertiesLoader.property_dialog_add_typefolder);
					info.setName(folderName);
					info.setChild(temp);

					if (infoList.size() != 0) {
						Boolean boolSameExist = false;
						for (int j = 0; j < infoList.size(); j++) {
							PropertyAddInfo sameFolder = (PropertyAddInfo) infoList
									.get(j);
							if (sameFolder.getName().equals(folderName)) {
								boolSameExist = true;
							} else {
								boolSameExist = false;
							}
						}
						if (boolSameExist) {
						} else {
							infoList.add(info);
						}
					} else {
						infoList.add(info);
					}
				}
			}

			if (infoList.size() != 0) {
				vo.setType(MessagePropertiesLoader.property_dialog_add_typepackage);
				vo.setName(packageName);
				vo.setChild(infoList);
				resultList.add(vo);
			}

		}

		return resultList;
	}

	private ArrayList getTempList(String fileFullPath, String packageName) {
		ArrayList returnList = new ArrayList();

		int n = fileFullPath.indexOf(packageName) + packageName.length() + 1;
		String fileFolderPath = fileFullPath.substring(n);

		returnList.add(packageName);

		String sub = ""; //$NON-NLS-1$
		while (fileFolderPath.contains("/")) { //$NON-NLS-1$
			int num = fileFolderPath.indexOf("/"); //$NON-NLS-1$
			String folder = fileFolderPath.substring(0, num);
			sub = fileFolderPath.substring(num + 1);

			returnList.add(folder);

			fileFolderPath = sub;

		}
		returnList.add(sub);

		return returnList;
	}

	private Object splitFiles(String subPre, String subSuff) {
		HashMap resultMap = new HashMap();

		String folder = ""; //$NON-NLS-1$
		if (subSuff.contains("/")) { //$NON-NLS-1$
			int n = subSuff.indexOf("/"); //$NON-NLS-1$
			folder = subSuff.substring(0, n);
			String sub = subSuff.substring(n + 1);
			resultMap.put(folder, splitFiles(subPre, sub));
		} else {
			return subSuff;
		}
		return resultMap;
	}

	private void setOkPressed() {
		selectList = new ArrayList();

		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();

		if (selection != null) {
			Object[] result = selection.toArray();
			TreePath[] path = (TreePath[]) ((TreeSelection) selection)
					.getPaths();
			int selectCount = path.length;
			if (selectCount != 0) {
				for (int i = 0; i < selectCount; i++) {
					if (result[i]
							.toString()
							.toUpperCase()
							.endsWith(
									MessagePropertiesLoader.property_dialog_add_xmlfile_extention)) {

						TreePath treePath = path[i];

						String packageName = ""; //$NON-NLS-1$
						String folderName = ""; //$NON-NLS-1$
						String fileName = ""; //$NON-NLS-1$

						for (int j = 0; j < treePath.getSegmentCount() - 1; j++) {
							PropertyAddInfo vo = (PropertyAddInfo) treePath
									.getSegment(j);

							if (vo.getType()
									.equals(MessagePropertiesLoader.property_dialog_add_typepackage)) {
								packageName = vo.getName();
							} else if (vo
									.getType()
									.equals(MessagePropertiesLoader.property_dialog_add_typefolder)) {
								folderName = vo.getName();
							} else {
								fileName = vo.getName();
							}

						}
						selectList.add(packageName.substring(1) + folderName
								+ "/" + result[i]); //$NON-NLS-1$
					}
				}
			}
		}

		QMPropertiesPage.scanList = selectList;
	}

	@Override
	protected void okPressed() {
		setOkPressed();
		QMPropertiesPage.settingScanTable();
		close();
	}

	@Override
	protected void cancelPressed() {
		close();
	}

}
