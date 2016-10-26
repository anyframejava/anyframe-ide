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
package org.anyframe.ide.common.dialog;

import java.util.List;
import java.util.Map;

import org.anyframe.ide.common.databases.JdbcOption;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * This is IDBSettingDialog class.
 * 
 * @author Sujeong Lee
 */
public interface IDBSettingDialog {
	public void init(IProject project, List<JdbcType> jdbcTypes);

	public void createUI(Composite composite);

	public void loadSettings(JdbcType type);

	public void saveSettings(IProject project, JdbcOption jdbcOption,
			boolean isChangedDBConfig);

	public void setDatabaseTypeSelectListener(SelectionEvent e);

	public Map<String, String> getInputData();

	public String getLocalRepositoryPath();

}
