package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.chat.open.data.DeviceInfo;
import com.dobybros.chat.utils.PBUtil;
import com.docker.rpc.RPCResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.ChatPB;

import java.util.HashMap;
import java.util.Map;

public class UserPresenceResponse extends RPCResponse {

	private GatewayServer gateway;
	private Integer onlineStatus;
	private Integer offlineUnread;

	private Map<Integer, DeviceInfo> deviceMap;

	public String toString() {
		StringBuilder builder = new StringBuilder(UserPresenceResponse.class.getSimpleName());
		builder.append("onlineStatus: ").append(onlineStatus).append(", offlineUnread: ").append(offlineUnread).append(", gateway: ").append(gateway).append(", deviceMap: ").append(deviceMap);
		return builder.toString();
	}

	public UserPresenceResponse() {
		super(UserPresenceRequest.RPCTYPE);
	}

//	GatewayServer gateway = 1;
//	int32 onlineStatus = 2; //是否在线
//	int32 offlineUnread = 3; //离线的未读数
//	map<int32, Device> devices = 4; //设备信息

	@Override
	public void resurrect() throws CoreException {
		byte[] bytes = getData();
		Byte encode = getEncode();
		if(bytes != null) {
			if(encode != null) {
				switch(encode) {
				case ENCODE_PB:
					try {
						ChatPB.UserPresenceResponse userPresenceResponse = ChatPB.UserPresenceResponse.parseFrom(bytes);
						ChatPB.GatewayServer gatewayServer = userPresenceResponse.getGateway();
						gateway = GatewayServer.fromPB(gatewayServer);
						if(userPresenceResponse.hasField(com.pbdata.generated.balancer.ChatPB.UserPresenceResponse.getDescriptor().findFieldByName("offlineUnread"))) {
							offlineUnread = userPresenceResponse.getOfflineUnread();
						}
						if(userPresenceResponse.hasField(com.pbdata.generated.balancer.ChatPB.UserPresenceResponse.getDescriptor().findFieldByName("onlineStatus"))) {
							onlineStatus = userPresenceResponse.getOnlineStatus();
						}
						deviceMap = new HashMap<>();
						Map<Integer, ChatPB.Device> deviceMapRes = userPresenceResponse.getDevicesMap();
						for (Integer terminal : deviceMapRes.keySet()) {
							ChatPB.Device device = deviceMapRes.get(terminal);
							DeviceInfo deviceUse = PBUtil.fromDevicePB(device);
							deviceMap.put(terminal, deviceUse);
						}
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

	//	GatewayServer gateway = 1;
//	int32 onlineStatus = 2; //是否在线
//	int32 offlineUnread = 3; //离线的未读数
//	map<int32, Device> devices = 4; //设备信息

	@Override
	public void persistent() throws CoreException {
		Byte encode = getEncode();
		if(encode == null)
			throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent");
		switch(encode) {
		case ENCODE_PB:
			ChatPB.UserPresenceResponse.Builder builder = ChatPB.UserPresenceResponse.newBuilder();
			if(gateway != null)
				builder.setGateway(GatewayServer.toPB(gateway));
			if(onlineStatus != null)
				builder.setOnlineStatus(onlineStatus);
			if(offlineUnread != null)
				builder.setOfflineUnread(offlineUnread);
            if (deviceMap != null) {
                for (Integer terminal : deviceMap.keySet()) {
					DeviceInfo device = deviceMap.get(terminal);
					builder.putDevices(terminal, PBUtil.toDevicePB(device, false).build());
                }
            }
			com.pbdata.generated.balancer.ChatPB.UserPresenceResponse response = builder.build();
			byte[] bytes = response.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			setType(UserPresenceRequest.RPCTYPE);
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}


    public Integer getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(Integer onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public Integer getOfflineUnread() {
		return offlineUnread;
	}

	public void setOfflineUnread(Integer offlineUnread) {
		this.offlineUnread = offlineUnread;
	}

	public GatewayServer getGateway() {
		return gateway;
	}

	public void setGateway(GatewayServer gateway) {
		this.gateway = gateway;
	}

	public Map<Integer, DeviceInfo> getDeviceMap() {
		return deviceMap;
	}

	public void setDeviceMap(Map<Integer, DeviceInfo> deviceMap) {
		this.deviceMap = deviceMap;
	}
}
