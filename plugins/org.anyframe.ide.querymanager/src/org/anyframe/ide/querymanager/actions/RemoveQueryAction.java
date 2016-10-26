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
package org.anyframe.ide.querymanager.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.querymanager.DTDResolver;
import org.anyframe.ide.querymanager.build.Location;
import org.anyframe.ide.querymanager.messages.MessagePropertiesLoader;
import org.anyframe.ide.querymanager.model.FileInfoVO;
import org.anyframe.ide.querymanager.model.QueryTreeVO;
import org.anyframe.ide.querymanager.util.AbstractQueryManagerAction;
import org.anyframe.ide.querymanager.views.QMExplorerView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Remove selected query ids. This class extends AbstractQueryManagerAction
 * class.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class RemoveQueryAction extends AbstractQueryManagerAction {

	public RemoveQueryAction() {
		super(
				MessagePropertiesLoader.view_explorer_action_remove_query_title,
				MessagePropertiesLoader.view_explorer_action_remove_query_desc,
				MessagePropertiesLoader.image_explorer_action_removiequery);
	}

	/**
	 * Run this Action
	 */
	public void run() {

		ISelection selected = (ISelection) QMExplorerView.getSelectedId();
		TreeSelection treeSelection = (TreeSelection) selected;

		if (treeSelection.size() == 1) {
			Object obj = treeSelection.getFirstElement();
			String select = obj.toString().substring(1);
			String selectedId = "";

			int start = select.indexOf("_duplicate_classification_");
			if (start != -1) {
				selectedId = select.substring(0, start);
			} else {
				selectedId = select;
			}

			if (MessageDialogUtil
					.confirmMessageDialog(
							MessagePropertiesLoader.view_explorer_action_remove_query_confirm_title,
							MessagePropertiesLoader.view_explorer_action_remove_query_confirm
									+ selectedId
									+ MessagePropertiesLoader.view_explorer_action_remove_query_confirm_suffix)) {

				Object ob = null;
				if (obj != null) {
					TreePath path = ((TreeSelection) selected).getPaths()[0];

					String project = "";
					String file = "";
					for (int i = 0; i < path.getSegmentCount(); i++) {
						if (path.getSegment(i) instanceof QueryTreeVO) {
							project = ((QueryTreeVO) path.getSegment(i))
									.getProject();
						} else if (path.getSegment(i) instanceof FileInfoVO) {
							file = ((FileInfoVO) path.getSegment(i))
									.getFileName();
						} else {

						}
					}

					ob = ((FileInfoVO) ((TreeSelection) selected).getPaths()[0]
							.getSegment(1)).getQueryId().get(obj);
					Location location = (Location) ob;

					try {
						removeIDfromFile(location.getFile(), selectedId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			ArrayList<String> selectedList = new ArrayList<String>();
			for (int i = 0; i < treeSelection.size(); i++) {
				selectedList.add(treeSelection.toArray()[i].toString()
						.substring(1));
			}

			if (MessageDialogUtil.confirmMessageDialog(
					"Confirm Delete",
					"Are you sure you want to delete the Query ID '"
							+ selectedList.toString().substring(1,
									selectedList.toString().length() - 1)
							+ "'?")) {
				for (int i = 0; i < treeSelection.size(); i++) {
					Object obj = treeSelection.toList().get(i);
					String selectedId = obj.toString().substring(1);

					TreePath path = ((TreeSelection) selected).getPaths()[0];

					String project = "";
					String file = "";
					for (int n = 0; n < path.getSegmentCount(); n++) {
						if (path.getSegment(n) instanceof QueryTreeVO) {
							project = ((QueryTreeVO) path.getSegment(n))
									.getProject();
						} else if (path.getSegment(n) instanceof FileInfoVO) {
							file = ((FileInfoVO) path.getSegment(n))
									.getFileName();
						} else {

						}
					}

					Object ob = ((FileInfoVO) ((TreeSelection) selected)
							.getPaths()[0].getSegment(1)).getQueryId().get(obj);
					Location location = (Location) ob;

					try {
						removeIDfromFile(location.getFile(), selectedId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void removeIDfromFile(IFile ifile, String select) {
		File file = ifile.getLocation().toFile();

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			db.setEntityResolver(new DTDResolver());
			Document doc = db.parse(file);

			Element root = doc.getDocumentElement();

			NodeList children = doc.getElementsByTagName("query"); //$NON-NLS-1$
			int size = children.getLength();
			for (int i = 0; i < size; i++) {
				Element node = (Element) children.item(i);
				if (node != null) {
					if (children.item(i).getAttributes().getNamedItem("id") //$NON-NLS-1$
							.getNodeValue().equals(select)) {

						for (int n = 0; n < root.getChildNodes().getLength(); n++) {
							Object o = root.getChildNodes().item(n);
							// if (o instanceof DeferredElementImpl) {
							if (o.toString().indexOf("queries") > -1) { //$NON-NLS-1$
								Node queriesNode = root.getChildNodes().item(n);

								root.getChildNodes().item(n)
										.removeChild(children.item(i));

							}
							// }
						}
					}
				}
			}

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");

			try {
				String publicID = doc.getDoctype().getPublicId();
				String systemID = doc.getDoctype().getSystemId();
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
						publicID);
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
						systemID);
			} catch (Exception e) {
				// this is not a file using dtd. using xsd file.
			}

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"));

			PrintWriter pw = new PrintWriter(out);

			StreamResult result = new StreamResult(pw);
			transformer.transform(source, result);

			pw.close();
			out.close();

			ifile.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		new QMExplorerView().refresh();
	}

	/**
	 * Available check this method
	 */
	public boolean isAvailable() {
		// if (getView() == null) {
		// 		return false;
		// }

		return true;
	}

	class NameSorter extends ViewerSorter {
	}
}
