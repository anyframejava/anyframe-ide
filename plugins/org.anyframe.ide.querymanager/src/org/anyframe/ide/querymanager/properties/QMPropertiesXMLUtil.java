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
package org.anyframe.ide.querymanager.properties;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.QueryManagerConstants;
import org.anyframe.ide.querymanager.util.XMLUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parsing xml files what enclude property information.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class QMPropertiesXMLUtil {

	private File getPropertyFile(IProject project){
		File file = new File(project
				.getLocation()
				+ Constants.FILE_SEPERATOR + Constants.SETTING_HOME + Constants.FILE_SEPERATOR 
				+ QueryManagerConstants.PROPERTY_FILE);
		return file;
	}
	public ArrayList loadPropertiesXML() {
		Document document;

		ArrayList<String> resultList = new ArrayList<String>();
		File file = getPropertyFile(QMPropertiesPage.getPropertyProject());
		try {
			if (file.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);

				Element root = document.getDocumentElement();
				NodeList mappings = root.getElementsByTagName("mapping");
				for (int i = 0; i < mappings.getLength(); i++) {
					Element node = (Element) mappings.item(i);
					resultList.add(node.getTextContent());
				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
		}
		return resultList;
	}

	public boolean getPropertiesComment() {
		boolean result = false;
		Document document;
		File file = getPropertyFile(QMPropertiesPage.getPropertyProject());
		try {
			if (file.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);

				Element root = document.getDocumentElement();
				NodeList comment = root.getElementsByTagName("comment");
				for (int i = 0; i < comment.getLength(); i++) {
					Element node = (Element) comment.item(i);
					if (node.getTextContent().equals("true")) {
						result = true;
					} else if (node.getTextContent().equals("false")) {
						result = false;
					}
				}

			}
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
		}

		return result;
	}

	public boolean getPropertiesComment2(IFile projectFile) {
		IProject project = projectFile.getProject();
		boolean result = false;
		Document document;
		File file = getPropertyFile(projectFile.getProject());
		try {
			if (file.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);

				Element root = document.getDocumentElement();
				NodeList comment = root.getElementsByTagName("comment");
				for (int i = 0; i < comment.getLength(); i++) {
					Element node = (Element) comment.item(i);
					if (node.getTextContent().equals("true")) {
						result = true;
					} else if (node.getTextContent().equals("false")) {
						result = false;
					}
				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
		}

		return result;
	}

	public boolean getPropertiesDupl() {
		boolean result = false;
		Document document;
		File file = getPropertyFile(QMPropertiesPage.getPropertyProject());
		try {
			if (file.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);

				Element root = document.getDocumentElement();
				NodeList duplicate = root.getElementsByTagName("duplicate");
				for (int i = 0; i < duplicate.getLength(); i++) {
					Element node = (Element) duplicate.item(i);
					if (node.getTextContent().equals("true")) {
						result = true;
					} else if (node.getTextContent().equals("false")) {
						result = false;
					}
				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
		}

		return result;
	}

	public boolean getPropertiesDupl2(IProject project) {
		boolean result = false;
		Document document;
		File file = getPropertyFile(project);
		try {
			if (file.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);

				Element root = document.getDocumentElement();
				NodeList duplicate = root.getElementsByTagName("duplicate");
				for (int i = 0; i < duplicate.getLength(); i++) {
					Element node = (Element) duplicate.item(i);
					if (node.getTextContent().equals("true")) {
						result = true;
					} else if (node.getTextContent().equals("false")) {
						result = false;
					}
				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
		}

		return result;
	}

	public boolean getPropertiesUsed() {
		boolean result = false;
		Document document;
		File file = getPropertyFile(QMPropertiesPage.getPropertyProject());
		try {
			if (file.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);

				Element root = document.getDocumentElement();
				NodeList used = root.getElementsByTagName("used");
				for (int i = 0; i < used.getLength(); i++) {
					Element node = (Element) used.item(i);
					if (node.getTextContent().equals("true")) {
						result = true;
					} else if (node.getTextContent().equals("false")) {
						result = false;
					}
				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
		}
		return result;
	}

	public boolean getPropertiesUsed2(IFile projectFile) {
		IProject project = projectFile.getProject();
		Document document;
		boolean result = false;
		File file = getPropertyFile(projectFile.getProject());
		try {
			if (file.exists()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(file);

				Element root = document.getDocumentElement();
				NodeList used = root.getElementsByTagName("used");
				for (int i = 0; i < used.getLength(); i++) {
					Element node = (Element) used.item(i);
					if (node.getTextContent().equals("true")) {
						result = true;
					} else if (node.getTextContent().equals("false")) {
						result = false;
					}
				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
		}
		return result;
	}

	public void savePropertiesXML(ArrayList xmlList, boolean boolComment,
			boolean boolDupl, boolean boolUsedUnused) {
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;
		try {
			parser = fact.newDocumentBuilder();
			Document doc = parser.newDocument();

			Element rootElement = doc
					.createElement("queryManagerProjectDescription");
			doc.appendChild(rootElement);

			Node project = doc.createElement("project");
			rootElement.appendChild(project);
			project.appendChild(doc.createTextNode(QMPropertiesPage
					.getPropertyProject().getName()));

			Element mappingElement = doc.createElement("mappings");
			rootElement.appendChild(mappingElement);

			for (int i = 0; i < xmlList.size(); i++) {
				Node mapping = doc.createElement("mapping");
				mappingElement.appendChild(mapping);
				mapping.appendChild(doc.createTextNode(xmlList.get(i)
						.toString()));
			}

			Node comment = doc.createElement("comment");
			rootElement.appendChild(comment);
			comment.appendChild(doc.createTextNode(boolComment + ""));

			Node duplicate = doc.createElement("duplicate");
			rootElement.appendChild(duplicate);
			duplicate.appendChild(doc.createTextNode(boolDupl + ""));

			Node used = doc.createElement("used");
			rootElement.appendChild(used);
			used.appendChild(doc.createTextNode(boolUsedUnused + ""));

			XMLUtil.save(doc, getPropertyFile(QMPropertiesPage.getPropertyProject()));

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
