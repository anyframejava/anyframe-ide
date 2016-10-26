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
package org.anyframe.ide.command.common;

import org.apache.maven.archetype.ArchetypeGenerationRequest;

/**
 * This is an DefaultPluginDBChanger interface class. This class is for changing
 * all db configurations based on selected db
 * 
 * @author Sooyeon Park
 */
public interface PluginDBChanger {
	/**
	 * change db configuration and data
	 * 
	 * @param request
	 *            information includes maven repository, db settings, plugin
	 * @param baseDir
	 *            current project folder
	 * @param encoding
	 *            file encoding style
	 */
	void change(ArchetypeGenerationRequest request, String baseDir,
			String encoding) throws Exception;
}
