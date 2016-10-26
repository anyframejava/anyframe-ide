/*
 * Copyright 2002-2013 the original author or authors.
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
package org.anyframe.ide.common.preferences;

import org.anyframe.ide.common.CommonActivator;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This is PreferencePage class.
 * 
 * @author Joonil Kim
 */
public class PreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * The Constructor
	 */
	public PreferencePage() {
		super(GRID);
		setPreferenceStore(CommonActivator.getInstance().getPreferenceStore());
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
	}

	/**
	 * @return boolean
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean value = super.performOk();
		CommonActivator.getInstance().savePluginPreferences();
		return value;
	}

	/**
	 * @param workbench
	 *            IWorkbench
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// do some initialization
	}

	/**
	 * @param field
	 *            FieldEditor
	 */
	private void initField(FieldEditor field) {
		field.setPreferenceStore(getPreferenceStore());
		try {
			// Eclipse 3.0
			field.setPreferencePage(this);
		} catch (Error e) {
			// OK
			PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, "Eclipse 3.0 Field Init has error + " + e.getMessage() + ".");
		}
		// Eclipse 3.1
		try {
			field.setPage(this);
		} catch (Error e) {
			// OK
			PluginLoggerUtil.info(CommonActivator.PLUGIN_ID, "Eclipse 3.1 Field Init has error + " + e.getMessage() + ".");
		}
		field.load();
	}

}
