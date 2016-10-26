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
package org.anyframe.ide.codegenerator.messages;

import org.eclipse.osgi.util.NLS;

/**
 * This is Message class.
 * 
 * @author Sujeong Lee
 */
public class Message extends NLS {
	private static final String BUNDLE_NAME = Message.class.getName();
	

	// [Common]
	public static String ide_button_browse;
//	public static String ide_button_build;
	public static String ide_button_refresh;
//	public static String ide_button_apply;
//	public static String ide_button_install;
//	public static String ide_button_uninstall;
	public static String ide_button_add;
	public static String ide_button_edit;
	public static String ide_message_title;
//	public static String ide_button_newtask;
	public static String ide_button_remove;
//	public static String ide_button_save;
//	public static String ide_button_run;
//	public static String ide_button_close;
//	public static String ide_button_update;
//	public static String ide_button_ok;
//	public static String ide_button_cancel;

	// [Images] 
//	public static String image_build;
	public static String image_table;
	public static String image_tables;
	public static String image_package;
	public static String image_refresh;
//	public static String image_refresh_small;
	public static String image_serarch;
//	public static String image_apply;
	public static String image_install;
	public static String image_uninstall;
	public static String image_checked;
	public static String image_unchecked;
//	public static String image_status_red;
//	public static String image_status_blue;
//	public static String image_status_grey;
//	public static String image_close;
	public static String image_edit;
	public static String image_new;
	public static String image_remove;
	public static String image_run;
//	public static String image_testcon;
	public static String image_update;
//	public static String image_update_small;
	public static String image_configuration;
	public static String image_server_edit;
	public static String image_java_class;
	public static String image_schema;

	// [Preferences] IDE 	
	public static String preferences_buildtype_title;
	public static String preferences_mavenhome_title;
	public static String preferences_mavenhome_label;
	public static String preferences_anyframehome_title;
	public static String preferences_anyframehome_label;
	public static String preferences_loglevel_title;
	public static String preferences_loglevel_label;
	public static String preferences_archetype_title;
	public static String preferences_archetype_basic_label;
		
	// [Wizard] Application 
	// application
	public static String wizard_application_page_preference_correct;
	public static String wizard_application_page ;
	public static String wizard_application_window_title ;
	public static String wizard_application_title ;
	public static String wizard_application_description ;
	public static String wizard_application_name ;
	public static String wizard_application_contents ;
	//public static String wizard_application_anyframehome ;
	public static String wizard_application_location ;
	public static String wizard_templatehome_location ;
	public static String wizard_templatehome_location_check ;
	public static String wizard_application_packagename ;
//	public static String wizard_modules_check_pjttype_title;
//	public static String wizard_modules_check_pjttype_web;
//	public static String wizard_modules_check_pjttype_service;
	public static String wizard_modules_check_pjtartifact_title;
	public static String wizard_offline_check ;
	//public static String wizard_check_pjt_title;
	//public static String wizard_check_pjt_ant;
	//public static String wizard_check_pjt_maven;
	public static String wizard_maven_groupid ;
	public static String wizard_maven_groupid_default ;
	public static String wizard_maven_artifactid ;
	public static String wizard_maven_version ;
	public static String wizard_maven_version_default ;
//	public static String wizard_maven_packaging ;
//	public static String wizard_maven_packaging_war ;
//	public static String wizard_maven_packaging_jar ;
	
	public static String wizard_plugin_group;
	public static String wizard_plugin_name;
	
	public static String wizard_templatetype_group;
	public static String wizard_templatetype_name;
	
//	public static String wizard_generation_window_title ;
	public static String wizard_generation_domain_title;
	public static String wizard_generation_domain_description;
	public static String wizard_generation_crud_title;
	
	//projects;
//	public static String wizard_module_page ;
//	public static String wizard_module_title ;
//	public static String wizard_module_description ;
//	public static String wizard_modules_check_singleprojectnamecheck;
//	public static String wizard_module_singlepjtname ;
//	public static String wizard_module_singlepjtname_value ;
//	public static String wizard_modules_check_multiprojectnamecheck;
//	public static String wizard_module_pjtname ;
//	public static String wizard_module_pjtname_value ;
//	public static String wizard_module_servicepjtname;
//	public static String wizard_module_servicepjtname_value ;
//	public static String wizard_module_webpjtname ;
//	public static String wizard_module_webpjtname_value ;
	public static String wizard_module_template ;
//	public static String wizard_application_dynamic ;
//	public static String wizard_module_template_home ;
	// jdbc configuration
	public static String wizard_jdbc_page;
	public static String wizard_jdbc_title ;
	public static String wizard_jdbc_description ;
	public static String wizard_jdbc_label ;
	public static String wizard_jdbc_typename;
//	public static String wizard_jdbc_dbname;
	public static String wizard_jdbc_schemaname;
	public static String wizard_jdbc_username;
	public static String wizard_jdbc_password;
//	public static String wizard_jdbc_server;
//	public static String wizard_jdbc_port;
	public static String wizard_jdbc_url;
	public static String wizard_jdbc_dialect;
	public static String wizard_jdbc_driverclass;
	public static String wizard_jdbc_driverjar;
	public static String wizard_jdbc_defaultschema;
	public static String wizard_jdbc_checkjdbc ;
	public static String wizard_jdbcdriver_label ;
	public static String wizard_jdbcdriver_hidden_title;

	// [Wizard] Class
//	public static String wizard_class_vo_title;
//	public static String wizard_class_vo_description;
//	public static String wizard_class_dao_title;
//	public static String wizard_class_dao_description;	
//	public static String wizard_class_description_close_comment;
//	public static String wizard_class_exception_error_properties;
	
	// [Wizard] Projects 
//	public static String wizard_module_single_title ;
//	public static String wizard_module_single_description ;
//	public static String wizard_module_contents ;
//	public static String wizard_module_location ;

	// [Wizard] Error & Warning & Information 
	// [Wizard] Application :: application;
//	public static String wizard_module_window_title ;
	public static String wizard_application_error_pjtname;
	public static String wizard_application_validation_pjtname;
	public static String wizard_application_validation_duplicatedpjtname;

	public static String wizard_application_error_apploc ;
	public static String wizard_application_validation_apploc ;
	public static String wizard_application_error_templatehome ;
	public static String wizard_application_validation_templatehome ;
//	public static String wizard_application_validation_templatehome_check ;
//	public static String wizard_application_validation_template_null ;
	public static String wizard_application_error_pkgname ;
	public static String wizard_application_validation_pkgname ;
	public static String wizard_application_validation_templatetype ;
//	public static String wizard_application_error_pkgname_valid ;
	public static String wizard_application_error_pjtgroupid ;
	public static String wizard_application_validation_pjtgroupid ;
	public static String wizard_application_error_pjtartifactid ;
	public static String wizard_application_validation_pjtartifactid ;
	public static String wizard_application_error_pjtversion ;
	public static String wizard_application_validation_pjtversion ;
	public static String wizard_application_validation_pi ;

	// [Wizard] Application :: projects;
//	public static String wizard_module_validation_issingle;
//	public static String wizard_module_validation_common_pjt_name ;
//	public static String wizard_module_validation_service_pjt_name ;
//	public static String wizard_module_validation_common_pjt_name_specialchar ;
//	public static String wizard_module_validation_service_pjt_name_specialchar ;
//	public static String wizard_module_validation_web_pjt_name_specialchar ;
//	public static String wizard_module_validation_duplicatedservicepjt;
//	public static String wizard_module_validation_issingle_specialchar;
//	public static String wizard_module_validation_iscommonpjt;
//	public static String wizard_module_validation_isservicenpjt;

	// [Wizard] Application :: jdbc configuration;
	public static String wizard_jdbc_setjdbc ;
	// public static String wizard_jdbc_error_drivergroupid ;
	// public static String wizard_jdbc_error_driverartifactid ;
	// public static String wizard_jdbc_error_driverversion ;

	// [Wizard] Class
//	public static String wizard_class_error_package_fragment_root;
//	public static String wizard_class_error_io;
//	public static String wizard_class_error_open_editor;
//	public static String wizard_class_error_open_dao_editor_java_editor_instead;
//	public static String wizard_class_error_open_vo_editor_java_editor_instead;
	
	// [Wizard] Projects;
//	public static String wizard_module_error_apploc_change ;
//	public static String wizard_module_information_createpjt ;
//	public static String wizard_module_error_pjtname ;
//	public static String wizard_module_error_pjtname_specialchar ;
//	public static String wizard_module_error_apploc_browse ;
//	public static String wizard_module_error_apploc_browse_detail ;
//	public static String wizard_module_error_apploc ;

	public static String wizard_domain_init_message;

	public static String wizard_crud_gen_template_valid_error;
	public static String wizard_crud_genwebsource;
	public static String wizard_crud_insertsampledata;

	public static String wizard_config_section;
	public static String wizard_config_description;
	
	public static String wizard_application_daoframeworks ;
//	public static String wizard_application_daoframeworks_hibernate ;
//	public static String wizard_application_daoframeworks_ibatis2 ;
//	public static String wizard_application_daoframeworks_mybatis ;
//	public static String wizard_application_daoframeworks_queryservice ;
//	public static String wizard_application_daoframeworks_springjdbc ;
//	public static String wizard_module_template_selection ;

	public static String properties_error_template_pluginlist_notmatch;
	
	// View Installation;
	public static String view_installation_description_prefix;
	public static String view_installation_description_suffix;
	public static String view_installation_fColumn1_name;
	public static String view_installation_fColumn2_name;
	public static String view_installation_fColumn3_name;
	public static String view_installation_fColumn4_name;
	public static String view_installation_fColumn5_name;
	public static String view_installation_fColumn6_name;
	public static String view_installation_fColumn7_name;
	public static String view_installation_menu_selectproject;
	public static String view_installation_menu_sample;
	public static String view_installation_action_install_title;
	public static String view_installation_action_install_tooltip;
	public static String view_installation_action_uninstall_title;
	public static String view_installation_action_uninstall_tooltip;
	public static String view_installation_action_updatecatalog_title;
	public static String view_installation_action_updatecatalog_tooltip;
	public static String view_installation_action_refresh_title;
	public static String view_installation_action_refresh_tooltip;
	
	// View CTIP Integration;
	public static String view_ctip_getpjtlist_warn;
	public static String view_ctip_description_prefix;
	public static String view_ctip_description_suffix;
	public static String view_ctip_fColumn1_name;
	public static String view_ctip_fColumn2_name;
	public static String view_ctip_fColumn3_name;
	public static String view_ctip_fColumn4_name;
	public static String view_ctip_fColumn5_name;
	public static String view_ctip_fColumn6_name;
	public static String view_ctip_fColumn7_name;
	public static String view_ctip_menu_selectproject;
	public static String view_ctip_run_progress;
	public static String view_ctip_run_warn_confirm;
	public static String view_ctip_run_warn_fail;
	public static String view_ctip_remove_warn_confirm;
	public static String view_ctip_remove_warn;
	public static String view_ctip_addserverpopup_name;
	public static String view_ctip_addserverpopup_location;
	public static String view_ctip_addserverpopup_title;
	public static String view_ctip_addserverpopup_valid_server;
	public static String view_ctip_addserverpopup_valid_location_prefix;
	public static String view_ctip_addserverpopup_valid_name;
	public static String view_ctip_addserverpopup_valid_location;
	public static String view_ctip_addjobpopup_taskdetail_type;
	public static String view_ctip_addjobpopup_taskdetail_type_build;
	public static String view_ctip_addjobpopup_taskdetail_type_report;
	public static String view_ctip_addjobpopup_taskdetail_taskname;
	public static String view_ctip_addjobpopup_taskdetail_workspace;
	public static String view_ctip_addjobpopup_taskdetail_scm_type;
	public static String view_ctip_addjobpopup_taskdetail_scm_url;
	public static String view_ctip_addjobpopup_taskdetail_schedule;
	public static String view_ctip_addjobpopup_taskdetail_childproject;
	public static String view_ctip_addjobpopup_taskname_build_postfix;
	public static String view_ctip_addjobpopup_taskname_report_postfix;
	public static String view_ctip_addjobpopup_apphome_prefix;
//	public static String view_ctip_addjobpopup_anyframe_prefix;
	public static String view_ctip_addjobpopup_anyframe_postfix;
	public static String view_ctip_addjobpopup_scmurl_init;
	public static String view_ctip_addjobpopup_schedule_init;
	public static String view_ctip_addjobpopup_taskdetail_type_check;
	public static String view_ctip_addjobpopup_taskdetail_taskname_check;
	public static String view_ctip_addjobpopup_taskdetail_taskname_valid;
	public static String view_ctip_addjobpopup_taskdetail_taskname_duplicated;
	public static String view_ctip_addjobpopup_taskdetail_taskname_fail;
	public static String view_ctip_addjobpopup_taskdetail_taskname_fail_detail;
	public static String view_ctip_addjobpopup_taskdetail_taskname_success;
	public static String view_ctip_addjobpopup_apphome_warn_change;
	public static String view_ctip_addjobpopup_new_warn_notexistpjt;
	public static String view_ctip_addjobpopup_new_warn_emptyscmurl;
	public static String view_ctip_addjobpopup_new_warn_invalidschedule;
	public static String view_ctip_addjobpopup_new_warn_invalidscmurl;
	public static String view_ctip_addjobpopup_save_warn_confirm;
	public static String view_ctip_addjobpopup_update_warn;
	public static String view_ctip_addjobpopup_update_warn_detail;
	public static String view_ctip_configpopup_title;
	public static String view_ctip_configpopup_anthome;
	public static String view_ctip_configpopup_mavenhome;
	public static String view_ctip_configpopup_hudsonurl;
	public static String view_ctip_configpopup_warn_empty;
	public static String view_ctip_configpopup_warn_valid;
	public static String view_ctip_configpopup_config_success;
	public static String view_ctip_configpopup_hudsonlink_init;
	public static String view_ctip_urlpopop_columnname;
	public static String view_ctip_urlpopop_columnurl;
	
	public static String view_ctip_action_runjob_title;
	public static String view_ctip_action_runjob_tooltip;
	public static String view_ctip_action_addjob_title;
	public static String view_ctip_action_addjob_tooltip;
	public static String view_ctip_action_editjob_title;
//	public static String view_ctip_action_editjob_tooltip;
	public static String view_ctip_action_removejob_title;
	public static String view_ctip_action_removejob_tooltip;
	public static String view_ctip_action_refresh_title;
	public static String view_ctip_action_refresh_tooltip;
	public static String view_ctip_action_openctipconfig_title;
	public static String view_ctip_action_openctipconfig_tooltip;
	public static String view_ctip_action_openctipserverconfig_title;
	public static String view_ctip_action_openctipserverconfig_tooltip;
	
	public static String view_ctip_button_testconnection;
	public static String view_ctip_popup_title;
	public static String view_ctip_popup_remove_notselected;
	public static String view_ctip_popup_remove_notremoved;
	public static String view_ctip_popup_remove_confirm;
	public static String view_ctip_popup_testconnection_notselected;
	public static String view_ctip_popup_testconnection_success;
	public static String view_ctip_popup_testconnection_fail;
	public static String view_ctip_popup_edit_notselected;
	
	public static String wizard_error_load_properties;
	public static String wizard_error_checkdb;
	public static String wizard_error_createmodel;
	public static String wizard_confirm_domain_overwrite;
	public static String wizard_confirm_overwrite;
	public static String wizard_codegen_createcode;
	public static String wizard_generation_domain_confirm;
	public static String wizard_generation_crud_confirm;
	public static String wizard_error_template;
	public static String wizard_error_properties;
	public static String wizard_error_templateconfig;

	public static String wizard_problem_connection;
	public static String wizard_validation_dbschema;
	public static String wizard_validation_dbclassname;
	public static String wizard_validation_dburl;
	public static String wizard_validation_dbjar;
	public static String wizard_validation_dbjar_valid;
	public static String wizard_error_selectedproject;

	public static String view_dialog_plugin_check;
	public static String view_dialog_plugintarget_install_info;
	public static String view_dialog_plugin_confirm_install;
	public static String view_dialog_plugin_confirm_uninstall;

	// Command Execution Message;
//	public static String command_execution_finished;
	
	public static String view_exception_init ;
	public static String view_exception_request ;
	public static String view_exception_conf;
//	public static String view_exception_refresh;
//	public static String view_exception_open;
//	public static String view_exception_pjtsection;
	public static String view_exception_checkoverwrite;
//	public static String view_exception_checkinstall;
	public static String view_exception_domain_checkoverwrite;
//	public static String view_exception_dbport;
//	public static String view_exception_popupmenu;
//	public static String view_exception_popupmenu_notdomain;
//	public static String view_exception_popupmenu_nodomain;
	public static String view_exception_savedbconfig;
//	public static String view_exception_saveconfig;
	public static String view_exception_loadconfig;
	public static String view_exception_findconfig;
	public static String view_exception_getschema;
	public static String view_exception_getpluginlist;
//	public static String view_exception_updateplugin;
	public static String view_exception_getinstalledpluginlist;
	public static String view_exception_installpluginlist;
	public static String view_exception_uninstallepluginlist;
//	public static String view_exception_jdbctypes;
//	public static String view_exception_getpjtlist;
//	public static String view_exception_opendialog;
	public static String view_exception_getconnection;
	public static String view_exception_closeconnection;
	public static String view_exception_gettable;
	public static String view_exception_invoked;
	public static String view_exception_interrupt;
	public static String view_exception_timesleep;
	public static String view_exception_loaddomaincrud;
	public static String view_exception_getsourcepath;
	public static String view_exception_loadjdbcconfig;
//	public static String view_exception_loadpjttype;
	public static String exception_log_findfile;
	public static String exception_log_iofile;
	public static String exception_log_name;
//	public static String exception_log_resource;
	public static String exception_log_antconfig;
//	public static String exception_log_ant;
	public static String exception_log_refresh;
	public static String exception_log_openpjt;
	public static String exception_log_cleanpjt;
	public static String exception_log_parsejtidy;
	public static String exception_log_createproject;
	public static String exception_log_getprojectlist;
	public static String exception_log_gethudsonconfig;

	public static String preferences_exception_archetypeversions;
	public static String preferences_mavenhome_validate_specified;
	public static String preferences_mavenhome_validate_correct;
	public static String preferences_anyframehome_validate_specified;
	public static String preferences_anyframehome_validate_correct;

	/**
	 * added [2013.03.21]
	 */	
	public static String view_ctip_modifyctipjob;//"Modify CTIP Job"
	public static String view_ctip_addnewjob;//"Add New CTIP Job"
	public static String view_ctip_typejobname;//"type filter text of job name"
	public static String view_ctip_typepluginname;//"type filter text of plugin name"

	public static String view_exception_failtomakeapi;//"Fail to make HudsonRemoteAPI."
	public static String view_exception_failtogetscmtype;//"Fail to get scm type."
	public static String view_exception_failtogetscmurl;//"Fail to get scm url."
	public static String view_exception_failtogetschedule;//"Fail to get schedule."
	public static String view_exception_failtogetchildproject;//"Fail to get child project."
	public static String view_exception_failtocreatemavenlaunchconfiguration;//"failed to create maven launch configuration"
	public static String view_exception_failtorefreshresources;//"failed to refresh resources."
	public static String view_exception_failtochagnedbinfo;//"failed to change db information"
	
	public static String wizard_crud_gen_basepackage;//"Base Package :"
	public static String wizard_crud_gen_domainmodelclasses;//"Domain Model Classes."
	public static String wizard_crud_gen_packageselection;//"Package Selection"
	public static String wizard_crud_gen_otheroptions;//"Other options"
	public static String wizard_crud_gen_selectdomainmodelclasses;//"Select Domain Model Classes."

	public static String wizard_generation_domain_selecttables;//"Select Tables"
	
	public static String wizard_application_error_cannotgetjdbcconfiguration;//"Can't get jdbc configuration."
	public static String wizard_application_schemaselection;//"Schema Selection"
	
	public static String exception_log_cannotcreatem2conf;//"Can't create m2.conf "
	public static String exception_log_unabletocreateproject;//"Unable to create project from archetype " + ID

	public static String property_error_noselectedtemplate;//"There is no selected Template or DAO Framework. You should select Template and DAO Framework."

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Message.class);
	}

	private Message() {
	}
}