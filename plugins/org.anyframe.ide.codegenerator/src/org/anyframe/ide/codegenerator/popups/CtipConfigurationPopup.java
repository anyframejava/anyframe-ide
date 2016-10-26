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
package org.anyframe.ide.codegenerator.popups;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.util.HudsonRemoteAPI;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.codegenerator.views.CtipView;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jdom.Element;

/**
 * This is a CtipConfigurationPopup class.
 * 
 * @author Soungmin Joo
 */
public class CtipConfigurationPopup extends Dialog {
	public static final String ID = CodeGeneratorActivator.PLUGIN_ID
			+ ".views.CtipView";

	private CtipView view;
	private String ctipUrl;

	private Composite dialogArea;

	private Label home;

	private Text homeText;
	private Text hudsonLinkUrlText;

	private HudsonRemoteAPI hudson = new HudsonRemoteAPI();

	private boolean isModify;

	public CtipConfigurationPopup(Shell parent, CtipView view, String ctipUrl) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.view = view;
		this.ctipUrl = ctipUrl;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		int screenWidth = shell.getDisplay().getPrimaryMonitor().getBounds().width;
		int screenHeight = shell.getDisplay().getPrimaryMonitor().getBounds().height;

		shell.setText(Message.view_ctip_configpopup_title);
		shell.setBounds((screenWidth - 400) / 2, (screenHeight - 130) / 3, 400,
				150);
	}

	protected Control createDialogArea(Composite parent) {
		dialogArea = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		dialogArea.setLayout(layout);

		createConfiguationInfo(dialogArea);

		loadHudsonConfigInfo();

		return dialogArea;
	}

	private void createConfiguationInfo(Composite dialogArea) {
		Composite composite = new Composite(dialogArea, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		home = new Label(composite, SWT.NONE);

		if (view.getProjectBuild().equals(
				CommonConstants.PROJECT_BUILD_TYPE_MAVEN))
			home.setText(Message.view_ctip_configpopup_mavenhome);
		else
			home.setText(Message.view_ctip_configpopup_anthome);

		homeText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		homeText.setLayoutData(layoutData);

		Label workspace = new Label(composite, SWT.NONE);
		workspace.setText(Message.view_ctip_configpopup_hudsonurl);

		hudsonLinkUrlText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		hudsonLinkUrlText.setLayoutData(layoutData);

	}

	private void loadHudsonConfigInfo() {
		try {
			hudson.setHudsonURL(ctipUrl);

			Element config = hudson.getHudsonConfig();

			homeText.setText(config.getChildText(view.getProjectBuild() + "Home"));
			hudsonLinkUrlText.setText(config.getChildText("hudsonURL"));
			homeText.setEditable(true);
			hudsonLinkUrlText.setEditable(true);
		} catch (Exception e) {
			PluginLoggerUtil
					.error(ID, Message.exception_log_gethudsonconfig, e);
		}
	}

	protected void okPressed() {
		if (onAddButtonClicked()) {
			close();
		}
	}

	protected void cancelPressed() {
		close();
	}

	private boolean onAddButtonClicked() {
		// ctip configuration save
		String homeTextTrim = homeText.getText().trim();
		homeTextTrim = homeTextTrim.replaceAll("\\\\", "/");
		String hudsonURL = hudsonLinkUrlText.getText().trim();

		if (homeTextTrim.equals("")) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title, home
					.getText().replace(":", "")
					+ Message.view_ctip_configpopup_warn_empty,
					MessageDialog.WARNING);
			return false;
		} else if (!ProjectUtil.existPath(homeTextTrim)
				|| !ProjectUtil.validatePath(homeTextTrim)) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title, home
					.getText().replace(":", "")
					+ Message.view_ctip_configpopup_warn_valid,
					MessageDialog.WARNING);
			return false;
		}

		try {
			if (view.getProjectBuild().equals(
					CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
				hudson.saveHudsonConfig("KEEP_AS_IS", homeTextTrim, hudsonURL);
			} else {
				hudson.saveHudsonConfig(homeTextTrim, "KEEP_AS_IS", hudsonURL);
			}
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_configpopup_config_success,
					MessageDialog.INFORMATION);
		} catch (Exception e) {
			MessageDialogUtil.openMessageDialog(Message.ide_message_title,
					Message.view_ctip_configpopup_hudsonlink_init,
					MessageDialog.WARNING);
			return false;
		}
		return true;
	}

}
