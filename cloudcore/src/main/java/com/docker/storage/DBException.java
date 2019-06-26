package com.docker.storage;
public class DBException extends Exception{
	private String debugReason;
	private int code;
	private int type;
	
	public static final int ERRORTYPE_UNKNOWN = 0;
	public static final int ERRORTYPE_DUPLICATEKEY = 1;
	public static final int ERRORTYPE_NETWORK = 2;
	public static final int ERRORTYPE_CURSORNOTFOUND = 3;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2866216484067863942L;

	public DBException(int type, int code, String message, String debugReason){
		super(message);
		this.debugReason = debugReason;
		this.code = code;
		this.type = type;
		
		System.out.println("[ERROR] " + this);
	}
	
	public DBException(int type, int code, String message){
		this(type, code, message, null);
	}
	
	public String getDebugReason(){
		return debugReason;
	}
	
	public String toString(){
		return type + "-" + code + ": " + getMessage() + "; " + debugReason;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}