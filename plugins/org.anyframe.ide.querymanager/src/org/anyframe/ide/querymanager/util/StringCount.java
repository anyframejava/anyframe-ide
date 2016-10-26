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
package org.anyframe.ide.querymanager.util;

/**
 * The class StringCount counts number of occurance of given string in query
 * statement
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class StringCount {

	private int count;

	private String source = "";

	/**
	 * Constructor with parameter source
	 * 
	 * @param source
	 *            source string
	 */
	public StringCount(String source) {
		this.source = source;
	}

	/**
	 * @param s
	 *            string
	 * @return integer
	 */
	public int stringCount(String s) {
		return stringCount(s, 0);
	}

	/**
	 * @param s
	 *            string to be searched in source
	 * @param pos
	 *            position
	 * @return int
	 */
	public int stringCount(String s, int pos) {
		int index = 0;
		if (s == null || s.length() == 0)
			return 0;
		if ((index = source.indexOf(s, pos)) != -1) {
			count++;
			stringCount(s, index + s.length());
		}
		return count;
	}
}
