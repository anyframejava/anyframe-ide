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
package org.anyframe.ide.command.cli.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is a XmlUtil class.
 * 
 * @see org.anyframe.ide.command.common.util.ConfigXmlUtil
 * 
 * @author Sujeong Lee
 */
public class XmlUtil {

	private static String projectHome;
	private static String projectName;
	private static String packageName;

	public static String getProjectHome() {
		return projectHome;
	}

	public static String getProjectName() {
		return projectName;
	}

	public static String getPackageName() {
		return packageName;
	}

	public static String getCommonConfigFile(String baseDir) throws Exception {
		String configFile = "";

		String commonConfig = baseDir + Messages.fileSeparator + Messages.PREFS_FILE;
		String defaultConfigFile = baseDir + Messages.fileSeparator + Messages.SETTING_HOME + Messages.fileSeparator
				+ Messages.COMMON_CONFIG_XML_FILE;
		File file = new File(commonConfig);
		if (!file.exists()) {
			// file not found - default path
			configFile = defaultConfigFile;
		} else {
			InputStream is = new FileInputStream(file);
		    Properties props = new Properties();
		    props.load(is);
		    
			configFile = String.valueOf(props.get(Messages.COMMON_CONFIG_PREFS_KEY));
			if ("".equals(configFile)) {
				configFile = defaultConfigFile;
			} else {
				configFile += Messages.fileSeparator + Messages.COMMON_CONFIG_XML_FILE;
			}

		}

		return configFile;
	}

	public static void getProjectConfig(String configFile) throws Exception {
		Document document;
		File file = new File(configFile);
		if (file.exists()) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(file);

			Element root = document.getDocumentElement();

			projectName = getText(root, Messages.PROJECT_NAME);
			packageName = getText(root, Messages.PACKAGE_NAME);
			projectHome = getText(root, Messages.PROJECT_HOME);
		} else {
			throw new Exception("Can not find a '" + configFile + "' file. Please check a location of your project.");
		}
	}

	private static String getText(Element root, String constants) {
		String result = "";
		try {
			result = nullToString(root.getElementsByTagName(constants).item(0).getTextContent());
		} catch (Exception e) {
		}
		return result;
	}

	private static String nullToString(String str) {
		if (str == null || str.trim().length() == 0) {
			return "";
		} else {
			return str.trim();
		}
	}

}
