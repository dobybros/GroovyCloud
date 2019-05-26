package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.ChatPB;

public class IsOnlineRequest extends RPCRequest {
	public static final String RPCTYPE = "isol";
	
	private String userId;
	private Boolean intendOnline;
	private String service;

	public String toString() {
		StringBuilder builder = new StringBuilder(IsOnlineRequest.class.getSimpleName());
		builder.append(": ").append(userId).append(": ").append(service).append(": ").append(intendOnline);
		return builder.toString();
	}

	public IsOnlineRequest() {
		super(RPCTYPE);
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
						ChatPB.IsOnlineRequest request = ChatPB.IsOnlineRequest.parseFrom(bytes);
						if(request.hasField(ChatPB.IsOnlineRequest.getDescriptor().findFieldByName("userId")))
							userId = request.getUserId();
						if(request.hasField(ChatPB.IsOnlineRequest.getDescriptor().findFieldByName("intendOnline")))
							intendOnline = request.getIntendOnline();
						if(request.hasField(ChatPB.IsOnlineRequest.getDescriptor().findFieldByName("service")))
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
			ChatPB.IsOnlineRequest.Builder builder = ChatPB.IsOnlineRequest.newBuilder();
			if(userId != null)
				builder.setUserId(userId);
			if(intendOnline != null)
				builder.setIntendOnline(intendOnline);
			if(service != null)
				builder.setService(service);
			ChatPB.IsOnlineRequest loginRequest = builder.build();
			byte[] bytes = loginRequest.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			setType(RPCTYPE);		
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public Boolean getIntendOnline() {
		return intendOnline;
	}

	public void setIntendOnline(Boolean intendOnline) {
		this.intendOnline = intendOnline;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
