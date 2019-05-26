package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.ChatPB;

public class DeviceUpdateRequest extends RPCRequest {
	public static final String RPCTYPE = "device";

	private String userId;

	private Integer terminal;
	private String service;
	private Long time;
	private String deviceToken;
	private String locale;

	public String toString() {
		StringBuilder builder = new StringBuilder(DeviceUpdateRequest.class.getSimpleName());
		builder.append(": ").append(userId).append(": ").append(service);
		return builder.toString();
	}

	public DeviceUpdateRequest() {
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
						ChatPB.DeviceUpdateRequest request = ChatPB.DeviceUpdateRequest.parseFrom(bytes);
						if(request.hasField(ChatPB.DeviceUpdateRequest.getDescriptor().findFieldByName("userId")))
							userId = request.getUserId();
						if(request.hasField(ChatPB.DeviceUpdateRequest.getDescriptor().findFieldByName("service")))
							service = request.getService();

						if(request.hasField(ChatPB.DeviceUpdateRequest.getDescriptor().findFieldByName("terminal")))
							terminal = request.getTerminal();
						if(request.hasField(ChatPB.DeviceUpdateRequest.getDescriptor().findFieldByName("deviceToken")))
							deviceToken = request.getDeviceToken();
						if(request.hasField(ChatPB.DeviceUpdateRequest.getDescriptor().findFieldByName("time")))
							time = request.getTime();
						if (request.hasField(ChatPB.DeviceUpdateRequest.getDescriptor().findFieldByName("locale")))
							locale = request.getLocale();
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
			ChatPB.DeviceUpdateRequest.Builder builder = ChatPB.DeviceUpdateRequest.newBuilder();
			if(userId != null)
				builder.setUserId(userId);
			if(service != null)
				builder.setService(service);
			if(terminal != null)
				builder.setTerminal(terminal);
			if(deviceToken != null)
				builder.setDeviceToken(deviceToken);
			if(time != null)
				builder.setTime(time);
			if (locale != null)
				builder.setLocale(locale);
			ChatPB.DeviceUpdateRequest loginRequest = builder.build();
			byte[] bytes = loginRequest.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			setType(RPCTYPE);		
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
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

	public Integer getTerminal() {
		return terminal;
	}

	public void setTerminal(Integer terminal) {
		this.terminal = terminal;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
}
