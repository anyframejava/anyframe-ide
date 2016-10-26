/*
 * Copyright 2002-2012 the original author or authors.
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
package org.anyframe.ide.command.ant.task;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.command.common.util.FileUtil;

/**
 * AbstractTaskTest is a commom class for other test classes.
 * 
 * @author Soyon Lim
 */
public abstract class AbstractTaskTest extends TestCase {
	protected String currentPath = new File(".").getAbsolutePath();
	protected String anyframeHome = currentPath
			+ CommonConstants.SRC_TEST_RESOURCES;
	protected File temporaryDir;

	/**
	 * initialize
	 */
	public void setUp() throws Exception {
		System.setProperty("user.home", currentPath
				+ CommonConstants.SRC_TEST_RESOURCES);

		temporaryDir = new File("./temp");
		if (temporaryDir.exists()) {
			FileUtil.deleteDir(temporaryDir, new ArrayList<String>());
		}
	}
}
