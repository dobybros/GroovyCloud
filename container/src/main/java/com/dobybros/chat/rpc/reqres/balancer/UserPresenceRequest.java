package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.ChatPB;

public class UserPresenceRequest extends RPCRequest {
	public static final String RPCTYPE = "presence";
	
	private String userId;
	/**
	 * 查在线状态时， 这个service是指的消息的service， 而不是includeServices和excludeServices
	 */
	private String service;

	private String fromLanId;

	public static final int COMMAND_NONE = 0;
	public static final int COMMAND_LOGOUT_IFNOTONLINE = 10;
	private Integer command;

	public UserPresenceRequest() {
		super(RPCTYPE);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(UserPresenceRequest.class.getSimpleName());
		builder.append(": ").append(userId).append(": ").append(service);
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
						ChatPB.UserPresenceRequest request = ChatPB.UserPresenceRequest.parseFrom(bytes);
						if(request.hasField(com.pbdata.generated.balancer.ChatPB.UserPresenceRequest.getDescriptor().findFieldByName("userId")))
							userId = request.getUserId();
						if(request.hasField(com.pbdata.generated.balancer.ChatPB.UserPresenceRequest.getDescriptor().findFieldByName("service")))
							service = request.getService();
						if(request.hasField(com.pbdata.generated.balancer.ChatPB.UserPresenceRequest.getDescriptor().findFieldByName("fromLanId")))
							fromLanId = request.getFromLanId();
						if(request.hasField(com.pbdata.generated.balancer.ChatPB.UserPresenceRequest.getDescriptor().findFieldByName("command")))
							command = request.getCommand();
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
			ChatPB.UserPresenceRequest.Builder builder = ChatPB.UserPresenceRequest.newBuilder();
			if(userId != null) 
				builder.setUserId(userId);
			if(service != null)
				builder.setService(service);
			if(fromLanId != null)
				builder.setFromLanId(fromLanId);
			if(command != null)
				builder.setCommand(command);
			ChatPB.UserPresenceRequest request = builder.build();
			byte[] bytes = request.toByteArray();
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

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getFromLanId() {
		return fromLanId;
	}

	public void setFromLanId(String fromLanId) {
		this.fromLanId = fromLanId;
	}

	public Integer getCommand() {
		return command;
	}

	public void setCommand(Integer command) {
		this.command = command;
	}
}
