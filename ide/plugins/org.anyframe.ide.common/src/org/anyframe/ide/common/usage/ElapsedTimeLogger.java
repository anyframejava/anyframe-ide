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
import java.util.HashMap;
import java.util.Map;

import org.anyframe.ide.common.util.PluginLogger;
import org.anyframe.ide.common.util.VersionUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.eclipse.ui.PlatformUI;

/**
 * This is ElapsedTimeLogger class.
 * 
 * @author JDI
 */
public class ElapsedTimeLogger {
	
	private static final String ELAPSED_TIME_BASE_DIRECTORY = "elapsedTimelogs";
	
	private static final ElapsedTimeLogger INSTANCE = new ElapsedTimeLogger();
	private String baseDir;
	
	private Map<String, ElapsedTimeInfo> watchBox;

	private ElapsedTimeLogger() {
		baseDir = LoggerCommon.getBaseDir(ELAPSED_TIME_BASE_DIRECTORY);
		watchBox = new HashMap<String, ElapsedTimeInfo>();
	}

	private static ElapsedTimeLogger getInstance() {
		return INSTANCE;
	}

	public static void start(){
		try{
			getInstance().startWatch();
		}catch(Exception e){
			//무시
			e.printStackTrace();
		}
	}
	
	public static void stop(){
		try{
			getInstance().stopWatch();
		}catch(Exception e){
			//무시
			e.printStackTrace();
		}
	}
	
	private void startWatch(){
		
		String pluginId = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPluginId();
		String title = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getTitle();
		
		ElapsedTimeInfo info = new ElapsedTimeInfo();
		
		info.setPluginId(pluginId);
		info.setSouceId(title);
		info.setStartTime(System.currentTimeMillis());
		
		if(watchBox.get(pluginId + "-" + title)==null){
			watchBox.put(pluginId + "-" + title, info);
		}
	}
	
	private void stopWatch(){
		
		String pluginId = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getPluginId();
		String title = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getTitle();
		
		ElapsedTimeInfo info = watchBox.get(pluginId + "-" + title);
		if(info==null){
			return; // 뭔가 잘못되어서 시작시간이 없다면 그냥 무시하자
		}
		long startTime = info.getStartTime();
		info.setElapsedTime(System.currentTimeMillis() - startTime);
		
		write(info);
		
		watchBox.remove(pluginId + "-" + title);
	}
	
	private void write(ElapsedTimeInfo info) {
		
		try {
			String filePath = baseDir + LoggerCommon.getCurrentFileName(info.getPluginId(), baseDir);
			File file = FileUtils.getFile(filePath);
			FileUtils.writeStringToFile(file, createLog(info) + "\n", true);
		} catch (Exception ex) {
			// Exception 발생하면 그냥 무시.
			PluginLogger.error(ex);
		}
		
	}
	
	private String createLog(ElapsedTimeInfo info){
		/*
		플러그인 아이디
		소스 아이디(Editor title, 보통 파일 이름)
		경과시간
		현재시간
		사용자
		이클립스 버전
		*/

		StringBuilder sb = new StringBuilder();
		sb.append(info.getPluginId());
		sb.append(LoggerCommon.LOG_SEPARATOR);
		sb.append(info.getSouceId());
		sb.append(LoggerCommon.LOG_SEPARATOR);
		sb.append(info.getElapsedTime());
		sb.append(LoggerCommon.LOG_SEPARATOR);
		sb.append(DateFormatUtils.format(Calendar.getInstance().getTime(), "yyyyMMddHHmmssSS"));
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
