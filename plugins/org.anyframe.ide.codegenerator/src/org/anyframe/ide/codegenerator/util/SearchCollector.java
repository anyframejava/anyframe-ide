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
package org.anyframe.ide.codegenerator.util;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ImportDeclaration;
import org.eclipse.jdt.internal.core.ResolvedSourceType;

/**
 * This is an SearchCollector class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
class SearchCollector extends SearchRequestor {
	private ArrayList<String> selectedClassList = new ArrayList<String>();

	public ArrayList<String> getSelectedClassList() {
		return selectedClassList;
	}

	public void setSelectedClassList(ArrayList<String> selectedClassList) {
		this.selectedClassList = selectedClassList;
	}

	private ArrayList<String> packageList;

	public ArrayList<String> getPackageList() {
		return packageList;
	}

	public void setPackageList(ArrayList<String> packageList) {
		this.packageList = packageList;
	}

	private ArrayList<ResolvedSourceType> foundList;

	public ArrayList<ResolvedSourceType> getFoundList() {
		return foundList;
	}

	public void setFoundList(ArrayList<ResolvedSourceType> foundList) {
		this.foundList = foundList;
	}

	public SearchCollector(ArrayList<ResolvedSourceType> list,
			ArrayList<String> packagelist) {
		this.foundList = list;
		this.packageList = packagelist;
	}

	@SuppressWarnings("restriction")
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		Object enclosingElement = match.getElement();

		// Import type
		if (enclosingElement instanceof ImportDeclaration
				&& packageList.size() > 0) {
			ImportDeclaration declaration = (ImportDeclaration) enclosingElement;
			String packageName = declaration.getNameWithoutStar();
			if (packageList.contains(packageName)) {
				selectedClassList.add(declaration.getParent().getParent()
						.getHandleIdentifier());
			}
			return;
		}

		// Source Type
		if (enclosingElement instanceof ResolvedSourceType) {
			ResolvedSourceType sourceType = (ResolvedSourceType) enclosingElement;

			if (foundList.contains(sourceType))
				return;
			if (packageList.size() == 0
					|| selectedClassList.contains(sourceType.getParent()
							.getHandleIdentifier())) {
				foundList.add(sourceType);
			}
		}

	}
}
