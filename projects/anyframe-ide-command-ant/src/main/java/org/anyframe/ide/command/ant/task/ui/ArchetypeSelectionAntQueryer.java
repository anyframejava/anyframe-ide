/*
 * Copyright 2002-2008 the original author or authors.
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
package org.anyframe.ide.command.ant.task.ui;

import java.util.Formatter;
import java.util.List;
import java.util.Vector;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;

/**
 * This is an ArchetypeSelectionAntQueryer class. This class is for supporting
 * selection-archetype ui through console
 * 
 * @author Jeryeon Kim
 */
public class ArchetypeSelectionAntQueryer {

	public PluginInfo selectArchetype(List<PluginInfo> archetypeList)
			throws BuildException {
		StringBuffer queryBuffer = new StringBuffer();

		// 1. show installable arghetype list
		Formatter formatter = new Formatter();
		formatter.format(CommonConstants.ARCHETYPE_NAME_VERSION, "no",
				"archetype name", "version");
		queryBuffer.append(formatter.toString() + "\n");
		queryBuffer
				.append("------------------------------------------------------------ \n");

		Vector<String> varargs = new Vector<String>();

		int i = 0;

		for (PluginInfo archetype : archetypeList) {
			formatter = new Formatter();
			formatter.format(CommonConstants.ARCHETYPE_NAME_VERSION, ++i,
					archetype.getArtifactId(), archetype.getVersion());
			queryBuffer.append(formatter.toString() + "\n");
			varargs.add("" + i);
		}
		queryBuffer.append("Choose a number: ");

		// 2. select plugin
		InputRequest request = new MultipleChoiceInputRequest(queryBuffer
				.toString(), varargs);

		InputHandler handler = new DefaultInputHandler();
		handler.handleInput(request);
		String answer = request.getInput();

		// 3. get plugin info
		PluginInfo pluginInfo = archetypeList.get(new Integer(answer)
				.intValue() - 1);
		if (pluginInfo == null) {
			System.out.println("Please, choose a number between 1 and "
					+ archetypeList.size() + " [selected number : " + answer
					+ "].");
			System.exit(0);
		}
		return pluginInfo;
	}
}
