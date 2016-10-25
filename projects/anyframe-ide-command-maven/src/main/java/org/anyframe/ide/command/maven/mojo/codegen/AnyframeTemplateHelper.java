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
package org.anyframe.ide.command.maven.mojo.codegen;

import java.io.File;

import org.hibernate.tool.hbm2x.TemplateHelper;

/**
 * This is an AnyframeTemplateHelper class to set default encoding and output
 * encoding.
 * 
 * @author SooYeon Park
 */
public class AnyframeTemplateHelper extends TemplateHelper {

	public void init(File outputDirectory, String[] templatePaths) {
		super.init(outputDirectory, templatePaths);
		freeMarkerEngine.setDefaultEncoding("UTF-8");
		freeMarkerEngine.setOutputEncoding("UTF-8");
	}
}
