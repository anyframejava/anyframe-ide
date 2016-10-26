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
package org.anyframe.ide.command.common.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a ValidationUtil class. This class is a utility for validating
 * values.
 * 
 * @author SooYeon Park
 */
public class ValidationUtil {

	/**
	 * Check if input path is absolute path.
	 * 
	 * @param loc
	 *            input path
	 * @return if input path is absolute path, return <code>true</code>, if not,
	 *         return <code>false</code>.
	 */
	public static boolean isAbsolutePath(String loc) {
		File path = new File(loc);
		return path.isAbsolute();
	}

	/**
	 * Check if input path is existing.
	 * 
	 * @param loc
	 *            input path
	 * @return if input path is existing, return <code>true</code>, if not,
	 *         return <code>false</code>.
	 */
	public static boolean isExistingPath(String loc) {
		File path = new File(loc);
		return path.exists();
	}

	/**
	 * Check if input name is valid.
	 * 
	 * @param name
	 *            input name
	 * @return if input name has special characters, return <code>false</code>,
	 *         if not, return <code>true</code>.
	 */
	public static boolean isValidName(String name) {
		Pattern p = Pattern
				.compile("^[a-zA-Z_0-9-]*+(\\.[a-zA-Z_0-9-][a-zA-Z_0-9-]*)*$");
		Matcher m = p.matcher(name);
		if (m.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * Check if package name is valid.
	 * 
	 * @param packageName
	 *            package name
	 * @return if package name has special characters besides '.', return
	 *         <code>false</code>, if not, return <code>true</code>.
	 */
	public static boolean isValidPackageName(String packageName) {
		Pattern p = Pattern
				.compile("^[a-zA-Z_]+[a-zA-Z_0-9]*+(\\.[a-zA-Z_][a-zA-Z_0-9]*)*$");
		Matcher m = p.matcher(packageName);
		if (m.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * Check if plugin name is valid.
	 * 
	 * @param pluginName
	 *            plugin name
	 * @return if plugin name has special characters besides '.','-', return
	 *         <code>false</code>, if not, return <code>true</code>.
	 */
	public static boolean isValidPluginName(String pluginName) {
		Pattern p = Pattern
				.compile("^[a-zA-Z_]+[a-zA-Z_0-9]*+(\\.[a-zA-Z_][a-zA-Z_0-9]*)*+(\\-[a-zA-Z_][a-zA-Z_0-9]*)*+(\\.[a-zA-Z_][a-zA-Z_0-9]*)*$");
		Matcher m = p.matcher(pluginName);
		if (m.matches()) {
			return true;
		}
		return false;
	}
}
