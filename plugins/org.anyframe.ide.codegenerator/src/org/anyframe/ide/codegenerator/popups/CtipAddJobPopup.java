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

import hudson.scheduler.CronTab;

import java.util.List;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.codegenerator.messages.Message;
import org.anyframe.ide.codegenerator.model.table.CtipDetailList;
import org.anyframe.ide.codegenerator.util.HudsonRemoteAPI;
import org.anyframe.ide.codegenerator.util.ProjectUtil;
import org.anyframe.ide.codegenerator.views.CtipView;
import org.anyframe.ide.command.common.util.CommonConstants;
import org.anyframe.ide.common.util.DialogUtil;
import org.anyframe.ide.common.util.MessageDialogUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.common.util.ProjectConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This is a CtipAddUrlPopup class.
 * 
 * @author Soungmin Joo
 */
public class CtipAddJobPopup extends Dialog {
	public static final String ID = CodeGeneratorActivator.PLUGIN_ID + ".views.CtipView";

	private final CtipView view;

	private final List<String> jobNameList;

	private final boolean isModify;

	private Text jobNameText;
	private Text workspaceText;
	private Text scmSeverUrlText;
	private Text scmScheduleText;
	private Text otherProjectText;

	private Button buildTypeCheck;
	private Button reportTypeCheck;

	private Combo scmSeverTypeCombo;

	private CtipDetailList detailList;

	private final HudsonRemoteAPI hudson = new HudsonRemoteAPI();

	private String projectBuild;

	private final String ctipUrl;

	public CtipAddJobPopup(Shell parent, CtipView view, List<String> jobNameList, String ctipUrl) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.view = view;
		this.jobNameList = jobNameList;
		this.isModify = false;
		this.ctipUrl = ctipUrl;
	}

	public CtipAddJobPopup(Shell parent, CtipView view, List<String> jobNameList, CtipDetailList detailList, String ctipUrl) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.view = view;
		this.jobNameList = jobNameList;
		this.detailList = detailList;
		this.ctipUrl = ctipUrl;
		this.isModify = true;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (isModify) {
			shell.setText(Message.view_ctip_modifyctipjob);
		} else {
			shell.setText(Message.view_ctip_addnewjob);
		}
		shell.setBounds(DialogUtil.center(450, 300));
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		dialogArea.setLayout(layout);

		createJobType(dialogArea);
		createJobInfo(dialogArea);

		if (isModify) {
			loadSettings();
		} else {
			setDefaultDetailText();
		}

		jobNameText.setFocus();
		enableFieldsIfReportType();
		buildTypeCheck.setEnabled(true);
		reportTypeCheck.setEnabled(true);
		jobNameText.setEditable(true);

		return dialogArea;
	}

	private void createJobType(Composite dialogArea) {
		Composite composite = new Composite(dialogArea, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label buildType = new Label(composite, SWT.NONE);
		buildType.setText(Message.view_ctip_addjobpopup_taskdetail_type);

		GridData buttonGridData = new GridData(85, 25);
		buttonGridData.horizontalAlignment = SWT.END;
		buttonGridData.grabExcessHorizontalSpace = true;

		buildTypeCheck = new Button(composite, SWT.CHECK);
		buildTypeCheck.setText(Message.view_ctip_addjobpopup_taskdetail_type_build);
		buildTypeCheck.setLayoutData(buttonGridData);

		reportTypeCheck = new Button(composite, SWT.CHECK);
		reportTypeCheck.setText(Message.view_ctip_addjobpopup_taskdetail_type_report);
		reportTypeCheck.setLayoutData(buttonGridData);
	}

	private void createJobInfo(Composite dialogArea) {
		Composite composite = new Composite(dialogArea, SWT.NONE);
		GridLayout gridLayout = new GridLayout(3, true);
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label jobName = new Label(composite, SWT.NONE);
		jobName.setText(Message.view_ctip_addjobpopup_taskdetail_taskname);

		jobNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		jobNameText.setLayoutData(layoutData);

		Label workspace = new Label(composite, SWT.NONE);
		workspace.setText(Message.view_ctip_addjobpopup_taskdetail_workspace);

		workspaceText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		workspaceText.setLayoutData(layoutData);

		Label scmServerType = new Label(composite, SWT.NONE);
		scmServerType.setText(Message.view_ctip_addjobpopup_taskdetail_scm_type);

		scmSeverTypeCombo = new Combo(composite, SWT.SINGLE | SWT.BORDER);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		scmSeverTypeCombo.setLayoutData(layoutData);
		scmSeverTypeCombo.addSelectionListener(comboListener);
		scmSeverTypeCombo.add("subversion", 0);
		scmSeverTypeCombo.add("cvs", 1);
		scmSeverTypeCombo.add("none", 2);
		scmSeverTypeCombo.select(0);

		Label scmSeverUrl = new Label(composite, SWT.NONE);
		scmSeverUrl.setText(Message.view_ctip_addjobpopup_taskdetail_scm_url);

		scmSeverUrlText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		scmSeverUrlText.setLayoutData(layoutData);

		Label scmSchedule = new Label(composite, SWT.NONE);
		scmSchedule.setText(Message.view_ctip_addjobpopup_taskdetail_schedule);

		scmScheduleText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		scmScheduleText.setLayoutData(layoutData);

		Label otherProject = new Label(composite, SWT.NONE);
		otherProject.setText(Message.view_ctip_addjobpopup_taskdetail_childproject);

		otherProjectText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		otherProjectText.setLayoutData(layoutData);
	}

	private void loadSettings() {
		if (detailList.getBuildType().equals("build"))
			buildTypeCheck.setSelection(true);
		else if (detailList.getBuildType().equals("report"))
			reportTypeCheck.setSelection(true);
		buildTypeCheck.setEnabled(false);
		reportTypeCheck.setEnabled(false);

		jobNameText.setText(detailList.getJobName());
		jobNameText.setEnabled(false);

		workspaceText.setText(detailList.getWorkSpace());
		scmSeverUrlText.setText(detailList.getScmServerUrl());
		scmScheduleText.setText(detailList.getSchedule());
		otherProjectText.setText(detailList.getOtherProject());

		if (detailList.getScmServerType().equals("subversion")) {
			scmSeverTypeCombo.select(0);
		} else if (detailList.getScmServerType().equals("cvs")) {
			scmSeverTypeCombo.select(1);
		} else {
			// none
			scmSeverTypeCombo.select(2);
			scmSeverUrlText.setEnabled(false);
			scmScheduleText.setEnabled(false);
		}
	}

	private void setDefaultDetailText() {
		buildTypeCheck.setSelection(true);
		reportTypeCheck.setSelection(true);

		ProjectConfig projectConfig = view.getProjectConfig();
		if (projectConfig == null)
			return;
		if (projectConfig.getAnyframeHome() != null && !"".equals(projectConfig.getAnyframeHome())) {
			projectBuild = CommonConstants.PROJECT_BUILD_TYPE_ANT;
		} else {
			projectBuild = CommonConstants.PROJECT_BUILD_TYPE_MAVEN;
		}
		String projectName = projectConfig.getPjtName();
		jobNameText.setText(projectName);

		String applicationHome = Message.view_ctip_addjobpopup_apphome_prefix + ProjectUtil.SLASH + ProjectUtil.SLASH
				+ Message.view_ctip_addjobpopup_anyframe_postfix;
		if (projectBuild.equals(CommonConstants.PROJECT_BUILD_TYPE_MAVEN)) {
			applicationHome = Message.view_ctip_addjobpopup_apphome_prefix + ProjectUtil.SLASH + Message.view_ctip_addjobpopup_anyframe_postfix;
		}
		workspaceText.setText(applicationHome);
		scmSeverTypeCombo.select(0);

		scmSeverUrlText.setText(Message.view_ctip_addjobpopup_scmurl_init + projectName);

		scmSeverUrlText.setEnabled(true);

		scmScheduleText.setText(Message.view_ctip_addjobpopup_schedule_init);
		otherProjectText.setText(projectName + Message.view_ctip_addjobpopup_taskname_build_postfix);
	}

	private void enableFieldsIfReportType() {
		scmSeverTypeCombo.setEnabled(true);
		// not selected "none" SCM Server Type
		if (scmSeverTypeCombo.getSelectionIndex() != 2) {
			scmSeverUrlText.setEnabled(true);
			scmScheduleText.setEnabled(true);
		} else {
			scmSeverUrlText.setText("");
			scmSeverUrlText.setEnabled(false);
			scmScheduleText.setText("");
			scmScheduleText.setEnabled(false);
		}
		otherProjectText.setEnabled(true);
	}

	private boolean isEditMode() {
		return detailList != null;
	}

	private boolean saveChanges() {
		String jobName = jobNameText.getText().trim();
		boolean result = false;
		hudson.setHudsonURL(ctipUrl);

		if (!isEditMode()) {
			// create job
			if (!buildTypeCheck.getSelection() && !reportTypeCheck.getSelection()) {
				MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_taskdetail_type_check,
						MessageDialog.WARNING);
				return false;
			} else {
				if ("".equals(jobName)) {
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_taskdetail_taskname_check,
							MessageDialog.WARNING);
					return false;
				} else if (!ProjectUtil.validateName(jobName)) {
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_taskdetail_taskname_valid,
							MessageDialog.WARNING);
					return false;
				}

				if (reportTypeCheck.getSelection()) {
					if (jobNameList.contains(jobName + Message.view_ctip_addjobpopup_taskname_report_postfix)) {
						MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_taskdetail_taskname_duplicated,
								MessageDialog.WARNING);
						return false;
					}
				}
				if (buildTypeCheck.getSelection()) {
					if (jobNameList.contains(jobName + Message.view_ctip_addjobpopup_taskname_build_postfix)) {
						MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_taskdetail_taskname_duplicated,
								MessageDialog.WARNING);
						return false;
					}
				}

				if (workspaceText.getText().indexOf(Message.view_ctip_addjobpopup_apphome_prefix) != -1) {
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_apphome_warn_change,
							MessageDialog.WARNING);
					return false;
				}

				if (buildTypeCheck.getSelection() && reportTypeCheck.getSelection()) {

					if (!otherProjectText.getText().equals(jobName + Message.view_ctip_addjobpopup_taskname_build_postfix)) {
						if (!jobNameList.contains(otherProjectText.getText())) {
							MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_new_warn_notexistpjt,
									MessageDialog.WARNING);
							return false;
						}

					}
				} else {
					if (otherProjectText.getText().equals(jobName + Message.view_ctip_addjobpopup_taskname_build_postfix)) {
						if (!jobNameList.contains(otherProjectText.getText())) {
							MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_new_warn_notexistpjt,
									MessageDialog.WARNING);
							return false;
						}
					}
				}
			}

			if (!scmSeverTypeCombo.getText().equals("none")) {
				if (StringUtils.isEmpty(scmSeverUrlText.getText())) {
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_new_warn_emptyscmurl,
							MessageDialog.WARNING);
					return false;
				}
			}

			String scmSchedule = scmScheduleText.getText();
			if (StringUtils.isNotEmpty(scmSchedule)) {
				if (StringUtils.isNotEmpty(scmSeverUrlText.getText())) {
					try {
						new CronTab(scmSchedule);
					} catch (Exception e) {
						MessageDialogUtil.openDetailMessageDialog(ID, Message.ide_message_title,
								Message.view_ctip_addjobpopup_new_warn_invalidschedule, e.getClass().getCanonicalName() + ": " + e.getMessage(),
								MessageDialog.WARNING);

						PluginLoggerUtil.error(ID, Message.view_ctip_addjobpopup_new_warn_invalidschedule, e);

						return false;
					}
				} else {
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_new_warn_invalidscmurl,
							MessageDialog.WARNING);
					return false;
				}
			}

			if (!MessageDialogUtil.confirmMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_save_warn_confirm + " \"" + jobName
					+ "\"?")) {
				return false;
			}

			result = createNewJob(jobName);

			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				PluginLoggerUtil.info(CodeGeneratorActivator.PLUGIN_ID, e.getMessage());
			}
			view.refreshPlugin();

		} else {
			// update job
			if (!otherProjectText.getText().equals("")) {
				if (buildTypeCheck.getSelection()) {
					if (otherProjectText.getText().equals(jobName)) {
						MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_new_warn_notexistpjt,
								MessageDialog.WARNING);
						return false;
					}
				}

				if (!jobNameList.contains(otherProjectText.getText())) {
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_new_warn_notexistpjt,
							MessageDialog.WARNING);
					return false;
				}
			}

			if (!scmSeverTypeCombo.getText().equals("none")) {
				if (StringUtils.isEmpty(scmSeverUrlText.getText())) {
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_new_warn_emptyscmurl,
							MessageDialog.WARNING);
					return false;
				}
			}

			String scmSchedule = scmScheduleText.getText();
			if (StringUtils.isNotEmpty(scmSchedule)) {
				if (StringUtils.isNotEmpty(scmSeverUrlText.getText())) {
					try {
						new CronTab(scmSchedule);
					} catch (Exception e) {
						MessageDialogUtil.openDetailMessageDialog(ID, Message.ide_message_title,
								Message.view_ctip_addjobpopup_new_warn_invalidschedule, e.getClass().getCanonicalName() + ": " + e.getMessage(),
								MessageDialog.WARNING);
						PluginLoggerUtil.error(ID, Message.view_ctip_addjobpopup_new_warn_invalidschedule, e);
						return false;
					}
				} else {
					MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_new_warn_invalidscmurl,
							MessageDialog.WARNING);
					return false;
				}
			}

			if (!MessageDialogUtil.confirmMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_save_warn_confirm + " \"" + jobName
					+ "\"?")) {
				return false;
			}

			result = updateExistingJob(jobName);
		}
		if (result) {
			view.refreshPlugin();

			MessageDialogUtil.openMessageDialog(Message.ide_message_title, Message.view_ctip_addjobpopup_taskdetail_taskname_success,
					MessageDialog.INFORMATION);
		}

		return true;
	}

	private boolean createNewJob(String jobName) {
		try {
			Context context = getParamContext();

			if (reportTypeCheck.getSelection()) {
				hudson.createJob(jobName + Message.view_ctip_addjobpopup_taskname_report_postfix, "report", context);
			}

			if (buildTypeCheck.getSelection()) {
				String type = "onlybuild";

				if (reportTypeCheck.getSelection()) {
					type = "build";

					if (!scmSeverTypeCombo.getText().equals("none")) {
						context.put("customWorkspace", context.get("customWorkspace") + "/" + jobName);
					}
				}

				hudson.createJob(jobName + Message.view_ctip_addjobpopup_taskname_build_postfix, type, context);
			}

			return true;

		} catch (Exception e) {
			MessageDialogUtil.openDetailMessageDialog(ID, Message.ide_message_title, Message.view_ctip_addjobpopup_taskdetail_taskname_fail,
					Message.view_ctip_addjobpopup_taskdetail_taskname_fail_detail, MessageDialog.WARNING);
			return false;
		}
	}

	private boolean updateExistingJob(String taskName) {
		try {
			Context context = getParamContext();
			hudson.updateJob(taskName, context);

			return true;
		} catch (Exception e) {
			MessageDialogUtil.openDetailMessageDialog(ID, Message.ide_message_title, Message.view_ctip_addjobpopup_update_warn,
					Message.view_ctip_addjobpopup_update_warn_detail, MessageDialog.WARNING);
			return false;
		}
	}

	private Context getParamContext() {
		Context context = new VelocityContext();
		context.put("customWorkspace", workspaceText.getText().trim());
		context.put("scmType", scmSeverTypeCombo.getItem(scmSeverTypeCombo.getSelectionIndex()));
		context.put("scmUrl", scmSeverUrlText.getText().trim());
		context.put("triggerSchedule", scmScheduleText.getText().trim());
		context.put("childProject", otherProjectText.getText().trim());
		context.put("projectBuild", this.projectBuild);
		return context;
	}

	private final SelectionListener comboListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			// selected "none" SCM Server Type
			if (scmSeverTypeCombo.getSelectionIndex() == 2) {
				scmSeverUrlText.setText("");
				scmSeverUrlText.setEnabled(false);
				scmScheduleText.setText("");
				scmScheduleText.setEnabled(false);
			} else {
				scmSeverUrlText.setEnabled(true);
				scmScheduleText.setEnabled(true);
			}
		}
	};

	protected void okPressed() {
		if (saveChanges()) {
			close();
		}
	}

	protected void cancelPressed() {
		close();
	}
}
