package com.dobybros.chat.storage.adapters;

import chat.errors.CoreException;
import com.dobybros.chat.data.serverstatus.ServerStatus;

/**
 * @author acucom
 *
 */
public interface ServerStatusAdapter extends StorageAdapter {

	public static final String SERVICE = "imserverstatus";

	/**
	 * 通过服务器名称过去服务器对象
	 *
	 *  @param server 服务器名称
	 * @return 返回服务器对象
	 */
	public ServerStatus getServerStatusByServer(String server) throws CoreException;

}
