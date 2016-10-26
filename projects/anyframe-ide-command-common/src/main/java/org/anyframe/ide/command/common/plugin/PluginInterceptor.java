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
package org.anyframe.ide.command.common.plugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an PluginInterceptor class. This object is converted from plugin
 * resource META-INF/anyframe/plugin.xml or plugin-build.xml using XStream. This
 * object is related to <interceptor> in plugin.xml or plugin-build.xml
 * 
 * <pre>
 * Example:
 * 	<interceptor>
 * 		<class>org.anyframe.plugin.interceptor.MipQueryPluginInterceptor</class>
 * 		<dependencies>
 *             ...
 * 			<dependency>
 *				<groupId>org.slf4j</groupId>
 *				<artifactId>slf4j-log4j12</artifactId>
 *				<version>1.6.4</version>
 *			</dependency>
 * 		</dependencies>
 * 	</interceptor>
 * </pre>
 * 
 * @author SoYon Lim
 */
public class PluginInterceptor implements Serializable {
	private static final long serialVersionUID = 1L;

	private String className;
	private List<PluginInterceptorDependency> dependencies;

	public List<PluginInterceptorDependency> getDependencies() {
		if (this.dependencies == null) {
			return new ArrayList<PluginInterceptorDependency>();
		}
		return dependencies;
	}

	public void setDependencies(List<PluginInterceptorDependency> dependencies) {
		this.dependencies = dependencies;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
