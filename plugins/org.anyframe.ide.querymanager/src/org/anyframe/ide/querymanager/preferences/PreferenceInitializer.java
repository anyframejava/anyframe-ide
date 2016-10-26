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
package org.anyframe.ide.querymanager.preferences;

import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Concrete implementation of the PreferenceInitializer class. This class
 * initialize the Default values for the preference pages.
 * 
 * @author Sreejesh.Nair
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	// the values are dummy ...this should be replaced
	public static final String CreateText_DefaultValue = "create";
	public static final String UpdatePrefixText_DefaultValue = "update";
	public static final String RemovePrefix_DefaultValue = "remove";
	public static final String FindPrefix_DefaultValue = "find";
	public static final String FindListSuffix_DefaultValue = "List";
	public static final String FindByPKSuffix_DefaultValue = "ByPk";
	public static final String ValueObjectPrefix = "vo";

	/** Setting the Default Values for the Anyframe Preference page */

	public void initializeDefaultPreferences() {

		final IPreferenceStore store = QueryManagerActivator.getDefault()
				.getPreferenceStore();

		/** Setting the Default Values for the Anyframe Preference page */

		store.setDefault(AnyframePreferencePage.CREATE_PREFIX_ID,
				CreateText_DefaultValue);
		store.setDefault(AnyframePreferencePage.UPDATE_PREFIX_ID,
				UpdatePrefixText_DefaultValue);
		store.setDefault(AnyframePreferencePage.REMOVE_PREFIX_ID,
				RemovePrefix_DefaultValue);
		store.setDefault(AnyframePreferencePage.FIND_PREFIX_ID,
				FindPrefix_DefaultValue);
		store.setDefault(AnyframePreferencePage.FIND_LIST_SUFFIX_ID,
				FindListSuffix_DefaultValue);
		store.setDefault(AnyframePreferencePage.FIND_BYPK_SUFFIX_ID,
				FindByPKSuffix_DefaultValue);
		store.setDefault(AnyframePreferencePage.VALUE_OBJECT_PREFIX_ID,
				ValueObjectPrefix);

	}

}
