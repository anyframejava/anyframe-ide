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
package org.anyframe.ide.querymanager.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.VersionUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.actions.LinkEditorAction;
import org.anyframe.ide.querymanager.actions.OpenQueryXMLEditorAction;
import org.anyframe.ide.querymanager.actions.QMExplorerViewActionGroup;
import org.anyframe.ide.querymanager.actions.RefreshAction;
import org.anyframe.ide.querymanager.build.BuilderHelper;
import org.anyframe.ide.querymanager.build.Location;
import org.anyframe.ide.querymanager.build.QMMarkerHelper;
import org.anyframe.ide.querymanager.model.FileInfoVO;
import org.anyframe.ide.querymanager.model.QueryExplorerViewContentProvider;
import org.anyframe.ide.querymanager.model.QueryExplorerViewLabelProvider;
import org.anyframe.ide.querymanager.model.QueryTreeVO;
import org.anyframe.ide.querymanager.properties.QMPropertiesXMLUtil;
import org.anyframe.ide.querymanager.util.BuilderUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

/**
 * The class implements Query Manager Explorer view where users can manage Query
 * Manpping XML files and query ids each project. Users can query id CRUD with
 * this view.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class QMExplorerView extends ViewPart implements
		ISelectionChangedListener {

	public static final String ID = QueryManagerActivator.PLUGIN_ID
			+ ".QMExplorerView";

	protected PatternFilter patternFilter;
	protected FilteredTree filter;

	public static TreeViewer viewer;
	private StyledText displayQueryTextArea;
	private SashForm sashForm;
	private Composite idTreeComposite;

	private Set daoSet = new HashSet();
	private HashMap daoMap = new HashMap();
	private HashMap duplicateIds = new HashMap();
	private HashMap duplAliasIdsMap = new HashMap();
	private static HashMap usedQueryIDsLocationMap = new HashMap();

	public Set getDaoSet() {
		return daoSet;
	}

	public HashMap getDaoMap() {
		return daoMap;
	}

	public HashMap getDuplicateIds() {
		return duplicateIds;
	}

	public HashMap getDuplAliasIdsMap() {
		return duplAliasIdsMap;
	}

	public static HashMap getUsedQueryIDsLocationMap() {
		return usedQueryIDsLocationMap;
	}

	public QMExplorerView() {
	}

	class NameSorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return super.compare(viewer, e1, e2);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		idTreeComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();

		idTreeComposite.setLayout(layout);
		GridData tableData = new GridData(GridData.FILL_BOTH);
		tableData.horizontalSpan = 1;
		idTreeComposite.setLayoutData(tableData);

		sashForm = new SashForm(idTreeComposite, SWT.VERTICAL);
		sashForm.setLayout(layout);
		sashForm.setLayoutData(tableData);

		GridData viewerData = new GridData(GridData.FILL_BOTH);
		viewerData.horizontalSpan = 1;

//		new VersionUtil();
		String platformVer = VersionUtil.getPlatformVersion();
		String productVer = VersionUtil.getProductVersion();

		patternFilter = new PatternFilter();

		if (platformVer.substring(0, 3).equals("3.5") //$NON-NLS-1$
				|| productVer.substring(0, 3).equals("3.5")) { //$NON-NLS-1$
			filter = new FilteredTree(sashForm, SWT.H_SCROLL | SWT.V_SCROLL
					| SWT.BORDER | SWT.MULTI, patternFilter, true);
		} else {
			filter = new FilteredTree(sashForm, SWT.H_SCROLL | SWT.V_SCROLL
					| SWT.BORDER | SWT.MULTI, patternFilter);
		}

		filter.getPatternFilter().setIncludeLeadingWildcard(true);
		viewer = filter.getViewer();

		viewer.setContentProvider(new QueryExplorerViewContentProvider());
		viewer.setLabelProvider(new QueryExplorerViewLabelProvider());
		viewer.setSorter(new NameSorter());
		new DrillDownAdapter(viewer);
		viewer.getTree().setLayoutData(viewerData);
		viewer.addSelectionChangedListener(this);
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent arg0) {
				ISelection selection = arg0.getSelection();

				Object ob = null;
				Object obj = ((TreeSelection) selection).getFirstElement();
				if (obj instanceof QueryTreeVO) {
					TreeViewer treeViewer = (TreeViewer) arg0.getViewer();
					if (treeViewer.getExpandedState(obj)) {
						treeViewer.setExpandedState(obj, false);
					} else {
						treeViewer.setExpandedState(obj, true);
					}
				} else if (obj instanceof FileInfoVO) {
					new OpenQueryXMLEditorAction().run();
				} else {
					ob = ((FileInfoVO) ((TreeSelection) selection).getPaths()[0]
							.getSegment(1)).getQueryId().get(obj);
				}
				if (selection != null) {
				}
				if (ob != null && ob instanceof Location) {
					Location location = (Location) ob;
					setReParsingOnlyFile(location);
				}
			}

		});

		displayQueryTextArea = new StyledText(sashForm, SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		GridData displayTextData = new GridData();
		displayTextData.horizontalSpan = 2;
		displayQueryTextArea.setEditable(false);
		displayQueryTextArea.setLayoutData(displayTextData);
		displayQueryTextArea.setText("");

		addContextMenu();
		contributeToActionBars();

		jobGetIds();

		parent.layout(true);
	}

	private void setReParsingOnlyFile(Location location) {
		IFile file = location.getFile();

		if (file != null) {
			String content = BuilderUtil.readFile(file);

			String findQueryID = location.getKey();
			String fullQueryID = "\"" + findQueryID + "\"";

			int idLocation = content.indexOf(fullQueryID);

			int idEndLocation = -1;
			if (idLocation < 0) {
			} else {
				idEndLocation = idLocation + fullQueryID.length();

				location.setCharStart(idLocation + 1);
				location.setCharEnd(idEndLocation - 1);

				QueryExplorerHelper.openQueryInEditorFromTreeViewer(location);
			}
		}
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(new LinkEditorAction());
		manager.add(new Separator());
		manager.add(new RefreshAction());
	}

	@Override
	public void setFocus() {
	}

	private void jobGetIds() {

		Job treeViewJob = new Job("Searching Queries") {

			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask("Searching the queries for Tree View", 100);
				monitor.subTask("Getting the Queries....");

				monitor.worked(25);
				QueryExplorerHelper helper = new QueryExplorerHelper();

				final ArrayList treeQueryList = helper
						.collectTreeViewQueriesFromProperties(monitor);
				monitor.worked(25);

				daoSet = helper.getDaoSet();
				daoMap = helper.getDaoMap();
				duplicateIds = helper.getDuplicateIds();
				duplAliasIdsMap = helper.getDuplAliasIdsMap();
				usedQueryIDsLocationMap = helper.getUsedQueryIDsLocationMap();

				monitor.worked(25);

				if (monitor.isCanceled()) {
					monitor.done();
				}

				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

					public void run() {
						viewer.setInput(treeQueryList);
						setMarker();
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

	private void jobGetIds_with_link() {

		Job treeViewJob = new Job("Searching Queries") {

			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask("Searching the queries for Tree View", 100);
				monitor.subTask("Getting the Queries....");

				monitor.worked(25);
				QueryExplorerHelper helper = new QueryExplorerHelper();

				final ArrayList treeQueryList = helper
						.collectTreeViewQueriesFromProperties(monitor);
				monitor.worked(25);

				daoSet = helper.getDaoSet();
				daoMap = helper.getDaoMap();
				duplicateIds = helper.getDuplicateIds();
				duplAliasIdsMap = helper.getDuplAliasIdsMap();
				usedQueryIDsLocationMap = helper.getUsedQueryIDsLocationMap();

				monitor.worked(25);

				if (monitor.isCanceled()) {
					monitor.done();
				}

				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

					public void run() {
						viewer.setInput(treeQueryList);
						setMarker();
						new LinkEditorAction().run();
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

	private void clearMarkers(IProject project) {
		try {
			project.deleteMarkers(QMMarkerHelper.SEARCH_UNUSED_MARKER_ID,
					false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Error while removing existing markers for project "
							+ project.getName() + "  ", e);
		}
	}

	public void refresh_with_link() {
		jobGetIds_with_link();
	}

	public void refresh() {
		jobGetIds();
	}

	private void setMarker() {
		try {
			BuilderHelper builder = BuilderHelper.getInstance();
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
					.getProjects();
			for (int i = 0; i < projects.length; i++) {
				if (projects[i].isOpen()
						&& projects[i]
								.hasNature("org.anyframe.ide.querymanager.build.AnyframeNature")) {
					IProject anyFrameProject = projects[i];
					clearMarkers(anyFrameProject);
					QMPropertiesXMLUtil util = new QMPropertiesXMLUtil();
					if (util.getPropertiesDupl2(anyFrameProject)) {
						builder.collectAllQueryIdsForBuilder(anyFrameProject);
						HashMap duplicateIds = new HashMap();
						if (builder.duplicateIds.containsKey(anyFrameProject
								.getName())) {
							duplicateIds = (HashMap) builder.duplicateIds
									.get(anyFrameProject.getName());
						}
						if (duplicateIds != null && duplicateIds.size() > 0) {
							Iterator duplicatesItr = duplicateIds.keySet()
									.iterator();
							while (duplicatesItr.hasNext()) {
								String queryId = (String) duplicatesItr.next();
								Collection col = (Collection) duplicateIds
										.get(queryId);
								if (col != null && col.size() > 0) {
									Iterator colItr = col.iterator();
									while (colItr.hasNext()) {
										Location loc = (Location) colItr.next();
										if (!builder
												.markAProblem(
														"Repeatition of Query Id",
														(Location) loc,
														1,
														true,
														QMMarkerHelper.BUILDER_ERROR_MARKER_ID)) {
											colItr.remove();
										}
									}
								}
							}
						}
						builder.markInvalidClassNames(anyFrameProject);
					} else {
					}

				} else {
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	public void selectionChanged(SelectionChangedEvent event) {

		ISelection selection = event.getSelection();

		Object ob = null;
		Object obj = ((TreeSelection) selection).getFirstElement();

		if (obj != null) {
			if (obj instanceof QueryTreeVO) {
				displayQueryTextArea.setText("");
			} else if (obj instanceof FileInfoVO) {
				displayQueryTextArea.setText("");
			} else {
				ob = ((FileInfoVO) ((TreeSelection) selection).getPaths()[0]
						.getSegment(1)).getQueryId().get(obj);
			}
			if (selection != null) {
			}
			if (ob != null && ob instanceof Location) {
				Location location = (Location) ob;
				displayQueryTextArea.setText(location.getTotalQuery());
			}
		} else {
			displayQueryTextArea.setText("");
		}
	}

	public static IEditorPart openEditor(IFile file) {
		// Open editor on new file.
		IEditorPart editorPart = null;
		String editorId = "org.anyframe.editor.xml.MultiPageEditor";

		IWorkbenchWindow dw = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (dw != null) {
			IWorkbenchPage page = dw.getActivePage();
			if (page != null) {
				try {
					editorPart = page.openEditor(new FileEditorInput(file),
							editorId, true);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		}

		return editorPart;
	}

	public static IMarker createMarker(IFile file, int charStart, int charEnd,
			String type, String msg) {
		IMarker marker = null;
		try {
			marker = file.createMarker(type);

			// marker.setAttribute(IMarker.MESSAGE, msg);
			marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			marker.setAttribute(IMarker.CHAR_START, charStart);
			marker.setAttribute(IMarker.CHAR_END, charEnd);
			// marker.setAttribute(IMarker.SEVERITY, IMarker.BOOKMARK);
		} catch (CoreException e) {
		}
		return marker;
	}

	public static Object getSelected() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection == null)
			return null;
		Object result = selection.getFirstElement();
		return result;
	}

	public static Object getSelectedId() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection == null)
			return null;
		return selection;
	}

	private void addContextMenu() {
		final QMExplorerViewActionGroup actionGroup = new QMExplorerViewActionGroup();
		MenuManager menuManager = new MenuManager("QueryManagerContextMenu");
		menuManager.setRemoveAllWhenShown(true);
		Menu contextMenu = menuManager.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(contextMenu);

		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				actionGroup.fillContextMenu(manager);
			}
		});

		Tree tree = viewer.getTree();
		Menu menu = menuManager.createContextMenu(tree);
		tree.setMenu(menu);
		getSite().registerContextMenu(menuManager, viewer);
	}

}
