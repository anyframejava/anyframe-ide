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
package org.anyframe.ide.codegenerator.util;

import org.anyframe.ide.codegenerator.CodeGeneratorActivator;
import org.anyframe.ide.common.util.StringUtil;
import org.apache.maven.settings.Proxy;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * This is an ProxyUtil class.
 * 
 * @author Sujeong Lee
 */
public class ProxyUtil {

	public static Proxy getProxy() {
		Proxy proxy = new Proxy();
		IProxyService proxyService = getProxyService();
		IProxyData[] proxyDataForHost = proxyService.getProxyData();
		for (IProxyData data : proxyDataForHost) {
			if (IProxyData.HTTP_PROXY_TYPE.equals(data.getType()) || IProxyData.HTTPS_PROXY_TYPE.equals(data.getType())) {
				proxy.setHost(data.getHost());
				proxy.setPort(data.getPort());
				if(StringUtil.isEmptyOrNull(data.getUserId())){
					proxy.setId(null);
					proxy.setPassword(null);
				}else{
					proxy.setId(data.getUserId());
					proxy.setPassword(data.getPassword());
				}
				break;
			}
		}
		return proxy;
	}

	private static IProxyService getProxyService() {
		BundleContext bc = CodeGeneratorActivator.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = bc.getServiceReference(IProxyService.class.getName());
		IProxyService service = (IProxyService) bc.getService(serviceReference);
		return service;
	}
}
