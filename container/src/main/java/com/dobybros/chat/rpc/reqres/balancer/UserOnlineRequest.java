package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.ChatPB;

public class UserOnlineRequest extends RPCRequest {
	public static final String RPCTYPE = "uol";

	private String userId;
	/**
	 * 查在线状态时， 这个service是指的消息的service， 而不是includeServices和excludeServices
	 */
	private String service;

	private GatewayServer gateway;


	public UserOnlineRequest() {
		super(RPCTYPE);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(UserOnlineRequest.class.getSimpleName());
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
						ChatPB.UserOnlineRequest request = ChatPB.UserOnlineRequest.parseFrom(bytes);
						if(request.hasField(ChatPB.UserOnlineRequest.getDescriptor().findFieldByName("userId")))
							userId = request.getUserId();
						if(request.hasField(ChatPB.UserOnlineRequest.getDescriptor().findFieldByName("service")))
							service = request.getService();
                        ChatPB.GatewayServer gatewayServer = request.getGateway();
                        gateway = GatewayServer.fromPB(gatewayServer);
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
			ChatPB.UserOnlineRequest.Builder builder = ChatPB.UserOnlineRequest.newBuilder();
			if(userId != null) 
				builder.setUserId(userId);
			if(service != null)
				builder.setService(service);
            builder.setGateway(GatewayServer.toPB(gateway));
			ChatPB.UserOnlineRequest request = builder.build();
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

    public GatewayServer getGateway() {
        return gateway;
    }

    public void setGateway(GatewayServer gateway) {
        this.gateway = gateway;
    }
}
