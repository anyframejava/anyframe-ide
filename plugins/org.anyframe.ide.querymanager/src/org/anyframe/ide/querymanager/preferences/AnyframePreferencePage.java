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

import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Concrete implementation of the PreferencePage abstract class. Overrides all
 * the abstract methods.The createContents(..) method provides the UI part of
 * console preference page.
 * 
 * @author Sreejesh.Nair
 */
public class AnyframePreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * AnyframePreferencePage consists of Text Fields where Abstract DAO prefix
	 * and suffix can be set
	 * 
	 */

	/* Widgets used */

	static Text findPKSufix;
	static Text createPrefix;
	static Text updatePrefix;
	static Text removePrefix;
	static Text findPrefix;
	static Text findListSufix;
	static Text valueObjectPrefix;

	/* Label of the text fields */

	private final String CREATE_PREFIX = "Create Prefix:";
	private final String UPDATE_PREFIX = "Update Prefix:";
	private final String REMOVE_PREFIX = "Remove Prefix:";
	private final String FIND_PREFIX = "Find Prefix:";
	private final String LIST_SUFFIX = "Find List suffix:";
	private final String PK_SUFFIX = "Find by PK suffix:";
	private final String VALUE_OBJECT = "Value Object prefix:";

	/* IDs for the Anyframe Preference page */

	public static final String CREATE_PREFIX_ID = "createPrefixID";
	public static final String UPDATE_PREFIX_ID = "updatePrefixID";
	public static final String REMOVE_PREFIX_ID = "removePrefixID";
	public static final String FIND_PREFIX_ID = "findPrefixID";
	public static final String FIND_LIST_SUFFIX_ID = "findListSuffixID";
	public static final String FIND_BYPK_SUFFIX_ID = "findByPKSuffixID";
	public static final String VALUE_OBJECT_PREFIX_ID = "valueObjectPrefixID";
	public static final String FIND_LIST_WITH_PAGING_SUFFIX_ID = "findListWithPaging";
	IPreferenceStore store;

	/*
	 * Default constructor.preference store.
	 */

	public AnyframePreferencePage() {
		super();
		store = QueryManagerActivator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription("AbstractDAO prefix and suffix");
	}

	/**
	 * @param parent
	 *            createContents method takes parent shell parameter of the
	 *            composite type as a parameter and responsible for creating UI
	 *            for the Anyframe preference page.
	 */
	protected Control createContents(Composite parent) {

		Composite preferenceWindow = new Composite(parent, SWT.NULL);

		GridData data = new GridData(
				org.eclipse.swt.layout.GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		preferenceWindow.setLayoutData(data);

		org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
		layout.numColumns = 2;
		preferenceWindow.setLayout(layout);

		Label label = new Label(preferenceWindow, SWT.NONE);
		label.setText(CREATE_PREFIX);

		// create prefix text

		createPrefix = new Text(preferenceWindow, SWT.BORDER);
		createPrefix.setLayoutData(data);
		createPrefix.setText(store.getString(CREATE_PREFIX_ID));

		label = new org.eclipse.swt.widgets.Label(preferenceWindow, SWT.NONE);
		label.setText(UPDATE_PREFIX);

		// update prefix text

		updatePrefix = new org.eclipse.swt.widgets.Text(preferenceWindow,
				SWT.BORDER);
		updatePrefix.setLayoutData(data);
		updatePrefix.setText(store.getString(UPDATE_PREFIX_ID));

		label = new org.eclipse.swt.widgets.Label(preferenceWindow, SWT.NONE);
		label.setText(REMOVE_PREFIX);

		// remove prefix text

		removePrefix = new org.eclipse.swt.widgets.Text(preferenceWindow,
				SWT.BORDER);
		removePrefix.setLayoutData(data);
		removePrefix.setText(store.getString(REMOVE_PREFIX_ID));

		label = new org.eclipse.swt.widgets.Label(preferenceWindow, SWT.NONE);
		label.setText(FIND_PREFIX);

		// find prefix text

		findPrefix = new org.eclipse.swt.widgets.Text(preferenceWindow,
				SWT.BORDER);
		findPrefix.setLayoutData(data);
		findPrefix.setText(store.getString(FIND_PREFIX_ID));

		label = new org.eclipse.swt.widgets.Label(preferenceWindow, SWT.NONE);
		label.setText(LIST_SUFFIX);

		// find list suffix

		findListSufix = new org.eclipse.swt.widgets.Text(preferenceWindow,
				SWT.BORDER);
		data = new org.eclipse.swt.layout.GridData(
				org.eclipse.swt.layout.GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		findListSufix.setLayoutData(data);
		findListSufix.setText(store.getString(FIND_LIST_SUFFIX_ID));

		label = new org.eclipse.swt.widgets.Label(preferenceWindow, SWT.NONE);
		label.setText(PK_SUFFIX);

		// find by pk suffix text

		findPKSufix = new org.eclipse.swt.widgets.Text(preferenceWindow,
				SWT.BORDER);
		findPKSufix.setLayoutData(data);
		findPKSufix.setText(store.getString(FIND_BYPK_SUFFIX_ID));

		label = new org.eclipse.swt.widgets.Label(preferenceWindow, SWT.NONE);
		label.setText(VALUE_OBJECT);

		// Value Object prefix

		valueObjectPrefix = new org.eclipse.swt.widgets.Text(preferenceWindow,
				SWT.BORDER);
		valueObjectPrefix.setLayoutData(data);
		valueObjectPrefix.setText(store.getString(VALUE_OBJECT_PREFIX_ID));

		return preferenceWindow;

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults() This
	 *      method is called when Restore Defaults button is selected. it
	 *      restores all the default values from the preference store to the
	 *      page fields.
	 */

	protected void performDefaults() {

		createPrefix.setText(store.getDefaultString(CREATE_PREFIX_ID));
		updatePrefix.setText(store.getDefaultString(UPDATE_PREFIX_ID));
		removePrefix.setText(store.getDefaultString(REMOVE_PREFIX_ID));
		findPrefix.setText(store.getDefaultString(FIND_PREFIX_ID));
		findListSufix.setText(store.getDefaultString(FIND_LIST_SUFFIX_ID));
		findPKSufix.setText(store.getDefaultString(FIND_BYPK_SUFFIX_ID));
		valueObjectPrefix.setText(store
				.getDefaultString(VALUE_OBJECT_PREFIX_ID));
		super.performDefaults();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk() This method
	 *      is called when OK button is selected. it stores all the values from
	 *      the fields into the Preference store.
	 */
	public boolean performOk() {

		store.setValue(CREATE_PREFIX_ID, createPrefix.getText());
		store.setValue(UPDATE_PREFIX_ID, updatePrefix.getText());
		store.setValue(REMOVE_PREFIX_ID, removePrefix.getText());
		store.setValue(FIND_PREFIX_ID, findPrefix.getText());
		store.setValue(FIND_LIST_SUFFIX_ID, findListSufix.getText());
		store.setValue(FIND_BYPK_SUFFIX_ID, findPKSufix.getText());
		store.setValue(VALUE_OBJECT_PREFIX_ID, valueObjectPrefix.getText());

		return super.performOk();
	}

}