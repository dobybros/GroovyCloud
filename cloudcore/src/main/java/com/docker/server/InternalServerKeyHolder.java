package com.docker.server;


public class InternalServerKeyHolder {
	public static final String KEY_HEADER = "key";
	
	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
