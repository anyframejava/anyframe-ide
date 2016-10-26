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
package org.anyframe.ide.codegenerator.model.table;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.util.HudsonRemoteAPI;
import org.anyframe.ide.common.util.ImageUtil;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * This is a PluginInfoLabelProvider class.
 * 
 * @author junghwan.hong
 * @author Sooyeon Park
 */
public class CtipInfoLabelProvider extends LabelProvider implements
		ITableLabelProvider {
	private final HudsonRemoteAPI hudson = new HudsonRemoteAPI();

	// icon for Agents at normal status
	private final ImageDescriptor _ctipServerNormalImageDescriptor = ImageUtil
			.getImageDescriptor(CodeGeneratorActivator.PLUGIN_ID,
					"images/status_online.png");
	private final Image _ctipServerNormalImag = ImageUtil
			.getImage(_ctipServerNormalImageDescriptor);

	// icon for Agents at abnormal status
	private final ImageDescriptor _ctipServerAbnormalImageDescriptor = ImageUtil
			.getImageDescriptor(CodeGeneratorActivator.PLUGIN_ID,
					"images/status_offline.png");
	private final Image _ctipServerAbnormalImage = ImageUtil
			.getImage(_ctipServerAbnormalImageDescriptor);

	// status blue, grey, red

	private final ImageDescriptor iconRedImageDescriptor = ImageUtil
			.getImageDescriptor(CodeGeneratorActivator.PLUGIN_ID,
					"images/icon_red.png");
	private final Image iconRedImage = ImageUtil.getImage(iconRedImageDescriptor);

	private final ImageDescriptor iconBlueImageDescriptor = ImageUtil
			.getImageDescriptor(CodeGeneratorActivator.PLUGIN_ID,
					"images/icon_blue.png");
	private final Image iconBlueImage = ImageUtil.getImage(iconBlueImageDescriptor);

	private final ImageDescriptor iconGreyImageDescriptor = ImageUtil
			.getImageDescriptor(CodeGeneratorActivator.PLUGIN_ID,
					"images/icon_black.png");
	private final Image iconGreyImage = ImageUtil.getImage(iconGreyImageDescriptor);

	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof java.lang.String && columnIndex == 0) {
			switch (columnIndex) {
			case 0: // checked
				if (isActiveServer(element.toString()))
					return _ctipServerNormalImag;
				else
					return _ctipServerAbnormalImage;
			default:
				break;
			}
		} else if (element instanceof java.lang.String && columnIndex != 0) {
			return null;
		}

		CtipDetailList detailList = (CtipDetailList) element;

		switch (columnIndex) {
		case 0:
			if (detailList.getStatus().equals("red")) {
				return iconRedImage;
			} else if (detailList.getStatus().equals("blue")) {
				return iconBlueImage;
			} else if (detailList.getStatus().equals("grey")) {
				return iconGreyImage;
			}
		default:
			break;
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		String result = "";

		if (element instanceof java.lang.String && columnIndex == 0) {
			switch (columnIndex) {
			case 0: // checked
				return element.toString();
			}
		} else if (element instanceof java.lang.String && columnIndex != 0) {
			return "";
		}
		CtipDetailList detailList = (CtipDetailList) element;

		switch (columnIndex) {
		case 0:
			return detailList.getJobName();
		case 1:
			return detailList.getBuildType();
		case 2:
			return detailList.getWorkSpace();
		case 3:
			return detailList.getScmServerType();
		case 4:
			return detailList.getScmServerUrl();
		case 5:
			return detailList.getSchedule();
		case 6:
			return detailList.getOtherProject();
		}
		return result;
	}

	private boolean isActiveServer(String ctipUrl) {
		hudson.setHudsonURL(ctipUrl.substring(ctipUrl.toLowerCase()
				.indexOf("http://")));
		try {
			hudson.getJobList();
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
}
