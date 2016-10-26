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
package org.anyframe.ide.querymanager.dialogs;

import java.util.ArrayList;

import org.anyframe.ide.common.util.ImageUtil;
import org.anyframe.ide.querymanager.messages.Message;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Label provider for Query Explorer view.
 * 
 * @author Sujeong Lee
 * @since 2.1.0
 */
public class XMLOnlyLabelProvider extends StyledCellLabelProvider implements
		ILabelProvider {
	public static final String PLUGIN_ID = "org.anyframe.ide.querymanager";
	
	ImageDescriptor XMLFileImageDescriptor = ImageUtil
			.getImageDescriptor(PLUGIN_ID, Message.image_properties_xmlfile);
	Image XMLFileImage = ImageUtil.getImage(XMLFileImageDescriptor);

	private static final ISharedImages PLATFORM_IMAGES = PlatformUI
			.getWorkbench().getSharedImages();

	public String getText(Object obj) {
		if (obj instanceof ArrayList) {
			return ((ArrayList) obj).toArray()[0].toString();
		} else if (obj instanceof PropertyAddInfo) {
			return ((PropertyAddInfo) obj).getName();
		} else {
			return obj.toString();
		}
	}

	public Image getImage(Object obj) {
		return null;
	}

	public void update(ViewerCell cell) {
		Object obj = cell.getElement();

		if (obj instanceof ArrayList) {
		} else if (obj instanceof PropertyAddInfo) {
			if (((PropertyAddInfo) obj)
					.getType()
					.equals(Message.property_dialog_scan_deselectall_title)) {
				cell.setImage(PLATFORM_IMAGES
						.getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER));
			} else {
				cell.setImage(PLATFORM_IMAGES
						.getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER));
			}
			cell.setText(((PropertyAddInfo) obj).getName().substring(1));
		} else {
			cell.setText(obj.toString());
			cell.setImage(XMLFileImage);
		}
	}

	public void dispose() {
		ImageUtil.disposeImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
		ImageUtil
				.disposeImage(Message.image_properties_xmlfile);
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}
