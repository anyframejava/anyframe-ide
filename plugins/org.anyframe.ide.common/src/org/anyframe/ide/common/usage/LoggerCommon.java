package org.anyframe.ide.common.usage;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.anyframe.ide.common.CommonActivator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SizeFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;

public class LoggerCommon {
	
	private static final String FILE_SEP = System.getProperty("file.separator");
	public static final String LOG_SEPARATOR = ",";
	
	private static long FILE_SIZE = 1024 * 500; //500KB
	
	public static String getBaseDir(String sub){
		IPath stateLocation = CommonActivator.getDefault().getStateLocation();
		return stateLocation + FILE_SEP + sub + FILE_SEP;
	}
	
	public static String getCurrentFileName(String pluginId, String baseDir) throws IOException{
		IOFileFilter prefixFilter = new PrefixFileFilter(pluginId);
		IOFileFilter sizeFilter = new SizeFileFilter(FILE_SIZE, false);
		IOFileFilter fileFilter = FileFilterUtils.and(new IOFileFilter[] { prefixFilter, sizeFilter });
		File dir = FileUtils.getFile(baseDir);
		if(dir.exists()==false){
			FileUtils.forceMkdir(dir);
		}
		Collection<File> files = FileUtils.listFiles(dir, fileFilter, null);
		
		if(files.isEmpty()){
			return getNewFileName(pluginId, baseDir);
		}
		return files.iterator().next().getName();
	}
	
	private static String getNewFileName(final String pluginId, String baseDir){
		Collection<File> files = FileUtils.listFiles(FileUtils.getFile(baseDir), new PrefixFileFilter(pluginId), TrueFileFilter.INSTANCE);
		
		Iterator<File> iter = files.iterator();
		List<Integer> fileIndexes = new ArrayList<Integer>();
		
		String hostAddress = "";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		while(iter.hasNext()){
			File file = iter.next();
			String num = file.getName().replaceAll(pluginId, "").replaceAll(hostAddress, "").replaceAll("-", "").replaceAll(".log", "");
			
			fileIndexes.add(StringUtils.isEmpty(num) ? 0 : Integer.parseInt(num));
		}
		
		if(fileIndexes.isEmpty()){
			return pluginId + "-" + hostAddress + "-" + 1 + ".log";
		}
		
		Integer max = Collections.max(fileIndexes, null);
		
		return pluginId + "-" + hostAddress + "-" + (max+1) + ".log";
		
	}

}
