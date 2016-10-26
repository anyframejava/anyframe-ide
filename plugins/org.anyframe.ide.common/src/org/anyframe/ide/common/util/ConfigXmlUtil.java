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
package org.anyframe.ide.common.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.databases.JdbcOption;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * This is a ConfigXmlUtil class. This class is a utility for config xml file.
 * 
 * @author Sujeong Lee
 */
public class ConfigXmlUtil {

	private static String EMPTY_STRING = "EMPTY";

	/**
	 * return org.anyframe.ide.common.config.xml file path
	 * 
	 * @param baseDir
	 * @return
	 * @throws Exception
	 */
	public static String getCommonConfigFile(String baseDir) throws Exception {
		String configFile = "";

		String commonConfig = baseDir + Constants.FILE_SEPERATOR + Constants.COMMON_CONFIG_PREFS_FILE;
		String defaultConfigFile = baseDir + Constants.FILE_SEPERATOR + Constants.SETTING_HOME + Constants.FILE_SEPERATOR
				+ Constants.COMMON_SETTINGS_XML_FILE;
		File file = new File(commonConfig);
		if (!file.exists()) {
			// file not found - default path
			configFile = defaultConfigFile;
		} else {
			PropertyUtil propertyUtil = new PropertyUtil(commonConfig);
			configFile = propertyUtil.getProperty(Constants.COMMON_CONFIG_PREFS_KEY);
			if ("".equals(configFile)) {
				configFile = defaultConfigFile;
			} else {
				configFile += Constants.FILE_SEPERATOR + Constants.COMMON_SETTINGS_XML_FILE;
			}

		}

		return configFile;
	}

	/**
	 * return ProjectConfig with parsing common config file
	 * 
	 * @param configFile
	 * @return
	 * @throws Exception
	 */
	public static ProjectConfig getProjectConfig(String configFile) throws Exception {
		ProjectConfig config = new ProjectConfig();

		Document document;
		File file = new File(configFile);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(file);

		Element root = document.getDocumentElement();

		String pjtName = getText(root, Constants.PROJECT_NAME);
		String packageName = getText(root, Constants.PACKAGE_NAME);
		String pjtHome = getText(root, Constants.PROJECT_HOME);

		String anyframeHome = "";
		String contextRoot = "";
		String offline = "";

		NodeList antList = root.getElementsByTagName(Constants.PROJECT_BUILD_TYPE_ANT);
		if (antList != null && antList.getLength() > 0) {
			Element antTag = (Element) antList.item(0);
			anyframeHome = getText(antTag, Constants.ANYFRAME_HOME);
			contextRoot = getText(antTag, Constants.CONTEXT_ROOT);
			offline = getText(antTag, Constants.OFFLINE);
		}

		NodeList pathList = root.getElementsByTagName(Constants.CONFIG_PATH);
		Element pathTag = (Element) pathList.item(0);
		String templatePath = getText(pathTag, Constants.XML_TAG_TEMPLATE);
		String jdbcdriverPath = getText(pathTag, Constants.XML_TAG_JDBCDRIVERS);
		String databasesPath = getText(pathTag, Constants.XML_TAG_DATABASES);

		config.setPjtHome(pjtHome);
		config.setPjtName(pjtName);
		config.setPackageName(packageName);

		config.setAnyframeHome(anyframeHome);
		config.setContextRoot(contextRoot);
		config.setOffline(offline);

		config.setTemplateHomePath(templatePath);
		config.setJdbcdriverPath(jdbcdriverPath);
		config.setDatabasesPath(databasesPath);

		return config;
	}

	/**
	 * return Default JdbcOption with baseDir
	 * 
	 * @param baseDir
	 * @return
	 * @throws Exception
	 */
	public static JdbcOption getDefaultDatabase(String baseDir) throws Exception {
		ProjectConfig projectConfig = getProjectConfig(getCommonConfigFile(baseDir));
		return getDefaultDatabaseFromFile(projectConfig.getDatabasesPath() + Constants.FILE_SEPERATOR + Constants.DB_SETTINGS_XML_FILE);
	}

	/**
	 * return Default JdbcOption with ProjectConfig
	 * 
	 * @param ProjectConfig
	 * @return
	 * @throws Exception
	 */
	public static JdbcOption getDefaultDatabase(ProjectConfig projectConfig) throws Exception {
		return getDefaultDatabaseFromFile(projectConfig.getDatabasesPath() + Constants.FILE_SEPERATOR + Constants.DB_SETTINGS_XML_FILE);
	}

	/**
	 * return Default JdbcOption with parsing database config file
	 * 
	 * @param configFile
	 * @return
	 * @throws Exception
	 */
	public static JdbcOption getDefaultDatabaseFromFile(String configFile) throws Exception {
		JdbcOption jdbc = new JdbcOption();

		Document document;
		File file = new File(configFile);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(file);

		Element root = document.getDocumentElement();

		NodeList nodeList = root.getElementsByTagName(Constants.XML_TAG_DATASOURCE);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			NamedNodeMap attrs = node.getAttributes();
			String isDefault = getText(attrs, Constants.XML_TAG_DEFAULT);

			if (!"".equals(nullToString(isDefault)) && Boolean.valueOf(nullToString(isDefault))) {
				// isDefault 값이 null이 아니고 true인 경우 - default return
				String name = getText(attrs, Constants.XML_TAG_NAME);
				String type = getText(attrs, Constants.XML_TAG_TYPE);
				String driverJar = getText(attrs, Constants.XML_TAG_DRIVER_PATH);
				String driverClassName = getText(attrs, Constants.XML_TAG_DRIVER_CLASS_NAME);
				String url = getText(attrs, Constants.XML_TAG_URL);
				String username = getText(attrs, Constants.XML_TAG_USERNAME);
				String password = EncryptUtil.decrypt(getText(attrs, Constants.XML_TAG_PASSWORD));
				String schema = getText(attrs, Constants.XML_TAG_SCHEMA);
				String dialect = getText(attrs, Constants.XML_TAG_DIALECT);
				String driverGroupId = getText(attrs, Constants.XML_TAG_DRIVER_GROUPID);
				String driverArtifactId = getText(attrs, Constants.XML_TAG_DRIVER_ARTIFACTID);
				String driverVersion = getText(attrs, Constants.XML_TAG_DRIVER_VERSION);
				String useDbSpecific = getText(attrs, Constants.XML_TAG_USE_DB_SPECIFIC);
				String runExplainPlan = getText(attrs, Constants.XML_TAG_RUN_EXPLAIN_PLAN);

				jdbc.setDbName(name);
				jdbc.setDbType(type);
				jdbc.setDriverJar(driverJar);
				jdbc.setDriverClassName(driverClassName);
				jdbc.setUrl(url);
				jdbc.setUserName(username);
				jdbc.setPassword(password);
				jdbc.setSchema(schema);
				jdbc.setDialect(dialect);
				jdbc.setMvnGroupId(driverGroupId);
				jdbc.setMvnArtifactId(driverArtifactId);
				jdbc.setMvnVersion(driverVersion);
				jdbc.setUseDbSpecific(Boolean.valueOf(useDbSpecific));
				jdbc.setRunExplainPaln(Boolean.valueOf(runExplainPlan));
				jdbc.setDefault(Boolean.valueOf(isDefault));
			}
		}
		return jdbc;
	}

	/**
	 * return JdbcOption List with baseDir
	 * 
	 * @param baseDir
	 * @return
	 * @throws Exception
	 */
	public static List<JdbcOption> getDatabaseList(String baseDir) throws Exception {
		ProjectConfig projectConfig = getProjectConfig(getCommonConfigFile(baseDir));
		return getDatabaseListFromFile(projectConfig.getDatabasesPath() + Constants.FILE_SEPERATOR + Constants.DB_SETTINGS_XML_FILE);
	}

	/**
	 * return JdbcOption List with ProjectConfig
	 * 
	 * @param ProjectConfig
	 * @return
	 * @throws Exception
	 */
	public static List<JdbcOption> getDatabaseList(ProjectConfig projectConfig) throws Exception {
		return getDatabaseListFromFile(projectConfig.getDatabasesPath() + Constants.FILE_SEPERATOR + Constants.DB_SETTINGS_XML_FILE);
	}

	/**
	 * return JdbcOption List with parsing database config file
	 * 
	 * @param configFile
	 * @return
	 * @throws Exception
	 */
	public static List<JdbcOption> getDatabaseListFromFile(String configFile) throws Exception {
		List<JdbcOption> result = new ArrayList<JdbcOption>();

		Document document;
		File file = new File(configFile);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(file);

		Element root = document.getDocumentElement();

		NodeList nodeList = root.getElementsByTagName(Constants.XML_TAG_DATASOURCE);
		for (int i = 0; i < nodeList.getLength(); i++) {
			JdbcOption jdbc = new JdbcOption();

			Node node = nodeList.item(i);
			NamedNodeMap attrs = node.getAttributes();

			String name = getText(attrs, Constants.XML_TAG_NAME);
			String type = getText(attrs, Constants.XML_TAG_TYPE);
			String driverJar = getText(attrs, Constants.XML_TAG_DRIVER_PATH);
			String driverClassName = getText(attrs, Constants.XML_TAG_DRIVER_CLASS_NAME);
			String url = getText(attrs, Constants.XML_TAG_URL);
			String username = getText(attrs, Constants.XML_TAG_USERNAME);
			String password = EncryptUtil.decrypt(getText(attrs, Constants.XML_TAG_PASSWORD));
			String schema = getText(attrs, Constants.XML_TAG_SCHEMA);
			String dialect = getText(attrs, Constants.XML_TAG_DIALECT);
			String driverGroupId = getText(attrs, Constants.XML_TAG_DRIVER_GROUPID);
			String driverArtifactId = getText(attrs, Constants.XML_TAG_DRIVER_ARTIFACTID);
			String driverVersion = getText(attrs, Constants.XML_TAG_DRIVER_VERSION);
			String useDbSpecific = getText(attrs, Constants.XML_TAG_USE_DB_SPECIFIC);
			String runExplainPlan = getText(attrs, Constants.XML_TAG_RUN_EXPLAIN_PLAN);
			String isDefault = getText(attrs, Constants.XML_TAG_DEFAULT);

			jdbc.setDbName(name);
			jdbc.setDbType(type);
			jdbc.setDriverJar(driverJar);
			jdbc.setDriverClassName(driverClassName);
			jdbc.setUrl(url);
			jdbc.setUserName(username);
			jdbc.setPassword(password);
			jdbc.setSchema(schema);
			jdbc.setDialect(dialect);
			jdbc.setMvnGroupId(driverGroupId);
			jdbc.setMvnArtifactId(driverArtifactId);
			jdbc.setMvnVersion(driverVersion);
			jdbc.setUseDbSpecific(Boolean.valueOf(useDbSpecific));
			jdbc.setRunExplainPaln(Boolean.valueOf(runExplainPlan));
			jdbc.setDefault(Boolean.valueOf(isDefault));

			result.add(jdbc);
		}
		return result;
	}

	/**
	 * save project configuration xml file
	 * 
	 * @param projectConfig
	 * @return
	 * @throws Exception
	 */
	public static boolean saveProjectConfig(ProjectConfig projectConfig) throws Exception {
		boolean result = true;

		String projectHome = projectConfig.getPjtHome();
		String configFile = getCommonConfigFile(projectHome);

		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;

		try {
			parser = fact.newDocumentBuilder();
			Document doc = parser.newDocument();

			Element rootElement = doc.createElement(Constants.CONFIG_ROOT);
			doc.appendChild(rootElement);

			createNode(doc, rootElement, Constants.PROJECT_HOME, projectConfig.getPjtHome());
			createNode(doc, rootElement, Constants.PROJECT_NAME, projectConfig.getPjtName());
			createNode(doc, rootElement, Constants.PACKAGE_NAME, projectConfig.getPackageName());

			if (projectConfig.getAnyframeHome() != null && !"".equals(projectConfig.getAnyframeHome())) {
				Element antElement = createNode(doc, rootElement, Constants.PROJECT_BUILD_TYPE_ANT, EMPTY_STRING);

				createNode(doc, antElement, Constants.ANYFRAME_HOME, projectConfig.getAnyframeHome());
				createNode(doc, antElement, Constants.CONTEXT_ROOT, projectConfig.getContextRoot());
				createNode(doc, antElement, Constants.OFFLINE, projectConfig.getOffline());
			}

			Element pathElement = createNode(doc, rootElement, Constants.CONFIG_PATH, EMPTY_STRING);

			createNode(doc, pathElement, Constants.XML_TAG_TEMPLATE, projectConfig.getTemplateHomePath());
			createNode(doc, pathElement, Constants.XML_TAG_JDBCDRIVERS, projectConfig.getJdbcdriverPath());
			createNode(doc, pathElement, Constants.XML_TAG_DATABASES, projectConfig.getDatabasesPath());

			File file = new File(configFile);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
			}

			save(doc, file);
		} catch (Exception e) {
			result = false;
		}

		return result;
	}

	/**
	 * save project database configuration xml file(only default database)
	 * 
	 * @param baseDir
	 * @param jdbcOption
	 * @return
	 * @throws Exception
	 */
	public static boolean saveDatabase(String baseDir, JdbcOption jdbcOption) throws Exception {
		boolean result = true;

		ProjectConfig projectConfig = getProjectConfig(getCommonConfigFile(baseDir));
		File file = new File(projectConfig.getDatabasesPath() + Constants.FILE_SEPERATOR + Constants.DB_SETTINGS_XML_FILE);
		Document document;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(file);

		Element root = document.getDocumentElement();

		NodeList nodeList = root.getElementsByTagName(Constants.XML_TAG_DATASOURCE);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element node = (Element) nodeList.item(i);
			NamedNodeMap attrs = node.getAttributes();
			boolean isDefault = Boolean.valueOf(getText(attrs, Constants.XML_TAG_DEFAULT));
			if (isDefault) {
				attrs.getNamedItem(Constants.XML_TAG_TYPE).setNodeValue(jdbcOption.getDbType());
				attrs.getNamedItem(Constants.XML_TAG_NAME).setNodeValue(jdbcOption.getDbName());
				attrs.getNamedItem(Constants.XML_TAG_DRIVER_PATH).setNodeValue(jdbcOption.getDriverJar());
				attrs.getNamedItem(Constants.XML_TAG_DRIVER_CLASS_NAME).setNodeValue(jdbcOption.getDriverClassName());
				attrs.getNamedItem(Constants.XML_TAG_URL).setNodeValue(jdbcOption.getUrl());
				attrs.getNamedItem(Constants.XML_TAG_USERNAME).setNodeValue(jdbcOption.getUserName());
				attrs.getNamedItem(Constants.XML_TAG_PASSWORD).setNodeValue(jdbcOption.getPassword());
				attrs.getNamedItem(Constants.XML_TAG_SCHEMA).setNodeValue(jdbcOption.getSchema());
				attrs.getNamedItem(Constants.XML_TAG_DIALECT).setNodeValue(jdbcOption.getDialect());
				attrs.getNamedItem(Constants.XML_TAG_DRIVER_GROUPID).setNodeValue(jdbcOption.getMvnGroupId());
				attrs.getNamedItem(Constants.XML_TAG_DRIVER_ARTIFACTID).setNodeValue(jdbcOption.getMvnArtifactId());
				attrs.getNamedItem(Constants.XML_TAG_DRIVER_VERSION).setNodeValue(jdbcOption.getMvnVersion());
				attrs.getNamedItem(Constants.XML_TAG_USE_DB_SPECIFIC).setNodeValue(String.valueOf(jdbcOption.isUseDbSpecific()));
				attrs.getNamedItem(Constants.XML_TAG_RUN_EXPLAIN_PLAN).setNodeValue(String.valueOf(jdbcOption.isRunExplainPaln()));
				attrs.getNamedItem(Constants.XML_TAG_DEFAULT).setNodeValue(String.valueOf(jdbcOption.getDefault()));
				break;
			}
		}
		save(document, file);
		return result;
	}

	/**
	 * save project database configuration xml file about all database info
	 * 
	 * @param baseDir
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static boolean saveDatabaseList(String baseDir, List<JdbcOption> list) throws Exception {
		boolean result = true;

		String configFile = getCommonConfigFile(baseDir);

		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;

		try {
			parser = fact.newDocumentBuilder();
			Document doc = parser.newDocument();

			Element rootElement = doc.createElement(Constants.XML_CONFIG_ROOT_PATH);
			doc.appendChild(rootElement);

			for (int i = 0; i < list.size(); i++) {
				JdbcOption jdbcOption = list.get(i);

				Element dbElement = createNode(doc, rootElement, Constants.XML_TAG_DATABASES, EMPTY_STRING);
				dbElement.setAttribute(Constants.XML_TAG_TYPE, jdbcOption.getDbType());
				dbElement.setAttribute(Constants.XML_TAG_NAME, jdbcOption.getDbName());
				dbElement.setAttribute(Constants.XML_TAG_DRIVER_PATH, jdbcOption.getDriverJar());
				dbElement.setAttribute(Constants.XML_TAG_DRIVER_CLASS_NAME, jdbcOption.getDriverClassName());
				dbElement.setAttribute(Constants.XML_TAG_URL, jdbcOption.getUrl());
				dbElement.setAttribute(Constants.XML_TAG_USERNAME, jdbcOption.getUserName());
				dbElement.setAttribute(Constants.XML_TAG_PASSWORD, EncryptUtil.encrypt(jdbcOption.getPassword()));
				dbElement.setAttribute(Constants.XML_TAG_SCHEMA, jdbcOption.getSchema());
				dbElement.setAttribute(Constants.XML_TAG_DIALECT, jdbcOption.getDialect());
				dbElement.setAttribute(Constants.XML_TAG_DRIVER_GROUPID, jdbcOption.getMvnGroupId());
				dbElement.setAttribute(Constants.XML_TAG_DRIVER_ARTIFACTID, jdbcOption.getMvnArtifactId());
				dbElement.setAttribute(Constants.XML_TAG_DRIVER_VERSION, jdbcOption.getMvnVersion());
				dbElement.setAttribute(Constants.XML_TAG_USE_DB_SPECIFIC, String.valueOf(jdbcOption.isUseDbSpecific()));
				dbElement.setAttribute(Constants.XML_TAG_RUN_EXPLAIN_PLAN, String.valueOf(jdbcOption.isRunExplainPaln()));
				dbElement.setAttribute(Constants.XML_TAG_DEFAULT, String.valueOf(jdbcOption.getDefault()));
			}

			File file = new File(configFile);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
			}
			save(doc, file);

		} catch (Exception e) {
			result = false;
		}

		return result;
	}

	private static String getText(Element root, String constants) {
		String result = "";
		try {
			result = nullToString(root.getElementsByTagName(constants).item(0).getTextContent());
		} catch (Exception e) {
		}
		return result;
	}

	private static String getText(NamedNodeMap attrs, String contants) {
		Node node = attrs.getNamedItem(contants);
		if(node == null){
			return "";
		}else{
			return nullToString(node.getTextContent());
		}
	}

	private static String nullToString(String str) {
		if (str == null || str.trim().length() == 0) {
			return "";
		} else {
			return str.trim();
		}
	}

	private static Element createNode(Document doc, Element element, String tagName, String value) {
		/**
		 * if, value is "" -> do not make Node. but, value is EMPTY_STRING ->
		 * make empty string Node
		 */
		if (value == null || "".equals(value)) {
			return null;
		} else {
			if (EMPTY_STRING.equals(value)) {
				value = "";
			}
			Element node = doc.createElement(tagName);
			element.appendChild(node);
			node.appendChild(doc.createTextNode(value));
			return node;
		}
	}

	private static void save(Document doc, File xmlFile) throws Exception {
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer transformer = tranFactory.newTransformer();

		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		OutputFormat format = new OutputFormat(doc);
		format.setIndenting(true);
		format.setIndent(4);
		format.setEncoding("utf-8");
		format.setPreserveSpace(false);

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xmlFile), "UTF-8"));

		XMLSerializer serializer = new XMLSerializer(out, format);

		serializer.serialize(doc.getDocumentElement());

		out.close();
	}

}
