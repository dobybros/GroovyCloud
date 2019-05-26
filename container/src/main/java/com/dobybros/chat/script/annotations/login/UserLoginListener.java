package com.dobybros.chat.script.annotations.login;

import com.dobybros.chat.data.userinfo.UserInfo;

public interface UserLoginListener {
	public Integer loginIfNotCreated(String userId, String service, Integer terminal, String scope);
	public Integer userRoaming(String account, String service, Integer terminal, String lanId, UserInfo userInfo);
}
