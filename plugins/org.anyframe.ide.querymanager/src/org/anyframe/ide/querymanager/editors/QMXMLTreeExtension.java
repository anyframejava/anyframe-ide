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
 *
 *******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *
 */
package org.anyframe.ide.querymanager.editors;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.contentmodel.util.CMDescriptionBuilder;
import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
import org.eclipse.wst.xml.ui.internal.tabletree.TreeExtension;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLEditorMessages;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLTableTreePropertyDescriptorFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The class XMLTreeExtension extends TreeExtension
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QMXMLTreeExtension extends TreeExtension {

	public final static String STRUCTURE_PROPERTY = XMLEditorMessages.XMLTreeExtension_0;

	public final static String VALUE_PROPERTY = XMLEditorMessages.XMLTreeExtension_1;

	protected Composite composite;

	protected MyCellModifier myCellModifier;

	protected XMLTableTreePropertyDescriptorFactory propertyDescriptorFactory;

	protected CMDescriptionBuilder cmdescriptionBuilder = new CMDescriptionBuilder();

	protected QMTreeContentHelper queryMgrTreeContentHelper = new QMTreeContentHelper();

	protected Color forGroundColor1, forGroundColor2, backGroundColor1,
			backGroundColor2;

	protected boolean cachedDataIsValid = true;

	/**
	 * Constructor
	 * 
	 * @param queryMgrTree
	 *            The Tree object
	 */
	public QMXMLTreeExtension(Tree queryMgrTree) {
		super(queryMgrTree);
		composite = queryMgrTree;
		myCellModifier = new MyCellModifier();
		setCellModifier(myCellModifier);
		String[] treeProperties = { STRUCTURE_PROPERTY, VALUE_PROPERTY };
		setColumnProperties(treeProperties);

		forGroundColor1 = queryMgrTree.getDisplay().getSystemColor(
				SWT.COLOR_BLACK);
		Color background = queryMgrTree.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND);

		int red = Math.abs(background.getRed() - 125);
		int green = Math.abs(background.getGreen() - 85);
		int blue = Math.abs(background.getBlue() - 105);

		forGroundColor2 = new Color(queryMgrTree.getDisplay(), red, green, blue);
		backGroundColor1 = queryMgrTree.getDisplay().getSystemColor(
				SWT.COLOR_LIST_SELECTION);
		backGroundColor2 = background;

		propertyDescriptorFactory = new XMLTableTreePropertyDescriptorFactory();
	}

	/**
	 * dispose method for this object
	 */
	public void dispose() {
		super.dispose();
		forGroundColor2.dispose();
	}

	/**
	 * Mehtod to read cache data.
	 */
	public void resetCachedData() {
		cachedDataIsValid = false;
	}

	/**
	 * Helper method to repaint
	 * 
	 * @param gcObject
	 *            GC object
	 * @param treeItems
	 *            TreeItem array
	 * @param rectangleBounds
	 *            A Rectangle object
	 */
	public void paintItems(GC gcObject, TreeItem[] treeItems,
			Rectangle rectangleBounds) {
		super.paintItems(gcObject, treeItems, rectangleBounds);
		cachedDataIsValid = true;
	}

	/**
	 * @param elemObject
	 *            object of Object
	 * @return Object array
	 */
	protected Object[] computeTreeExtensionData(Object elemObject) {
		Color elemObjectColor = forGroundColor1;
		String nodeData = ""; //$NON-NLS-1$
		if (nodeData.length() == 0) {
			nodeData = (String) myCellModifier.getValue(elemObject,
					VALUE_PROPERTY);
			elemObjectColor = forGroundColor1;
		}
		if ((nodeData.length() == 0) && (elemObject instanceof Element)) {
			nodeData = getElementValueHelper((Element) elemObject);
			elemObjectColor = forGroundColor2;
		}
		Object[] arrData = new Object[2];
		arrData[0] = nodeData;
		arrData[1] = elemObjectColor;
		return arrData;
	}

	/**
	 * Helper method to repaint
	 * 
	 * @param gcObject
	 *            GC object
	 * @param treeItem
	 *            TreeItem
	 * @param rectangleBounds
	 *            A Rectangle object
	 */
	protected void paintItem(GC gcObject, TreeItem treeItem,
			Rectangle rectangleBounds) {
		super.paintItem(gcObject, treeItem, rectangleBounds);
		Object[] arrData = computeTreeExtensionData(treeItem.getData());
		if ((arrData != null) && (arrData.length == 2)) {
			gcObject.setClipping(columnPosition, rectangleBounds.y + 1,
					controlWidth, rectangleBounds.height);
			gcObject.setForeground((Color) arrData[1]);
			gcObject.drawString((String) arrData[0], columnPosition + 5,
					rectangleBounds.y + 1);
			gcObject.setClipping((Rectangle) null);
		}
	}

	/**
	 * @see org.eclipse.wst.xml.ui.internal.tabletree.TreeExtension#addEmptyTreeMessage(org.eclipse.swt.graphics.GC)
	 * @param gcObject
	 *            GC object
	 */
	protected void addEmptyTreeMessage(GC gcObject) {
		// here we print a message when the document is
		// empty just to give the
		// user a visual cue
		// so that they know how to proceed to edit the
		// blank view
		gcObject.setForeground(fTree.getDisplay().getSystemColor(
				SWT.COLOR_BLACK));
		gcObject.setBackground(fTree.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		gcObject.drawString(XMLEditorMessages.XMLTreeExtension_3, 10, 10);
		gcObject.drawString(XMLEditorMessages.XMLTreeExtension_4, 10,
				10 + gcObject.getFontMetrics().getHeight());
	}

	/**
	 * @param elemObject
	 *            The Element object
	 * @return String representing the value of the element
	 */
	public String getElementValueHelper(Element elemObject) {
		String nodeValue = null;

		ModelQuery modelQuery = ModelQueryUtil.getModelQuery(elemObject
				.getOwnerDocument());
		if ((nodeValue == null) && (modelQuery != null)) {
			CMElementDeclaration cmElementDeclaration = modelQuery
					.getCMElementDeclaration(elemObject);
			if ((cmElementDeclaration != null)
					&& !Boolean.TRUE.equals(cmElementDeclaration
							.getProperty("isInferred"))) { //$NON-NLS-1$
				nodeValue = cmdescriptionBuilder
						.buildDescription(cmElementDeclaration);
			}
		}
		return nodeValue != null ? nodeValue : ""; //$NON-NLS-1$
	}

	/**
     * 
     */
	public class MyCellModifier implements ICellModifier,
			TreeExtension.ICellEditorProvider {
		/**
		 * @param elemObject
		 *            The Element object
		 * @param propertyOfObj
		 *            property of the object
		 * @return boolean
		 */
		public boolean canModify(Object elemObject, String propertyOfObj) {
			boolean nodeValue = false;
			if (elemObject instanceof Node) {
				Node nodeObj = (Node) elemObject;
				nodeValue = (propertyOfObj == VALUE_PROPERTY)
						&& queryMgrTreeContentHelper.isEditable(nodeObj);
			}
			return nodeValue;
		}

		/**
		 * @param elemObject
		 *            The Element object
		 * @param propertyOfObj
		 *            property of the object
		 * @return Object The element object
		 */
		public Object getValue(Object elemObject, String propertyOfObj) {
			String nodeValue = null;
			if (elemObject instanceof Node) {
				nodeValue = queryMgrTreeContentHelper
						.getNodeValue((Node) elemObject);
			}
			return (nodeValue != null) ? nodeValue : ""; //$NON-NLS-1$
		}

		/**
		 * @param elemObject
		 *            The Element object
		 * @param propertyOfObj
		 *            property of the object
		 * @param nodeValue
		 *            the value of the object
		 */
		public void modify(Object elemObject, String propertyOfObj,
				Object nodeValue) {
			// enableNodeSelectionListener(false);
			Item tableItem = (Item) elemObject;
			String oldNodeValue = queryMgrTreeContentHelper
					.getNodeValue((Node) tableItem.getData());
			String newNodeValue = nodeValue.toString();
			if ((newNodeValue != null) && !newNodeValue.equals(oldNodeValue)) {
				queryMgrTreeContentHelper.setNodeValue(
						(Node) tableItem.getData(), nodeValue.toString());
			}
			// enableNodeSelectionListener(true);
		}

		/**
		 * @param elemObject
		 *            the object or element.
		 * @param couumnNo
		 *            the integer.
		 * @return The CellEditor object
		 */
		public CellEditor getCellEditor(Object elemObject, int couumnNo) {
			IPropertyDescriptor propertyDescriptor = propertyDescriptorFactory
					.createPropertyDescriptor(elemObject);
			return propertyDescriptor != null ? propertyDescriptor
					.createPropertyEditor(composite) : null;
		}
	}
}
