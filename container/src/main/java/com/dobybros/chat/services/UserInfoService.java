package com.dobybros.chat.services;

import com.dobybros.chat.data.userinfo.UserInfo;

public interface UserInfoService {

	/**
	 * 通过userId和service删除一个UserInfo
	 */
	public long deleteUserInfo(String userId, String service);

	/**
	 * 通过userId匹配到的所有UserInfo
	 */
	public long deleteUserInfos(String userId);

	/**
	 * service-userId只会对应一个UserInfo， 新的会覆盖旧的。
	 * 例如time变量， 如果参数对象的time为空， 那么该方法应该生成time， 否则使用已有的time。
	 */
	public void updateUserInfo(UserInfo userInfo);

	/**
	 * service-userId只会对应一个UserInfo， 如果已经存在， 就抛出错误。
	 * 例如time变量， 如果参数对象的time为空， 那么该方法应该生成time， 否则使用已有的time。
	 */
	public void addUserInfo(UserInfo userInfo);

	/**
	 * 为多个服务添加多个UserInfo。
	 */
	public void addUpdUserInfos(String userId, String service, UserInfo userInfo);

	/**
	 * 获取UserInfo
	 */
	public UserInfo getUserInfo(String userId, String service);

}
