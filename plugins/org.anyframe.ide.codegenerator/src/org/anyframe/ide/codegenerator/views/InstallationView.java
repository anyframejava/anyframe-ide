/*
 * Copyright 2007-2013 Samsung SDS Co., Ltd.
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
package org.anyframe.ide.codegenerator.views;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.CommandExecution;
import org.anyframe.ide.codegenerator.action.InstallViewAction;
import org.anyframe.ide.codegenerator.action.RefreshInstallViewAction;
import org.anyframe.ide.codegenerator.action.UninstallViewAction;
import org.anyframe.ide.codegenerator.action.UpdateListInstallViewAction;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.model.table.PluginInfoContentProvider;
import org.anyframe.ide.codegenerator.model.table.PluginInfoLabelProvider;
import org.anyframe.ide.codegenerator.model.table.PluginInfoList;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.common.util.ConfigXmlUtil;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.ProjectConfig;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;

/**
 * This is a InstallationView class.
 * 
 * @author junghwan.hong
 * @since 2.7.0
 * 
 */
public class InstallationView extends ViewPart {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID + ".views.InstallationView";

	private FilteredTree fFilteredTree;

	private Tree fTree;

	private TreeColumn fColumn1;
	private TreeColumn fColumn2;
	private TreeColumn fColumn3;
	private TreeColumn fColumn4;
	private TreeColumn fColumn5;
	private TreeColumn fColumn6;
	private TreeColumn fColumn7;

	private ProjectConfig projectConfig = null;

	private Action sampleDataAction;

	private IContributionItem selecProjectAction;

	private boolean isSampleCheck;

	private String selectedPjtName;

	private IProject selectedProject;

	private LinkedList<String> projectList;

	private int NAME_ORDER;

	private int GROUP_ORDER;

	private int ARTIFACT_ORDER;

	private int LATEST_ORDER;

	private int INSTALL_ORDER;

	private int DESC_ORDER;

	private LinkedMap projectMap;

	private IMenuManager mgr;

	private Action updateListaction;

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub

		// initial data(selected project name, projects ...)
		init();

		Composite composite = new Composite(parent, 0);
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(1808));

		createViewer(composite);
		// column sorting
		initializeViewerSorter();
		// action
		createActions();

		getSite().getShell().getDisplay().asyncExec(new Runnable() {

			public void run() {
				if (selectedPjtName == null)
					selectedPjtName = "";
				setContentDescription(Message.view_installation_description_prefix + " " + selectedPjtName + "\t"
						+ Message.view_installation_description_suffix);

			}

		});

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void createViewer(Composite parent) {
		PatternFilter filter = new PatternFilter() {

			protected boolean isLeafMatch(Viewer viewer, Object element) {
				if (element instanceof PluginInfo) {
					PluginInfo pluginEntry = (PluginInfo) element;
					String pluginName = pluginEntry.getName();
					return wordMatches(pluginName);
				} else {
					return false;
				}
			}

		};

		fFilteredTree = new FilteredTree(parent, 65536, filter, true);

		if (fFilteredTree.getFilterControl() != null) {
			Composite filterComposite = fFilteredTree.getFilterControl().getParent();
			GridData gd = (GridData) filterComposite.getLayoutData();
			gd.verticalIndent = 2;
			gd.horizontalIndent = 1;
		}
		//
		fFilteredTree.setBackground(parent.getDisplay().getSystemColor(25));
		fFilteredTree.setLayoutData(new GridData(1808));
		fFilteredTree.setInitialText(Message.view_ctip_typepluginname);

		fTree = fFilteredTree.getViewer().getTree();
		fTree.setLinesVisible(true);

		createColumns(fTree);
		// // fFilteredTree.getViewer().setAutoExpandLevel(2);
		fFilteredTree.getViewer().setContentProvider(new PluginInfoContentProvider());
		fFilteredTree.getViewer().setLabelProvider(new PluginInfoLabelProvider());
		fFilteredTree.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent e) {
				// TODO Auto-generated method stub
				ISelection selection = e.getSelection();
				PluginInfo pluginInfo = (PluginInfo) ((TreeSelection) selection).getFirstElement();
				String checked = "";
				if (pluginInfo != null) {
					checked = pluginInfo.isChecked() ? "false" : "true";
					pluginInfo.setChecked(checked);
				}

				fFilteredTree.getViewer().refresh();
			}
		});
		if (projectConfig != null) {
			PluginInfoList pluginInfoList = new PluginInfoList(projectConfig);
			fFilteredTree.getViewer().setInput(pluginInfoList);
		}
		fFilteredTree.redraw();
	}

	private void createColumns(Tree tree) {

		fColumn1 = new TreeColumn(tree, 16384);
		fColumn1.setText(Message.view_installation_fColumn1_name);
		fColumn1.setWidth(50);
		fColumn1.setAlignment(SWT.CENTER);

		fColumn2 = new TreeColumn(tree, 16384);
		fColumn2.setText(Message.view_installation_fColumn2_name);
		fColumn2.setWidth(100);
		fColumn2.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				NAME_ORDER = fTree.getSortDirection() == 1024 ? 1 : -1;
				ViewerComparator comparator = getViewerComparator((byte) 0);
				fFilteredTree.getViewer().setComparator(comparator);

				setColumnSorting(fColumn2, NAME_ORDER);
			}

		});

		fColumn3 = new TreeColumn(tree, 16384);
		fColumn3.setText(Message.view_installation_fColumn3_name);
		fColumn3.setWidth(140);
		fColumn3.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				GROUP_ORDER = fTree.getSortDirection() == 1024 ? 1 : -1;
				ViewerComparator comparator = getViewerComparator((byte) 1);
				fFilteredTree.getViewer().setComparator(comparator);

				setColumnSorting(fColumn3, GROUP_ORDER);
			}

		});

		fColumn4 = new TreeColumn(tree, 16384);
		fColumn4.setText(Message.view_installation_fColumn4_name);
		fColumn4.setWidth(160);
		fColumn4.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				ARTIFACT_ORDER = fTree.getSortDirection() == 1024 ? 1 : -1;
				ViewerComparator comparator = getViewerComparator((byte) 2);
				fFilteredTree.getViewer().setComparator(comparator);

				setColumnSorting(fColumn4, ARTIFACT_ORDER);
			}

		});

		fColumn5 = new TreeColumn(tree, 16384);
		fColumn5.setText(Message.view_installation_fColumn5_name);
		fColumn5.setWidth(100);
		fColumn5.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				LATEST_ORDER = fTree.getSortDirection() == 1024 ? 1 : -1;
				ViewerComparator comparator = getViewerComparator((byte) 3);
				fFilteredTree.getViewer().setComparator(comparator);

				setColumnSorting(fColumn5, LATEST_ORDER);
			}

		});

		fColumn6 = new TreeColumn(tree, 16384);
		fColumn6.setText(Message.view_installation_fColumn6_name);
		fColumn6.setWidth(100);
		fColumn6.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				INSTALL_ORDER = fTree.getSortDirection() == 1024 ? 1 : -1;
				ViewerComparator comparator = getViewerComparator((byte) 4);
				fFilteredTree.getViewer().setComparator(comparator);

				setColumnSorting(fColumn6, INSTALL_ORDER);
			}

		});

		fColumn7 = new TreeColumn(tree, 16384);
		fColumn7.setText(Message.view_installation_fColumn7_name);
		fColumn7.setWidth(400);

		fColumn7.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				DESC_ORDER = fTree.getSortDirection() == 1024 ? 1 : -1;
				ViewerComparator comparator = getViewerComparator((byte) 5);
				fFilteredTree.getViewer().setComparator(comparator);

				setColumnSorting(fColumn7, DESC_ORDER);
			}

		});

		tree.setHeaderVisible(true);
	}

	private void createActions() {
		IActionBars bars = getViewSite().getActionBars();

		// toolbar(tool bar menu)
		IToolBarManager toolBarManager = bars.getToolBarManager();
		// install
		toolBarManager.add(new InstallViewAction());
		// uninstall
		toolBarManager.add(new UninstallViewAction());
		toolBarManager.add(new Separator());
		// refresh
		toolBarManager.add(new RefreshInstallViewAction());
		// menu bar(pull down menu)
		mgr = bars.getMenuManager();
		// select a project
		selecProjectAction = createSelectProjectAction();
		mgr.add(selecProjectAction);
		// Separator
		mgr.add(new Separator());
		// update list
		updateListaction = new UpdateListInstallViewAction();
		updateListaction.setEnabled(checkOfflineMode());
		mgr.add(updateListaction);

		// Separator
		mgr.add(new Separator());
		// Install plugins with sample
		sampleDataAction = createSampleDataAction();
		mgr.add(sampleDataAction);
	}

	private IMenuManager createSelectProjectAction() {
		int i = 0;
		IMenuManager manager = new MenuManager(Message.view_installation_menu_selectproject);

		for (String project : projectList) {
			manager.add(new selectProjectAction(project, i));
			i++;
		}

		return manager;
	}

	class selectProjectAction extends Action {

		private String text;

		public void run() {
			// run coding
			if (!text.equals(selectedPjtName)) {
				// selectdPjtName, selectedProject 로직 추가
				selectedPjtName = text;
				selectedProject = (IProject) projectMap.get(selectedPjtName);

				refreshPlugin();
			}
		}

		public selectProjectAction(String text, int project) {
			super(text, 8);
			this.text = text;

			if (selectedPjtName.equals(text) && existProject())
				setChecked(true);
		}
	}

	private Action createSampleDataAction() {
		Action action = new Action(Message.view_installation_menu_sample) {

			public void run() {
				isSampleCheck = isChecked() ? true : false;
			}

		};
		action.setChecked(true);
		isSampleCheck = true;

		return action;
	}

	public void init() {
		// getProjectList
		getProjectList();
		// getSelectedProject
		getSelectedProject();
	}

	private void getProjectList() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] iprojectlist = workspace.getRoot().getProjects();
		projectMap = new LinkedMap();
		projectList = new LinkedList<String>();

		try {
			for (IProject project : iprojectlist) {
				if (ProjectUtil.isAnyframeProject(project) && project.isOpen()) {
					projectMap.put(project.getName(), project);
					projectList.add(project.getName());
				}
			}
		} catch (Exception exception) {
			PluginLoggerUtil.error(ID, Message.exception_log_getprojectlist, exception);
		}
	}

	@SuppressWarnings("restriction")
	private void getSelectedProject() {
		// get the current selected project in package explorer or project
		// explorer
		TreeSelection selection = null;

		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = window.getActivePage();

		// selectedProject value initialize
		if (!existProject()) {
			selectedPjtName = "";
			selectedProject = (IProject) projectMap.get(selectedPjtName);

			if (activePage != null) {
				IViewReference[] views = activePage.getViewReferences();

				for (IViewReference view : views) {
					if (!view.getId().equals(CodeGeneratorActivator.PLUGIN_ID + ".views.InstallationView")) {
						IViewPart viewpart = view.getView(true);

						if ("org.eclipse.jdt.ui.PackageExplorer".equals(view.getId()) && isActiveView(activePage, viewpart)) {
							selection = (TreeSelection) window.getSelectionService().getSelection("org.eclipse.jdt.ui.PackageExplorer");
							break;
						} else if ("org.eclipse.ui.navigator.ProjectExplorer".equals(view.getId()) && isActiveView(activePage, viewpart)) {
							selection = (TreeSelection) window.getSelectionService().getSelection("org.eclipse.ui.navigator.ProjectExplorer");
							break;

						}
					}
				}
			}

			if (selection != null && !selection.isEmpty()) {
				TreePath path = selection.getPaths()[0];
				IProject project = null;

				if (path.getFirstSegment() instanceof IProject)
					project = (IProject) path.getFirstSegment();
				else if (path.getFirstSegment() instanceof JavaProject) {
					project = ((JavaProject) path.getFirstSegment()).getProject();
				}

				if (ProjectUtil.isAnyframeProject(project)) {
					selectedPjtName = project.getName();
					selectedProject = project.getProject();
				} else {
					// default setting

					Iterator<String> it = projectMap.keySet().iterator();
					while (it.hasNext()) {
						selectedPjtName = it.next();
						selectedProject = (IProject) projectMap.get(selectedPjtName);
						break;
					}
				}
			} else {
				// default setting

				Iterator<String> it = projectMap.keySet().iterator();
				while (it.hasNext()) {
					selectedPjtName = it.next();
					selectedProject = (IProject) projectMap.get(selectedPjtName);
					break;
				}
			}
		}
		try {
			if (selectedProject != null) {
				String configFile = ConfigXmlUtil.getCommonConfigFile(selectedProject.getLocation().toOSString());
				projectConfig = ConfigXmlUtil.getProjectConfig(configFile);
			} else {
				projectConfig = null;
			}
		} catch (Exception exception) {
			PluginLoggerUtil.error(ID, Message.exception_log_iofile, exception);
		}
	}

	private boolean isActiveView(final IWorkbenchPage activePage, final IViewPart view) {
		// obtain active page from WorkbenchWindow
		final IWorkbenchPart activeView = activePage.getActivePart();
		return activeView == null ? false : activeView.equals(view);
	}

	private ViewerComparator getViewerComparator(byte sortType) {
		if (sortType == 0)
			return new ViewerComparator() {

				public int compare(Viewer viewer, Object e1, Object e2) {
					if ((e1 instanceof PluginInfo) && (e2 instanceof PluginInfo)) {
						PluginInfo entry1 = (PluginInfo) e1;
						PluginInfo entry2 = (PluginInfo) e2;

						return getComparator().compare(entry1.getName(), entry2.getName()) * NAME_ORDER;
					} else {
						return 0;
					}
				}

			};
		if (sortType == 1)
			return new ViewerComparator() {

				public int compare(Viewer viewer, Object e1, Object e2) {
					if ((e1 instanceof PluginInfo) && (e2 instanceof PluginInfo)) {
						PluginInfo entry1 = (PluginInfo) e1;
						PluginInfo entry2 = (PluginInfo) e2;

						return getComparator().compare(entry1.getGroupId(), entry2.getGroupId()) * GROUP_ORDER;
					} else {
						return 0;
					}
				}

			};

		if (sortType == 2)
			return new ViewerComparator() {

				public int compare(Viewer viewer, Object e1, Object e2) {
					if ((e1 instanceof PluginInfo) && (e2 instanceof PluginInfo)) {
						PluginInfo entry1 = (PluginInfo) e1;
						PluginInfo entry2 = (PluginInfo) e2;

						return getComparator().compare(entry1.getArtifactId(), entry2.getArtifactId()) * ARTIFACT_ORDER;
					} else {
						return 0;
					}
				}

			};
		if (sortType == 3)
			return new ViewerComparator() {

				public int compare(Viewer viewer, Object e1, Object e2) {
					if ((e1 instanceof PluginInfo) && (e2 instanceof PluginInfo)) {
						PluginInfo entry1 = (PluginInfo) e1;
						PluginInfo entry2 = (PluginInfo) e2;

						return getComparator().compare(entry1.getLatestVersion(), entry2.getLatestVersion()) * LATEST_ORDER;
					} else {
						return 0;
					}
				}

			};
		if (sortType == 4)
			return new ViewerComparator() {

				public int compare(Viewer viewer, Object e1, Object e2) {
					if ((e1 instanceof PluginInfo) && (e2 instanceof PluginInfo)) {
						PluginInfo entry1 = (PluginInfo) e1;
						PluginInfo entry2 = (PluginInfo) e2;

						return getComparator().compare(entry1.isInstalled() ? entry1.getVersion() : "X",
								entry2.isInstalled() ? entry2.getVersion() : "X")
								* INSTALL_ORDER;
					} else {
						return 0;
					}
				}

			};
		if (sortType == 5)
			return new ViewerComparator() {

				public int compare(Viewer viewer, Object e1, Object e2) {
					if ((e1 instanceof PluginInfo) && (e2 instanceof PluginInfo)) {
						PluginInfo entry1 = (PluginInfo) e1;
						PluginInfo entry2 = (PluginInfo) e2;

						return getComparator().compare(entry1.getDescription(), entry2.getDescription()) * DESC_ORDER;
					} else {
						return 0;
					}
				}

			};

		return null;
	}

	private void initializeViewerSorter() {
		byte orderType = 0;
		ViewerComparator comparator = getViewerComparator(orderType);
		fFilteredTree.getViewer().setComparator(comparator);
		if (orderType == 0) {
			setColumnSorting(fColumn2, NAME_ORDER);
			setColumnSorting(fColumn3, GROUP_ORDER);
			setColumnSorting(fColumn6, INSTALL_ORDER);
			setColumnSorting(fColumn7, DESC_ORDER);
		}

	}

	private void setColumnSorting(TreeColumn column, int order) {
		fTree.setSortColumn(column);
		fTree.setSortDirection(order != 1 ? 1024 : 128);
	}

	/**
	 * Anyframe plug in installation Action
	 */
	public void installPlugin() {
		PluginInfoList pluginInfoList = (PluginInfoList) fFilteredTree.getViewer().getInput();

		Map<String, PluginInfo> pList = pluginInfoList.getPluginInfoList();

		CommandExecution commandExecution = new CommandExecution();
		StringBuffer pluginNamesBuffer = new StringBuffer();
		Iterator<String> itr = pList.keySet().iterator();
		boolean hasInstalled = false;
		while (itr.hasNext()) {
			String pluginName = itr.next();
			PluginInfo info = pList.get(pluginName);

			if (info.isChecked()) {
				pluginNamesBuffer.append(pluginName + ",");
				if (info.isInstalled())
					hasInstalled = true;
			}
		}

		String pluginNames = pluginNamesBuffer.toString();
		if (!checkPluginNames(pluginNames))
			return;

		if (hasInstalled) {
			if (!MessageDialogUtil.questionMessageDialog(Message.ide_message_title, Message.view_dialog_plugintarget_install_info))
				return;
		} else {
			if (!MessageDialogUtil.questionMessageDialog(Message.ide_message_title, Message.view_dialog_plugin_confirm_install))
				return;
		}
		commandExecution.installPlugins(pluginNames, (Boolean) isSampleCheck, selectedProject.getLocation().toOSString());

		// refreshPlugin();
	}

	/**
	 * Anyframe plug in uninstallation Action
	 */
	public void uninstallPlugin() {
		PluginInfoList pluginInfoList = (PluginInfoList) (fFilteredTree.getViewer().getInput());

		Map<String, PluginInfo> pList = pluginInfoList.getPluginInfoList();

		CommandExecution commandExecution = new CommandExecution();
		StringBuffer pluginNamesBuffer = new StringBuffer();
		Iterator<String> itr = pList.keySet().iterator();
		while (itr.hasNext()) {
			String pluginName = itr.next();
			PluginInfo info = pList.get(pluginName);

			if (info.isChecked()) {
				pluginNamesBuffer.append(pluginName + ",");
			}
		}

		String pluginNames = pluginNamesBuffer.toString();
		if (!checkPluginNames(pluginNames))
			return;

		if (!MessageDialogUtil.questionMessageDialog(Message.ide_message_title, Message.view_dialog_plugin_confirm_uninstall))
			return;

		commandExecution.uninstallPlugins(pluginNames, (Boolean) isSampleCheck, selectedProject.getLocation().toOSString());
	}

	/**
	 * Anyframe plug in uninstallation Action
	 */
	public void refreshPlugin() {

		try {
			// refresh menu action list start

			getProjectList();
			getSelectedProject();

			mgr.removeAll();

			selecProjectAction = createSelectProjectAction();

			mgr.add(selecProjectAction);

			// Separator
			mgr.add(new Separator());
			// update list
			updateListaction = new UpdateListInstallViewAction();
			mgr.add(updateListaction);

			// Separator
			mgr.add(new Separator());
			// Install plugins with sample
			sampleDataAction = createSampleDataAction();
			mgr.add(sampleDataAction);

			// refresh menu action list end
			for (String pjtName : projectList) {
				if (pjtName.equals(selectedProject.getName())) {
					String configFile = ConfigXmlUtil.getCommonConfigFile(selectedProject.getLocation().toOSString());
					projectConfig = ConfigXmlUtil.getProjectConfig(configFile);
				}
				// update list activate check
				updateListaction.setEnabled(checkOfflineMode());
			}

		} catch (Exception exception) {
			PluginLoggerUtil.error(ID, Message.exception_log_refresh, exception);
		}
		// title bar refresh
		getSite().getShell().getDisplay().asyncExec(new Runnable() {

			public void run() {
				if (selectedPjtName == null)
					selectedPjtName = "";
				setContentDescription(Message.view_installation_description_prefix + " " + selectedPjtName + "\t"
						+ Message.view_installation_description_suffix);

			}

		});

		fFilteredTree.setInitialText(Message.view_ctip_typepluginname);

		if (projectConfig != null) {
			PluginInfoList pluginInfoList = new PluginInfoList(projectConfig);
			fFilteredTree.getViewer().setInput(pluginInfoList);
		} else {
			fFilteredTree.getViewer().setInput(null);
		}
		fFilteredTree.getViewer().refresh(true);
	}

	/**
	 * Anyframe plug in uninstallation Action
	 */
	public void updateCatalogList() {
		CommandExecution genExecution = new CommandExecution();
		genExecution.updateCatalog(selectedProject.getLocation().toOSString());
	}

	private boolean checkPluginNames(String pluginNames) {
		boolean checkResult = true;
		if (StringUtils.isEmpty(pluginNames)) {

			MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_dialog_plugin_check, MessageDialog.WARNING);
			checkResult = false;
		}
		return checkResult;
	}

	public boolean checkOfflineMode() {
		// check offline
		boolean enabled = false;
		if (projectConfig != null) {
			if (Boolean.valueOf(projectConfig.getOffline())) {
				enabled = true;
			}
		}
		return enabled;
	}

	private boolean existProject() {
		try {
			String configFile = ConfigXmlUtil.getCommonConfigFile(selectedProject.getLocation().toOSString());
			
			if (new File(configFile).exists())
				return true;
		} catch (Exception e) {
			return false;
		}

		return false;
	}
}
