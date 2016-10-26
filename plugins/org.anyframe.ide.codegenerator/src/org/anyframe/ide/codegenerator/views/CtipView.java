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
package org.anyframe.ide.codegenerator.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.action.CtipViewActionGroup;
import org.anyframe.ide.codegenerator.action.RefreshCtipViewAction;
import org.anyframe.ide.codegenerator.action.ServerConfigureCtipViewAction;
import org.anyframe.ide.codegenerator.config.AnyframeConfig;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.model.table.CtipDetailList;
import org.anyframe.ide.codegenerator.model.table.CtipInfoContentProvider;
import org.anyframe.ide.codegenerator.model.table.CtipInfoLabelProvider;
import org.anyframe.ide.codegenerator.model.table.CtipServerList;
import org.anyframe.ide.codegenerator.popups.CtipAddJobPopup;
import org.anyframe.ide.codegenerator.popups.CtipConfigurationPopup;
import org.anyframe.ide.codegenerator.popups.CtipManageUrlPopup;
import org.anyframe.ide.codegenerator.util.HudsonRemoteAPI;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.PropertiesIO;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.apache.commons.collections.map.LinkedMap;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
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
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * This is a CtipView class.
 * 
 * @author junghwan.hong
 * @since 2.7.0
 * 
 */
public class CtipView extends ViewPart {

	public static final String ID = CodeGeneratorActivator.PLUGIN_ID
			+ ".views.CtipView";

	private Composite parent;

	private HudsonRemoteAPI hudson = new HudsonRemoteAPI();

	private FilteredTree fFilteredTree;

	private Tree fTree;

	private TreeColumn fColumn1;
	private TreeColumn fColumn2;
	private TreeColumn fColumn3;
	private TreeColumn fColumn4;
	private TreeColumn fColumn5;
	private TreeColumn fColumn6;
	private TreeColumn fColumn7;

	private LinkedMap projectMap;

	private LinkedList<String> projectList;

	private String selectedPjtName;

	private IProject selectedProject;

	private IContributionItem selecProjectAction;

	private AnyframeConfig anyframeConfig;

	private CtipServerList ctipInfoList;

	private ArrayList<String> jobNameList = new ArrayList<String>();

	private Cursor waitCursor;

	private String projectBuild;

	private PropertiesIO appProps;

	private IMenuManager mgr;

	public PropertiesIO getAppProps() {
		return appProps;
	}

	public String getProjectBuild() {
		return projectBuild;
	}

	public AnyframeConfig getAnyframeConfig() {
		return anyframeConfig;
	}

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		// initial data(selected project name, projects ...)
		this.parent = parent;
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

		// action
		createActions();

		getSite().getShell().getDisplay().asyncExec(new Runnable() {

			public void run() {
				if (selectedPjtName == null)
					selectedPjtName = "";
				setContentDescription(Message.view_ctip_description_prefix
						+ " " + selectedPjtName + "\t"
						+ Message.view_ctip_description_suffix);
			}

		});

		// tree context menu
		addContextMenu();
	}

	private void createViewer(Composite parent) {
		PatternFilter filter = new PatternFilter() {

			protected boolean isLeafMatch(Viewer viewer, Object element) {
				if (element instanceof CtipDetailList) {
					CtipDetailList entry = (CtipDetailList) element;
					String jobName = entry.getJobName();
					return wordMatches(jobName);
				} else {
					return false;
				}
			}

		};

		fFilteredTree = new FilteredTree(parent, 65536, filter, true);

		if (fFilteredTree.getFilterControl() != null) {
			Composite filterComposite = fFilteredTree.getFilterControl()
					.getParent();
			GridData gd = (GridData) filterComposite.getLayoutData();
			gd.verticalIndent = 2;
			gd.horizontalIndent = 1;
		}
		//
		fFilteredTree.setBackground(parent.getDisplay().getSystemColor(25));
		fFilteredTree.setLayoutData(new GridData(1808));
		fFilteredTree.setInitialText("type filter text of job name");

		fTree = fFilteredTree.getViewer().getTree();
		fTree.setLinesVisible(true);

		createColumns(fTree);

		fFilteredTree.getViewer().setAutoExpandLevel(2);

		fFilteredTree.getViewer().setContentProvider(
				new CtipInfoContentProvider());
		fFilteredTree.getViewer().setLabelProvider(new CtipInfoLabelProvider());

		if (selectedProject != null) {
			ctipInfoList = new CtipServerList(selectedProject);

			fFilteredTree.getViewer().setInput(ctipInfoList);
		}
		fFilteredTree.getViewer().refresh();

		Object[] objs = fFilteredTree.getViewer().getExpandedElements();
		int i = 0;
		for (Object obj : objs) {
			if (!(i == 0))
				fFilteredTree.getViewer().setExpandedState(obj, false);
			else
				fFilteredTree.getViewer().setExpandedState(obj, true);
			i++;
		}

		// double click action (tree expends)
		fFilteredTree.getViewer().addDoubleClickListener(
				new IDoubleClickListener() {

					public void doubleClick(DoubleClickEvent arg0) {
						// TODO Auto-generated method stub
						ISelection selection = arg0.getSelection();
						Object obj = ((TreeSelection) selection)
								.getFirstElement();

						TreeViewer treeViewer = (TreeViewer) arg0.getViewer();
						if (treeViewer.getExpandedState(obj)) {
							treeViewer.setExpandedState(obj, false);
						} else {
							treeViewer.setExpandedState(obj, true);
						}
					}
				});

		if (ctipInfoList != null)
			anyframeConfig = ctipInfoList.getAnyframeConfig();
	}

	private void createColumns(Tree tree) {
		fColumn1 = new TreeColumn(tree, 16384);
		fColumn1.setText(Message.view_ctip_fColumn1_name);
		fColumn1.setWidth(260);

		fColumn2 = new TreeColumn(tree, 16384);
		fColumn2.setText(Message.view_ctip_fColumn2_name);
		fColumn2.setWidth(70);

		fColumn3 = new TreeColumn(tree, 16384);
		fColumn3.setText(Message.view_ctip_fColumn3_name);
		fColumn3.setWidth(250);

		fColumn4 = new TreeColumn(tree, 16384);
		fColumn4.setText(Message.view_ctip_fColumn4_name);
		fColumn4.setWidth(140);

		fColumn5 = new TreeColumn(tree, 16384);
		fColumn5.setText(Message.view_ctip_fColumn5_name);
		fColumn5.setWidth(150);

		fColumn6 = new TreeColumn(tree, 16384);
		fColumn6.setText(Message.view_ctip_fColumn6_name);
		fColumn6.setWidth(130);

		fColumn7 = new TreeColumn(tree, 16384);
		fColumn7.setText(Message.view_ctip_fColumn7_name);
		fColumn7.setWidth(130);

		tree.setHeaderVisible(true);
	}

	private void addContextMenu() {
		final CtipViewActionGroup actionGroup = new CtipViewActionGroup();
		MenuManager menuManager = new MenuManager("CtipContextMenu");
		menuManager.setRemoveAllWhenShown(true);

		Menu contextMenu = menuManager.createContextMenu(fFilteredTree
				.getViewer().getTree());
		fFilteredTree.getViewer().getTree().setMenu(contextMenu);

		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				actionGroup.fillContextMenu(manager);
			}
		});

		Tree tree = fFilteredTree.getViewer().getTree();
		Menu menu = menuManager.createContextMenu(tree);
		tree.setMenu(menu);
		getSite().registerContextMenu(menuManager, fFilteredTree.getViewer());
	}

	private void createActions() {
		IActionBars bars = getViewSite().getActionBars();

		// toolbar(tool bar menu)
		IToolBarManager toolBarManager = bars.getToolBarManager();
		// server configure
		toolBarManager.add(new ServerConfigureCtipViewAction());
		// refresh
		toolBarManager.add(new RefreshCtipViewAction());
		// menu bar(pull down menu)
		mgr = bars.getMenuManager();
		// select a project
		selecProjectAction = createSelectProjectAction();
		mgr.add(selecProjectAction);
	}

	private IMenuManager createSelectProjectAction() {
		int i = 0;
		IMenuManager manager = new MenuManager(
				Message.view_ctip_menu_selectproject);

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

			if (selectedPjtName.equals(text))
				setChecked(true);
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void init() {
		// getProjectList
		getProjectList();
		// getSelectedProject
		getSelectedProject();
		// build type check
		loadBuildPropertiesFile();
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
			PluginLoggerUtil.error(ID, Message.exception_log_getprojectlist,
					exception);
		}
	}

	@SuppressWarnings("restriction")
	private void getSelectedProject() {
		// get the current selected project in package explorer or project
		// explorer
		TreeSelection selection = null;

		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = window.getActivePage();

		// selectedProject value initialize
		if(! existProject()) {
			selectedPjtName = "";
			selectedProject = (IProject) projectMap.get(selectedPjtName);
	
			if (activePage != null) {
				IViewReference[] views = activePage.getViewReferences();
	
				for (IViewReference view : views) {
					if (!view.getId().equals(
							CodeGeneratorActivator.PLUGIN_ID + ".views.CtipView")) {
						IViewPart viewpart = view.getView(true);
	
						if ("org.eclipse.jdt.ui.PackageExplorer".equals(view
								.getId())
								&& isActiveView(activePage, viewpart)) {
							selection = (TreeSelection) window
									.getSelectionService().getSelection(
											"org.eclipse.jdt.ui.PackageExplorer");
							break;
						} else if ("org.eclipse.ui.navigator.ProjectExplorer"
								.equals(view.getId())
								&& isActiveView(activePage, viewpart)) {
							selection = (TreeSelection) window
									.getSelectionService()
									.getSelection(
											"org.eclipse.ui.navigator.ProjectExplorer");
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
						selectedProject = (IProject) projectMap
								.get(selectedPjtName);
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
	}

	private void loadBuildPropertiesFile() {
		try {
			appProps = ProjectUtil.getProjectProperties(selectedProject);
			projectBuild = appProps
					.readValue(CommonConstants.PROJECT_BUILD_TYPE);
		} catch (Exception e) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.wizard_error_properties, MessageDialog.WARNING);
		}
	}

	private boolean isActiveView(final IWorkbenchPage activePage,
			final IViewPart view) {
		// obtain active page from WorkbenchWindow
		final IWorkbenchPart activeView = activePage.getActivePart();
		return activeView == null ? false : activeView.equals(view);
	}

	public void openCtipManageUrlPopup() {
		CtipManageUrlPopup addUrl = new CtipManageUrlPopup(Display.getCurrent()
				.getActiveShell(), this);
		addUrl.open();
	}

	public void openCtipAddJobPopup(String ctipUrl, List<String> jobList) {
		CtipAddJobPopup addUrl = new CtipAddJobPopup(Display.getCurrent()
				.getActiveShell(), this, jobList, ctipUrl);
		addUrl.open();
	}

	public void openCtipModifyJobPopup(CtipDetailList detailList,
			String ctipUrl, List<String> jobList) {
		CtipAddJobPopup modifyUrl = new CtipAddJobPopup(Display.getCurrent()
				.getActiveShell(), this, jobList, detailList, ctipUrl);
		modifyUrl.open();
	}

	public void openCtipConfigurationPopup(String ctipUrl) {
		CtipConfigurationPopup ctipConfig = new CtipConfigurationPopup(Display
				.getCurrent().getActiveShell(), this, ctipUrl);
		ctipConfig.open();
	}

	public void addCtipServerToUrl(String name, String url) {

		List<String> urlList = anyframeConfig.getCtipUrlList();
		urlList.add(name + " - " + url);

		anyframeConfig.setCtipUrlList(urlList);
		ctipInfoList.saveOnlyAnyframeConfigFile();

		fFilteredTree.getViewer().setInput(ctipInfoList);

	}

	public void updateCtipServerToUrl(String name, String newUrl, String oldKey) {
		List<String> urlList = anyframeConfig.getCtipUrlList();
		int index = urlList.indexOf(oldKey);
		urlList.set(index, name + " - " + newUrl);
		anyframeConfig.setCtipUrlList(urlList);
		ctipInfoList.saveOnlyAnyframeConfigFile();

		fFilteredTree.getViewer().setInput(ctipInfoList);
	}

	public void removeCtipServerToUrl(String name, String url) {
		List<String> urlList = anyframeConfig.getCtipUrlList();
		urlList.remove(name + " - " + url);
		anyframeConfig.setCtipUrlList(urlList);
		ctipInfoList.saveOnlyAnyframeConfigFile();

		fFilteredTree.getViewer().setInput(ctipInfoList);
	}

	public boolean getJobListFromHudson(String hudsonUrl) {

		hudsonUrl = hudsonUrl.substring(hudsonUrl.toLowerCase().indexOf(
				"http://"));
		hudson.setHudsonURL(hudsonUrl);

		try {
			jobNameList.clear();
			List<Element> jobList = hudson.getJobList();

			int count = 0;
			for (int i = 0; i < jobList.size(); i++) {
				Element elem = jobList.get(i);
				jobNameList.add(elem.getChildText("name"));
				count++;
			}
			return true;

		} catch (Exception e) {
			return false;
		}
	}

	public Object getSelectedId() {
		IStructuredSelection selection = (IStructuredSelection) fFilteredTree
				.getViewer().getSelection();

		if (selection == null)
			return null;

		return selection;
	}

	public TreeItem getSelectedItem() {

		TreeItem selection = (TreeItem) fFilteredTree.getViewer()
				.getSelection();
		if (selection == null)
			return null;

		return selection;
	}

	public TreeViewer getTreeviewer() {
		return fFilteredTree.getViewer();
	}

	public void runJob(String ctipUrl) {
		String jobName = "";
		TreeSelection selection = (TreeSelection) getSelectedId();

		if (selection.size() == 1) {
			Object select = selection.getFirstElement();
			if (select instanceof CtipDetailList) {
				CtipDetailList detail = (CtipDetailList) select;
				jobName = detail.getJobName();
			}
		}
		if (!MessageDialogUtil.questionMessageDialog(Message.ide_message_title,
				Message.view_ctip_run_warn_confirm + " \"" + jobName + "\"?")) {
			return;
		}

		Cursor oldCursor = parent.getCursor();
		try {
			reqeustBuildAndWaitForFirstResponse(jobName, ctipUrl);

		} catch (Exception ex) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_run_warn_fail, MessageDialog.WARNING);
		}
		parent.setCursor(oldCursor);
	}

	private void reqeustBuildAndWaitForFirstResponse(String jobName,
			String ctipUrl) throws JDOMException, IOException, Exception {
		parent.setCursor(waitCursor);
		hudson.setHudsonURL(ctipUrl);
		Element detail = hudson.getJobDetail(jobName);

		String lastBuildNum = "";
		if (detail.getChild("lastBuild") != null) {
			lastBuildNum = detail.getChild("lastBuild").getChildText("number");
		}
		try {
			hudson.executeBuild(jobName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			detail = hudson.getJobDetail(jobName);
			// // if new build started exit while
			// // loop.(Wait until new build starts.)
			if (detail.getChild("lastBuild") != null
					&& !detail.getChild("lastBuild").getChildText("number")
							.equals(lastBuildNum)) {
				TreeItem[] treeItems = fFilteredTree.getViewer().getTree()
						.getSelection();
				for (TreeItem item : treeItems) {
					item.setText("building");
				}
				break;
			}
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
	}

	public void deleteJob(String ctipUrl) {
		String jobName = "";

		TreeSelection selection = (TreeSelection) getSelectedId();

		if (selection.size() == 1) {
			Object select = selection.getFirstElement();
			if (select instanceof CtipDetailList) {
				CtipDetailList detail = (CtipDetailList) select;
				jobName = detail.getJobName();
			}
		}

		if (!MessageDialogUtil
				.questionMessageDialog(Message.ide_message_title,
						Message.view_ctip_remove_warn_confirm + " \"" + jobName
								+ "\"?")) {
			return;
		}

		try {
			hudson.setHudsonURL(ctipUrl);
			hudson.deleteJob(jobName);
		} catch (Exception ex) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_remove_warn, MessageDialog.WARNING);
		}
		refreshPlugin();
	}

	/**
	 * Anyframe plug in uninstallation Action
	 */
	public void refreshPlugin() {

		try {
			// refresh project list start
			getProjectList();
			getSelectedProject();

			mgr.removeAll();

			selecProjectAction = createSelectProjectAction();
			mgr.add(selecProjectAction);
			// refresh project list end

			if (selectedProject != null) {
				ctipInfoList = new CtipServerList(selectedProject);

				fFilteredTree.getViewer().setInput(ctipInfoList);
				fFilteredTree.getViewer().refresh();

				anyframeConfig = ctipInfoList.getAnyframeConfig();

				loadBuildPropertiesFile();
			} else {
				fFilteredTree.getViewer().setInput(null);
				fFilteredTree.getViewer().refresh();
			}

		} catch (Exception exception) {
			PluginLoggerUtil
					.error(ID, Message.exception_log_refresh, exception);
		}

		// title bar refresh
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (selectedPjtName == null)
					selectedPjtName = "";
				setContentDescription(Message.view_ctip_description_prefix
						+ " " + selectedPjtName + "\t"
						+ Message.view_ctip_description_suffix);

			}

		});

		fFilteredTree.setInitialText("type filter text of job name");
		fFilteredTree.getViewer().refresh(true);
	}

	private boolean existProject() {
		try {
			PropertiesIO check = ProjectUtil
					.getProjectProperties(selectedProject);
			if (check != null)
				return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}

		return false;
	}
}
