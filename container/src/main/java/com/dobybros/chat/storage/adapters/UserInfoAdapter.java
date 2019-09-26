package com.dobybros.chat.storage.adapters;

import chat.errors.CoreException;
import com.dobybros.chat.data.userinfo.ServerInfo;
import com.dobybros.chat.data.userinfo.UserInfo;

import java.util.List;


/**
 * userInfo的管理
 * <p>
 * 使用Lan里的MongoDB数据库
 *
 * @author aplombchen
 */
public interface UserInfoAdapter extends StorageAdapter {
    public static final String SERVICE ="imuserinfo";

    /**
     * 通过userId和service删除一个UserInfo
     *
     * @param userId
     * @param service
     * @return
     * @throws CoreException
     */
    public long deleteUserInfo(String userId, String service) throws CoreException;

    /**
     * 删除一个device信息
     *
     * @param userId
     * @param service
     * @param terminal
     * @return
     * @throws CoreException
     */
    public Long deleteDevice(String userId, String service, Integer terminal) throws CoreException;

    /**
     * 删除多个device信息
     *
     * @param userId
     * @param service
     * @param terminals
     * @throws CoreException
     */
    public void deleteDevices(String userId, String service, List<Integer> terminals) throws CoreException;

    /**
     * service-userId只会对应一个UserInfo， 新的会覆盖旧的。
     * 例如time变量， 如果参数对象的time为空， 那么该方法应该生成time， 否则使用已有的time。
     *
     * @throws CoreException
     */
    public void updateUserInfo(UserInfo userInfo) throws CoreException;

    /**
     * service-userId只会对应一个UserInfo， 如果已经存在， 就抛出错误。
     * 例如time变量， 如果参数对象的time为空， 那么该方法应该生成time， 否则使用已有的time。
     *
     * @throws CoreException
     */
    public void addUserInfo(UserInfo userInfo) throws CoreException;

    public void addUpdUserInfo(String userId, String service, UserInfo userInfo) throws CoreException;

    /**
     * 获取UserInfo
     *
     * @param userId
     * @param service
     * @return
     * @throws CoreException
     */
    public UserInfo getUserInfo(String userId, String service) throws CoreException;

    /**
     * 增加未读数
     *
     * @param userId
     * @param service
     * @param inc
     * @return
     * @throws CoreException
     */
    public Long increaseUnreadCount(String userId, String service, long inc) throws CoreException;

    public Long setServerInfoNX(String userId, String service, ServerInfo serverInfo) throws CoreException;

    public ServerInfo getServerInfo(String userId, String service) throws CoreException;

    public void deleteServerInfo(String userId, String service) throws CoreException;
}
