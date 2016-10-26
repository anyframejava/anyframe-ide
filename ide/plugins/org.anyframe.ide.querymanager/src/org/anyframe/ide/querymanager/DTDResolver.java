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

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * This is DTDResolver class.
 * 
 * @author Junghwan Hong
 */
public class DTDResolver implements EntityResolver {
	// file:/D:/eclipse_3.5/
	// String home = System.getProperty("eclipse.home.location");
	String home = Platform.getInstallLocation().getURL().getPath();
	String plug = "plugins/";
	String dtd = "!/dtds/anyframe-core-query-mapping-3.2.dtd";

	public InputSource resolveEntity(String publicId, String systemId) {
		String home_ext = home;
		String jar = "";

		String full = "";

		File f = new File(home_ext + plug);
		File files[] = f.listFiles();
		for (File file : files)
			if (file.getName().startsWith("org.anyframe.ide.querymanager")) {
				jar = file.getName();
				break;
			}
		full = "jar:file:" + home + plug + jar + dtd;

		if (publicId.equals("-//ANYFRAME//DTD QUERYSERVICE//EN")
				|| systemId
						.equals("http://www.anyframejava.org/dtd/anyframe-core-query-mapping-3.2.dtd")) {
			InputSource xmlInputSource = new InputSource(full);
			return xmlInputSource;
		} else {
			// use the default behaviour
			return null;
		}
	}

	/**
	 * @return replace source(pattern) with replace
	 */
	private String replaceIgnoreCase(String source, String pattern,
			String replace) {
		int sIndex = 0;
		int eIndex = 0;
		String sourceTemp = null;
		StringBuffer result = new StringBuffer();
		sourceTemp = source.toUpperCase();
		while ((eIndex = sourceTemp.indexOf(pattern.toUpperCase(), sIndex)) >= 0) {
			result.append(source.substring(sIndex, eIndex));
			result.append(replace);
			sIndex = eIndex + pattern.length();
		}
		result.append(source.substring(sIndex));
		return result.toString();
	}

}
