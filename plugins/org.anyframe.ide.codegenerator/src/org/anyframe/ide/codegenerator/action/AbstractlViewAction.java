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
package org.anyframe.ide.codegenerator.action;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.common.util.ImageUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * This is an AbstractViewAction class.
 * 
 * @author junghwan.hong
 * @since 2.7.0
 * 
 */
public class AbstractlViewAction extends Action implements IViewActionDelegate {

	/**
	 * Constructor of AbstractViewAction class.
	 * 
	 * @param actionId
	 * @param actionTooltipText
	 * @param actionIconId
	 */
	public AbstractlViewAction(String actionId, String actionToolTipText,
			String actionIconId) {
		this(actionId, actionToolTipText, actionIconId, SWT.NONE);
	}

	/**
	 * Constructor of AbstractViewAction class.
	 * 
	 * @param actionId
	 * @param actionTooltipText
	 * @param actionIconId
	 * @param style
	 */
	public AbstractlViewAction(String actionId, String actionToolTipText,
			String actionIconId, int style) {
		super(actionId);
		setToolTipText(actionToolTipText);
		if (actionIconId != null) {
			ImageDescriptor image = ImageUtil.getImageDescriptor(
					CodeGeneratorActivator.PLUGIN_ID, actionIconId);
			setImageDescriptor(image);
			setHoverImageDescriptor(image);
		}
	}

	public void init(IViewPart arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * run method of AbstractViewAction class.
	 * 
	 * @param arg0
	 * @param style
	 */
	public void run(IAction arg0) {
		// TODO Auto-generated method stub
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		action.setEnabled(true);
	}

	public boolean isAvailable() {
		return true;
	}

}
