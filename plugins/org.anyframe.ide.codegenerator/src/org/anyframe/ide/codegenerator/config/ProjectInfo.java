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
package org.anyframe.ide.codegenerator.config;

/**
 * This is an ProjectInfo class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class ProjectInfo {

	protected ProjectInfo() {
		throw new UnsupportedOperationException(); // prevents calls from
													// subclass
	}

	private static String location;
	private static String name;

	public static String getLocation() {
		return location;
	}

	public static void setLocation(String location) {
		ProjectInfo.location = location;
	}

	public static String getName() {
		return name;
	}

	public static void setName(String name) {
		ProjectInfo.name = name;
	}
}