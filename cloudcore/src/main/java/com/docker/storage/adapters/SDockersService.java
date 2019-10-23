package com.docker.storage.adapters;

import chat.errors.CoreException;
import org.bson.Document;


/**
 * 管理服务器在线状态的接口
 * 
 * 使用Lan里的MongoDB数据库
 * 
 * @author aplombchen
 *
 */
public interface SDockersService {

	Document getSDockerConf(String ip, Integer port) throws CoreException;

}
