package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.BalancerPB;

public class LoginResponse extends RPCResponse {
	private String ip;
	private Integer port;
	private String domain;
	
	public LoginResponse() {
		super(LoginRequest.RPCTYPE);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(LoginResponse.class.getSimpleName());
		builder.append(": ").append(ip).append(": ").append(port).append(": ").append(domain);
		return builder.toString();
	}

	@Override
	public void resurrect() throws CoreException {
		byte[] bytes = getData();
		Byte encode = getEncode();
		if(bytes != null) {
			if(encode != null) {
				switch(encode) {
				case ENCODE_PB:
					try {
						com.pbdata.generated.balancer.BalancerPB.LoginResponse request = BalancerPB.LoginResponse.parseFrom(bytes);
						ip = request.getIp();
						port = request.getPort();
						domain = request.getDomain();
					} catch (InvalidProtocolBufferException e) {
						e.printStackTrace();
						throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODE_PB_PARSE_FAILED, "PB parse data failed, " + e.getMessage());
					}
					break;
					default:
						throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for resurrect");
				}
			}
		}
	}

	@Override
	public void persistent() throws CoreException {
		Byte encode = getEncode();
		if(encode == null)
			throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent");
		switch(encode) {
		case ENCODE_PB:
			BalancerPB.LoginResponse.Builder builder = BalancerPB.LoginResponse.newBuilder();
			if(ip != null)
				builder.setIp(ip);
			if(port != null)
				builder.setPort(port);
			if(domain != null)
				builder.setDomain(domain);
			com.pbdata.generated.balancer.BalancerPB.LoginResponse loginResponse = builder.build();
			byte[] bytes = loginResponse.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			setType(LoginRequest.RPCTYPE);		
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
