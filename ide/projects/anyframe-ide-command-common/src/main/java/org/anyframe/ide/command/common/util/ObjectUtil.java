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
package org.anyframe.ide.command.common.util;

import java.io.File;
import java.lang.reflect.Method;

/**
 * This is an ObjectUtils class. This class is a utility for Object.
 * 
 * @author SoYon Lim
 */
public class ObjectUtil {

	public static Class loadClass(ClassLoader pluginInterceptorLoader,
			File pluginJarFile, String interceptorlClass) throws Exception {
		Class clazz = null;

		try {
			clazz = pluginInterceptorLoader.loadClass(interceptorlClass);
		} catch (ClassNotFoundException ce) {
			throw new Exception("Can't find a interceptor class in "
					+ pluginJarFile.getAbsolutePath());
		}
		
		return clazz;
	}

	public static void invokeMethod(Class clazz, String methodName,
			String baseDir, File pluginJarFile) throws Exception {

		Method method = null;

		if (clazz != null) {
			try {
				method = clazz.getMethod(methodName, new Class[] {
						String.class, File.class });
			} catch (Exception me) {
				// ignored
				return;
			}

			if (method != null)
				method.invoke(clazz.newInstance(), new Object[] { baseDir,
						pluginJarFile });
		}
	}
}
