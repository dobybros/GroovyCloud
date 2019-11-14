package com.dobybros.chat.data;


public class MessageResult {
	public static final int CODE_SUCCESS = 1;
	
	private int code;
	private String description;
	/**
	 * 这个Result对应的消息ID。
	 */
	private String mid;
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

}
