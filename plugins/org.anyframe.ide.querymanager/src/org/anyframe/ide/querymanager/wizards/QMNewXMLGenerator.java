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
package org.anyframe.ide.querymanager.wizards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.wst.sse.core.internal.encoding.CommonEncodingPreferenceNames;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.ui.internal.wizards.NewXMLGenerator;

/**
 * QMNewXMLGenerator class creates SQL XML file with Default Content.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QMNewXMLGenerator extends NewXMLGenerator {

	/**
	 * creates SQL XML file with Default Content
	 * 
	 * @param newQMFile
	 *            The IFile object
	 * @throws Exception
	 *             when this method fails
	 */
	public void createEmptyXMLDocument(IFile newQMFile) throws Exception {

		ByteArrayOutputStream queryMgrEditorOutputStream = new ByteArrayOutputStream();
		String peferedCharSet = getUserPreferredCharset();

		PrintWriter queryMgrEditorWriter = new PrintWriter(new OutputStreamWriter(
				queryMgrEditorOutputStream, peferedCharSet));
		//Declaration and invocation for well-formed Anyframe XML
		queryMgrEditorWriter.println("<?xml version=\"1.0\" encoding=\"" + peferedCharSet + "\"?>"); //$NON-NLS-1$ //$NON-NLS-2$
		//for using xsd only
		queryMgrEditorWriter.println("<queryservice xmlns=\"http://www.anyframejava.org/schema/query/mapping\" ");
		queryMgrEditorWriter.println("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		queryMgrEditorWriter.println("    xsi:schemaLocation=\"http://www.anyframejava.org/schema/query/mapping http://www.anyframejava.org/schema/query/anyframe-query-mapping-1.0.xsd \">");
		//for using dtd only				
//		queryMgrEditorWriter.println("<!DOCTYPE queryservice PUBLIC \"-//ANYFRAME//DTD QUERYSERVICE//EN\"");
//		queryMgrEditorWriter.println("        \"http://www.anyframejava.org/dtd/anyframe-core-query-mapping-3.2.dtd\">");
//		queryMgrEditorWriter.println("<queryservice>");
		
		queryMgrEditorWriter.println("    <queries>");
		queryMgrEditorWriter.println("    </queries>");
		queryMgrEditorWriter.println("</queryservice>");
		queryMgrEditorWriter.flush();
		queryMgrEditorOutputStream.close();

		ByteArrayInputStream qmEditorInputStream = new ByteArrayInputStream(
				queryMgrEditorOutputStream.toByteArray());
		newQMFile.setContents(qmEditorInputStream, true, true, null);
		qmEditorInputStream.close();
	}

	/**
	 * Get User Preferred Charset
	 * 
	 * @return charSet
	 */
	private String getUserPreferredCharset() {
		Preferences qmEditorPreference = XMLCorePlugin.getDefault()
				.getPluginPreferences();
		String peferedCharSet = qmEditorPreference
				.getString(CommonEncodingPreferenceNames.OUTPUT_CODESET);
		return peferedCharSet;
	}

}
