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
package org.anyframe.ide.command.maven.mojo.codegen;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * This is GenerationUtil class for generate Domain/CRUD class.
 * 
 * @author Sujeong Lee
 */
public class GenerationUtil {

	private static String DB_TO_JAVA = "db-to-java";

	public static Map<String, String> getTypeMappingConfig(String templateHome) throws Exception {
		String commonTemplateDirectory = templateHome + CommonConstants.fileSeparator + "common";

		// initial
		Map<String, String> typeMapper = new HashMap<String, String>();

		String typeMappingFile = commonTemplateDirectory + CommonConstants.fileSeparator + "typeMapping.xml";

		Document document;
		File file = new File(typeMappingFile);
		if (file.exists()) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(file);

			Element root = document.getDocumentElement();
			Element dbToJavaElement = (Element) root.getElementsByTagName(DB_TO_JAVA).item(0);

			NodeList nodeList = dbToJavaElement.getElementsByTagName("type");

			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);
				String javaType = element.getTextContent();

				NamedNodeMap attrs = element.getAttributes();
				String dbType = attrs.getNamedItem("name").getTextContent();

				typeMapper.put(dbType, javaType);
				typeMapper.put(dbType.toLowerCase(), javaType);
			}
		}

		return typeMapper;
	}
}
