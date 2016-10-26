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
package org.anyframe.ide.querymanager.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * The JavaClassTemplateGenerator class helps to create and Modify VO classes
 * using Java-based template engine called Velocity.
 * 
 * @author Surindhar.Kondoor
 * @author Neha.Prasad
 */
public class JavaClassTemplateGenerator_EQS {

	// public String templateType;
	// public HashMap inputMap= new HashMap();

	// private static final Log LOGGER =
	// LogFactory.getLog(JavaClassTemplateGenerator.class);

	public CreateVoClass createVoClass = new CreateVoClass();

	// create the template engine

	public VelocityEngine velocityEngine = new VelocityEngine();

	public InputStream voClassInputStream;

	public String path = null;

	public String fileName = null;

	public static final String TEMPLATE_FOLDER = "template/";

	public static final String TEMPLATE_NAME = "VO_EQS.vm";

	public void generateVoClass(HashMap inputMap, IProject project,
			String inputFileName, boolean isVoexist) {

		String[] fileNameArr = inputFileName.split("/");
		for (int i = 0; i < fileNameArr.length; i++) {
			if (i == 0)
				path = fileNameArr[i];
			else if (i < fileNameArr.length - 1)
				path = path + "/" + fileNameArr[i];
			else
				fileName = fileNameArr[i];
		}
		try {

			// get the VO Class content
			voClassInputStream = generateVoClassSource(inputMap, TEMPLATE_NAME);

			// create Vo Class
			createVoClass.createVO(project, path, fileName, voClassInputStream,
					isVoexist, "VO");
			openEditor(createVoClass.getFile());

		} catch (Exception e) {
			// error

		}

	}

	public void openEditor(IFile file) {
		// Open editor on new file.
		String editorId = "org.eclipse.jdt.ui.CompilationUnitEditor";
		IWorkbenchWindow dw = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if (page != null) {
					IEditorPart editorPart = page.openEditor(
							new FileEditorInput(file), editorId, true);
				}

			}
		} catch (PartInitException exception) {
			return;
		}

	}

	public InputStream generateVoClassSource(HashMap inputMap,
			String templateName) {

		Set keySet = inputMap.keySet();
		StringWriter writer = new StringWriter();
		try {

			// set the custom template loader

			Properties properties = new Properties();
			properties.setProperty("resource.loader", "class");
			properties
					.setProperty("class.resource.loader.class",
							"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			properties.setProperty("runtime.log.logsystem.class",
					"org.apache.velocity.runtime.log.NullLogSystem");
			// now initialize the engine
			velocityEngine.init(properties);

			Template javaClassTemplate = null;
			try {
				// Choose a template
				javaClassTemplate = velocityEngine.getTemplate(TEMPLATE_FOLDER
						+ templateName);
			} catch (ResourceNotFoundException rnfe) {
				// couldn't find the template

				// LOGGER.error("couldn't find the template", rnfe);
				PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
						"couldn't find the template", rnfe);
			} catch (ParseErrorException pee) {
				// syntax error: problem parsing the
				// template
				// LOGGER.error("syntax error: problem parsing the template",
				// pee);
				PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
						"syntax error: problem parsing the template", pee);
			} catch (MethodInvocationException mie) {
				// something invoked in the template
				// threw an exception

				// LOGGER
				// .error(
				// "something invoked in the template threw an exception",
				// mie);
				PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
						"something invoked in the template threw an exception",
						mie);
			} catch (Exception exception) {

				PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
						"Some error occured", exception);
			}

			// Create a Context object
			VelocityContext context = new VelocityContext();

			Iterator iter = keySet.iterator();
			String key = null;
			String value = null;
			Vector v_value = null;

			// Add your data objects to the Context.
			while (iter.hasNext()) {

				key = (String) iter.next();
				if (key.equals("tableColumn")) {

					v_value = (Vector) inputMap.get(key);
					context.put(key, v_value);
				} else {

					value = (String) inputMap.get(key);
					context.put(key, value);
				}
			}

			// Merge the template and your data to
			// produce the ouput.
			javaClassTemplate.merge(context, writer);

		} catch (Exception exception) {
			// Fail to transform template

			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID,
					"Fail to transform template", exception);
		}
		return new ByteArrayInputStream(writer.toString().getBytes());
	}

}
