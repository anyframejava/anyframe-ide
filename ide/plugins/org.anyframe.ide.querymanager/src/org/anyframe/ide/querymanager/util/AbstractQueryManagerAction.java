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
package org.anyframe.ide.querymanager.util;

import org.anyframe.ide.common.util.ImageUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * This is AbstractQueryManagerAction class.
 * 
 * @author Junghwan Hong
 */
public class AbstractQueryManagerAction extends Action implements
		IViewActionDelegate {
	public static final String PLUGIN_ID = "org.anyframe.ide.querymanager";
	
	/**
	 * Constructor of AbstractQueryExplorerViewAction class.
	 * 
	 * @param actionId
	 * @param actionTooltipText
	 * @param actionIconId
	 */
	public AbstractQueryManagerAction(String actionId,
			String actionToolTipText, String actionIconId) {
		this(actionId, actionToolTipText, actionIconId, SWT.NONE);
	}

	/**
	 * Constructor of AbstractQueryExplorerViewAction class.
	 * 
	 * @param actionId
	 * @param actionTooltipText
	 * @param actionIconId
	 * @param style
	 */
	public AbstractQueryManagerAction(String actionId,
			String actionToolTipText, String actionIconId, int style) {
		super(actionId);
		setToolTipText(actionToolTipText);
		if (actionIconId != null) {
			ImageDescriptor image = ImageUtil.getImageDescriptor(PLUGIN_ID, actionIconId);
			setImageDescriptor(image);
			setHoverImageDescriptor(image);
		}
	}

	public void init(IViewPart arg0) {
	}

	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(true);
	}

	public boolean isAvailable() {
		return true;
	}

	// protected QMExplorerView getView() {
	// 		return QueryManagerPlugin.getDefault().getSnapshotView();
	// }
}
