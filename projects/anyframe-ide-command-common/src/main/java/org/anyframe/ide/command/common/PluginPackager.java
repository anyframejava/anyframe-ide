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
package org.anyframe.ide.command.common;

import org.apache.maven.archetype.ArchetypeGenerationRequest;

/**
 * This is a PluginPackager interface class. This class is for packaging a new
 * plugin with plugin resources.
 * 
 * @author Sooyeon Park
 */
public interface PluginPackager {
	/**
	 * package a new plugin
	 * 
	 * @param request
	 *            information includes maven repository, db settings, etc.
	 * @param baseDir
	 *            plugin project root folder which has plugin sample codes
	 * @throws Exception
	 */
	void packagePlugin(ArchetypeGenerationRequest request, String baseDir) throws Exception;

}
