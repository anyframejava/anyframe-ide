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
package org.anyframe.ide.common.usage;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.anyframe.ide.common.util.PluginLogger;
import org.anyframe.ide.common.util.VersionUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.eclipse.ui.PlatformUI;

/**
 * This is UsageLogger class.
 * 
 * @author Dongin Jung
 */
public class UsageLogger {
	
	
	private static final String USAGELOGS_BASE_DIRECTORY = "usagelogs";
	private String baseDir;
	
//	private static final UsageLogger INSTANCE = new UsageLogger();
	

	public UsageLogger() {
		baseDir = LoggerCommon.getBaseDir(USAGELOGS_BASE_DIRECTORY);
	}

//	public static UsageLogger getInstance() {
//		return INSTANCE;
//	}

	public void write(String eventSourceId) {
		
		String pluginId = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPluginId();
		
		try {
			String filePath = baseDir + LoggerCommon.getCurrentFileName(pluginId, baseDir);
			
			String msg = createLog(pluginId, eventSourceId);
			File file = FileUtils.getFile(filePath);
			FileUtils.writeStringToFile(file, msg + "\n", true);
		} catch (Exception ex) {
			// Exception 발생하면 그냥 무시.
			PluginLogger.error(ex);
		}
	}
	
	private String createLog(String pluginId, String eventSourceId){
		/*
		플러그인 아이디
		이벤트소스 아이디(버튼 아이디)
		현재시간
		사용자
		이클립스 버전
		*/

		StringBuilder sb = new StringBuilder();
		sb.append("[USAGE]");
		sb.append(pluginId);
		sb.append(LoggerCommon.LOG_SEPARATOR);
		sb.append(eventSourceId);
		sb.append(LoggerCommon.LOG_SEPARATOR);
		
		sb.append(DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMddHHmmss"));
		sb.append(LoggerCommon.LOG_SEPARATOR);
		
		String hostAddress = "";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		sb.append(hostAddress);
		sb.append(LoggerCommon.LOG_SEPARATOR);
		sb.append(VersionUtil.getProductVersion());
		
		return sb.toString();
	}

}
