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
package org.anyframe.ide.querymanager.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;

/**
 * QMResourceListSelectionDialog class used to select the Projects in the
 * workspace
 * 
 * @author Gangab
 * @author Pavanesh
 */
public class QMResourceListSelectionDialog extends ResourceListSelectionDialog {

	public QMResourceListSelectionDialog(Shell parentShell,
			IContainer container, int typeMask) {
		super(parentShell, container, typeMask);
	}

	/**
	 * Overridden adjustPattern() If the text is having length of zero then we
	 * will return '?'. So that the user can see the projects without entering
	 * any characters.
	 */
	protected String adjustPattern() {
		String text = super.adjustPattern();
		if (text.length() <= 0) {
			return "?*";
		} else
			return text;

	}

	/**
	 * Overridden createDialogArea so that refresh() method can be called to
	 * make sure that QMResourceListSelectionDialog's adjustPattern() method is
	 * called.
	 */
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		refresh(true);
		return control;
	}

}
