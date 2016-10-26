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
package org.anyframe.ide.querymanager;

import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.views.QMExplorerView;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * This is QueryManagerActivator class.
 * 
 * @author Junghwan Hong
 */
public class QueryManagerActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.anyframe.ide.querymanager";

	private QMExplorerView qmExplorerView;

	private static QueryManagerActivator queryManagerActivator;

	// private static final Log QMLogger = LogFactory
	// .getLog(QueryManagerPlugin.class);

	public QueryManagerActivator() {
		queryManagerActivator = this;
	}

	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		queryManagerActivator = null;
		super.stop(bundleContext);
	}

	public static QueryManagerActivator getDefault() {
		return queryManagerActivator;
	}

	/**
	 * Utility method to get the workspace reference.
	 * 
	 * @return IWorkspace object
	 */
	public IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public IWorkbenchPage getActiveWorkbenchPage() {
		IWorkbenchPage activePage = QueryManagerActivator.getDefault()
				.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		return activePage;
	}

	public QMExplorerView getQMExplorerView() {
		return getQMExplorerView(true);
	}

	public QMExplorerView getQMExplorerView(boolean create) {
		if (qmExplorerView == null) {
			IWorkbenchPage page = getActivePage();
			if (page != null) {
				qmExplorerView = (QMExplorerView) page.findView(PLUGIN_ID
						+ ".QMExplorerView");
				if (qmExplorerView == null && create) {
					try {
						qmExplorerView = (QMExplorerView) page
								.showView(PLUGIN_ID + ".QMExplorerView");
					} catch (PartInitException partInitException) {
						partInitException.printStackTrace();
					}
				}
			}
		}

		return qmExplorerView;
	}

	public String getAddQuertyTitleName() {
		String title = Message.editor_querymanager_message_addquerytitle_prefix
				+ "0"
				+ Message.editor_querymanager_message_addquerytitle_suffix;
		IWorkbenchPage Page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		IEditorReference[] refEditors = Page.getEditorReferences();

		int maxNum = 0;
		int queryPage = 0;
		if (refEditors.length != 0) {
			for (IEditorReference refEditor : refEditors) {
				if (refEditor
						.getTitle()
						.startsWith(
								Message.editor_querymanager_message_addquerytitle_prefix)) {
					int titleNum = getTitleNum(refEditor.getTitle());
					if (maxNum < titleNum || maxNum == titleNum)
						maxNum = titleNum;
					queryPage++;
				}
			}
			if (queryPage == 0)
				title = Message.editor_querymanager_message_addquerytitle_prefix
						+ String.valueOf(maxNum)
						+ Message.editor_querymanager_message_addquerytitle_suffix;
			else
				title = Message.editor_querymanager_message_addquerytitle_prefix
						+ String.valueOf(maxNum + 1)
						+ Message.editor_querymanager_message_addquerytitle_suffix;

			return title;
		} else {
			return title;
		}
	}

	private int getTitleNum(String titlename) {
		int index = titlename.indexOf("[");
		return Integer.valueOf(titlename.substring(index + 1, index + 2));
	}

	private IWorkbenchPage getActivePage() {
		if (getWorkbench() != null
				&& getWorkbench().getActiveWorkbenchWindow() != null)
			return getWorkbench().getActiveWorkbenchWindow().getActivePage();
		return null;
	}
}
