package com.docker.version;

import com.docker.utils.AutoReloadProperties;
import com.docker.utils.SpringContextUtil;

public class VersionControl {

	public interface DependencyHandler {
		public abstract void supported();
		public abstract void unSupported();
	}
	
	public static AutoReloadProperties version;
	/**
	 * 配置文件中，minVersion应为不支持(也就是旧版本)的版本的最后一版的版本号
	 * @param featureName
	 * @param currentVersion
	 * @param terminal
	 * @param dh
	 */
	public static void handleVersionDependency(String featureName, Integer currentVersion, String terminal, DependencyHandler dh) {
		if(version == null)
			version = (AutoReloadProperties) SpringContextUtil.getBean("version");
		
    	Integer minVersion = null;
    	if(terminal == null)
    		minVersion = Integer.MAX_VALUE;
    	else 
    		minVersion = Integer.parseInt(version.getProperty(terminal + "." + featureName));
    	if(dh == null)
    		return;
    	if(currentVersion == null)
    		currentVersion = 0;
		if(currentVersion > minVersion)
			dh.supported();
		else
			dh.unSupported();
    }	
}