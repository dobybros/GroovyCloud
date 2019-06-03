package com.dobybros.gateway.channels.data;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.chat.open.data.Message;
import com.dobybros.gateway.pack.HailPack;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.mobile.MobilePB;

public class OutgoingMessage extends Data {
	
	public OutgoingMessage() {
		super(HailPack.TYPE_OUT_OUTGOINGMESSAGE);
	}
	private String id;
	private String userId;
	private String service;
	
	private Long time;
	
	private String contentType;
	private Integer contentEncode;
	private byte[] content;
	
	private Boolean needAck;
	
	////////////////////////only for keep Message in memory
	private Message message;
	
	@Override
	public void resurrect() throws CoreException {
		byte[] bytes = getData();
		Byte encode = getEncode();
		if(bytes != null) {
			if(encode != null) {
				switch(encode) {
				case ENCODE_PB:
					try {
						MobilePB.OutgoingMessage request = MobilePB.OutgoingMessage.parseFrom(bytes);
						if(request.hasField(MobilePB.OutgoingMessage.getDescriptor().findFieldByName("id")))
							id = request.getId();
						if(request.hasField(MobilePB.OutgoingMessage.getDescriptor().findFieldByName("userId")))
							userId = request.getUserId();
						if(request.hasField(MobilePB.OutgoingMessage.getDescriptor().findFieldByName("time")))
							time = request.getTime();
						if(request.hasField(MobilePB.OutgoingMessage.getDescriptor().findFieldByName("service")))
							service = request.getService();
						if(request.hasField(MobilePB.OutgoingMessage.getDescriptor().findFieldByName("contentType")))
							contentType = request.getContentType();
						if(request.hasField(MobilePB.OutgoingMessage.getDescriptor().findFieldByName("needAck")))
							needAck = request.getNeedAck();
						if(request.hasField(MobilePB.OutgoingMessage.getDescriptor().findFieldByName("contentEncode")))
							contentEncode = request.getContentEncode();
						ByteString contentString = request.getContent();
						if(contentString != null) {
							content = contentString.toByteArray();
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
			encode = ENCODE_PB;//throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent");
		switch(encode) {
		case ENCODE_PB:
			MobilePB.OutgoingMessage.Builder builder = MobilePB.OutgoingMessage.newBuilder();
			if(userId != null)
				builder.setUserId(userId);
			if(service != null)
				builder.setService(service);
			if(content != null)
				builder.setContent(ByteString.copyFrom(content));
			if(contentType != null)
				builder.setContentType(contentType);
			if(time != null)
				builder.setTime(time);
			if(needAck != null)
				builder.setNeedAck(needAck);
			if(contentEncode != null)
				builder.setContentEncode(contentEncode);
			if(id != null)
				builder.setId(id);
			MobilePB.OutgoingMessage incomingMessageRequest = builder.build();
			byte[] bytes = incomingMessageRequest.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void fromMessage(Message message) {
		this.message = message;
		content = message.getData();
		contentType = message.getType();
		contentEncode = message.getEncode();
		id = message.getId();
		service = message.getService();
		time = message.getTime();
		userId = message.getUserId();
		Boolean notSaveOfflineMessage = message.getNotSaveOfflineMessage();
		if(notSaveOfflineMessage == null)
			notSaveOfflineMessage = false;
		needAck = !notSaveOfflineMessage;
	}

	public Boolean getNeedAck() {
		return needAck;
	}

	public void setNeedAck(Boolean needAck) {
		this.needAck = needAck;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Integer getContentEncode() {
		return contentEncode;
	}

	public void setContentEncode(Integer contentEncode) {
		this.contentEncode = contentEncode;
	}
}