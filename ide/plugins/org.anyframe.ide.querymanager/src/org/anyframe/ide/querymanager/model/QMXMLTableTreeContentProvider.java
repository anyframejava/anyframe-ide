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
package org.anyframe.ide.querymanager.model;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLTableTreeContentProvider;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * The class QMXMLTableTreeContentProvider extends XMLTableTreeContentProvider.
 * This class provides contents for Treeviewer
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QMXMLTableTreeContentProvider extends XMLTableTreeContentProvider {

	/**
	 * Default Constructor
	 */
	public QMXMLTableTreeContentProvider() {
		super();
	}

	/**
	 * Returns the value for given node
	 * 
	 * @param object
	 *            The Object object
	 * @return string
	 */

	public String getText(Object object) {
		if (object instanceof INodeNotifier) {
			((INodeNotifier) object).getAdapterFor(IJFaceNodeAdapter.class);
		}

		String result = null;
		if (object instanceof Node) {
			Node node = (Node) object;
			switch (node.getNodeType()) {
			case Node.ATTRIBUTE_NODE: {
				result = node.getNodeName();
				break;
			}
			case Node.DOCUMENT_TYPE_NODE: {
				result = "DOCTYPE"; //$NON-NLS-1$
				break;
			}
			case Node.ELEMENT_NODE: {
				if (node != null) {
					result = node.getNodeName();
					if (result.equalsIgnoreCase("query")) {
						NamedNodeMap attributes = node.getAttributes();
						if (attributes != null) {
							Node nodeName = attributes.getNamedItem("id");
							if (nodeName != null) {
								result = nodeName.getNodeValue();
							}
						}
					}
				}
				break;
			}
			case Node.PROCESSING_INSTRUCTION_NODE: {
				result = ((ProcessingInstruction) node).getTarget();
				break;
			}
			}
		}
		return result != null ? result : ""; //$NON-NLS-1$
	}

	/**
	 * @param object
	 *            The Object object
	 * @return The object of Image.
	 */
	public Image getImage(Object object) {
		return null;
	}

}
