package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.BalancerPB;

public class LoginRequest extends RPCRequest {
	public static final String RPCTYPE = "login";
	
	private String userId;
	private Integer terminal;
	private String service;
	
	public LoginRequest() {
		super(RPCTYPE);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(LoginRequest.class.getSimpleName());
		builder.append(": ").append(userId).append(": ").append(terminal).append(": ").append(service);
		return builder.toString();
	}

	@Override
	public boolean canRetry() {
		return false;
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
						com.pbdata.generated.balancer.BalancerPB.LoginRequest request = BalancerPB.LoginRequest.parseFrom(bytes);
						if(request.hasField(com.pbdata.generated.balancer.BalancerPB.LoginRequest.getDescriptor().findFieldByName("terminal")))
							terminal = request.getTerminal();
						if(request.hasField(com.pbdata.generated.balancer.BalancerPB.LoginRequest.getDescriptor().findFieldByName("userId")))
							userId = request.getUserId();
						if (request.hasField(com.pbdata.generated.balancer.BalancerPB.LoginRequest.getDescriptor().findFieldByName("service")))
							service = request.getService();
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
			BalancerPB.LoginRequest.Builder builder = BalancerPB.LoginRequest.newBuilder();
			if(userId != null)
				builder.setUserId(userId);
			if(terminal != null)
				builder.setTerminal(terminal);
			if (service != null)
				builder.setService(service);
			com.pbdata.generated.balancer.BalancerPB.LoginRequest loginRequest = builder.build();
			byte[] bytes = loginRequest.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			setType(RPCTYPE);		
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getTerminal() {
		return terminal;
	}

	public void setTerminal(Integer terminal) {
		this.terminal = terminal;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}
}
