package com.docker.storage.adapters;

import chat.errors.CoreException;
import com.docker.data.Lan;

import java.util.List;


/**
 * 管理服务器在线状态的接口
 * 
 * 使用Lan里的MongoDB数据库
 * 
 * @author aplombchen
 *
 */
public interface LansService {

	Lan getLan(String lanId) throws CoreException;

	List<Lan> getLans()
			throws CoreException;

}
