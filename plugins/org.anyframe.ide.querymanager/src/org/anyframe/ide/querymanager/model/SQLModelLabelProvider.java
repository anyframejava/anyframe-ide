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

import org.anyframe.ide.common.util.ImageUtil;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.services.ILabelService;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.services.LabelService;
import org.eclipse.datatools.connectivity.sqm.core.ui.explorer.virtual.IVirtualNode;
import org.eclipse.datatools.connectivity.sqm.core.ui.services.IDataToolsUIServiceManager;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * This is SQLModelLabelProvider class.
 * 
 * @author JungHwan Hong
 * @since 2.7.0
 */
public class SQLModelLabelProvider implements ILabelProvider {

	private LabelService labelService;

	private static final IDataToolsUIServiceManager imageService;

	static {
		imageService = IDataToolsUIServiceManager.INSTANCE;
	}

	public Image getImage(Object element) {
		// TODO Auto-generated method stub
		// return getImageService(element);
		return imageService.getLabelService(element).getIcon();
	}

	public String getText(Object parent) {
		// TODO Auto-generated method stub
		if (parent instanceof IVirtualNode)
			return ((IVirtualNode) parent).getDisplayName();
		else if (parent instanceof ENamedElement)
			return ((ENamedElement) parent).getName();
		else
			return getLabelService(parent).getName();
	}

	public void addListener(ILabelProviderListener ilabelproviderlistener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub
		ImageUtil.disposeImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
	}

	public boolean isLabelProperty(Object obj, String s) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener ilabelproviderlistener) {
		// TODO Auto-generated method stub

	}

	// private Image getImageService(Object element) {
	// 		return getLabelService(element).getIcon();
	// }

	private ILabelService getLabelService(Object element) {
		if (labelService == null)
			labelService = new LabelService();
		labelService.setElement(element);
		return labelService;
	}
}
