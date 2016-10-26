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
package org.anyframe.ide.querymanager.editors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.anyframe.ide.common.databases.JdbcOption;
import org.anyframe.ide.common.util.ConnectionUtil;
import org.anyframe.ide.common.util.EDPUtil;
import org.anyframe.ide.common.util.ImageUtil;
import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.messages.MessagePropertiesLoader;
import org.anyframe.ide.querymanager.model.EditorInput;
import org.anyframe.ide.querymanager.model.QueryAttribute;
import org.anyframe.ide.querymanager.model.QueryInputAttribute;
import org.anyframe.ide.querymanager.model.QueryOutputAttribute;
import org.anyframe.ide.querymanager.model.SQLModelContentProvider;
import org.anyframe.ide.querymanager.model.SQLModelDecoratingLabelProvider;
import org.anyframe.ide.querymanager.model.SQLModelLabelProvider;
import org.anyframe.ide.querymanager.parsefile.ParseTemplateString;
import org.anyframe.ide.querymanager.properties.QMPropertiesXMLUtil;
import org.anyframe.ide.querymanager.util.DynamicQueryUtil;
import org.anyframe.ide.querymanager.util.GenerateSQLQuery;
import org.anyframe.ide.querymanager.views.QMResultsView;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.datatools.modelbase.sql.tables.BaseTable;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Implement Query Page. This class implements page class.
 * 
 * @author Junghwan Hong
 * @since 2.1.0
 */
public class QueryPage implements Page {

	// private final int leftWidth = 200;
	// private final int rightWidth = 150;
	public static final String PLUGIN_ID = "org.anyframe.ide.querymanager";
	
	private FormToolkit toolkit;
	private String queryId = "";
	private String queryString = "";
	private String fileName;
	private String queryIdString;
	private String statementString;
	private String passcreatedDateString;
	private String passAuthorName;
	private Document document;

	public Document getDocument() {
		return document;
	}

	public String getStatementString() {
		return statementString;
	}

	int limitRecords = 0;

	// NodeList
	private NodeList queries;

	// WorkBenchSite
	private IWorkbenchPartSite site;

	// input text
	private Text queryIdText;
	private Text queryText;
	private Text resultClassText;
	private Combo mappingStyleCombo;
	private Combo dbConnectionCombo;

	public String getStrQueryIdText() {
		return queryIdText.getText();
	}

	private Text limitRowsText;

	// Button & Check
	private Button isDynamic;
	private Button deleteButton;
	private Button insertButton;
	private Button paramGetButton;
	private Button testButton;
	private Button classButton;

	public Button getTestButton() {
		return testButton;
	}

	private Button limitRows;
	// Section
	private Section input_section;
	// private SQLTextEditor textEditor;

	// TreeViewer
	private TreeViewer treeViewer;
	private TableViewer viewer;

	// table
	private Table table;
	private TableItem items[] = null;

	// array
	String[] typeArray;

	// attribute
	public QueryAttribute queryAttribute;

	private static ImageDescriptor addRowImage;
	private static ImageDescriptor getParamImage;
	private static ImageDescriptor removeRowImage;

	// set
	private static Set paramListNames = new HashSet();
	public static Set velocityContextkey = new HashSet();

	// boolean
	private boolean modifyFlag = false;
	boolean testButtonflag = false;
	boolean querySuccessfull = false;
	boolean limitRowsFlag = true;

	// Editor Input
	private static EditorInput initInput;

	// Connection
	private Connection conn = null;

	private String[] proposals;

	private Control control;

	boolean isDynamicTag;
	private String createdDateString;
	private String createdDateStringModified;
	private IFile file;
	private String pagetitle;

	private Element query;

	private ScrolledForm headForm;

	private IProject project;

	public String[] getProposal() {
		return proposals;
	}

	public Connection getConn() {
		return conn;
	}

	// Title Image
	private Image _TitleImage = ImageUtil.getImage(ImageUtil
			.getImageDescriptor(PLUGIN_ID, "images/queryMgr_Logo.gif"));
	private Image _RunImmage = ImageUtil.getImage(ImageUtil
			.getImageDescriptor(PLUGIN_ID, "images/database_run.png"));

	public void setInput(IEditorInput input) {
		initInput = (EditorInput) input;
	}

	// template
	private Template t2 = null;

	ParseTemplateString parseTemplateString = new ParseTemplateString();

	static {
		URL url = null;
		try {
			url = new URL(QueryManagerActivator.getDefault().getDescriptor()
					.getInstallURL(), "images/queryMgr_getParam.png");
			getParamImage = ImageDescriptor.createFromURL(url);

			url = new URL(QueryManagerActivator.getDefault().getDescriptor()
					.getInstallURL(), "images/queryMgr_addRow.png");
			addRowImage = ImageDescriptor.createFromURL(url);

			url = new URL(QueryManagerActivator.getDefault().getDescriptor()
					.getInstallURL(), "images/queryMgr_removeRow.png");
			removeRowImage = ImageDescriptor.createFromURL(url);

		} catch (MalformedURLException e) {
		}

	}

	/*
	 * 
	 */
	public static QueryPage getInstance() {
		return new QueryPage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * anyframe.querymanager.eclipse.core.querytest.Page#getPage(org.eclipse
	 * .swt.widgets.Composite)
	 */
	public Composite getPage(Composite parent) {
		// TODO Auto-generated method stub
		toolkit = new FormToolkit(parent.getDisplay());

		final ScrolledForm form = toolkit.createScrolledForm(parent);

		// Editor input Initialize
		initEditInput();

		// define section start
		createHeadSection(form, toolkit);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);

		Composite whole = toolkit.createComposite(form.getBody());
		whole.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		whole.setLayoutData(gd);

		createLeftSection(form, toolkit, whole);

		createRightSection(form, toolkit, whole);

		// define section end
		initSetData();

		textListener();

		return form;
	}

	/*
	 * EditorInput initialize
	 */
	private void initEditInput() {
		this.modifyFlag = initInput.isModifyFlag();
		this.queryId = initInput.getName();
		this.queryString = initInput.getQueryString();
		this.queries = initInput.getQueries();
		this.fileName = initInput.getFileName();
		this.site = initInput.getSite();
		this.document = initInput.getDocument();
		this.file = initInput.getFile();
		this.pagetitle = initInput.getTitle();
		this.project = initInput.getProject();
	}

	/*
	 * Initialize setting data(Parameter table)
	 */
	private void initSetData() {
		NodeList nodeList = getDocument().getElementsByTagName("query");

		for (int i = 0; i < nodeList.getLength(); i++) {
			Element node = (Element) nodeList.item(i);

			if (node != null && node.getAttribute("id").equals(queryId)) {

				NodeList childNode = node.getChildNodes();

				// dynamic query check
				if (node.getAttribute("isDynamic").equals("false")) {
					isDynamic.setSelection(false);
				} else {
					isDynamic.setSelection(true);
				}
				// mapping style check
				if (node.getAttribute("mappingStyle").equals("camel")) {
					mappingStyleCombo.select(0);
				} else if (node.getAttribute("mappingStyle").equals("lower")) {
					mappingStyleCombo.select(1);
				} else if (node.getAttribute("mappingStyle").equals("upper")) {
					mappingStyleCombo.select(2);
				} else if (node.getAttribute("mappingStyle").equals("none")) {
					mappingStyleCombo.select(3);
				} else {
					// default camel
					mappingStyleCombo.select(0);
				}
				for (int j = 0; j < childNode.getLength(); j++) {
					if (childNode.item(j).getNodeName()
							.equalsIgnoreCase("result")) {
						NamedNodeMap classAttributes = childNode.item(j)
								.getAttributes();
						String className = classAttributes
								.getNamedItem("class").getNodeValue();
						if (!className.equals(""))
							resultClassText.setText(className);
					}
				}
			}

		}
	}

	private void getQueryAttribute() {
		NodeList nodeList = getDocument().getElementsByTagName("query");
		int num = 1;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element node = (Element) nodeList.item(i);
			if (node != null && node.getAttribute("id").equals(queryId)
					&& node.getAttribute("isDynamic").equals("true")) {
				NodeList childList = nodeList.item(i).getChildNodes();
				// Handle Parameter Mapping
				for (int j = 0; j < childList.getLength(); j++) {
					if (childList.item(j).getNodeName()
							.equalsIgnoreCase("param")) {
						NamedNodeMap paramNodeAttributes = childList.item(j)
								.getAttributes();
						QueryInputAttribute mQueryInputAttribute = new QueryInputAttribute();
						mQueryInputAttribute.setNo(num);

						mQueryInputAttribute.setType(paramNodeAttributes
								.getNamedItem("type").getNodeValue());

						if (childList.item(j).getAttributes()
								.getNamedItem("binding") != null) {
							mQueryInputAttribute.setBinding(paramNodeAttributes
									.getNamedItem("binding").getNodeValue());
						} else {
							mQueryInputAttribute.setBinding("");
						}

						if (childList.item(j).getAttributes()
								.getNamedItem("name") != null) {
							mQueryInputAttribute.setName(paramNodeAttributes
									.getNamedItem("name").getNodeValue());
						} else {
							mQueryInputAttribute.setName("");
						}

						mQueryInputAttribute.setTest("");
						queryAttribute.getInputParamVect().addElement(
								mQueryInputAttribute);
						num++;

					}
				}
			}
		}

	}

	/*
	 * Define Head Section(Tiltle, Description, Message)
	 */
	private void createHeadSection(final ScrolledForm form, FormToolkit toolkit) {
		headForm = form;

		if (modifyFlag) {
			form.setText(MessagePropertiesLoader.editor_querymanager_editquery_title);
			form.setMessage(
					MessagePropertiesLoader.editor_querymanager_editqurey_title_description
							+ " " + this.fileName, SWT.NONE);
//			 queryIdText.setEnabled(false);
		} else {
			form.setText(MessagePropertiesLoader.editor_querymanager_addqurey_title);
			form.setMessage(
					MessagePropertiesLoader.editor_querymanager_addqurey_title_description,
					SWT.NONE);
		}

		form.getForm().getToolBarManager().update(true);
		form.setImage(_TitleImage);

//		fillLocalToolBar(form.getForm().getToolBarManager());

		form.getForm().getToolBarManager().update(true);

		toolkit.decorateFormHeading(form.getForm());

		// get DB session
		// getSession();

		// attribute setting
		queryAttribute = new QueryAttribute();
	}

	private void setMessage(String messageText, int type) {
		headForm.getForm().setMessage(messageText, type);
	}

	public void removeMessage() {
		if (modifyFlag)
			headForm.setMessage(
					MessagePropertiesLoader.editor_querymanager_editqurey_title_description
							+ " " + this.fileName, SWT.NONE);
		else
			headForm.setMessage(
					MessagePropertiesLoader.editor_querymanager_addqurey_title_description,
					SWT.NONE);
	}

//	private void fillLocalToolBar(IToolBarManager toolBarManager) {
//		toolBarManager.add(new QueryPageRefreshAction(pagetitle));
//	}

	private void createLeftSection(final ScrolledForm form,
			FormToolkit toolkit, Composite parent) {
		Composite client_left = toolkit.createComposite(parent);
		client_left.setLayout(new GridLayout());

		GridData gd = new GridData(GridData.FILL_BOTH
				| GridData.VERTICAL_ALIGN_BEGINNING);
		client_left.setLayoutData(gd);

		// start left section....
		createQueryInformation(
				form,
				toolkit,
				MessagePropertiesLoader.editor_querymanager_queryinfo_title,
				client_left);
		createInputParameterInfo(
				form,
				toolkit,
				MessagePropertiesLoader.editor_querymanager_queryinfo_inputparam,
				client_left);
		createResultMappingInfo(form, toolkit, "Result Mapping", client_left);

	}

	private void createRightSection(final ScrolledForm form,
			FormToolkit toolkit, Composite parent) {
		Composite client_right = toolkit.createComposite(parent);
		client_right.setLayout(new GridLayout());

		GridData gd = new GridData(GridData.FILL_BOTH
				| GridData.VERTICAL_ALIGN_BEGINNING);
		client_right.setLayoutData(gd);

		// Get DB Connection
		// start right section....
		createDBBrowser(
				form,
				toolkit,
				MessagePropertiesLoader.editor_querymanager_dbbrowser_title,
				client_right);
		// end right section....
	}

	/**
	 * Get the session Object from SQL Explorer Plug-in and get the connection
	 * object using that session object.
	 */
//	public void getSession() {
//
//		try {
//			// conn = DBSession.getSQLConnection().getConnection();
//			// s
//		} catch (Exception e) {
//			conn = null;
//			PluginLoggerUtil.warning(QueryManagerActivator.PLUGIN_ID,
//					"DB Connection is not.");
//		}
//	}

	/*
	 * Define mapping xml file info(Query ID, Query Statement, mapping style)
	 */
	private void createQueryInformation(final ScrolledForm form,
			FormToolkit toolkit, String title, Composite parent) {
		createSpacer(toolkit, form.getBody(), 4, 2);
		Section section = toolkit.createSection(parent, Section.DESCRIPTION
				| Section.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup()
				.getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(
				FormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);

		// createSpacer(toolkit, client, 2, 4);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		client.setLayout(layout);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		client.setLayoutData(gd);

		// start Qurey Information Desgin
		Label label = new Label(client, SWT.NULL);
		label.setText(MessagePropertiesLoader.editor_querymanager_queryinfo_queryid);

		queryIdText = new Text(client, SWT.BORDER | SWT.SINGLE);
		queryIdText.setText(queryId);
		if(modifyFlag){
			disableQueryID();
		}

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		queryIdText.setLayoutData(gd);

		gd = new GridData(GridData.FILL_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_END);
		// Label dummy = new Label(client, SWT.NULL);
		isDynamic = new Button(client, SWT.CHECK);
		isDynamic
				.setText(MessagePropertiesLoader.editor_querymanager_queryinfo_isdynamic);

		isDynamic.setLayoutData(gd);
		isDynamic.setEnabled(true);

		isDynamic.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				QueryEditor editor = QueryEditor.getDefault(pagetitle);
				editor.setDirty(true);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		// mapping style combobox
		label = new Label(client, SWT.NULL);
		label.setText("Mapping Style:");

		mappingStyleCombo = new Combo(client, SWT.READ_ONLY);
		mappingStyleCombo.setBounds(50, 50, 150, 65);
		String items[] = { "camel", "lower", "upper", "none" };
		mappingStyleCombo.setItems(items);

		if(! modifyFlag){
			// default mapping style
			mappingStyleCombo.select(0);
		}
		
		GridData queryStmtData = new GridData();
		queryStmtData.horizontalSpan = 4;
		label = new Label(client, SWT.NULL);
		label.setText(MessagePropertiesLoader.editor_querymanager_queryinfo_querystatement);
		label.setLayoutData(queryStmtData);

		GridData queryTextData = new GridData(GridData.FILL_BOTH);
		queryTextData.horizontalSpan = 4;
		queryTextData.widthHint = 200;
		queryTextData.heightHint = 40;
		queryTextData.verticalSpan = 40;

		queryText = new Text(client, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);

		queryText.setText(parseQueryText(queryString.trim()));
		queryText.setLayoutData(queryTextData);

		// textListener();

		// queryTestGroup (Query Btn &Test Query Btn & Row Limit)
		GridData gds = new GridData();

		testButton = new Button(client, SWT.PUSH);
		testButton.setFont(client.getFont());
		testButton
				.setText(MessagePropertiesLoader.editor_querymanager_queryinfo_querystatement_testquerybutton);

		testButton.setImage(_RunImmage);
		testButton.setLayoutData(gds);

		limitRows = new Button(client, SWT.CHECK);

		limitRows
				.setText(MessagePropertiesLoader.editor_querymanager_queryinfo_querystatement_rowlimit);
		limitRows.setSelection(true);

		limitRows.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {

				if (limitRows.getSelection()) {
					limitRowsText.setEnabled(true);

				} else {
					if (limitRowsText.getText() == null
							|| limitRowsText.getText().equals("")) {
						// noOfRecordsText.setText("100");
					}
					limitRowsText.setEnabled(false);

				}

			}

		});

		GridData recordData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		limitRowsText = new Text(client, SWT.BORDER | SWT.SINGLE);

		limitRowsText
				.setText(MessagePropertiesLoader.editor_querymanager_queryinfo_querystatement_defaultlimit);

		limitRows.setLayoutData(recordData);
		limitRowsText.setEnabled(true);
		limitRowsText.setTextLimit(3);
		limitRowsText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateResultCount();
			}
		});
		// queryTestGroup END

		// enable Tst Query button
		try {
			 if (null == conn || conn.isClosed()) {
			 		testButton.setEnabled(false);
			} else {
				testButton.setEnabled(true);
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_addquerywizard_sql,
					e);
		}

		testButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				// test Button event
				if (validate())
					runTestBtn();
			}

		});

		// end Query Information Design
		section.setText(title);
		section.setDescription(MessagePropertiesLoader.editor_querymanager_queryinfo_section_desc);
		section.setClient(client);
		section.setExpanded(true);

		gd = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(gd);
	}

	public boolean validate() {
		if (queryIdText.getText().trim().equals(""))
			setMessage(
					MessagePropertiesLoader.editor_querymanager_message_queryidempty,
					IMessage.ERROR);
		else if (queryText.getText().trim().equals(""))
			setMessage(
					MessagePropertiesLoader.editor_querymanager_message_statementEmpty,
					IMessage.ERROR);
		else if (isExistString(queryIdText.getText())
				&& (!modifyFlag && queryId.equals(queryIdText.getText())))
			setMessage(
					MessagePropertiesLoader.editor_querymanager_message_queryidexist,
					IMessage.ERROR);
		else
			return true;
		return false;
	}

	public void editQuery() {
		// query id create or modify(add or edit xml page)
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getShell());
		try {
			dialog.run(false, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor arg0)
						throws InvocationTargetException, InterruptedException {
					// Query Service add or modify
					refreshPage();
					doQSFinish();
				}
			});
			modifyFlag = true;
			fileName = file.getName();
			disableQueryID();
		} catch (InvocationTargetException e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_editXMLFIlE, e);
		} catch (InterruptedException e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_editXMLFIlE, e);
		}
	}

	private void disableQueryID() {
		queryIdText.setEnabled(false);
	}

	private void doQSFinish() {
		Element root = document.getDocumentElement();

		if (!modifyFlag) {
			// Add new Query to SQL XML File
			NodeList nodeList = document.getElementsByTagName("queries");
			populateTagsInfo(null);
			nodeList.item(nodeList.getLength() - 1).appendChild(query);
		} else {
			// Modify Query id In SQL XML file
			NodeList queries = root.getElementsByTagName("query");
			for (int i = 0; i < queries.getLength(); i++) {
				Element node = (Element) queries.item(i);
				if (node != null) {
					if (node.getAttribute("id").equals(queryId)) {
						for (int n = 0; n < root.getChildNodes().getLength(); n++) {
							Object nodeItem = root.getChildNodes().item(n);
							// if (nodeItem instanceof DeferredElementNSImpl) {
							if (nodeItem.toString().indexOf("queries") > -1) {
								try {
									// root.getChildNodes().item(n).appendChild(comment);
									populateTagsInfo(node);
									root.getChildNodes().item(n)
											.replaceChild(query, node);
								} catch (Exception e) {
									PluginLoggerUtil
											.error(QueryManagerActivator.PLUGIN_ID,
													MessagePropertiesLoader.exception_log_editXMLFIlE,
													e);
								}
							}
							// }

						}
					}
				}
			}
		}

		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");

			try {
				String publicID = document.getDoctype().getPublicId();
				String systemID = document.getDoctype().getSystemId();
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
						publicID);
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
						systemID);
			} catch (Exception e) {
				// this is not a file using dtd. using xsd file.
			}

			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			OutputFormat format = new OutputFormat(document);
			format.setIndenting(true);
			format.setIndent(4);
			format.setEncoding("utf-8");
			format.setPreserveSpace(false);

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file.getLocation().toFile()), "UTF-8"));

			XMLSerializer serializer = new XMLSerializer(out, format);

			serializer.serialize(document.getDocumentElement());
			out.close();

			file.refreshLocal(IResource.DEPTH_INFINITE, null);
			queryId = queryIdText.getText();
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_editXMLFIlE, e);
		}

	}

	private void populateTagsInfo(Element node) {
		// define local variables
		String typeValue = "";
		String nameValue = "";
		String bindValue = "";
		Comment comment;

		NodeList queryIdBackup = document.getElementsByTagName("query");
		NodeList statementBackup = document.getElementsByTagName("statement");
		Node statementNode = null;
		// NodeList resultBackup = document.getElementsByTagName("result");
		NodeList resultBackup = null;
		Node resultNode = null;

		for (int i = 0; i < queryIdBackup.getLength(); i++) {
			String id = queryIdBackup.item(i).getAttributes()
					.getNamedItem("id").getNodeValue();
			if (id.equalsIgnoreCase(queryId)) {
				statementNode = statementBackup.item(i);
				resultBackup = queryIdBackup.item(i).getChildNodes();
				for (int j = 0; j < resultBackup.getLength(); j++) {
					if (resultBackup.item(j).getNodeName()
							.equalsIgnoreCase("result")) {
						resultNode = resultBackup.item(j).getAttributes()
								.getNamedItem("class");
					}
				}
			}
		}
		// create comment
		if (passcreatedDateString != null)
			passcreatedDateString = passcreatedDateString.trim();

		if (passAuthorName != null)
			passAuthorName = passAuthorName.trim();

		if (!modifyFlag) {
			createdDateString = "\n" + "\t\t\t\t@date.created\t"
					+ getDateTime() + "\n\t\t\t\t@date.modified\t"
					+ getDateTime() + "\n\t\t\t\t@author\t\t"
					+ System.getProperty("user.name") + "\n\t\t\t";
			comment = document.createComment(createdDateString);
			query = document.createElement("query");
		} else {
			getModifyQueryData();
			createdDateStringModified = "\n" + passcreatedDateString
					+ "\n\t\t\t\t@date.modified\t" + getDateTime() + "\n"
					+ passAuthorName + "\n\t\t\t";
			comment = document.createComment(createdDateStringModified);
			query = node;
		}

		query.setAttribute("id", queryIdText.getText());
		query.setAttribute("isDynamic", isDynamic.getSelection() ? "true"
				: "false");

		// add mappingStyle 12/10/17
		if (mappingStyleCombo.getSelectionIndex() != 0)
			query.setAttribute("mappingStyle", mappingStyleCombo.getText());

		Element statement = document.createElement("statement");

		CDATASection queryStatement = document.createCDATASection("");
		queryStatement.setTextContent("\n\t\t\t\t"
				+ parseQueryText(queryText.getText()) + "\n\t\t\t");
		statement.appendChild(queryStatement);

		// add result class 12/10/22
		Element resultClass = null;
		if (!resultClassText.getText().equals("")) {
			resultClass = document.createElement("result");
			resultClass.setAttribute("class", resultClassText.getText());

		}

		QMPropertiesXMLUtil util = new QMPropertiesXMLUtil();
		if (!modifyFlag) {
			if (util.getPropertiesComment2(file)) {
				query.appendChild(comment);
			}
			query.appendChild(statement);
			// add result class 12/10/22
			if (resultClass != null)
				query.appendChild(resultClass);
		} else {
			query.replaceChild(statement, statementNode);
			// add result class 12/10/22
			if (resultClass != null)
				resultNode.setNodeValue(resultClassText.getText());

			if (util.getPropertiesComment2(file)) {
				query.insertBefore(comment, statement);
			}
		}

		int count = viewer.getTable().getItemCount();
		int i = 0;
		for (int index = 0; index < count; index++) {
			i++;
			String paramAttribute = "";
			typeValue = ((QueryInputAttribute) viewer.getElementAt(index))
					.getType().trim();
			String nameInQueryInputAttribute = ((QueryInputAttribute) viewer
					.getElementAt(index)).getName();
			if (nameInQueryInputAttribute != null) {
				nameValue = nameInQueryInputAttribute.trim();
			}
			String bindInQueryInputAttribute = ((QueryInputAttribute) viewer
					.getElementAt(index)).getBinding();
			if (bindInQueryInputAttribute != null) {
				bindValue = bindInQueryInputAttribute.trim();
			}
			Element param = null;
			param = document.createElement("param");
			if (checkDynamic(nameValue)) {
				if ((nameValue != null) && nameValue.length() > 0) {
					param.setAttribute("name", nameValue);
				}
				if ((typeValue != null) && typeValue.length() > 0) {
					param.setAttribute("type", typeValue);
				}
				if ((bindValue != null) && bindValue.length() > 0) {
					param.setAttribute("binding", bindValue);
				}
			}
			if (param != null) {
				// query.appendChild(param);
			}
		}

		queryAttribute.setQuery(queryText.getText());
		queryAttribute.setQueryId(queryIdText.getText());
	}

	/*
	 * parsing QueryText for query editor or xml editor
	 */
	private String parseQueryText(String queryText) {
		return queryText.indexOf("\t\t\t") > 0 ? replaceIgnoreCase(queryText,
				"\t\t\t\t", "") : replaceIgnoreCase(queryText, "\r\n",
				"\n\t\t\t\t");
	}

	private void getModifyQueryData() {
		Element root = document.getDocumentElement();
		NodeList queries = root.getElementsByTagName("query");

		String authorString = "@author";
		String dateCreatedString = "@date.created";

		for (int i = 0; i < queries.getLength(); i++) {
			Element node = (Element) queries.item(i);

			if (node != null) {
				if (node.getAttribute("id").equals(queryId)) {
					NodeList children = node.getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						if (children.item(j).getNodeName()
								.equalsIgnoreCase("#comment")) {
							String content = children.item(j).getNodeValue();
							String[] comments = content.split("\n");

							for (String comment : comments) {
								if (comment.indexOf(authorString) > 0)
									passAuthorName = comment;
								else if (comment.indexOf(dateCreatedString) > 0)
									passcreatedDateString = comment;
							}
							node.removeChild(children.item(j));
						}
					}
				}
			}
			if (passAuthorName == null)
				passAuthorName = "\t\t\t\t@author\t\t"
						+ System.getProperty("user.name");
			else if (passcreatedDateString == null)
				passcreatedDateString = "\t\t\t\t@date.created\t"
						+ getDateTime();
		}
	}

	/**
	 * @return replace source(pattern) with replace
	 */
	private static String replaceIgnoreCase(String source, String pattern,
			String replace) {
		int sIndex = 0;
		int eIndex = 0;
		String sourceTemp = null;
		StringBuffer result = new StringBuffer();
		sourceTemp = source.toUpperCase();
		while ((eIndex = sourceTemp.indexOf(pattern.toUpperCase(), sIndex)) >= 0) {
			result.append(source.substring(sIndex, eIndex));
			result.append(replace);
			sIndex = eIndex + pattern.length();
		}
		result.append(source.substring(sIndex));
		return result.toString();
	}

	private boolean checkDynamic(String nameValue) {
		return !((nameValue.startsWith("{{") && nameValue.endsWith("}}")) || nameValue
				.startsWith("$"));
	}

	private void runTestBtn() {
		// clear message
		removeMessage();

		setTestButtonflag(true);
		String queryStatement = "";
		try {
			queryStatement = getExecutableQuery();
		} catch (ParseErrorException e1) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_run_query, e1);
		} catch (MethodInvocationException e1) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_run_query, e1);
		} catch (ResourceNotFoundException e1) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_run_query, e1);
		} catch (IOException e1) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_run_query, e1);
		}
		if (queryStatement != null) {
			queryStatement = queryStatement.trim();
		} else {
			setMessage(
					MessagePropertiesLoader.editor_querymanager_message_query_doesnt_suported,
					IMessage.ERROR);
			return;
		}
		if (isSupport(queryStatement)) {
			if (!limitRows.getSelection()) {
				limitRecords = 100;
				showResultsView(queryStatement);
			}
			limitRecords = Integer.parseInt(limitRowsText.getText());
			showResultsView(queryStatement);

		} else {
			// Query is call Procedure or velocity, {{ }}
			setMessage(
					MessagePropertiesLoader.editor_querymanager_message_query_doesnt_suported,
					IMessage.WARNING);
		}

	}

	private boolean isSupport(String queryStatement) {
		if (queryStatement.toLowerCase().indexOf("call") == -1
				&& queryStatement.indexOf("$") == -1
				&& queryStatement.indexOf("{{") == -1
				&& queryStatement.indexOf("#if") == -1
				&& queryStatement.indexOf("#foreach") == -1)
			// support
			return true;
		else
			return false;
	}

	private void textListener() {
		queryIdText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				QueryEditor editor = QueryEditor.getDefault(pagetitle);
				editor.setDirty(true);
				if (validate())
					removeMessage();
			}
		});

		queryText.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
			}

			public void keyPressed(KeyEvent event) {
				// TODO Auto-generated method stub
				if (event.stateMask == SWT.CTRL && event.keyCode == 13) {
					event.doit = false;
					// run Test Query
					if (testButton.getEnabled()) {
						runTestBtn();
					} else {
						setMessage(
								MessagePropertiesLoader.editor_querymanager_message_connection,
								IMessage.WARNING);
					}
				}
			}
		});

		queryText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				QueryEditor editor = QueryEditor.getDefault(pagetitle);
				editor.setDirty(true);
				if (validate())
					removeMessage();
			}
		});

		resultClassText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent arg0) {
				// TODO Auto-generated method stub
				QueryEditor editor = QueryEditor.getDefault(pagetitle);
				editor.setDirty(true);
				if (validate())
					removeMessage();

			}
		});

		mappingStyleCombo.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				QueryEditor editor = QueryEditor.getDefault(pagetitle);
				editor.setDirty(true);
				if (validate())
					removeMessage();
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void setTestButtonflag(boolean testButtonflag) {
		this.testButtonflag = testButtonflag;
	}

	/**
	 * This utility method will convert the statement into SQL statement by
	 * populating test data.
	 * 
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 * @throws MethodInvocationException
	 * @throws ParseErrorException
	 */
	public String getExecutableQuery() throws ParseErrorException,
			MethodInvocationException, ResourceNotFoundException, IOException {

		String nameValue = "";

		// Parameter Data type
		String typeValue = "";

		// Parameter test data
		String testValue = "";

		ArrayList testValueArrayList = new ArrayList();

		// Statement
		String queryStatement = "";

		if (null == conn) {
			setMessage(
					MessagePropertiesLoader.editor_querymanager_message_connection_not,
					IMessage.ERROR);
			return null;
		}

		else {
			queryAttribute.setQuery(queryText.getText());
			queryAttribute.setQueryId(queryIdText.getText());

			queryStatement = queryText.getText();

			// Check whether Query is Dynamic or not

			// Dynamic query

			int count = viewer.getTable().getItemCount();
			Object[] data = new Object[count];
			HashMap typeMap = new HashMap();
			HashMap contextMap = new HashMap();
			for (int index = 1; index <= count; index++) {
				nameValue = ((QueryInputAttribute) viewer
						.getElementAt(index - 1)).getName();
				if (nameValue == null)
					nameValue = "";
				nameValue = nameValue.trim();

				if (nameValue.startsWith("{{") && nameValue.endsWith("}}")) {
					nameValue = nameValue.substring(2, nameValue.length() - 2);
				}

				if (nameValue.startsWith("$")) {
					nameValue = nameValue.substring(1, nameValue.length());
				}

				testValue = ((QueryInputAttribute) viewer
						.getElementAt(index - 1)).getTest();
				if (testValue == null)
					testValue = "";
				testValue = testValue.trim();

				if (!testValue.equalsIgnoreCase("null")) {
					if (testValue.indexOf(",") > -1) {
						ArrayList arrTestValue = DynamicQueryUtil.split(
								testValue, ",");

						Iterator itr = arrTestValue.iterator();
						while (itr.hasNext()) {
							String itrNext = itr.next().toString();
							if (itrNext.startsWith("\"")
									&& itrNext.endsWith("\"")) {
								itrNext = itrNext.substring(1,
										itrNext.length() - 1);
							}
							testValueArrayList.add(itrNext);

						}

						data[index - 1] = nameValue + "=" + arrTestValue;
					} else
						data[index - 1] = nameValue + "=" + testValue;

					// set Velocity context value

					boolean hasKey = velocityContextkey.contains("$"
							+ nameValue);
					if (hasKey) {

						contextMap.put(nameValue, testValueArrayList);
					}
				}
				typeValue = ((QueryInputAttribute) viewer
						.getElementAt(index - 1)).getType();

				typeMap.put(nameValue, typeValue);

			}

			// Call the method ConvertDynamicQuery
			// to convert
			// Dynamic Query to executable SQL
			// query
			DynamicQueryUtil dynamicQueryUtil;
			dynamicQueryUtil = new DynamicQueryUtil();
			try {
				queryStatement = dynamicQueryUtil.convertDynamicQuery(
						queryStatement, typeMap, data, true, contextMap);
			} catch (Exception e) {
				PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
						MessagePropertiesLoader.exception_log_run_query, e);
			}

			if (DynamicQueryUtil.errorMsg != null) {
				// error occurred while converting
				// Dynamic query
				setMessage(DynamicQueryUtil.errorMsg, IMessage.ERROR);

			} else {
				// no error
				// errorMessage = false;
			}
			if (isDynamic.getSelection()) {
				return queryStatement;

			}

			// if (!errorMessage) {

			count = viewer.getTable().getItemCount();
			for (int index = 0; index < count; index++) {
				typeValue = ((QueryInputAttribute) viewer.getElementAt(index))
						.getType();
				nameValue = ((QueryInputAttribute) viewer.getElementAt(index))
						.getName();
				testValue = ((QueryInputAttribute) viewer.getElementAt(index))
						.getTest();

				if (nameValue != null
						&& (nameValue.startsWith("{{") && nameValue
								.endsWith("}}"))
						|| (nameValue != null && nameValue.startsWith("$"))) {

				} else {

					if (typeValue.equalsIgnoreCase("BIGINT")
							|| typeValue.equalsIgnoreCase("DECIMAL")
							|| typeValue.equalsIgnoreCase("DOUBLE")
							|| typeValue.equalsIgnoreCase("FLOAT")
							|| typeValue.equalsIgnoreCase("INTEGER")
							|| typeValue.equalsIgnoreCase("NUMERIC")
							|| typeValue.equalsIgnoreCase("REAL")
							|| typeValue.equalsIgnoreCase("SMALLINT")
							|| typeValue.equalsIgnoreCase("TINYINT")) {

						// Replace question Mark(?)
						// by test data
						queryStatement = DynamicQueryUtil.replace(
								queryStatement, "?", testValue);

					} else {
						// Handling NULL and NOT
						// NULL constraints
						if (testValue != null
								&& (testValue.equalsIgnoreCase("NULL") || testValue
										.equalsIgnoreCase("NOT NULL"))) {
							queryStatement = DynamicQueryUtil.replace(
									queryStatement, "?", testValue);
						} else {
							queryStatement = DynamicQueryUtil.replace(
									queryStatement, "?", "'" + testValue + "'");
						}
					}

				}
			}
		}

		return queryStatement;

	}

	/**
	 * Checks Duplication of Query Ids
	 * 
	 * @param queryId
	 *            query id needs to be searched for duplication
	 * @return boolean
	 */
	public boolean isExistString(String queryId) {
		Element root = document.getDocumentElement();
		NodeList queries = root.getElementsByTagName("query");

		for (int i = 0; i < queries.getLength(); i++) {
			Element node = (Element) queries.item(i);
			if (node != null)
				if (node.getAttribute("id").equals(queryId))
					return true;
		}
		return false;
	}

	/*
	 * Create Result Mapping setting information
	 */
	private void createResultMappingInfo(final ScrolledForm form,
			FormToolkit toolkit, String title, Composite parent) {
		input_section = toolkit.createSection(parent, Section.DESCRIPTION
				| Section.TWISTIE | Section.TITLE_BAR);
		input_section.setActiveToggleColor(toolkit.getHyperlinkGroup()
				.getActiveForeground());
		input_section.setToggleColor(toolkit.getColors().getColor(
				FormColors.SEPARATOR));

		Composite client = toolkit.createComposite(input_section, SWT.WRAP);

		createSpacer(toolkit, client, 3, 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		client.setLayout(layout);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		client.setLayoutData(gd);
		// start Result Mapping Design
		Label label = new Label(client, SWT.NULL);
		label.setText("Result Class :");

		resultClassText = new Text(client, SWT.BORDER | SWT.SINGLE);
		resultClassText.setLayoutData(gd);
		resultClassText.setEditable(false);

		classButton = new Button(client, SWT.PUSH);
		classButton.setText("Browse...");
		classButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});

		// end Result Mapping Design
		input_section.setText(title);
		input_section
				.setDescription(MessagePropertiesLoader.editor_querymanager_querymapping_section_desc);
		input_section.setClient(client);
		input_section.setExpanded(false);

		input_section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {

			}
		});

		gd = new GridData(GridData.FILL_BOTH);
		input_section.setLayoutData(gd);
	}

	/*
	 * Create Input Parameter table information
	 */
	private void createInputParameterInfo(final ScrolledForm form,
			FormToolkit toolkit, String title, Composite parent) {
		input_section = toolkit.createSection(parent, Section.DESCRIPTION
				| Section.TWISTIE | Section.TITLE_BAR);
		input_section.setActiveToggleColor(toolkit.getHyperlinkGroup()
				.getActiveForeground());
		input_section.setToggleColor(toolkit.getColors().getColor(
				FormColors.SEPARATOR));

		Composite client = toolkit.createComposite(input_section, SWT.WRAP);

		createSpacer(toolkit, client, 4, 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		client.setLayout(layout);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		client.setLayoutData(gd);
		// start Input Parameter Design
		getQueryAttribute();
		setColumnAttrubuteControls(client);
		// end Input Parameter Design
		input_section.setText(title);
		input_section
				.setDescription(MessagePropertiesLoader.editor_querymanager_parammapping_section_desc);
		input_section.setClient(client);
		input_section.setExpanded(true);

		input_section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {

			}
		});

		gd = new GridData(GridData.FILL_BOTH);
		input_section.setLayoutData(gd);
	}

	/*
	 * Define Database Browser(Table info, column info ect...)
	 */
	private void createDBBrowser(final ScrolledForm form, FormToolkit toolkit,
			String title, Composite parent) {
		Section section = toolkit.createSection(parent, Section.DESCRIPTION
				| Section.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup()
				.getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(
				FormColors.SEPARATOR));

		Composite client = toolkit.createComposite(section, SWT.WRAP);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		client.setLayout(layout);

		// select data connection profile
		GridData dbData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		client.setLayoutData(dbData);

		Label label = new Label(client, SWT.NULL);
		label.setText("Connection Profile:");

		dbConnectionCombo = new Combo(client, SWT.READ_ONLY);
		dbConnectionCombo.setBounds(50, 50, 150, 65);
		dbConnectionCombo.setLayoutData(dbData);

		if (EDPUtil.getConnectionProfile(project) != null) {
			dbConnectionCombo.setItems(EDPUtil.getConnectionProfile(project));
			dbConnectionCombo.select(0);
		} else {
			setMessage(
					MessagePropertiesLoader.editor_querymanager_message_connection,
					IMessage.WARNING);
		}
		
		dbConnectionCombo.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent arg0) {
				// except "select the profile name"
					testButton.setEnabled(false);
					if (dbConnectionCombo.getSelectionIndex() != 0) {
						// check connection db test
						try {
							if (EDPUtil.connectionTest(project, dbConnectionCombo.getText())) {
								// DB Browser value setting function call
								getDataBaseBrowser();
								testButton.setEnabled(true);
							} 
						} catch(Exception e) {
							setMessage(
									MessagePropertiesLoader.editor_querymanager_message_connection,
									IMessage.WARNING);
							// data browser empty
							if(treeViewer.getInput() != null)
								treeViewer.setInput(null);
							testButton.setEnabled(false);
									
						}	
					} else {
						// data browser empty
						removeMessage();
						
						if(treeViewer.getInput() != null)
							treeViewer.setInput(null);
						testButton.setEnabled(false);
					}
			}
			
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
			}
		});
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		client.setLayoutData(gd);
		// start Qurey Information Desgin
		Group dbBrowser = new Group(client, 0);
		dbBrowser
				.setText(MessagePropertiesLoader.editor_querymanager_dbbrowser_title);
		GridData dbBrowserGD = new GridData(GridData.FILL_BOTH);
		dbBrowserGD.widthHint = 70;
		dbBrowserGD.heightHint = 200;
		dbBrowserGD.horizontalSpan = 2;

		dbBrowser.setLayoutData(dbBrowserGD);
		dbBrowser.setLayout(layout);

		treeViewer = new TreeViewer(dbBrowser, SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.MULTI | SWT.BORDER | SWT.FILL | SWT.WRAP);

		// use hash lookup to improve performance
		treeViewer.setUseHashlookup(true);
		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

//		if (conn != null) 
//			// DB Browser value setting function call
//			getDataBaseBrowser();
		

		// end Query Information Design
		section.setText(title);
		section.setDescription(MessagePropertiesLoader.editor_querymanager_databrowser_section_desc);
		section.setClient(client);
		section.setExpanded(true);

		gd = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(gd);
	}

	private void createSpacer(FormToolkit toolkit, Composite parent, int span,
			int height) {
		Label spacer = toolkit.createLabel(parent, "");
		GridData gd = new GridData();
		gd.heightHint = height;
		gd.horizontalSpan = span;
		spacer.setLayoutData(gd);
	}
	
	private Connection getConnection(JdbcOption jdbcOption) throws Exception {
		Properties connectionProps = new Properties();
		connectionProps.put("user", jdbcOption.getUserName());
		connectionProps.put("password", jdbcOption.getPassword());

		try {
			Driver driver = ConnectionUtil.getDriverFromPath(jdbcOption
					.getDriverJar(), jdbcOption.getDriverClassName());
			return driver.connect(jdbcOption.getUrl(), connectionProps);
		} catch (Exception e) {
			throw e;
		}
	}

	public void getDataBaseBrowser() throws Exception {
		try {
			String profileName = dbConnectionCombo.getText();
			conn = EDPUtil.getConnection(project, profileName);
			treeViewer.setContentProvider(new SQLModelContentProvider());
			
			ILabelProvider lp = new SQLModelLabelProvider();
			ILabelDecorator decorator = PlatformUI.getWorkbench()
					.getDecoratorManager().getLabelDecorator();
			
			treeViewer.setLabelProvider(new SQLModelDecoratingLabelProvider(lp, decorator));
			
			treeViewer.setInput(EDPUtil.getDatabase(project, profileName));

			treeViewer.refresh();

			// data browser double click event
			treeViewer.addDoubleClickListener(new IDoubleClickListener() {

				public void doubleClick(DoubleClickEvent arg0) {
					// TODO Auto-generated method stub
					ISelection selection = arg0.getSelection();
					Object obj = ((TreeSelection) selection).getFirstElement();

					TreeViewer treeViewer = (TreeViewer) arg0.getViewer();
					if (treeViewer.getExpandedState(obj)) {
						treeViewer.setExpandedState(obj, false);
					} else {
						treeViewer.setExpandedState(obj, true);
					}
				}
			});

			if (conn != null) {
				final GenerateSQLQuery genSQLQuery = new GenerateSQLQuery();

				treeViewer.addDoubleClickListener(new IDoubleClickListener() {
					public void doubleClick(DoubleClickEvent event) {
						TreeSelection selected = (TreeSelection) event
								.getSelection();
						Object obj = ((TreeSelection) selected)
								.getFirstElement();
						String qText = queryText.getText();
						// This works for Tables and Views.
						if (obj instanceof BaseTable) {
							String tempStr = genSQLQuery
									.appendAtCursorPosInSQLClause(qText,
											queryText.getCaretPosition(),
											((BaseTable) obj).getName(),
											"TABLE");
							if (!tempStr.equals(qText)) {
								queryText.setText(tempStr);
								queryText.setSelection(genSQLQuery
										.getNewCursorPos());
							} else if (genSQLQuery.getErrorMessage() != null) {
								genSQLQuery.setErrorMessageToNull();
							} else {
							}
						} else if (obj instanceof Column) {
							String tempStr = genSQLQuery
									.appendAtCursorPosInSQLClause(qText,
											queryText.getCaretPosition(),
											((Column) obj).getName(), "COLUMN");
							if (!tempStr.equals(qText)) {
								queryText.setText(tempStr);
								queryText.setSelection(genSQLQuery
										.getNewCursorPos());
							} else if (genSQLQuery.getErrorMessage() != null) {

								genSQLQuery.setErrorMessageToNull();
							}
						}
					}
				});
				testButton.setEnabled(true);
			} else {
				PluginLoggerUtil
						.warning(
								QueryManagerActivator.PLUGIN_ID,
								MessagePropertiesLoader.editor_querymanager_message_connection);
			}

		} catch (Exception e) {
			PluginLoggerUtil
			.warning(
					QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.editor_querymanager_message_connection);
			throw new Exception("Get SQL Connection Exception");
		}

		treeViewer.refresh();
	}

	/**
	 * Create Input parameter table and populate that table with data
	 * 
	 * @param container
	 *            Composite object
	 * 
	 */

	private void setColumnAttrubuteControls(final Composite container) {
		table = new Table(container, SWT.MULTI | SWT.FULL_SELECTION
				| SWT.V_SCROLL | SWT.BORDER);

		viewer = new TableViewer(table);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// for alignment of add and delete buttons
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
		gridData.verticalSpan = 10;
		gridData.heightHint = 60;

		TableColumn tableColumn = new TableColumn(table, 0);
		tableColumn.setAlignment(0x1000000);
		tableColumn
				.setText(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_no);

		tableColumn = new TableColumn(table, 0);
		tableColumn.setAlignment(0x1000000);
		tableColumn
				.setText(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_name);

		tableColumn = new TableColumn(table, 0);
		tableColumn.setAlignment(0x1000000);
		tableColumn
				.setText(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_datatype);

		tableColumn = new TableColumn(table, 0);
		tableColumn.setAlignment(0x1000000);
		tableColumn
				.setText(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_binding);

		tableColumn = new TableColumn(table, 0);
		tableColumn.setAlignment(0x1000000);
		tableColumn
				.setText(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_testdata);

		// all columns visible in the table
		for (int i = 0; i < 5; i++) {
			TableColumn tc = table.getColumn(i);
			if (i == 0) {
				tc.setWidth(40);
			} else if (i == 1)
				tc.setWidth(80);
			else if (i == 2)
				tc.setWidth(80);
			else if (i == 3)
				tc.setWidth(80);
			else
				// reduced the width
				tc.setWidth(120);
		}
		// setting the table gridData
		table.setLayoutData(gridData);

		attachContentProvider(viewer);
		attachLabelProvider(viewer);
		attachCellEditors(viewer, table);
		for (int i = 0; i < queryAttribute.getInputParamVect().size(); i++) {
			QueryInputAttribute column = (QueryInputAttribute) queryAttribute
					.getInputParamVect().elementAt(i);
			viewer.add(column);
		}

		Label dummy = new Label(container, SWT.NULL);

		paramGetButton = new Button(container, SWT.FLAT);
		paramGetButton
				.setToolTipText(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_getparameter);
		paramGetButton.setImage(getParamImage.createImage());

		paramGetButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				// remove message
				removeMessage();
				queryAttribute.getInputParamVect().clear();
				// table.clearAll();
				table.removeAll();

				if (isSupport(queryText.getText()))
					getParamRow();
				else
					setMessage(
							MessagePropertiesLoader.editor_querymanager_message_query_doesnt_suported,
							IMessage.WARNING);
			}

		});

		// Insert Button to insert rows in parameter

		insertButton = new Button(container, SWT.FLAT);
		insertButton
				.setToolTipText(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_add);
		insertButton.setImage(addRowImage.createImage());
		insertButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {

				// remove message
				removeMessage();

				// Insert the row after the row which
				// is selected, not velocity
				if (isSupport(queryText.getText())) {
					addRow();
				} else {
					queryAttribute.getInputParamVect().clear();
					table.clearAll();

					setMessage(
							MessagePropertiesLoader.editor_querymanager_message_query_doesnt_suported,
							IMessage.WARNING);
				}
			}

		});
		// Delete Button to delete rows in parameter
		deleteButton = new Button(container, SWT.FLAT);
		deleteButton
				.setToolTipText(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_delete);
		deleteButton.setImage(removeRowImage.createImage());

		deleteButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {

				// remove message
				removeMessage();

				// Logic for Multiple deletion
				int selectionIndices[] = table.getSelectionIndices();
				int selectedIndex;
				// not velocity
				if (populateParamsNames_())
					for (int j = 0; j < selectionIndices.length; j++) {
						if (j == 0) {
							selectedIndex = selectionIndices[j];
						} else {
							selectedIndex = selectionIndices[j] - j;
						}

						if (selectedIndex != -1) {
							table.remove(selectedIndex);

							if (selectedIndex != table.getItemCount())
								table.setSelection(selectedIndex);
							else
								table.setSelection(selectedIndex - 1);

							for (int i = 0; i < queryAttribute
									.getInputParamVect().size(); i++) {
								if (i == selectedIndex) {

									queryAttribute.getInputParamVect()
											.remove(i);
									break;
								}
							}
							// update Parameter table
							// Serial number
							updateParamNumber(selectedIndex);

						}
						String parseText = queryText.getText();
						if (!isDynamic.getSelection()) {
							int count = parseQ(parseText, false);
							int rowCount = table.getItemCount();

							if (count == rowCount) {
								// setMessage(
								// MessagePropertiesLoader.anyframe_querymanager_eclipse_core_addQueryWizardPage_testQuery,
								// IMessage.ERROR);
							} else if (count < rowCount) {
								setMessage(
										MessagePropertiesLoader.editor_querymanager_message_error,
										IMessage.ERROR);
							} else if (count > rowCount) {
								setMessage(
										MessagePropertiesLoader.editor_querymanager_message_error,
										IMessage.ERROR);
							}

						} else {

							int rowCount = table.getItemCount();
							int count = parseQ(parseText, true);
							if (count < rowCount) {
								setMessage(
										MessagePropertiesLoader.editor_querymanager_message_error,
										IMessage.ERROR);
							} else if (count > rowCount) {
								setMessage(
										MessagePropertiesLoader.editor_querymanager_message_error,
										IMessage.ERROR);
							}
						}

					}
			}

		});

	}

	private void getParamRow() {
		paramListNames.clear();
		velocityContextkey.clear();

		if (modifyFlag) {
			// modify query
			String parseText = queryText.getText();

			if (parseDynamicQ(parseText) > 0)
				isDynamic.setSelection(true);
			if (!isDynamic.getSelection()) {
				populateParamsNames();
				setQueryParams(parseQ(parseText, false));
				viewer.setInput(null);
				ArrayList list = new ArrayList();
				for (int i = 0; i < queryAttribute.getInputParamVect().size(); i++) {
					QueryInputAttribute column = (QueryInputAttribute) queryAttribute
							.getInputParamVect().elementAt(i);
					list.add(column);
				}
				viewer.setInput(list.toArray());
			} else {
				populateParamsNames();
				setQueryParams(parseQ(parseText, true));
				viewer.setInput(null);
				ArrayList list = new ArrayList();
				for (int i = 0; i < queryAttribute.getInputParamVect().size(); i++) {
					QueryInputAttribute column = (QueryInputAttribute) queryAttribute
							.getInputParamVect().elementAt(i);
					list.add(column);
				}
				viewer.setInput(list.toArray());
			}
		} else {
			// add query
			populateParamsNames();
			String parseText = queryText.getText();
			if (parseDynamicQ(parseText) > 0)
				isDynamic.setSelection(true);
			if (!isDynamic.getSelection()) {
				int count = parseQ(parseText, false);
				setQueryParams(count);
				viewer.setInput(null);
				ArrayList list = new ArrayList();
				for (int i = 0; i < queryAttribute.getInputParamVect().size(); i++) {
					QueryInputAttribute column = (QueryInputAttribute) queryAttribute
							.getInputParamVect().elementAt(i);
					list.add(column);
				}
				viewer.setInput(list.toArray());
			} else {
				populateParamsNames();
				int count = parseQ(parseText, true);
				setQueryParams(count);
				viewer.setInput(null);
				ArrayList list = new ArrayList();
				for (int i = 0; i < queryAttribute.getInputParamVect().size(); i++) {

					QueryInputAttribute column = (QueryInputAttribute) queryAttribute
							.getInputParamVect().elementAt(i);
					list.add(column);
				}
				viewer.setInput(list.toArray());
				viewer.refresh();
			}

		}

	}

	private void addRow() {
		if (table.getSelectionIndex() != -1) {
			int selectedIndex = table.getSelectionIndex();

			QueryInputAttribute mQueryInputAttribute = new QueryInputAttribute();
			mQueryInputAttribute.setNo(selectedIndex + 2);
			mQueryInputAttribute.setName("");
			mQueryInputAttribute
					.setType(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_varchar);
			mQueryInputAttribute.setBinding("");
			mQueryInputAttribute.setTest("");
			queryAttribute.getInputParamVect().insertElementAt(
					mQueryInputAttribute, selectedIndex + 1);

			viewer.insert(mQueryInputAttribute, selectedIndex + 1);

			table.setSelection(selectedIndex + 1);

			// update Parameter table Serial
			// number
			updateParamNumber(selectedIndex + 2);

		} else {

			QueryInputAttribute mQueryInputAttribute = new QueryInputAttribute();
			mQueryInputAttribute.setNo(table.getItemCount() + 1);
			mQueryInputAttribute.setName("");

			mQueryInputAttribute
					.setType(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_varchar);
			mQueryInputAttribute.setBinding("");
			mQueryInputAttribute.setTest("");
			queryAttribute.getInputParamVect().addElement(mQueryInputAttribute);

			viewer.add(mQueryInputAttribute);
			table.setSelection(table.getItemCount() - 1);
		}
		String parseText = queryText.getText();
		if (!isDynamic.getSelection()) {
			int count = parseQ(parseText, false);
			int rowCount = table.getItemCount();

			if (count < rowCount) {
				setMessage(
						MessagePropertiesLoader.editor_querymanager_message_error,
						IMessage.ERROR);
			} else if (count > rowCount) {
				setMessage(
						MessagePropertiesLoader.editor_querymanager_message_error,
						IMessage.ERROR);
			}
		} else {

			int rowCount = table.getItemCount();
			int count = parseQ(parseText, true);
			if (count < rowCount) {
				setMessage(
						MessagePropertiesLoader.editor_querymanager_message_error,
						IMessage.ERROR);
			} else if (count > rowCount) {
				setMessage(
						MessagePropertiesLoader.editor_querymanager_message_error,
						IMessage.ERROR);
			}
		}
	}

	/**
	 * Content provider for Input Parameter table viewer
	 * 
	 * @param viewer
	 *            TableViewer object
	 */

	private void attachContentProvider(TableViewer viewer) {
		viewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {

				return (Object[]) inputElement;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
	}

	/**
	 * Label provider for Input Parameter table viewer
	 * 
	 * @param viewer
	 *            TableViewer object
	 */
	private void attachLabelProvider(TableViewer viewer) {
		viewer.setLabelProvider(new ITableLabelProvider() {

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {

				switch (columnIndex) {
				case 0:
					// return Integer.toString(rowNum);
					return Integer.toString(((QueryInputAttribute) element)
							.getNo());
				case 1:
					return ((QueryInputAttribute) element).getName();
				case 2:
					return ((QueryInputAttribute) element).getType();
				case 3:
					return ((QueryInputAttribute) element).getBinding();
				case 4:
					return ((QueryInputAttribute) element).getTest();
				default:
					return "";
				}
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}
		});
	}

	/**
	 * This method is called when the user modifies a cell in the tableViewer
	 * 
	 * @param viewer
	 *            Input Parameter table viewer
	 * @param parent
	 *            The composite object
	 */

	private void attachCellEditors(final TableViewer viewer, Composite parent) {
		typeArray = getSQLTypes();
		viewer.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				if (property.equals("no"))
					return false;
				else
					return true;
			}

			public Object getValue(Object element, String property) {

				if (property.equals("no"))
					return Integer.toString(((QueryInputAttribute) element)
							.getNo());
				else if (property.equals("name"))
					return ((QueryInputAttribute) element).getName();
				else if (property.equals("type")) {
					int i = Arrays.asList(typeArray).indexOf(
							((QueryInputAttribute) element).getType()
									.toUpperCase());
					return i == -1 ? null : new Integer(i);

				}

				else if (property.equals("binding"))
					return ((QueryInputAttribute) element).getBinding();
				else if (property.equals("test"))
					return ((QueryInputAttribute) element).getTest();
				else
					return null;
			}

			public void modify(Object element, String property, Object value) {

				TableItem tableItem = (TableItem) element;
				QueryInputAttribute queryInputAttribute = (QueryInputAttribute) tableItem
						.getData();
				if (property.equals("no"))
					queryInputAttribute.setNo(Integer.getInteger(
							value.toString()).intValue());
				else if (property.equals("name")) {
					queryInputAttribute.setName(value.toString());
				} else if (property.equals("type")) {
					int i = ((Integer) value).intValue();
					queryInputAttribute.setType(typeArray[i]);
				}

				else if (property.equals("binding")) {
					queryInputAttribute.setBinding(value.toString());
				} else if (property.equals("test")) {
					queryInputAttribute.setTest(value.toString());
				} else {
				} // do nothing

				viewer.refresh(queryInputAttribute);
			}
		});

		viewer.setCellEditors(new CellEditor[] { new TextCellEditor(parent),

		new TextCellEditor(parent),
				new ComboBoxCellEditor(parent, typeArray, SWT.READ_ONLY),
				new TextCellEditor(parent), new TextCellEditor(parent) });

		viewer.setColumnProperties(new String[] { "no", "name", "type",
				"binding", "test" });
	}

	/**
	 * Returns java.sql Data types
	 * 
	 * @return String[] array of data Types
	 */

	public String[] getSQLTypes() {

		Field[] fields = java.sql.Types.class.getFields();
		String returnTypes[] = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			returnTypes[i] = fields[i].getName();
		}
		Arrays.sort(returnTypes);
		return returnTypes;

	}

	/**
	 * This method returns the number of ? in the parse text
	 * 
	 * @param query
	 * @return
	 */
	private static int parseDynamicQ(String dynamiQuery) {
		Set resultSet = new HashSet();
		String[] result = dynamiQuery.split("\\s");
		String token = "";

		boolean cmatch = false;
		for (int x = 0; x < result.length; x++) {

			char chars[] = result[x].toCharArray();
			boolean preChar = false;
			for (int i = 0; i < result[x].length(); i++) {
				char ch = chars[i];
				String s = "";
				String s1 = "";
				if (ch == '$' || ch == ':') {
					token = result[x].substring(i, result[x].length());
					Pattern pattern = Pattern.compile("\\W");
					Matcher matcher = pattern.matcher(token);
					s = matcher.replaceAll("");
					resultSet.add(s);
				}
			}
		}

		return resultSet.size();
	}

	private boolean populateParamsNames_() {
		String parseText = queryText.getText();

		try {
			if (ParseTemplateString.getTemplate(parseText) == null) {
				setMessage(
						MessagePropertiesLoader.editor_querymanager_message_query_doesnt_suported,
						IMessage.WARNING);
				return false;
			}
		} catch (Exception e1) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_run_query, e1);
		}
		return true;
	}

	private void populateParamsNames() {
		String parseText = queryText.getText();
		int nQuestionMarks = 0;

		try {
			if (ParseTemplateString.getTemplate(parseText) == null) {
				// setMessage(
				// MessagePropertiesLoader.QUERYMANAGER_QUERYTESTEDITOR_Msg_improperVelocityTemplate_error,
				// IMessage.ERROR);
				setMessage(
						MessagePropertiesLoader.editor_querymanager_message_query_doesnt_suported,
						IMessage.WARNING);
				return;
			} else
				t2 = ParseTemplateString.getTemplate(parseText);
		} catch (Exception e1) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_run_query, e1);
		}

		VelocityContext ctx = new VelocityContext();

		Writer writer = new StringWriter();

		try {
			t2.merge(ctx, writer);
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_run_query, e);
		}
		String queryWithOutVeocity = writer.toString();
		String[] result2 = queryWithOutVeocity.split("\\s");

		// we are parsing the queryWithOutVeocity

		for (int x = 0; x < result2.length; x++) {
			char chars[] = result2[x].toCharArray();
			// boolean preChar = false;
			for (int i = 0; i < result2[x].length(); i++) {
				char ch = chars[i];

				int end = i + 1;
				if (ch == '$' || ch == ':') {
					if (result2[x].length() > 1) {
						for (int j = i + 1; j < result2[x].length(); j++) {
							char c = result2[x].charAt(j);
							if (c == '(') {
								break;
							} else if (c == '.') {
								end = j;
							} else if (!Character.isLetterOrDigit(c)
									&& c != '_') {
								end = j;
								break;
							} else if (j == result2[x].length() - 1) { // last
								// character
								end = j + 1;
							}
						}
						paramListNames.add(result2[x].substring(i + 1, end));
					}
				} else if (ch == '?') {
					paramListNames.add("?" + nQuestionMarks++);
				}
			}
		}

		// we are parsing the complete text including the velocity text also ...

		Template t = null;
		try {
			t = ParseTemplateString.getTemplate(parseText);
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					MessagePropertiesLoader.exception_log_run_query, e);
		}
		// List l contains the list of all the tokens inside the velocity
		// template
		List l = parseTemplateString.referenceList(t);

		for (int i = 0; i < l.size(); i++) {

			if (l.get(i).equals(parseTemplateString.getForeachListToken())) {
				velocityContextkey.add(l.get(i));
			}
		}

		// now adding the velocityContextkey items to paramListNames

		Iterator itr = velocityContextkey.iterator();
		while (itr.hasNext()) {
			paramListNames.add(itr.next());

		}
	}

	/**
	 * This method returns the number of ? in the parse text
	 * 
	 * @param query
	 * @return
	 */
	private int parseQ(String query, boolean isDynamic) {
		return paramListNames.size();
	}

	/**
	 * create count number of rows in parameter table and populate the those
	 * rows with default data
	 * 
	 * @param count
	 *            Number of rows to be created in parameter table
	 */
	private void setQueryParams(int count) {

		int num = 1;
		String parseText = queryText.getText();

		Object names[] = paramListNames.toArray();
		if (count - queryAttribute.getInputParamVect().size() > 1) {

			for (int i = queryAttribute.getInputParamVect().size(); i < count; i++) {
				QueryInputAttribute mQueryInputAttribute = new QueryInputAttribute();

				mQueryInputAttribute.setNo(i + 1);
				if (((String) names[i]).startsWith("?")) // just
					mQueryInputAttribute.setName("");
				else
					mQueryInputAttribute.setName((String) names[i]);
				if ((parseTemplateString.getForeachListToken() != null)
						&& (mQueryInputAttribute.getName()
								.equalsIgnoreCase(parseTemplateString
										.getForeachListToken()))) {
					mQueryInputAttribute.setType("ARRAY");
					mQueryInputAttribute.setTest("\"\",\"\"");

				} else {
					mQueryInputAttribute
							.setType(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_varchar);
					mQueryInputAttribute.setTest("");
				}

				mQueryInputAttribute.setBinding("");
				queryAttribute.getInputParamVect().addElement(
						mQueryInputAttribute);
			}

		} else {
			for (int i = queryAttribute.getInputParamVect().size(); i < count; i++) {
				QueryInputAttribute mQueryInputAttribute = new QueryInputAttribute();
				mQueryInputAttribute.setNo(num
						+ queryAttribute.getInputParamVect().size());
				if (names.length > 0)
					mQueryInputAttribute.setName((String) names[i]);
				else
					mQueryInputAttribute.setName("");

				if ((parseTemplateString.getForeachListToken() != null)
						&& (mQueryInputAttribute.getName()
								.equalsIgnoreCase(parseTemplateString
										.getForeachListToken()))) {
					mQueryInputAttribute.setType("ARRAY");
					mQueryInputAttribute.setTest("\"\",\"\"");

				} else {
					mQueryInputAttribute
							.setType(MessagePropertiesLoader.editor_querymanager_queryinfo_parameter_varchar);
					mQueryInputAttribute.setTest("");
				}
				mQueryInputAttribute.setBinding("");
				queryAttribute.getInputParamVect().addElement(
						mQueryInputAttribute);

				num = num + 1;
			}
		}
	}

	public boolean isTestButtonflag() {
		return testButtonflag;
	}

	/**
	 * Update Input Parameter table serial Number
	 * 
	 * @param selectedIndex
	 *            index of selected row
	 */
	private void updateParamNumber(int selectedIndex) {
		items = table.getItems();

		for (int i = selectedIndex; i < items.length; i++) {
			items[i].setText(0, String.valueOf(i + 1));
			QueryInputAttribute column = (QueryInputAttribute) queryAttribute
					.getInputParamVect().elementAt(i);
			column.setNo(i + 1);

		}
	}

	/**
	 * Validate limitRowsText Text field
	 */
	void validateResultCount() {

		limitRowsFlag = true;
		String resltCounts = limitRowsText.getText();
		if (resltCounts != null && !resltCounts.equals("")) {
			for (int j = 0; j < resltCounts.length(); j++) {
				char ch = resltCounts.charAt(j);
				if (ch >= '0' && ch <= '9') {
				} else {
					limitRowsFlag = false;
					break;
				}
			}
		} else {
			limitRowsFlag = false;
		}
		// checkComplete();
	}

	/**
	 * Executes query and shows the results in Result view if it is successful
	 * 
	 * @param queryStatement
	 *            Query statement to be executed
	 */
	public void showResultsView(String queryStatement) {

		try {

			if (null == conn || conn.isClosed()) {
				setMessage(
						MessagePropertiesLoader.editor_querymanager_message_connection,
						IMessage.WARNING);
				querySuccessfull = false;
				return;

			}
			queryStatement = queryStatement.trim();

			// if (queryStatement.toLowerCase().startsWith("select")) {

			// remove this code for SQL Explorer Version problem
			/*
			 * QMQuery qmQuery = new QMQuery(new StringBuffer(queryStatement),
			 * limitRecords); DatabaseProduct.ExecutionResults results = null;
			 * DatabaseProduct product = SQLExplorerPlugin.getDefault()
			 * .getDatabaseStructureView().getSession() .getDatabaseProduct();
			 * try { if (queryStatement.toLowerCase().startsWith("select")) {
			 * results = product .executeQuery(DBSession.getSQLConnection(),
			 * qmQuery, limitRecords); } } catch (RuntimeException e) {
			 * querySuccessfull = false; throw new SQLException(e.getMessage());
			 * } catch (SQLException e) {
			 * setMessage("Check the query. Query is not available.",
			 * IMessage.ERROR); }
			 */

			// DataSet dataSet;
			// dataSet = results.nextDataSet();

			// }

			Statement stmt = conn.createStatement();
			stmt.setMaxRows(limitRecords);
			ResultSet rs = null;
			ResultSetMetaData metaData = null;
			int count = 0;

			String startText = queryStatement.toUpperCase();
			try {
				if (startText.startsWith("SELECT")) {
					rs = stmt.executeQuery(queryStatement);
					metaData = rs.getMetaData();
				} else if (startText.startsWith("INSERT")
						|| startText.startsWith("DELETE")
						|| startText.startsWith("UPDATE")) {
					count = stmt.executeUpdate(queryStatement);
					String querySe = queryStatement.toUpperCase().startsWith(
							"INSERT") ? "inserted." : "updated.";
					String successMessage = count + " " + "counts" + " "
							+ querySe;
					setMessage(successMessage, IMessage.INFORMATION);
				} else {
					setMessage("Check the query. Query is not supported.",
							IMessage.ERROR);
				}
			} catch (RuntimeException e) {
				querySuccessfull = false;
				throw new SQLException(e.getMessage());
			} catch (SQLException e) {
				setMessage("Check the query. Query is not available.",
						IMessage.ERROR);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			// ResultSet rs = stmt.executeQuery(queryStatement);
			// ResultSetMetaData metaData = rs.getMetaData();

			if (rs != null && count == 0) {
				String queryAttrWithoutUnderScore = "";
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					// camelCheck.setEnabled(true);

					QueryOutputAttribute queryOutputAttribute = new QueryOutputAttribute();

					// set Column name
					queryOutputAttribute.setColumnName(rs.getMetaData()
							.getColumnName(i));

					queryOutputAttribute.setAttribute(rs.getMetaData()
							.getColumnName(i).toLowerCase());

					String dataType = rs.getMetaData().getColumnClassName(i);

					// set VO attribute's data type
					queryOutputAttribute.setDataType(dataType.substring(
							dataType.lastIndexOf(".") + 1, dataType.length()));
					queryAttribute.getAttrOutputVect().addElement(
							queryOutputAttribute);
				}
				querySuccessfull = true;
			}

			querySuccessfull = true;

			try {
				if (rs != null && rs.getMetaData() != null) {
				
					QMResultsView resultsView = (QMResultsView) site
							.getPage()
							.showView(
									"org.anyframe.ide.querymanager.views.QMResultsView");
					resultsView.setResults(rs, queryStatement);
					
				}
			} finally {
				try {
					if (rs != null)
						rs.close();
				} catch (SQLException e) {
				}
			}
			// rs.close();
			stmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getQueryIdString() {
		return queryIdString;
	}

	public String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	private void setDocument(File file) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			// builder.setEntityResolver(new DTDResolver());
			document = builder.parse(file);
		} catch (FileNotFoundException e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Check DTD in XML file.", e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void refreshPage() {
		setDocument(file.getLocation().toFile());
		queries = document.getDocumentElement().getElementsByTagName("query");
	}

	/**
	 * This utility method opens the dialog to select projects from current
	 * workspace
	 */

	@SuppressWarnings("restriction")
	private void handleBrowse() {

		FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				false, PlatformUI.getWorkbench().getProgressService(),
				SearchEngine.createWorkspaceScope(), IJavaSearchConstants.TYPE);

		dialog.setTitle("Result Class");
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				resultClassText.setText(((SourceType) result[0])
						.getFullyQualifiedName());
			}
		}
	}
}
