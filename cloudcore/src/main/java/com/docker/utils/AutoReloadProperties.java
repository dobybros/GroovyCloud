package com.docker.utils;

import chat.utils.ReloadHandler;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class AutoReloadProperties extends Properties{
	public interface PropertiesReloadListener {
		public void reloaded();
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -5599710163250746638L;
	protected static final String TAG = AutoReloadProperties.class.getSimpleName();
	private Properties properties;
	private String path;
	private String absolutePath;
	private Long lastModified;
	private ReloadHandler reloadHandler;
	private PropertiesReloadListener reloadListener;
	public AutoReloadProperties() {
		reloadHandler = new ReloadHandler() {
			@Override
			public void load() throws Throwable {
				File file = null;
				if(path != null) {
					ClassPathResource resource = new ClassPathResource(path);
					file = resource.getFile();
				} else if(absolutePath != null){
					file = new File(absolutePath);
				}
				long modified = file.lastModified();
				if(lastModified == null || lastModified != modified) {
					lastModified = modified; 
					Properties properties = new Properties();
					InputStream is = FileUtils.openInputStream(file);
					try {
						properties.load(new InputStreamReader(is, "UTF-8"));
					} finally {
						is.close();
					}
					AutoReloadProperties.this.properties = properties;
					onLoad();
					if(reloadListener != null) {
						reloadListener.reloaded();
					}
				}
			}
		};
	}
	
	protected void onLoad(){
	};
	
	public void init() throws IOException {
		reloadHandler.init();
	};
	
	@Override
	public String getProperty(String key) {
		if(properties == null)
			return null;
		return properties.getProperty(key);
	}
	
	public String getProperty(String key, String defaultValue) {
		if(properties == null)
			return null;
		return properties.getProperty(key, defaultValue);
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setReloadHandler(ReloadHandler reloadHandler) {
		this.reloadHandler = reloadHandler;
	}

	public Long getLastModified() {
		return lastModified;
	}

	public PropertiesReloadListener getReloadListener() {
		return reloadListener;
	}

	public void setReloadListener(PropertiesReloadListener reloadListener) {
		this.reloadListener = reloadListener;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
}
