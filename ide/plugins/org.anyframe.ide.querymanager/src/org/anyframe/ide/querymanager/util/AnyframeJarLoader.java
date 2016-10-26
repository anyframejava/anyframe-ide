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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.anyframe.ide.common.util.PluginLoggerUtil;
import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.messages.Message;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.packageadmin.RequiredBundle;

/**
 * This is AnyframeJarLoader class.
 * 
 * @author Surindhar.Kondoor
 * @author Raveendra G
 */
public class AnyframeJarLoader {
//	private static final Log LOGGER = LogFactory.getLog(AnyframeJarLoader.class);	
	public HashMap getClassNamesHashMap() throws IOException {
		String jarPath = null;
		Enumeration enumeration = null;
		try {
			
			BundleContext bundleContext = QueryManagerActivator.getDefault().getBundle().getBundleContext();
			ServiceReference ref = bundleContext.getServiceReference(PackageAdmin.class.getName());
			PackageAdmin admin = (PackageAdmin)bundleContext.getService(ref);
			//Bundle bundle[] = admin.getBundles("testopia-core", "1.0");
			RequiredBundle requiredBundle[] = admin.getRequiredBundles(null);
			for(int count=0;count<requiredBundle.length;count++){
				RequiredBundle requiredBundle1 =requiredBundle[count];
				
				if(requiredBundle1.getSymbolicName().equalsIgnoreCase("org.anyframe.common.eclipse.core")){
					Bundle bundle3 = requiredBundle1.getBundle();
					URL resourceURL = bundle3.getResource("lib/anyframe.core.query.ria-3.2.0.jar");
					BundleHost bundleHost = (BundleHost)bundle3;
					String bundleLocation = bundleHost.getBundleData().getLocation();
					jarPath = bundleLocation.substring(bundleLocation.indexOf("@")+1,bundleLocation.length()-1)
					+resourceURL.getPath().toString();
				}
			}
		} catch (Exception e) {
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID, Message.exception_loadjarfile, e);
		}
		//1.get jar file
		JarFile jarFile = null;
		try{
			jarFile = new JarFile(jarPath);
		}catch(Exception e){
			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID, Message.exception_loadjarfile, e);
		}
		//2.parse the jar file. construct HashMap. Return.
		HashMap classMethodsMap = new HashMap();
		if(jarFile != null){
		Enumeration enumaration = jarFile.entries();
		while (enumaration.hasMoreElements())
		{
			JarEntry je = (JarEntry) enumaration.nextElement();
			String name = je.getName();
			if(name != null && name.indexOf("anyframe/core/query") > -1 && name.indexOf("impl") < 0 && name.indexOf("$") < 0 && name.endsWith(".class")){
				String key1 = name.replaceAll("/", ".");
				key1 = key1.substring(0, key1.lastIndexOf("."));
				String key = name.substring(name.lastIndexOf("/")+1, name.lastIndexOf("."));
				try {
					String name1 = name;
					name1 = name1.replace('/', '.');
					name1 = name1.substring(0, name1.lastIndexOf("."));
					Class cls = Class.forName(name1,false,this.getClass()
							.getClassLoader());
					Method[] meths = cls.getDeclaredMethods();
					Collection methodNames = new ArrayList();
					for (int i = 0; i < meths.length; i++) {
						String value = meths[i].toString();
						int start = value.indexOf(key);
						value = value.substring(start, value.indexOf("(", start));
						value = value.substring(value.indexOf(".")+1);
						methodNames.add(value);
					}	
					classMethodsMap.put(key1, methodNames);
				} catch (ClassNotFoundException e) {
					PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID, 
							Message.exception_loadjarfile,
										e);
					
				}				
			}
		}
		}
		return classMethodsMap;
	}
	
	
	public HashMap getRuntimeProjectTechnicalServicesDetails(IProject project){
		//1. Find the runtime project
		HashMap classMethodsMap = new HashMap();
		IJavaProject javaProject = JavaCore.create(project);
		try {
			JarFile jarFile = null;
			IClasspathEntry[] dependencies = javaProject.getResolvedClasspath(true);
			URLClassLoader urlClassLoader = createURLClassLoader(dependencies);
			//urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL("file:///"+jarPath)});
			
			
			forLoop: for (int i = 0; i < dependencies.length; i++) {
                IClasspathEntry obj = dependencies[i];
                if (obj.getEntryKind() == IClasspathEntry.CPE_LIBRARY && obj.getContentKind() == 2) {
                	String jarPath = obj.getPath().toString();
                	if(jarPath.indexOf("anyframe.core.query.ria") > -1){
                		try{
                			jarFile = new JarFile(jarPath);
                			
                		}catch(Exception e){
                			PluginLoggerUtil.error(QueryManagerActivator.PLUGIN_ID, Message.exception_loadjarfile, e);
                			continue forLoop;
                		}                		
                	}
                	else
                		continue forLoop;;
//                
                }
                else
                	continue forLoop;
                if(jarFile != null){
            		Enumeration enumaration = jarFile.entries();
            		whileLoop: while (enumaration.hasMoreElements())
            		{
            			JarEntry je = (JarEntry) enumaration.nextElement();
            			String name = je.getName();
            			if(name != null && name.indexOf("anyframe/core/query/ria") > -1 && name.indexOf("impl") < 0 && name.indexOf("$") < 0 && name.endsWith(".class")){
            				String key1 = name.replaceAll("/", ".");
            				key1 = key1.substring(0, key1.lastIndexOf("."));
            				String key = name.substring(name.lastIndexOf("/")+1, name.lastIndexOf("."));
            				try {
            					String name1 = name;
            					name1 = name1.replace('/', '.');
            					name1 = name1.substring(0, name1.lastIndexOf("."));
            					
            					Class cls = Class.forName(name1,false,urlClassLoader);
            				
            					
            					Method[] meths = cls.getDeclaredMethods();
            					Collection methodNames = new ArrayList();
            					for (int j = 0; j < meths.length; j++) {
            						String value = meths[j].toString();
            						int start = value.indexOf(key);
            						value = value.substring(start, value.indexOf("(", start));
            						value = value.substring(value.indexOf(".")+1);
            						methodNames.add(value);
            					}	
            					classMethodsMap.put(key1, methodNames);
            				} catch (ClassNotFoundException e) {
//            					LOGGER.debug("There was a problem while Loading And Reading the Jar File. "+e.getMessage());
            					continue whileLoop;
            				} catch(NoClassDefFoundError noClass){
//            					LOGGER.error("There was a problem while Loading And Reading the Jar File. "+noClass.getMessage());
            					continue whileLoop;            					
            				}
            			}
            		}
            		}
               
            }			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//2. Read all the dependencies jar files
		//3. Check whether any of the jar file matches the required pattern.
		//4. If matches, parse through and find class name and method list, for each service and continue.
		return classMethodsMap;
	}


	private URLClassLoader createURLClassLoader(IClasspathEntry[] dependencies) {
		JarFile jarFile = null;
		Collection urlStrArray = new ArrayList();
		forLoop: for (int i = 0; i < dependencies.length; i++) {
            IClasspathEntry obj = dependencies[i];
            if (obj.getEntryKind() == IClasspathEntry.CPE_LIBRARY && obj.getContentKind() == 2) {
            	String jarPath = obj.getPath().toString();
            	urlStrArray.add(jarPath);
            }
            else
            	continue forLoop;
	
		}
		URL[] urlArr = new URL[urlStrArray.size()];
		Iterator itr = urlStrArray.iterator();
		int i = 0;
		while(itr.hasNext()){
			String str = itr.next().toString();
			try {
				urlArr[i++] = new URL("file:///"+str);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
		}
		//Object[] urlArray = urlStrArray.toArray();
		URLClassLoader urlClassLoader = URLClassLoader.newInstance(urlArr);
		return urlClassLoader;
	}
}