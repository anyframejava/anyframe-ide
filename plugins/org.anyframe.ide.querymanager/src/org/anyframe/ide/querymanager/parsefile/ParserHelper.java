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
package org.anyframe.ide.querymanager.parsefile;

import java.util.ArrayList;
import java.util.Collection;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * This is ParserHelper class.
 * 
 * @author Surindhar.Kondoor
 * @author viswa.srikant
 */
public class ParserHelper {

	// private static final Log LOGGER =
	// LogFactory.getLog(ParserHelper.class);
	private static ParserHelper parserHelper = new ParserHelper();

	public static ParserHelper getInstance() {
		return parserHelper;
	}

	/**
	 * Helper method to collect all the Files exist in a project. This helper
	 * method uses eclipse api to collect all the file information.
	 * 
	 * @param project
	 *            The <code>IProject</code> instance for which files information
	 *            needs to be fetched.
	 * @param path
	 *            The root path of the project.
	 * @return Collection of all the file information exists in the project.
	 */
	public Collection getAllFileInfo(IProject project, IPath path) {
		Collection files = new ArrayList();
		IJavaProject jproj = JavaCore.create(project);
		if (jproj == null)
			return files;

		try {
			IPackageFragmentRoot[] pfr = jproj.getPackageFragmentRoots();
			for (int i = 0; i < pfr.length; i++) {
				if (pfr[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
					files.addAll(allFiles(pfr[i].getResource().getLocation()));
				}
			}
		} catch (CoreException e) {
			// QueryManagerPlugin.showErrorLog(e);
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Exception occurred while parsing file.", e);
		}
		return files;
	}

	private Collection allFiles(IPath path) throws CoreException {
		Collection files = new ArrayList();

		IFileStore store = EFS.getStore(URIUtil.toURI(path));

		IFileInfo[] finfos = store.childInfos(
				IResource.FILE | IResource.FOLDER, null);
		for (int i = 0; i < finfos.length; i++) {
			IPath fpath = path.append(finfos[i].getName());
			if (finfos[i].isDirectory()) {
				files.addAll(allFiles(fpath));
			} else {
				files.add(fpath.toString());
			}
		}
		return files;
	}

	/**
	 * This is a utility method for getting all the output folders of a project.
	 * 
	 * @param project
	 *            the IProject object
	 * @return Collection of all the output folders in a string representation.
	 */
	private Collection getAllOutputFolders(IProject project) {
		ArrayList outputPaths = new ArrayList();
		IJavaProject javaProject = JavaCore.create(project);
		String outputFolder = null;
		try {
			outputFolder = javaProject.getOutputLocation().toString();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		int start = outputFolder.indexOf(project.getName());
		int projLen = project.getName().length();
		outputFolder = outputFolder.substring(start + projLen + 1);
		outputPaths.add(project.getLocation().append(outputFolder));
		try {
			IClasspathEntry[] resources = javaProject.getRawClasspath();
			for (int i = 0; i < resources.length; i++) {
				IClasspathEntry obj = resources[i];
				// start from here
				if (obj.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					// this is a source folder...
					// sourceFolders.add(obj.getPath());
					// if the output location for this
					// source folder is not null
					// it means output folder is
					// configured.
					if (obj.getOutputLocation() != null) {
						outputFolder = obj.getOutputLocation().toString();
						start = outputFolder.indexOf(project.getName());
						// projLen =
						// project.getName().length();
						outputFolder = outputFolder.substring(start + projLen
								+ 1);

						outputPaths.add(project.getLocation().append(
								outputFolder));
						// LOGGER.debug("obj.getOutputLocation() :::::: "
						// + obj.getOutputLocation());
						// LOGGER
						// .debug("obj.getOutputLocation().removeLastSegments(1) :::::: "
						// + obj.getOutputLocation().removeLastSegments(1));
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return outputPaths;
	}

	/**
	 * This utility method will check whether a filePath is an output folder or
	 * not.
	 * 
	 * @param project
	 *            the IProject object
	 * @param filePath
	 *            the path of the folder or file
	 * @param outputFolders
	 *            all output folders of this project.
	 * @return true if it is a output folder , false otherwise.
	 */
	private boolean isOutputFolder(IProject project, IPath filePath,
			ArrayList outputFolders) {
		IPath newFilePath = filePath;
		for (int i = 0; i < filePath.segmentCount(); i++) {

			if (outputFolders.contains(newFilePath)) {
				return true;
			}
			newFilePath = newFilePath.removeLastSegments(1);
		}
		return false;
	}
}
