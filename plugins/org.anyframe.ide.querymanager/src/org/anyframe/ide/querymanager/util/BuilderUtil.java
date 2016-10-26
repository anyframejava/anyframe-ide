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
package org.anyframe.ide.querymanager.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.build.AnyframeNature;
import org.anyframe.ide.querymanager.build.AnyframeProjectBuilder;
import org.anyframe.ide.querymanager.messages.Message;
import org.anyframe.ide.querymanager.views.QueryExplorerHelper;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

/**
 * This is BuilderUtil class.
 * 
 * @author Surindhar.Kondoor
 */
public class BuilderUtil {
	// private static final Log LOGGER =
	// LogFactory.getLog(BuilderUtil.class);
	private static HashMap qsMethodMap = null;

	public static HashMap getQsMethodMap() {
		if (qsMethodMap != null)
			return qsMethodMap;
		qsMethodMap = new HashMap();
		qsMethodMap.put("findWithRowCount", "findWithRowCount");
		qsMethodMap.put("findWithColInfo", "findWithColInfo");
		// qsMethodMap.put("findBySQLWithRowCount", "findBySQLWithRowCount");
		// qsMethodMap.put("createBySQL", "createBySQL");
		// qsMethodMap.put("updateBySQL", "updateBySQL");
		// qsMethodMap.put("removeBySQL", "removeBySQL");
		// qsMethodMap.put("executeBySQL", "executeBySQL");
		qsMethodMap.put("batchUpdate", "batchUpdate");
		// qsMethodMap.put("batchUpdateBySQL", "batchUpdateBySQL");
		qsMethodMap.put("getStatement", "getStatement");
		qsMethodMap.put("getQueryParams", "getQueryParams");
		qsMethodMap.put("find", "find");
		qsMethodMap.put("remove", "remove");
		qsMethodMap.put("create", "create");
		qsMethodMap.put("update", "update");
		qsMethodMap.put("execute", "execute");
		qsMethodMap.put("findByPk","findByPk");
		qsMethodMap.put("findList","findList");
		qsMethodMap.put("findListWithPaging","findListWithPaging");
		
		return qsMethodMap;
	}

	public static HashMap appendVarToMethods(HashMap methodMap,
			String appendString) {
		if (methodMap == null || appendString == null)
			return methodMap;
		HashMap newMap = new HashMap();
		Iterator itr = methodMap.keySet().iterator();
		while (itr.hasNext()) {
			String meth = methodMap.get(itr.next()).toString();
			meth = appendString.trim() + meth.trim();
			newMap.put(meth, meth);
		}
		return newMap;
	}

	/**
	 * This helper method reads a file content.
	 * 
	 * @param file
	 *            The file for which the content should be read.
	 * @return File content as a string representation.
	 */
	public static String readFile(IFile file) {
		if (!file.exists())
			return "";

		InputStream stream = null;
		StringBuffer result = new StringBuffer(4096);
		try {
			stream = file.getContents();

			// FileInputStream fis = new FileInputStream(file.getLocation().toFile());

			// Reader reader = new BufferedReader(new InputStreamReader(stream));
			Reader reader = new BufferedReader(new InputStreamReader(stream,
					file.getCharset()));

			// Reader reader = new BufferedReader(new InputStreamReader(fis));

			char[] buf = new char[2048];
			while (true) {
				int count = reader.read(buf);
				if (count < 0)
					break;

				result.append(buf, 0, count);

				buf = new char[2048];
			}
			return result.toString();
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					Message.exception_problemreadfile + file.getName(), e);
		} finally {
			try {
				result = null;
				stream = null;
				if (stream != null)
					stream.close();
			} catch (Exception e) {
				PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
						Message.exception_problemclosingstream + file.getName(), e);
			}
		}
		return null;
	}

	/**
	 * if import .... and IQuerySerivce declaration is commented, this will not
	 * be work well. if IQueryService is declared several times(local or
	 * global), this isn't work well
	 */
	public static String getVarName(String content,
			String importClassFullyQualName, String className) {
		final String IQS = className;
		try {
			// valid chk: IQueryService package should be imported
			int id1 = getEndOfQSImporStatementIdx(content,
					importClassFullyQualName);
			if (id1 == -1)
				return null;

			// find IQueryService
			int id2 = content.indexOf(IQS, id1);
			if (id2 == -1)
				return null;

			// valid chk: before that, there're whitespaces
			if (!Character.isWhitespace(content.charAt(id2 - 1)))
				return null;

			// valid chk: after that, there're whitespaces
			if (!Character.isWhitespace(content.charAt(id2 + IQS.length())))
				return null;

			// get variable string
			String var = content.substring(id2 + IQS.length() + 1,
					content.indexOf(';', id2));

			// trim =
			int id3 = var.indexOf('=');
			if (id3 != -1)
				var = var.substring(0, id3);

			// trim whitespace
			return var.trim();
		} catch (IndexOutOfBoundsException e) {
		}
		return null;
	}

	/**
	 * if import .... and IQuerySerivce definition is commented, this will not
	 * be work well.
	 */
	private static int getEndOfQSImporStatementIdx(String content,
			String importClassFullyQualName) {
		final String importedPackage = importClassFullyQualName;
		int id = content.indexOf(importedPackage);
		return id == -1 ? id : id + importedPackage.length();
	}

	public boolean runJobToAddBuilder(final IProject anyframeProject) {
		Job addBilderJob = new Job(Message.util_addingbuildertoproject
				+ anyframeProject.getName()) {
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(
						Message.util_checkbuilderalreadyadded, 1000);
				monitor.subTask(Message.util_checkbuilderalreadyadded);
				final ArrayList treeQueryList = new QueryExplorerHelper()
						.collectTreeViewQueries(monitor);
				if (monitor.isCanceled()) {
					monitor.done();
				}
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						addAnyFrameBuilder(anyframeProject);
					}
				});
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		addBilderJob.setSystem(false);
		addBilderJob.setUser(true);
		addBilderJob.schedule();
		return true;
	}

	public boolean addAnyFrameBuilder(IProject anyframeProject) {
		if (!anyframeProject.isOpen())
			return false;
		// return if Anyframe nature is added.
		try {
			if (anyframeProject.getNature(AnyframeNature.NATURERID) != null)
				return false;
		} catch (CoreException e1) {
		}
		// if nature is not there check for the builder now.
		IProjectDescription desc = null;
		try {
			desc = anyframeProject.getDescription();
		} catch (CoreException e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					Message.exception_couldnotaddanyframebuilder
							+ anyframeProject.getName() + e.getMessage(), e);
		}
		if (desc == null)
			return false;
		ICommand[] commands = desc.getBuildSpec();
		if (!checkForAnyFrameBuilder(commands)) {
			ArrayList commandList = new ArrayList();
			// ICommand anyFrameCommand = null;
			for (int i = 0; i < commands.length; i++) {
				commandList.add(commands[i]);
				// anyFrameCommand = commands[i];
			}
			ICommand anyFrameCommand = desc.newCommand();
			anyFrameCommand.setBuilderName(AnyframeProjectBuilder.BUILDERID);
			commandList.add(anyFrameCommand);

			Object[] objArr = commandList.toArray();
			ICommand buildCommand[] = new ICommand[objArr.length];
			System.arraycopy(objArr, 0, buildCommand, 0, objArr.length);

			desc.setBuildSpec(buildCommand);
			try {
				anyframeProject.setDescription(desc, new NullProgressMonitor());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	private boolean checkForAnyFrameBuilder(ICommand[] commands) {
		for (int i = 0; i < commands.length; i++) {
			if (commands[i].getBuilderName().equalsIgnoreCase(
					AnyframeProjectBuilder.BUILDERID))
				return true;
		}
		return false;
	}
}
