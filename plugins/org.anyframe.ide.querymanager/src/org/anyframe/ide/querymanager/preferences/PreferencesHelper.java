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
package org.anyframe.ide.querymanager.preferences;

import java.util.HashMap;

import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This is PreferencesHelper class.
 * 
 * @author Surindhar.Kondoor
 */
public class PreferencesHelper {
	// Only object of this class.
	private static PreferencesHelper preferencesHelper;
	private static IPreferenceStore store;

	/**
	 * Private Constructor. Restrict access.
	 */
	private PreferencesHelper() {

	}

	/**
	 * Helper method to get the singleton object of this class.
	 * 
	 * @return
	 */
	public static PreferencesHelper getPreferencesHelper() {
		if (preferencesHelper == null)
			preferencesHelper = new PreferencesHelper();
		if (store == null)
			store = QueryManagerActivator.getDefault().getPreferenceStore();
		return preferencesHelper;
	}

	/**
	 * 
	 * @param PreferenceID
	 * @return
	 */
	public String getCreatePrefix() {
		return store.getString(AnyframePreferencePage.CREATE_PREFIX_ID).equals(
				"") ? PreferenceInitializer.CreateText_DefaultValue : store
				.getString(AnyframePreferencePage.CREATE_PREFIX_ID);
	}

	public String getUpdatePrefix() {
		return store.getString(AnyframePreferencePage.UPDATE_PREFIX_ID).equals(
				"") ? PreferenceInitializer.UpdatePrefixText_DefaultValue
				: store.getString(AnyframePreferencePage.UPDATE_PREFIX_ID);
	}

	public String getFindByPKSuffix() {
		return store.getString(AnyframePreferencePage.FIND_BYPK_SUFFIX_ID)
				.equals("") ? PreferenceInitializer.FindByPKSuffix_DefaultValue
				: store.getString(AnyframePreferencePage.FIND_BYPK_SUFFIX_ID);
	}

	public String getFindListSuffix() {
		return store.getString(AnyframePreferencePage.FIND_LIST_SUFFIX_ID)
				.equals("") ? PreferenceInitializer.FindListSuffix_DefaultValue
				: store.getString(AnyframePreferencePage.FIND_LIST_SUFFIX_ID);
	}

	public String getRemovePrefix() {
		return store.getString(AnyframePreferencePage.REMOVE_PREFIX_ID).equals(
				"") ? PreferenceInitializer.RemovePrefix_DefaultValue : store
				.getString(AnyframePreferencePage.REMOVE_PREFIX_ID);
	}

	public String getFindPrefix() {
		return store.getString(AnyframePreferencePage.FIND_PREFIX_ID)
				.equals("") ? PreferenceInitializer.FindPrefix_DefaultValue
				: store.getString(AnyframePreferencePage.FIND_PREFIX_ID);
	}

	public String[] populateArrayWithPreferences(String[] arr) {
		arr = new String[6];
		arr[0] = getCreatePrefix();
		arr[1] = getUpdatePrefix();
		arr[2] = getFindPrefix();
		arr[3] = getRemovePrefix();
		arr[4] = getFindByPKSuffix();
		arr[5] = getFindListSuffix();
		return arr;
	}

	public HashMap populateHashMapWithPreferences(HashMap map) {
		map.put(AnyframePreferencePage.CREATE_PREFIX_ID, getCreatePrefix());
		map.put(AnyframePreferencePage.FIND_PREFIX_ID, getFindPrefix());
		map.put(AnyframePreferencePage.REMOVE_PREFIX_ID, getRemovePrefix());
		map.put(AnyframePreferencePage.UPDATE_PREFIX_ID, getUpdatePrefix());
		map.put(AnyframePreferencePage.FIND_BYPK_SUFFIX_ID, getFindByPKSuffix());
		map.put(AnyframePreferencePage.FIND_LIST_SUFFIX_ID, getFindListSuffix());
		map.put(AnyframePreferencePage.FIND_LIST_WITH_PAGING_SUFFIX_ID,
				"findListWithPaging");
		return map;
	}

	public HashMap populateHashMapWithAbstractDAOMethodNames() {
		HashMap map = new HashMap();
		map.put(AnyframePreferencePage.CREATE_PREFIX_ID, "create");
		map.put(AnyframePreferencePage.FIND_PREFIX_ID, "find");
		map.put(AnyframePreferencePage.REMOVE_PREFIX_ID, "remove");
		map.put(AnyframePreferencePage.UPDATE_PREFIX_ID, "update");
		map.put(AnyframePreferencePage.FIND_BYPK_SUFFIX_ID, "findByPk");
		map.put(AnyframePreferencePage.FIND_LIST_SUFFIX_ID, "findList");
		map.put(AnyframePreferencePage.FIND_LIST_WITH_PAGING_SUFFIX_ID,
				"findListWithPaging");
		return map;
	}

	public String getVOPrefix() {
		return store.getString(AnyframePreferencePage.VALUE_OBJECT_PREFIX_ID);
	}

	public String getTheDisplayValueForQuery(String queryId) {
		/*
		 * 1. If queryId startwith "findPrefix" and ends with "findByPK Suffix"
		 * 2. If queryId startwith "findPrefix" and ends with "findList suffix"
		 * 3. if query id starts with any other (four categories 1.findPrefix,
		 * 2.remove prefix, 3. update prefix and 4. create)
		 */
		StringBuffer retStrBuf = new StringBuffer("");
		HashMap preferencesMap = new HashMap();
		preferencesMap = preferencesHelper
				.populateHashMapWithPreferences(preferencesMap);
		String findPrefix = preferencesMap.get(
				AnyframePreferencePage.FIND_PREFIX_ID).toString();
		String findByPkSuffix = preferencesMap.get(
				AnyframePreferencePage.FIND_BYPK_SUFFIX_ID).toString();
		String findListSuffix = preferencesMap.get(
				AnyframePreferencePage.FIND_LIST_SUFFIX_ID).toString();
		String createPrefix = preferencesMap.get(
				AnyframePreferencePage.CREATE_PREFIX_ID).toString();
		String removePrefix = preferencesMap.get(
				AnyframePreferencePage.REMOVE_PREFIX_ID).toString();
		String updatePrefix = preferencesMap.get(
				AnyframePreferencePage.UPDATE_PREFIX_ID).toString();
		if (queryId.startsWith(findPrefix) && queryId.endsWith(findByPkSuffix)) {
			retStrBuf.append(queryId.substring(findPrefix.length(),
					queryId.indexOf(findByPkSuffix)));
		} else if (queryId.startsWith(findPrefix)
				&& queryId.endsWith(findListSuffix)) {
			retStrBuf.append(queryId.substring(findPrefix.length(),
					queryId.indexOf(findListSuffix)));
		} else if (queryId.startsWith(findPrefix)) {
			retStrBuf.append(queryId.substring(findPrefix.length()));
		} else if (queryId.startsWith(createPrefix)) {
			retStrBuf.append(queryId.substring(createPrefix.length()));
		} else if (queryId.startsWith(removePrefix)) {
			retStrBuf.append(queryId.substring(removePrefix.length()));
		} else if (queryId.startsWith(updatePrefix)) {
			retStrBuf.append(queryId.substring(updatePrefix.length()));
		} else {
			retStrBuf.append("");
		}
		return retStrBuf.toString();
	}
}
