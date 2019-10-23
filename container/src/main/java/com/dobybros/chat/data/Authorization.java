package com.dobybros.chat.data;

import com.docker.data.DataObject;

public class Authorization extends DataObject {

	private String userId;

	private String authorizationCode;

	private Long authorizationExpireTime;

	private String service;

	private String redirectUri;

	private Long time;

	private Integer terminal;

	/**
	 * Whether the authorizationCode has been used.
	 */
	private Boolean authorized;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAuthorizationCode() {
		return authorizationCode;
	}

	public void setAuthorizationCode(String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}

	public Long getAuthorizationExpireTime() {
		return authorizationExpireTime;
	}

	public void setAuthorizationExpireTime(Long authorizationExpireTime) {
		this.authorizationExpireTime = authorizationExpireTime;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Integer getTerminal() {
		return terminal;
	}

	public void setTerminal(Integer terminal) {
		this.terminal = terminal;
	}

	public Boolean getAuthorized() {
		return authorized;
	}

	public void setAuthorized(Boolean authorized) {
		this.authorized = authorized;
	}
}