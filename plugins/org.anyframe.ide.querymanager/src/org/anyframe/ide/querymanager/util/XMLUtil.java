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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.w3c.dom.Document;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Utility class for reading and writing XML files for Anyframe Oden Eclipse
 * plug-in.
 * 
 * @author Junghwan Hong
 */
public class XMLUtil {

	/**
	 * 
	 * @param xmlRoot
	 * @param xmlFile
	 */
	// public static void save(Element xmlRoot, File xmlFile) {
	// 		try {
	// 			XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(xmlFile),
	// 			OutputFormat.createPrettyPrint());
	// 			xmlWriter.startDocument();
	// 			xmlWriter.write(xmlRoot);
	// 			xmlWriter.endDocument();
	// 			xmlWriter.flush();
	// 			xmlWriter.close();
	// 		} catch (Exception e) {
	// // 		ueryManagerPlugin.showErrorLog(e, "Save failed: " + xmlFile.getAbsolutePath());
	// 			QueryManagerPlugin.error("Save failed: " + xmlFile.getAbsolutePath(), e);
	// 		}
	// }

	public static void save(Document doc, File xmlFile) {
		try {
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

			// Source src = new DOMSource(doc);
			// Result dest = new StreamResult(xmlFile);
			//
			// transformer.transform(src, dest);
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Save failed: " + xmlFile.getAbsolutePath(), e);
		}

	}

	/**
	 * 
	 * @param xmlFile
	 * @return
	 */
	// public static Element readRoot(File xmlFile) {
	// 		if(!xmlFile.exists()) {
	// 			return null;
	// 		}
	// 		try {
	// 			return readRoot(new FileInputStream(xmlFile));
	// 		} catch (DocumentException t) {
	// 				// QueryManagerPlugin.showErrorLog(t, "Unable to load: " +
	// 				xmlFile.getAbsolutePath());
	// 			QueryManagerPlugin.error("Unable to load: " + xmlFile.getAbsolutePath(),
	// 				t);
	// 		} catch (FileNotFoundException fileNotFoundException) {
	// 				// cannot handle exception
	// 		}
	// 		return null;
	// }
	//
	// /**
	// *
	// * @param xmlFile
	// * @return
	// * @throws DocumentException
	// */
	// public static Element readRoot(InputStream xmlFile) throws
	// DocumentException {
	// 		SAXReader reader = new SAXReader();
	// 		return reader.read(xmlFile).getRootElement();
	// }

}
