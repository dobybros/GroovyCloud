package com.dobybros.chat.storage.adapters;

import chat.errors.CoreException;
import com.dobybros.chat.data.userinpresence.UserInPresence;

import java.util.Collection;


/**
 * 用户在PresenceServer上的分布情况 
 * 
 * 使用跨Lan的全局Redis数据库
 * 
 * 如果从不同Lan访问全局某个Lan下面的Redis数据库， 也通过Remote的方式访问， 而不是直连数据库。 
 * 
 * @author aplombchen
 *
 */
public interface UserInPresenceAdapter extends StorageAdapter {
	public static final String SERVICE ="imuserinpresence";
	/**
	 * 根据用户Id获取lanServer
	 * 
	 * @param userId
	 * @return
	 * @throws CoreException
	 */
	public UserInPresence getLanServer(String userId) throws CoreException;
	
	/**
	 * 如果server不存在就保存。 
	 * 
	 * @param userId
	 * @return 返回null， 表示server存储成功， 返回非null， 表示server已经存在并返回。
	 * @throws CoreException
	 */
	public UserInPresence saveIfAbsent(String userId, UserInPresence userInPresence) throws CoreException;

	/**
	 * 保存。
	 *
	 * @param userId
	 * @return 返回null， 表示server存储成功， 返回非null， 表示server已经存在并返回。
	 * @throws CoreException
	 */
	public Long save(String userId, UserInPresence userInPresence) throws CoreException;

	/**
	 * 删除某个用户的lanServer
	 * 
	 * @param userId
	 * @throws CoreException
	 */
	public void delete(String userId) throws CoreException;

	public void deleteMany(Collection<String> userIds) throws CoreException;
}
