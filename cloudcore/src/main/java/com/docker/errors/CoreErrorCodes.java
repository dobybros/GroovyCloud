package com.docker.errors;



/**
 */
public interface CoreErrorCodes {


	public static final int CODE_CORE = 0;//0~999

	public static final int ERROR_RPC_ILLEGAL = CODE_CORE + 192;

	public static final int ERROR_SCRIPT_UNZIP_FAILED = CODE_CORE + 342;
	public static final int ERROR_LANID_ILLEGAL = CODE_CORE + 343;
	public static final int ERROR_REDIS = CODE_CORE + 344;
	public static final int ERROR_KAFKA_CONFCENT_ILLEGAL = CODE_CORE + 345;


	public static final int ERROR_LOCK = 8000;
	public static final int ERROR_LOCK_CAN_NOT_FOUND = ERROR_LOCK + 5;
	public static final int ERROR_LOCK_VERIFY_FIELD = ERROR_LOCK + 2;
}
