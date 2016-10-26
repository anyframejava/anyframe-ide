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
package org.anyframe.ide.querymanager.actions;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.anyframe.ide.querymanager.messages.Message;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.ui.internal.XMLUIMessages;
import org.eclipse.wst.xml.ui.internal.actions.NodeAction;
import org.eclipse.wst.xml.ui.internal.contentoutline.XMLNodeActionManager;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * The class QMXMLNodeActionManager extends XMLNodeActionManager. This class
 * creates Delete Action class which removes query id from SQL XML file
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QMXMLNodeActionManager extends XMLNodeActionManager {
	Viewer viewer;

	/**
	 * Constructor with parameter model and viewer
	 * 
	 * @param model
	 *            IStructuredModel object
	 * @param viewer
	 *            Viewer object
	 */
	public QMXMLNodeActionManager(IStructuredModel model, Viewer viewer) {
		super(model, viewer);
		this.viewer = viewer;
	}

	/**
	 * The class DeleteAction extends NodeAction. This class creates and
	 * implements the Delete Action which removes query id from SQL XML file
	 */
	public class DeleteAction extends NodeAction {

		protected List list;

		/**
		 * Constructor with parameter list
		 * 
		 * @param list
		 *            list of table rows to be deleted.
		 */
		public DeleteAction(List list) {
			setText(XMLUIMessages._UI_MENU_REMOVE); //$NON-NLS-1$
			this.list = list;
		}

		/**
		 * Constructor with parameter node
		 * 
		 * @param node
		 *            node to be deleted.
		 */
		public DeleteAction(Node node) {
			setText(XMLUIMessages._UI_MENU_REMOVE); //$NON-NLS-1$
			list = new Vector();
			list.add(node);
		}

		/**
		 * Get context menu name for Delete operation
		 * 
		 * @return String
		 */
		public String getUndoDescription() {
			return XMLUIMessages.DELETE; //$NON-NLS-1$
		}

		/**
		 * This method gets called wher developer selects Remove option from
		 * context menu to delete query id
		 */
		public void run() {
			beginNodeAction(this);

			for (Iterator i = list.iterator(); i.hasNext();) {
				Node node = (Node) i.next();
				if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
					Attr attr = (Attr) node;
					attr.getOwnerElement().removeAttributeNode(attr);
				} else {
					Node parent = node.getParentNode();
					if (parent != null) {
						Node previousSibling = node.getPreviousSibling();
						if (previousSibling != null
								&& isWhitespaceTextNode(previousSibling)) {
							parent.removeChild(previousSibling);
						}
						if (MessageDialog.openConfirm(
								viewer.getControl().getShell(),
								Message.view_explorer_action_deleteconfirmation,
								Message.view_explorer_action_deletenodeconfirm 
										+ node.getNodeName() + " ?")) {

							// Removing query id from tree viewer
							parent.removeChild(node);

						}
					}
				}
			}

			endNodeAction(this);
		}
	}

	/**
	 * Instantiating DeleteAction and returning that action
	 * 
	 * @param selection
	 *            selection of rows from the table.
	 * @return deleteAction Action object
	 */

	protected Action createDeleteAction(List selection) {
		DeleteAction deleteAction = new DeleteAction(selection);
		deleteAction.setEnabled(selection.size() > 0);
		return deleteAction;
	}

}
