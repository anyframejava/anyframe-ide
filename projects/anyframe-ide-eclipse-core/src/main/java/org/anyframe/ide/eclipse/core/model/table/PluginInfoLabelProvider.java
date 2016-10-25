/*   
 * Copyright 2008-2011 the original author or authors.   
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
package org.anyframe.ide.eclipse.core.model.table;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * This is a PluginInfoLabelProvider class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class PluginInfoLabelProvider extends LabelProvider implements
        ITableLabelProvider {

    public static final String CHECKED_IMAGE = "icon_checked";
    public static final String UNCHECKED_IMAGE = "icon_unchecked";

    private static ImageRegistry imageRegistry = new ImageRegistry();

    static {
        imageRegistry.put(CHECKED_IMAGE, ImageDescriptor.createFromFile(
            PluginInfoLabelProvider.class,
            MessageUtil.getMessage("image.checked")));

        imageRegistry.put(UNCHECKED_IMAGE, ImageDescriptor.createFromFile(
            PluginInfoLabelProvider.class,
            MessageUtil.getMessage("image.unchecked")));
    }

    private Image getImage(boolean isSelected) {
        String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
        return imageRegistry.get(key);
    }

    public Image getColumnImage(Object element, int columnIndex) {
        Image result = null;
        if (columnIndex == 0)
            result = getImage(((PluginInfo) element).isChecked());

        return result;
    }

    public String getColumnText(Object element, int columnIndex) {
        String result = "";
        PluginInfo pluginInfo = (PluginInfo) element;

        switch (columnIndex) {
        case 0: // checked
            break;
        case 1:
            result = pluginInfo.getName();
            break;
        case 2:
            result = pluginInfo.getGroupId();
            break;
        case 3:
            result = pluginInfo.getArtifactId();
            break;
        case 4:
            result = pluginInfo.getLatestVersion();
            break;
        case 5:
            result = pluginInfo.isInstalled() ? pluginInfo.getVersion() : "X";
            break;
        default:
            break;
        }

        return result;
    }
}
