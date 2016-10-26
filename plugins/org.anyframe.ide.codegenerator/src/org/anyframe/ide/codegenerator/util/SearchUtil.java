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
package org.anyframe.ide.codegenerator.util;

import java.util.ArrayList;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.ResolvedSourceType;

/**
 * This is a SearchUtil class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public abstract class SearchUtil {
	public static final String ID = CodeGeneratorActivator.PLUGIN_ID;

	protected SearchUtil() {
		throw new UnsupportedOperationException(); // prevents
		// calls
		// from
		// subclass
	}

	public static ArrayList<ResolvedSourceType> search(String stringPattern,
			ArrayList<String> listAnnotations, IProject currentProject) {

		ArrayList<ResolvedSourceType> listClassFile = new ArrayList<ResolvedSourceType>();

		try {
			IJavaProject javaProject = JavaCore.create(currentProject);

			SearchRequestor requestor = new SearchCollector(listClassFile,
					listAnnotations);

			SearchPattern pattern = SearchPattern.createPattern(stringPattern,
					IJavaSearchConstants.CLASS,
					IJavaSearchConstants.ALL_OCCURRENCES,
					SearchPattern.R_PATTERN_MATCH);

			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
					new IJavaElement[] { javaProject },
					IJavaSearchScope.SOURCES);

			SearchParticipant[] participants = new SearchParticipant[] { SearchEngine
					.getDefaultSearchParticipant() };

			SearchEngine searchEngine = new SearchEngine();

			searchEngine.search(pattern, participants, scope, requestor, null);
		} catch (CoreException e) {
			PluginLoggerUtil
					.error(ID, Message.view_exception_loaddomaincrud, e);
		}

		return listClassFile;
	}

	@SuppressWarnings("restriction")
	public static String getSourcePath(IProject currentProject) {

		ArrayList<String> pathList = new ArrayList<String>();

		IProject project = currentProject;
		if (project.isOpen() && JavaProject.hasJavaNature(project)) {
			IJavaProject javaProject = JavaCore.create(project);

			IClasspathEntry[] classpathEntries = null;
			try {
				classpathEntries = javaProject.getResolvedClasspath(true);
			} catch (JavaModelException e) {
				PluginLoggerUtil.error(ID,
						Message.view_exception_getsourcepath, e);
			}

			for (int i = 0; i < classpathEntries.length; i++) {
				IClasspathEntry entry = classpathEntries[i];
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					String relativePath = entry.getPath().toString();
					String javadir = ResourcesPlugin.getWorkspace().getRoot()
							.findMember(relativePath).getLocation().toString();
					pathList.add(javadir);
				}
			}
		}

		String[] paths = (String[]) pathList.toArray(new String[0]);
		if (paths.length == 0)
			return null;

		return paths[0];
	}

}
