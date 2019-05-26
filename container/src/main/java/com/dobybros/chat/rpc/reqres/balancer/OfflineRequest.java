package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.ChatPB;

public class OfflineRequest extends RPCRequest {
	public static final String RPCTYPE = "offline";
	
	private String userId;
	private Integer terminal;
	private Integer unread;
	private String service;
	
	public OfflineRequest() {
		super(RPCTYPE);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(OfflineRequest.class.getSimpleName());
		builder.append(": ").append(userId).append(": ").append(terminal).append(": ").append(unread);
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
						com.pbdata.generated.balancer.ChatPB.OfflineRequest request = ChatPB.OfflineRequest.parseFrom(bytes);
						if(request.hasField(com.pbdata.generated.balancer.ChatPB.OfflineRequest.getDescriptor().findFieldByName("userId")))
							userId = request.getUserId();
						if(request.hasField(com.pbdata.generated.balancer.ChatPB.OfflineRequest.getDescriptor().findFieldByName("terminal")))
							terminal = request.getTerminal();
						if(request.hasField(com.pbdata.generated.balancer.ChatPB.OfflineRequest.getDescriptor().findFieldByName("unread")))
							unread = request.getUnread();
						if(request.hasField(com.pbdata.generated.balancer.ChatPB.OfflineRequest.getDescriptor().findFieldByName("service")))
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
			ChatPB.OfflineRequest.Builder builder = ChatPB.OfflineRequest.newBuilder();
			if(userId != null)
				builder.setUserId(userId);
			if(terminal != null)
				builder.setTerminal(terminal);
			if(unread != null)
				builder.setUnread(unread);
			if (service != null)
				builder.setService(service);
			com.pbdata.generated.balancer.ChatPB.OfflineRequest loginRequest = builder.build();
			byte[] bytes = loginRequest.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			setType(RPCTYPE);		
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public Integer getUnread() {
		return unread;
	}

	public void setUnread(Integer unread) {
		this.unread = unread;
	}

	public Integer getTerminal() {
		return terminal;
	}

	public void setTerminal(Integer terminal) {
		this.terminal = terminal;
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
}
