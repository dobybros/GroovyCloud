package com.docker.utils;
public interface HttpConstants {
	/**
	 * userId
	 */
	public static final String PARAMETER_TERMINAL = "terminal";
	public static final String PARAMETER_ACCOUNTID = "account";
	public static final String PARAMETER_SESSIONID = "sid";
	public static final String PARAMETER_SERVICE = "service";
	public static final String PARAMETER_SERVER = "s";
	public static final String PARAMETER_COOKIE = "cookie";
	
	public static final String ATTRIBUTE_ONLINEUSER = "onlineuser";
	public static final String ATTRIBUTE_TERMINAL = "terminal";
	public static final String ATTRIBUTE_INTERNAL_USERID = "uid";

	public static final String SESSION_USER = "user";
	public static final String SESSION_LOCALE = "locale";
	public static final String SESSION_SUBDOMAIN = "subdomain";


	public static final String HEADER_CHANNELID = "cid";
	public static final String HEADER_SETCHANNELID = "set-cid";
	
	public static final int TIMEOUT = 30 * 60;
//	public static final int TIMEOUT = 10;
	
	public static final String EXTREMELONGTIME = "Mon,12 May 8021 00:20:00 GMT";
}
