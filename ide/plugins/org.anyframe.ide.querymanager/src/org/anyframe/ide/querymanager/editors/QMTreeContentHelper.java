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
package org.anyframe.ide.querymanager.editors;

import org.eclipse.wst.xml.ui.internal.tabletree.TreeContentHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * This performs the work of taking a DOM tree and converting it to a
 * displayable 'UI' tree. For example : - white space text nodes are ommited
 * from the 'UI' tree - adjacent Text and EntityReference nodes are combined
 * into a single 'UI' node - Elements with 'text only' children are diplayed
 * without children
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QMTreeContentHelper extends TreeContentHelper {

	/**
	 * Get the value of Node
	 * 
	 * @param node
	 *            Node object
	 * @return String
	 */
	public String getNodeValue(Node node) {

		String result = null;
		int nodeType = node.getNodeType();

		switch (nodeType) {
		case Node.ATTRIBUTE_NODE: {
			result = ((Attr) node).getValue();

			break;
		}
		case Node.CDATA_SECTION_NODE:
			// drop thru
		case Node.COMMENT_NODE: {
			result = ((CharacterData) node).getData();
			break;
		}
		case Node.DOCUMENT_TYPE_NODE: {
			result = getDocumentTypeValue((DocumentType) node);
			break;
		}
		case Node.ELEMENT_NODE: {
			result = getElementNodeValue((Element) node);

			if (node.getNodeName().equalsIgnoreCase("query")) {

				NodeList children = node.getChildNodes();
				int max = children.getLength();

				for (int i = 0; i < max; i++) {
					if ("statement".equalsIgnoreCase(children.item(i)
							.getNodeName())) {
						result = getElementNodeValue((Element) children.item(i));

						NodeList ch = children.item(i).getChildNodes();
						for (int j = 0; j < ch.getLength(); j++) {
							if ("#cdata-section".equalsIgnoreCase(ch.item(j)
									.getNodeName())) {
								result = getNodeValue(ch.item(j));
								break;
							}
						}
						break;
					}
				}

				if (result != null) {
					StringBuffer resultBuffr = new StringBuffer();
					for (int i = 0; i < result.length(); i++) {
						char c = result.charAt(i);
						if (c > 31)
							resultBuffr.append(c);
					}
					result = resultBuffr.toString();
				}

			}

			break;
			// result.replaceAll("", "");
		}
		case Node.ENTITY_REFERENCE_NODE:
			// drop thru
		case Node.TEXT_NODE: {
			result = getTextNodeValue(node);
			break;
		}
		case Node.PROCESSING_INSTRUCTION_NODE: {
			result = ((ProcessingInstruction) node).getData();
			break;
		}
		}
		return result;
	}

}
