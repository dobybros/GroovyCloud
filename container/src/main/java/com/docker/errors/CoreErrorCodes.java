package com.docker.errors;



/**
 * code -39999~-30000
 */
public interface CoreErrorCodes {


	public static final int CODE_CORE = -30000;//0~999

	public static final int ERROR_RPC_ILLEGAL = CODE_CORE + 1;

	public static final int ERROR_SCRIPT_UNZIP_FAILED = CODE_CORE + 2;
	public static final int ERROR_LANID_ILLEGAL = CODE_CORE + 3;
	public static final int ERROR_REDIS = CODE_CORE + 4;
	public static final int ERROR_KAFKA_CONFCENT_ILLEGAL = CODE_CORE + 5;
	public static final int ERROR_RPC_TYPE_NOSERVERADAPTER = CODE_CORE +6;
	public static final int ERROR_RPC_TYPE_NOMAPPING = CODE_CORE + 7;
	public static final int ERROR_LOADPROPERTIES_FAILED = CODE_CORE + 8;
	public static final int ERROR_GROOVYCLOUDCONFIG_ILLEGAL = CODE_CORE + 9;

	public static final int ERROR_LOCK = -35000;
	public static final int ERROR_LOCK_CAN_NOT_FOUND = ERROR_LOCK + 1;
	public static final int ERROR_LOCK_VERIFY_FIELD = ERROR_LOCK + 2;
	public static final int ERROR_ZOOKEEPER_CLIENT_NULL = ERROR_LOCK + 3;
	public static final int ERROR_ZOOKEEPER_EXECUTE_FAILED = ERROR_LOCK + 4;



}
