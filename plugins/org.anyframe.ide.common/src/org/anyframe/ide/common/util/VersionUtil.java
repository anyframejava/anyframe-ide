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

import java.util.Dictionary;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.anyframe.ide.common.CommonActivator;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;

/**
 * This utility class finds out versions of Eclipse platform or Eclipse product.
 * 
 * @author RHIE Jihwan
 * 
 */
public class VersionUtil {

	/**
	 * Gets Eclipse platform version
	 * 
	 * @return Eclipse platform version (eg. 3.5.0.I20090604-2000)
	 */
	@SuppressWarnings({ "restriction", "unchecked" })
	public static String getPlatformVersion() {
		String version = null;

		try {
			Dictionary dictionary = org.eclipse.ui.internal.WorkbenchPlugin
					.getDefault().getBundle().getHeaders();
			version = (String) dictionary.get("Bundle-Version"); //$NON-NLS-1$
		} catch (NoClassDefFoundError e) {
			version = getProductVersion();
		}

		return version;
	}

	/**
	 * Gets Eclipse product version
	 * 
	 * @return Eclipse product version (eg. 3.5.0)
	 */
	public static String getProductVersion() {
		String version = null;

		try {
			IProduct product = Platform.getProduct();
			String aboutText = product.getProperty("aboutText"); //$NON-NLS-1$

			String pattern = "Version: (.*)\n"; //$NON-NLS-1$
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(aboutText);
			boolean found = m.find();

			if (found) {
				version = m.group(1);
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(CommonActivator.PLUGIN_ID, e.getMessage(), e);
		}

		return version;
	}

}
