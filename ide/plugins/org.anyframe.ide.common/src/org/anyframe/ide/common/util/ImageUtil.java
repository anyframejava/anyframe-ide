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
package org.anyframe.ide.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.messages.Message;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This is ImageUtil class.
 * 
 * @author Sujeong Lee
 */
public class ImageUtil extends AbstractUIPlugin {

	private final static Map<ImageDescriptor, Image> IMAGECACHE = new HashMap<ImageDescriptor, Image>();

	public static Image getImage(String pid, String imageName) {
		return CommonActivator.getDefault().getImageRegistry().get(imageName);
	}

	public static ImageDescriptor getImageDescriptor(String pid,
			String imagePath) {
		return imageDescriptorFromPlugin(pid, imagePath);
	}

	public static Image getImage(ImageDescriptor imageDescriptor) {
		if (imageDescriptor == null) {
			return null;
		}

		Image image = IMAGECACHE.get(imageDescriptor);
		if (image == null) {
			Image createImage = imageDescriptor.createImage();
			IMAGECACHE.put(imageDescriptor, createImage);
		}
		return IMAGECACHE.get(imageDescriptor);
	}

	public static void disposeImage(String imageProperty) {
		try {
			Image image = (Image) IMAGECACHE.get(imageProperty);

			if (image == null) {
				return;

			}

			Iterator<Image> iter = IMAGECACHE.values().iterator();
			while (iter.hasNext()) {
				iter.next().dispose();
				IMAGECACHE.remove(imageProperty);
			}

		} catch (Throwable throwable) {
			PluginLoggerUtil.error("org.anyframe.ide.common.util.ImageUtil",
					Message.exception_disposingimage, throwable);
		}
	}

	public void disposeImages() {
		Iterator<Image> iter = IMAGECACHE.values().iterator();
		while (iter.hasNext()) {
			iter.next().dispose();
		}

		IMAGECACHE.clear();
	}

}
