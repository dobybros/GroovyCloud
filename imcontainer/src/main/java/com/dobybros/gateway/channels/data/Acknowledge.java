package com.dobybros.gateway.channels.data;

import chat.errors.CoreException;
import chat.utils.ChatUtils;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.errors.IMCoreErrorCodes;
import com.dobybros.gateway.pack.HailPack;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Acknowledge extends Data{
	private String id;
	private Set<String> msgIds;
	private String service;
	
	public Acknowledge(){
		super(HailPack.TYPE_IN_ACKNOWLEDGE);
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(ChatUtils.toString(msgIds));
		return new String(buffer); 
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
						com.pbdata.generated.mobile.MobilePB.Acknowledge request = com.pbdata.generated.mobile.MobilePB.Acknowledge.parseFrom(bytes);
						List<String> msgIdList = request.getMsgIdsList();
						if(msgIdList != null)
							msgIds = new HashSet<String>(msgIdList);
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.Acknowledge.getDescriptor().findFieldByName("id")))
							id = request.getId();
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.Acknowledge.getDescriptor().findFieldByName("service")))
							service = request.getService();
					} catch (InvalidProtocolBufferException e) {
						e.printStackTrace();
						throw new CoreException(IMCoreErrorCodes.ERROR_RPC_ENCODE_PB_PARSE_FAILED, "PB parse data failed, " + e.getMessage());
					}
					break;
					default:
						throw new CoreException(IMCoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for resurrect");
				}
			}
		}
	}

	@Override
	public void persistent() throws CoreException {
		Byte encode = getEncode();
		if(encode == null)
			encode = ENCODE_PB;//throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent");
		switch(encode) {
		case ENCODE_PB:
			com.pbdata.generated.mobile.MobilePB.Acknowledge.Builder builder = com.pbdata.generated.mobile.MobilePB.Acknowledge.newBuilder();
			if(msgIds != null)
				builder.addAllMsgIds(msgIds);
			if(id != null)
				builder.setId(id);
			if(service != null)
				builder.setService(service);
			com.pbdata.generated.mobile.MobilePB.Acknowledge loginRequest = builder.build();
			byte[] bytes = loginRequest.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			break;
			default:
				throw new CoreException(IMCoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<String> getMsgIds() {
		return msgIds;
	}

	public void setMsgIds(Set<String> msgIds) {
		this.msgIds = msgIds;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

}
