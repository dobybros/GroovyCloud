package com.dobybros.chat.storage.adapters;

import chat.errors.CoreException;
import com.dobybros.chat.data.OfflineMessage;

import java.util.Collection;
import java.util.List;

/**
 * 存储离线消息
 * 
 * 使用Lan里的MongoDB数据库
 *
 * 离线消息是就近存储原则， 不会出现跨区获取离线消息。
 * 
 * @author aplombchen
 *
 */
public interface OfflineMessageAdapter extends StorageAdapter {


	public void saveOfflineMessage(OfflineMessage message) throws CoreException;

	public long removeOfflineMessages(String userId, Collection<String> offlineMessageIds)
			throws CoreException;

	/**
	 * 读取离线消息, 按消息时间的升序返回
	 * 
	 * @param userId 谁的
	 * @param offset 偏移
	 * @param limit 返回限制
	 * @return 返回受limit影响的返回总数。 
	 * @throws CoreException
	 */
	public List<OfflineMessage> readOfflineMessages(String userId, String service, Integer offset, Integer limit) throws CoreException;
	
	/**
	 * 清理过期消息
	 * 
	 * @param overdueTime
	 * @return
	 * @throws CoreException
	 */
	public long removeOverdueOfflineMessages(Long overdueTime) throws CoreException;
	
	/**
	 * 清理没有消息接收者的消息(这个方法没有用到，sdocker里面也没有写)
	 * 
	 * @return
	 * @throws CoreException
	 */
	//long removeEmptyOfflineMessages() throws CoreException;
}
