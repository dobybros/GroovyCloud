package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.BalancerPB;

public class LogoutRequest extends RPCRequest {
	public static final String RPCTYPE = "logout";
	
	private String userId;
	
	public LogoutRequest() {
		super(RPCTYPE);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(LogoutRequest.class.getSimpleName());
		builder.append(": ").append(userId);
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
						com.pbdata.generated.balancer.BalancerPB.LogoutRequest request = BalancerPB.LogoutRequest.parseFrom(bytes);
						if(request.hasField(com.pbdata.generated.balancer.BalancerPB.LogoutRequest.getDescriptor().findFieldByName("userId")))
							userId = request.getUserId();
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
			BalancerPB.LogoutRequest.Builder builder = BalancerPB.LogoutRequest.newBuilder();
			if(userId != null)
				builder.setUserId(userId);
			com.pbdata.generated.balancer.BalancerPB.LogoutRequest logoutRequest = builder.build();
			byte[] bytes = logoutRequest.toByteArray();
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
}
