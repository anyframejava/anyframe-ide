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
package org.anyframe.ide.common.properties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.messages.Message;
import org.anyframe.ide.common.util.ConfigXmlUtil;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.MessageUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.ProjectConfig;
import org.anyframe.ide.common.util.PropertyUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This is PropertiesSettingUtil class.
 * 
 * @author Sujeong Lee
 */
public class PropertiesSettingUtil {

	public static String TEMPLATE_LOC = "";
	public static String JDBCDRIVERS_LOC = "";
	public static String DATABASES_LOC = "";

	// Common Setting XML File
	private static String commonXml = "";
	private static String commonXmlLoc = "";

	// parsing - init
	public static void parsing(String pjtLoc) {
		getPrefs(pjtLoc);

		File file = new File(commonXml);
		try {
			if (file.exists()) {
				ProjectConfig projectConfig = ConfigXmlUtil.getProjectConfig(commonXml);

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();

				TEMPLATE_LOC = projectConfig.getTemplateHomePath();
				JDBCDRIVERS_LOC = projectConfig.getJdbcdriverPath();
				DATABASES_LOC = projectConfig.getDatabasesPath();
			} else {

				MessageUtil.showConfirmMessage("", Message.exception_load_config_xml);
				throw new RuntimeException(Message.exception_load_config_xml);
				
//				boolean isSuccessCreateAndSave = create(pjtLoc);
//				if (isSuccessCreateAndSave==false) { //파일을 생성하고 저장이 잘 되지 않았다면
//					PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_create_config_xml, new Exception());
//					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.exception_create_config_xml, MessageDialog.ERROR);
//				} else { // 성공했는데 왜 재귀호출하냐
//					parsing(pjtLoc);
//				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_load_config_xml, e);
		}
	}

	// save
	public static boolean save(String pjtLoc, ProjectConfig vo) {
		boolean result = true;

		getPrefs(pjtLoc);

		try {
			ConfigXmlUtil.saveProjectConfig(vo);
		} catch (Exception e) {
			result = false;
		}

		return result;
	}

	public static String getTemplateLoc(String pjtLoc) {
		getPrefs(pjtLoc);

		Document document;
		File file = new File(commonXml);
		try {
			if (file.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);

				Element root = document.getDocumentElement();
				NodeList template = root.getElementsByTagName(Constants.XML_TAG_TEMPLATE);

				for (int i = 0; i < template.getLength(); i++) {
					Element node = (Element) template.item(i);
					TEMPLATE_LOC = node.getTextContent();
				}
			} else {
				if (!create(pjtLoc)) {
					PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_create_config_xml, new Exception());
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.exception_create_config_xml, MessageDialog.ERROR);
				} else {
					TEMPLATE_LOC = getTemplateLoc(pjtLoc);
				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_load_config_xml, e);
		}
		return TEMPLATE_LOC;
	}

	public static String getJdbcdriversFile(String pjtLoc) {
		getPrefs(pjtLoc);

		Document document;
		File file = new File(commonXml);
		try {
			if (file.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);

				Element root = document.getDocumentElement();
				NodeList jdbcdrivers = root.getElementsByTagName(Constants.XML_TAG_JDBCDRIVERS);

				for (int i = 0; i < jdbcdrivers.getLength(); i++) {
					Element node = (Element) jdbcdrivers.item(i);
					JDBCDRIVERS_LOC = node.getTextContent();
				}
			} else {
				if (!create(pjtLoc)) {
					PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_create_config_xml, new Exception());
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.exception_create_config_xml, MessageDialog.ERROR);
				} else {
					JDBCDRIVERS_LOC = getJdbcdriversFile(pjtLoc);
				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_load_config_xml, e);
		}
		return JDBCDRIVERS_LOC + Constants.FILE_SEPERATOR + Constants.DRIVER_SETTING_XML_FILE;
	}

	public static String getDatabasesFile(String pjtLoc) {
		getPrefs(pjtLoc);

		Document document;
		File file = new File(commonXml);
		try {
			if (file.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);

				Element root = document.getDocumentElement();
				NodeList databases = root.getElementsByTagName(Constants.XML_TAG_DATABASES);

				for (int i = 0; i < databases.getLength(); i++) {
					Element node = (Element) databases.item(i);
					DATABASES_LOC = node.getTextContent();
				}
			} else {
				if (!create(pjtLoc)) {
					PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_create_config_xml, new Exception());
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.exception_create_config_xml, MessageDialog.ERROR);
				} else {
					DATABASES_LOC = getDatabasesFile(pjtLoc);
				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, Message.exception_load_config_xml, e);
		}
		return DATABASES_LOC + Constants.FILE_SEPERATOR + Constants.DB_SETTINGS_XML_FILE;
	}

	// check and setting default
	public static String getPrefs(String pjtLoc) {

		String propertyFile = pjtLoc + Constants.FILE_SEPERATOR + Constants.COMMON_CONFIG_PREFS_FILE;

		File file = new File(propertyFile);
		if (!file.exists()) {
			file.getParentFile().mkdirs();

			FileOutputStream out = null;
			try {
				out = new FileOutputStream(propertyFile);
			} catch (IOException e) {
				PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, Message.exception_save_properties + e.getMessage() + ".");
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, Message.exception_save_properties + e.getMessage() + ".");
				}
			}

			commonXml = getDefaultCommonXml(pjtLoc);
			commonXmlLoc = getDefaultCommonXmlLoc(pjtLoc);

			PropertyUtil propertyUtil = new PropertyUtil(propertyFile);
			propertyUtil.setProperty(Constants.COMMON_CONFIG_PREFS_KEY, commonXmlLoc);
			propertyUtil.write();
		} else {
			PropertyUtil propertyUtil = new PropertyUtil(propertyFile);
			String value = propertyUtil.getProperty(Constants.COMMON_CONFIG_PREFS_KEY);
			if (value == null || "".equals(value)) {
				commonXml = getDefaultCommonXml(pjtLoc);
				commonXmlLoc = getDefaultCommonXmlLoc(pjtLoc);

				propertyUtil.setProperty(Constants.COMMON_CONFIG_PREFS_KEY, commonXmlLoc);
				propertyUtil.write();
			} else {
				commonXmlLoc = propertyUtil.getProperty(Constants.COMMON_CONFIG_PREFS_KEY);
				commonXml = commonXmlLoc + Constants.FILE_SEPERATOR + Constants.COMMON_SETTINGS_XML_FILE;
			}
		}
		return propertyFile;
	}

	private static boolean create(String pjtLoc) {
		// create xml file with default value
		ProjectConfig vo = new ProjectConfig();
		vo.setTemplateHomePath(pjtLoc + Constants.FILE_SEPERATOR + Constants.DFAULT_TEMPLATE_HOME);
		vo.setJdbcdriverPath(pjtLoc + Constants.FILE_SEPERATOR + Constants.SETTING_HOME);
		vo.setDatabasesPath(pjtLoc + Constants.FILE_SEPERATOR + Constants.SETTING_HOME);
		vo.setPjtHome(pjtLoc);
		return save(pjtLoc, vo);
	}

	private static String getDefaultCommonXml(String pjtLoc) {
		return pjtLoc + Constants.FILE_SEPERATOR + Constants.SETTING_HOME + Constants.FILE_SEPERATOR + Constants.COMMON_SETTINGS_XML_FILE;
	}

	private static String getDefaultCommonXmlLoc(String pjtLoc) {
		return pjtLoc + Constants.FILE_SEPERATOR + Constants.SETTING_HOME;
	}
}
