package com.dobybros.chat.services;

import com.dobybros.chat.data.Authorization;

public interface AuthorizationService {

	/**
	 * 从登陆服务器保存一个Authorization对象
	 *
	 * @param session
	 */
	public void saveAuthorization(Authorization session);


	/**
	 * 通过authorizationCode验证并消费Authorization对象。
	 *
	 * @param userId
	 * @param authorizationCode
	 * @return
	 */
	public Authorization consumeAuthorizationByCode(String userId,
                                                    String authorizationCode);

}
