package com.docker.rpc;

public abstract class RPCBase extends BinaryCodec {
	private String type;
	
	public RPCBase(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
