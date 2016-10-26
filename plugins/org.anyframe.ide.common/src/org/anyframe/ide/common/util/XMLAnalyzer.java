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
package org.anyframe.ide.common.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.anyframe.ide.common.CommonActivator;
import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is XMLAnalyzer class.
 * 
 * @author Sujeong Lee
 */
public class XMLAnalyzer {

	public static final String XML_ELEMENT_NODE_NAME = "XML_ELEMENT_NODE_NAME";
	public static final String XML_ELEMENT_NODE_TEXT_CONTENT = "XML_ELEMENT_NODE_TEXT_CONTENT";

	// get ALL elements from xml file name with path
	public static List<Map<String, String>> getData(String xmlFileName,
			String xPathExpressionString) throws Exception {
		Document doc = getDocumentFromFileName(xmlFileName);
		return getData(doc, xPathExpressionString);
	}

	// get Document from xml file name with path
	public static Document getDocumentFromFileName(String xmlFileName)
			throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
		return builder.parse(new FileInputStream(xmlFileName));
	}

	// get Document from IFile
	public static Document getDocumentFromContentsString(
			String xmlContentsString) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(
				xmlContentsString)));

		return doc;
	}

	// get Document from IFile
	public static Document getDocumentFromIfile(IFile file) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
		return builder.parse(file.getContents());
	}

	// get Document from IFile
	public static Document getDocumentFromIfileWithoutDtdValidation(IFile file)
			throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
		disableDtdValidation(builder);
		return builder.parse(file.getContents());
	}

	// get SPECIFIED elements from Document
	public static List<Map<String, String>> getData(Document doc,
			String xPathExpressionString) throws Exception {
		// public static List<Map<String, String>> getData(Document doc, String
		// xPathExpressionString, String[] elems) throws Exception {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile(xPathExpressionString);

		Object result = expr.evaluate(doc, XPathConstants.NODESET);

		NodeList nodeList = (NodeList) result;

		List<Map<String, String>> list = new ArrayList<Map<String, String>>();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);

			Map<String, String> step = new HashMap<String, String>();

			if (xPathExpressionString.endsWith("*")) {
				step.put(XML_ELEMENT_NODE_NAME, node.getNodeName());
			}

			// if(elems != null) {
			// for(String elem : elems) {
			// step.put(elem, getAttribute(node, elem));
			// }
			// } else {
			NamedNodeMap attrs = node.getAttributes();
			for (int idx = 0; idx < attrs.getLength(); idx++) {
				Node attr = attrs.item(idx);
				step.put(attr.getNodeName(),
						StringUtil.null2str(attr.getNodeValue()));
			}
			// }
			if (node.getTextContent() != null
					&& !"".equals(node.getTextContent().trim())) {
				step.put(XML_ELEMENT_NODE_TEXT_CONTENT, node.getTextContent()
						.trim());
			}
			list.add(step);
		}
		return list;
	}

	// for Debug
	public static void printMetatDataList(List<Map<String, String>> metaDataList) {
		if (metaDataList == null) {
			return;
		}
		for (Map<String, String> metaData : metaDataList) {
			PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, metaData.toString());
		}
	}

	/**
	 * disable the dtd validation by setting a custom entity resolver, for
	 * preventing java.net.UnknownHostException under network disabled
	 * circumstance.
	 */
	public static void disableDtdValidation(DocumentBuilder db) {
		db.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				return new InputSource(new StringReader(""));
				// return new InputSource(new
				// ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
			}
		});
	}

	/*
	 * blocked by bonobono, 10/04/22, to be deleted // get SPECIFIED elements
	 * from xml file name with path public static List<Map<String, String>>
	 * getData(String xmlFileName, String xPathExpressionString, String[] elems)
	 * throws Exception { Document doc = getDocumentFromFileName(xmlFileName);
	 * return getData(doc, xPathExpressionString, elems); }
	 * 
	 * // get ALL elements from Document public static List<Map<String, String>>
	 * getData(Document doc, String xPathExpressionString) throws Exception {
	 * return getData(doc, xPathExpressionString, null); }
	 * 
	 * private static String getAttribute(Node node, String name) { Node
	 * namedItem = node.getAttributes().getNamedItem(name); if (namedItem ==
	 * null) return ""; return namedItem.getNodeValue(); }
	 */

	/*
	 * blocked by bonobono, 10/04/12, for test public static void main (String
	 * ... args) { List<Map<String, String>> tt = new
	 * ArrayList<Map<String,String>>() ; try { // tt = getData("C:/Sam.message",
	 * "message/nested[@array=\"addrInfoCnt\"]/data"); Document doc =
	 * getDocument("C:/Sam.message"); tt = getData(doc, "message/*");
	 * System.out.println(tt.size()); tt = getData(doc, "message/data");
	 * System.out.println(tt.size()); } catch (Exception e) {
	 * e.printStackTrace(); } // for(Map<String, String> t : tt ) { //
	 * System.out.println(t); // } // System.out.println(tt.size()); } //
	 */

}