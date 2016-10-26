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

import java.util.ArrayList;
import java.util.HashMap;

import org.anyframe.ide.common.util.ImageUtil;
import org.anyframe.ide.querymanager.messages.Message;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Tree content provider for Query Explorer view.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class QueryExplorerViewLabelProvider extends StyledCellLabelProvider
		implements ILabelProvider {
	public static final String PLUGIN_ID = "org.anyframe.ide.querymanager";
	
	ImageDescriptor XMLFileImageDescriptor = ImageUtil
			.getImageDescriptor(PLUGIN_ID, Message.image_explorer_xmlfile);
	Image XMLFileImage = ImageUtil.getImage(XMLFileImageDescriptor);

	ImageDescriptor usedQueryIDImageDescriptor = ImageUtil
			.getImageDescriptor(PLUGIN_ID, Message.image_explorer_usedid);
	Image usedQueryIDImage = ImageUtil.getImage(usedQueryIDImageDescriptor);

	ImageDescriptor unusedQueryIDImageDescriptor = ImageUtil
			.getImageDescriptor(PLUGIN_ID, Message.image_explorer_unusedid);
	Image unusedQueryIDImage = ImageUtil.getImage(unusedQueryIDImageDescriptor);

	ImageDescriptor usedQueryIDDuplicatedImageDescriptor = ImageUtil
			.getImageDescriptor(PLUGIN_ID, Message.image_explorer_usediddup);
	Image usedQueryIDDuplicatedImage = ImageUtil
			.getImage(usedQueryIDDuplicatedImageDescriptor);

	ImageDescriptor unusedQueryIDDuplicatedImageDescriptor = ImageUtil
			.getImageDescriptor(PLUGIN_ID, Message.image_explorer_unusediddup);
	Image unusedQueryIDDuplicatedImage = ImageUtil
			.getImage(unusedQueryIDDuplicatedImageDescriptor);

	private HashMap daoMap = new HashMap();
	private HashMap duplicateIds = new HashMap();
	private HashMap duplAliasIdsMap = new HashMap();

	private static final ISharedImages PLATFORM_IMAGES = PlatformUI
			.getWorkbench().getSharedImages();

	public String getText(Object obj) {
		if (obj instanceof ArrayList) {
			return ((ArrayList) obj).get(0).toString();
		} else if (obj instanceof QueryTreeVO) {
			return ((QueryTreeVO) obj).getProject();
		} else if (obj instanceof FileInfoVO) {
			String fileFullPath = ((FileInfoVO) obj).getFileName();
			int index = fileFullPath.lastIndexOf("/"); //$NON-NLS-1$
			String fileName = fileFullPath.substring(index + 1);
			return fileName;
		} else {
			return obj.toString();
		}
	}

	public Image getImage(Object element) {

		if (element instanceof ArrayList) {
			return PLATFORM_IMAGES
					.getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
		} else if (element instanceof QueryTreeVO) {
			return PLATFORM_IMAGES
					.getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
		} else if (element instanceof FileInfoVO) {
			return XMLFileImage;
		} else {
			return usedQueryIDImage;
		}
	}

	public void update(ViewerCell cell) {

		Object obj = cell.getElement();
		if (obj instanceof ArrayList) {
			cell.setText(((ArrayList) obj).get(0).toString());
		} else if (obj instanceof QueryTreeVO) {
			cell.setText(((QueryTreeVO) obj).getProject());
			cell.setImage(PLATFORM_IMAGES
					.getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_PROJECT));
		} else if (obj instanceof FileInfoVO) {
			String alias = ((FileInfoVO) obj).getAlias();

			String fileFullPath = ((FileInfoVO) obj).getFileName();
			int index = fileFullPath.lastIndexOf("/"); //$NON-NLS-1$
			String filePath = fileFullPath.substring(0, index);
			String fileName = fileFullPath.substring(index + 1);

			String decoration = " [" + filePath + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			StyledString styledString = null;

			if (alias == null || alias.equals("")) { //$NON-NLS-1$
				styledString = new StyledString(fileName, null);
			} else {
				styledString = new StyledString(alias, null);
			}

			styledString.append(decoration, StyledString.DECORATIONS_STYLER);
			cell.setText(styledString.toString());
			cell.setStyleRanges(styledString.getStyleRanges());
			cell.setImage(XMLFileImage);
		} else {
			String id = obj.toString();
			int index = id
					.indexOf(Message.view_explorer_util_queryid_suffix);
			String queryID = id.substring(1);

			if (index == -1) {
				// duplicate
				if (id.substring(0, 1).equals("O")) { //$NON-NLS-1$
					// used in dao
					cell.setText(queryID);
					cell.setImage(usedQueryIDImage);
				} else {
					// un-unsed in dao
					cell.setText(queryID);
					// cell.setImage(usedQueryIDImage);
					cell.setImage(unusedQueryIDImage);
				}
			} else {
				// non-duplicate
				if (id.substring(0, 1).equals("O")) { //$NON-NLS-1$
					cell.setText(queryID.substring(0, index - 1));
					cell.setImage(usedQueryIDDuplicatedImage);
				} else {
					cell.setText(queryID.substring(0, index - 1));
					// cell.setImage(usedQueryIDDuplicatedImage);
					cell.setImage(unusedQueryIDDuplicatedImage);

				}
			}
		}
	}

	public void dispose() {
		ImageUtil.disposeImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
		ImageUtil
				.disposeImage(Message.image_explorer_xmlfile);
		ImageUtil
				.disposeImage(Message.image_explorer_usedid);
		ImageUtil
				.disposeImage(Message.image_explorer_unusedid);
		ImageUtil
				.disposeImage(Message.image_explorer_usediddup);
		ImageUtil
				.disposeImage(Message.image_explorer_unusediddup);

	}

	public void addListener(ILabelProviderListener listener) {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}
