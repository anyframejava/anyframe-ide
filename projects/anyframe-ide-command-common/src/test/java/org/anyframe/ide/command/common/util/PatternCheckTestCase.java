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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * TestCase Name : PatternCheckTestCase <br>
 * <br>
 * [Description] : Test for pattern handling<br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Nagative Case : get latest archetype version</li>
 * </ul>
 */
public class PatternCheckTestCase extends TestCase {
	
	/**
	 * [Flow #-1] Nagative Case : get latest archetype version
	 * 
	 * @throws Exception
	 */
	public void testPattern() throws Exception {
		String pluginName = "ibatis2";

		Pattern pattern = Pattern.compile("(?i)(?u)(?s)\\A(?:(?:\\<\\!\\-\\-"
				+ pluginName
				+ "\\-)).*(?i)(?u)(?s).*(?:(?:\\-START\\-\\-\\>))\\z");

		String aLine = "<!--iBatis2-getMovieList-START-->";
		Matcher matcher = pattern.matcher(aLine);

		assertTrue("fail to match with ignoring case.", matcher.find());

		pattern = Pattern.compile("(?s)\\A(?:(?:\\<\\!\\-\\-" + pluginName
				+ "\\-)).*(?i)(?u)(?s).*(?:(?:\\-START\\-\\-\\>))\\z");

		matcher = pattern.matcher(aLine);

		assertFalse("fail to match with ignoring case.", matcher.find());
	}
}
