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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.Constants;
import org.anyframe.ide.common.dialog.JdbcType;
import org.anyframe.ide.common.messages.Message;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.thoughtworks.xstream.XStream;

/**
 * This is XMLUtil class.
 * 
 * @author Sujeong Lee
 */
public class XMLUtil {

	public static Document getDocument(String file) throws Exception {
		return getDocument(file, true);
	}

	public static Document getDocumentWithoutDtdValidation(String file)
			throws Exception {
		return getDocument(file, false);
	}

	public static Element getRoot(Document document) throws Exception {
		return document.getDocumentElement();
	}

	public static NodeList getNodeList(Element parent, String tagname)
			throws Exception {
		return parent.getElementsByTagName(tagname);
	}

//	public static Element createElement(Document document, String tagname,
//			Map attributes, String content) throws Exception {
//		Element result = null;
//		return result;
//	}

	public static void save(Document doc, File xmlFile) throws Exception {
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer transformer = tranFactory.newTransformer();

		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		OutputFormat format = new OutputFormat(doc);
		format.setIndenting(true);
		format.setIndent(4);
		format.setEncoding("utf-8");
		format.setPreserveSpace(false);

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(xmlFile), "UTF-8"));

		XMLSerializer serializer = new XMLSerializer(out, format);

		serializer.serialize(doc.getDocumentElement());
		out.close();
	}

	private static Document getDocument(String file, boolean boolDtdValid)
			throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
		if (!boolDtdValid) {
			disableDtdValidation(builder);
		}
		return builder.parse(new FileInputStream(file));
	}

	private static void disableDtdValidation(DocumentBuilder db) {
		db.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				return new InputSource(new StringReader(""));
			}
		});
	}

	public static void saveObjectToXml(Object xmlContents,
			String anyframeConfigFileLocation) {
		XStream xstream = getXStream();

		String anyframeConfigXml = xstream.toXML(xmlContents);

		File anyframeConfigFile = new File(anyframeConfigFileLocation);
		try {
			anyframeConfigFile.createNewFile();
			FileOutputStream fileOutputStream = new FileOutputStream(
					anyframeConfigFile);
			fileOutputStream.write(anyframeConfigXml.getBytes());
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID,
					Message.exception_log_find_file, e);
		} catch (IOException e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID,
					Message.exception_log_io_file, e);
		}
	}

	public static Object getObjectFromXml(String anyframeConfigFileLocation) {
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(anyframeConfigFileLocation);
		} catch (FileNotFoundException e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID,
					Message.exception_log_find_file, e);
		}
		return getXStream().fromXML(fileReader);
	}

	public static Object getObjectFromInputStream(InputStream inputStream) {
		return getXStream().fromXML(inputStream);
	}

	private static XStream getXStream() {
		XStream xstream = new XStream();

		// Annotations.configureAliases(xstream, JdbcType.class);
		xstream.alias("jdbcType", JdbcType.class);
		xstream.setMode(XStream.NO_REFERENCES);

		return xstream;
	}

	public static String adjustLineEndingsForOS(String property) {
		String os = System.getProperty("os.name");

		if (os.startsWith("Linux") || os.startsWith("Mac")) {
			// remove the \r returns
			property = property.replaceAll("\r", "");
		} else if (os.startsWith("Windows")) {
			// use windows line endings
			property = property.replaceAll(">\n", ">\r\n");
		}

		return property;
	}

	public static InputStream getJdbcConfigFileInputStream() throws IOException {
		Bundle bundle = Platform.getBundle(CommonActivator.PLUGIN_ID);
		return bundle.getEntry(Constants.DRIVER_SETTING_XML_FILE)
				.openStream();
	}
}
