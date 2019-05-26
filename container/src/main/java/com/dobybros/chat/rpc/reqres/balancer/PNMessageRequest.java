package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.chat.open.data.PNInfo;
import com.dobybros.chat.utils.PBUtil;
import com.docker.rpc.RPCRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.ChatPB;

import java.util.HashMap;
import java.util.Map;

public class PNMessageRequest extends RPCRequest {
	public static final String RPCTYPE = "pnmsg";

	// 接收者id
	private String userId;

	// service
	private String service;

	// 发送者id
	private String senderId;

	// key：locale   value：shortMessage
	private Map<String, String> shortMessageMap;

	// pn基于terminal的信息
	private Map<Integer, PNInfo> pnInfoMap;

	
	public PNMessageRequest() {
		super(RPCTYPE);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(PNMessageRequest.class.getSimpleName());
		builder.append("").append(senderId).append(" send pnmessage to ").append(userId).append("#").append(service);
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
						ChatPB.PNMessageRequest request = ChatPB.PNMessageRequest.parseFrom(bytes);
						shortMessageMap = request.getShortMessageMapMap();
						if(request.hasField(ChatPB.PNMessageRequest.getDescriptor().findFieldByName("userId")))
							userId = request.getUserId();
						if(request.hasField(ChatPB.PNMessageRequest.getDescriptor().findFieldByName("service")))
							service = request.getService();
						if(request.hasField(ChatPB.PNMessageRequest.getDescriptor().findFieldByName("senderId")))
							senderId = request.getSenderId();
						pnInfoMap = new HashMap<>();
						for (Integer terminal : request.getPnInfoMapMap().keySet()) {
							ChatPB.PNInfo pnInfoPB = request.getPnInfoMapMap().get(terminal);
							PNInfo pnInfo = PBUtil.fromPNInfoPB(pnInfoPB);
							pnInfoMap.put(terminal, pnInfo);
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

	@Override
	public void persistent() throws CoreException {
		Byte encode = getEncode();
		if(encode == null)
			throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent");
		switch(encode) {
		case ENCODE_PB:
			ChatPB.PNMessageRequest.Builder builder = ChatPB.PNMessageRequest.newBuilder();
			if(shortMessageMap != null)
				builder.putAllShortMessageMap(shortMessageMap);
			if(userId != null)
				builder.setUserId(userId);
			if(service != null)
				builder.setService(service);
			if(senderId != null)
				builder.setSenderId(senderId);
			if(pnInfoMap != null)
				for (Integer terminal : pnInfoMap.keySet()) {
					PNInfo pnInfo = pnInfoMap.get(terminal);
					ChatPB.PNInfo pnInfoPB = PBUtil.toPNInfoPB(pnInfo).build();
					builder.putPnInfoMap(terminal, pnInfoPB);
				}

			ChatPB.PNMessageRequest loginRequest = builder.build();
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

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public Map<String, String> getShortMessageMap() {
		return shortMessageMap;
	}

	public void setShortMessageMap(Map<String, String> shortMessageMap) {
		this.shortMessageMap = shortMessageMap;
	}

	public Map<Integer, PNInfo> getPnInfoMap() {
		return pnInfoMap;
	}

	public void setPnInfoMap(Map<Integer, PNInfo> pnInfoMap) {
		this.pnInfoMap = pnInfoMap;
	}
}
