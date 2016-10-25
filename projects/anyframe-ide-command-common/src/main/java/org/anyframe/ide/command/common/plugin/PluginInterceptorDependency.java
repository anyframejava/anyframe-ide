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
package org.anyframe.ide.command.common.plugin;

import java.io.Serializable;

/**
 * This is an PluginInterceptorDependency class. This object is converted from
 * plugin resource META-INF/anyframe/plugin.xml or plugin-build.xml using
 * XStream. This object is related to <dependency> in plugin.xml or
 * plugin-build.xml
 * 
 * <pre>
 * Example:
 * 	<interceptor>
 * 		<class>org.anyframe.plugin.interceptor.MipQueryPluginInterceptor</class>
 * 		<dependencies>
 *             ...
 * 			<dependency>
 * 				<groupId>commons-logging</groupId>
 * 				<artifactId>commons-logging</artifactId>
 * 				<version>1.1.1</version>
 * 			</dependency>
 * 		</dependencies>
 * 	</interceptor>
 * </pre>
 * 
 * @author SoYon Lim
 */
public class PluginInterceptorDependency implements Serializable {
	private static final long serialVersionUID = 1L;
	private String groupId;
	private String artifactId;
	private String version;
	private String scope;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

}
