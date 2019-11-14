package com.dobybros.gateway.channels.data;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.errors.IMCoreErrorCodes;
import com.dobybros.chat.open.data.Message;
import com.dobybros.gateway.pack.HailPack;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashSet;
import java.util.Set;

/**
 * IncomingData转换成为Message之后， IncomingData的id会是Message的clientId。
 * 这个转化过程是在Gateway服务器进行的
 * 
 * @author aplombchen
 *
 */
public class IncomingData extends Data{

	private String id;
	private String service;
	private String contentType;
	private Integer contentEncode;
	private byte[] content;

	public IncomingData() {
		super(HailPack.TYPE_IN_INCOMINGDATA);
	}

	// 为向同一个serviceUser的不同terminal发消息而生
	public Message toMessage(String userId) {
		Message msg = new Message();
		msg.setClientId(id);
		msg.setId(script.memodb.ObjectId.get().toString());
		Set<String> userIds = new HashSet<>();
		userIds.add(userId);
		msg.setReceiverIds(userIds);
		msg.setService(service);
		msg.setTime(System.currentTimeMillis());
		msg.setType(contentType);
		msg.setData(content);
		msg.setEncode(contentEncode);
		msg.setUserId(userId);
		msg.setReceiverService(service);
		msg.setNotSaveOfflineMessage(true);
		return msg;
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
						com.pbdata.generated.mobile.MobilePB.IncomingData request = com.pbdata.generated.mobile.MobilePB.IncomingData.parseFrom(bytes);
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.IncomingData.getDescriptor().findFieldByName("id")))
							id = request.getId();
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.IncomingData.getDescriptor().findFieldByName("service")))
							service = request.getService();
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.IncomingData.getDescriptor().findFieldByName("contentType")))
							contentType = request.getContentType();
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.IncomingData.getDescriptor().findFieldByName("contentEncode")))
							contentEncode = request.getContentEncode();
						ByteString contentString = request.getContent();
						if(contentString != null) {
							content = contentString.toByteArray();
						}
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
			com.pbdata.generated.mobile.MobilePB.IncomingData.Builder builder = com.pbdata.generated.mobile.MobilePB.IncomingData.newBuilder();
			if(service != null)
				builder.setService(service);
			if(content != null)
				builder.setContent(ByteString.copyFrom(content));
			if(contentType != null)
				builder.setContentType(contentType);
			if(contentEncode != null)
				builder.setContentEncode(contentEncode);
			if(id != null)
				builder.setId(id);
			com.pbdata.generated.mobile.MobilePB.IncomingData incomingDataRequest = builder.build();
			byte[] bytes = incomingDataRequest.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			break;
			default:
				throw new CoreException(IMCoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}


	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getContentEncode() {
		return contentEncode;
	}

	public void setContentEncode(Integer contentEncode) {
		this.contentEncode = contentEncode;
	}

}