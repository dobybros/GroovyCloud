package com.dobybros.chat.storage.adapters;

import chat.errors.CoreException;

import java.util.Map;

public interface ApnPushAdapter extends StorageAdapter {

	void sendMessage(String service, String deviceToken, String msg, Integer badgeNumber,
                     String soundFileName) throws CoreException;

	public void sendMessage(String service, String deviceToken, String msg,
                            Integer badgeNumber, String soundFileName,
                            Map<String, String> customPropertyMap) throws CoreException;
}
