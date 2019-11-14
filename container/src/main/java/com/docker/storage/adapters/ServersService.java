package com.docker.storage.adapters;

import chat.errors.CoreException;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;


/**
 * 管理服务器在线状态的接口
 * 
 * 使用Lan里的MongoDB数据库
 * 
 * @author aplombchen
 *
 */
public interface ServersService {

	Document getServerConfig(String serverType) throws CoreException;

	void deleteServerConfig(Bson bson) throws CoreException;

	void addServerConfig(Document document) throws CoreException;

	List<Document> getServerConfigs() throws CoreException;
}
