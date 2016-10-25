/*   
 * Copyright 2008-2011 the original author or authors.   
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
package org.anyframe.ide.command.ant.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;

/**
 * This is an PackageUtil class.
 * 
 * @author Sooyeon Park
 */
public class PackageUtil {

	/**
	 * Returns a collection of all Classes inside the given package and
	 * subpackages. Note that this method relies on the classes living in class
	 * files (not jars) in a single directory.
	 * 
	 * @param packageName
	 *            Fully qualified name of the package; eg "com.example.foo"
	 * @return a collection of all Classes inside the given package and
	 *         subpackages.
	 * @throws FileNotFoundException
	 *             if the package name is invalid.
	 */
	public Collection<Object> getClassesInPackage(String packageName,
			File directory) throws FileNotFoundException {
		Collection<Object> packages = new HashSet<Object>();
		// Find all class files in this directory and
		// sub-directories.
		getClassesRecursively(packageName, directory, packages);
		return packages;
	}

	/**
	 * Updates the given class list with class files in the given file-system
	 * directory and sub directories.
	 * 
	 * @param packageName
	 *            The package name corresponding to the given directory.
	 * @param directory
	 *            The directory to extract class files from.
	 * @param packageList
	 *            The list of packages found. This is appended to recursively.
	 */
	private void getClassesRecursively(String packageName, File directory,
			Collection<Object> packageList) {
		// Loop over the contents of this directory:
		File[] childFiles = directory.listFiles();
		for (int i = 0; i < childFiles.length; i++) {
			File childFile = childFiles[i];
			if (childFile.isFile()) {
				String fileName = childFile.getName();
				// We only want the .class files.
				if (fileName.endsWith(CLASS_FILE_EXTENSION)) {
					packageList.add(packageName);
				}
			} else if (childFile.isDirectory()) {
				// Recurse into this subdirectory
				if (packageName.length() > 0)
					getClassesRecursively(packageName + '.'
							+ childFile.getName(), childFile, packageList);
				else
					getClassesRecursively(childFile.getName(), childFile,
							packageList);
			}
		}
	}

	private static final String CLASS_FILE_EXTENSION = ".java";

}
