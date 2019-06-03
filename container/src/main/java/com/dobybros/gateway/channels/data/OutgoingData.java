package com.dobybros.gateway.channels.data;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.chat.open.data.Message;
import com.dobybros.gateway.pack.HailPack;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.mobile.MobilePB;

public class OutgoingData extends Data {

	public OutgoingData() {
		super(HailPack.TYPE_OUT_OUTGOINGDATA);
	}
	private String id;
	private Long time;
	private String service;
	private String contentType;
	private Integer contentEncode;
	private byte[] content;
	private Boolean needAck;
	
	@Override
	public void resurrect() throws CoreException {
		byte[] bytes = getData();
		Byte encode = getEncode();
		if(bytes != null) {
			if(encode != null) {
				switch(encode) {
				case ENCODE_PB:
					try {
						MobilePB.OutgoingData request = MobilePB.OutgoingData.parseFrom(bytes);
						if(request.hasField(MobilePB.OutgoingData.getDescriptor().findFieldByName("id")))
							id = request.getId();
						if(request.hasField(MobilePB.OutgoingData.getDescriptor().findFieldByName("time")))
							time = request.getTime();
						if(request.hasField(MobilePB.OutgoingData.getDescriptor().findFieldByName("service")))
							service = request.getService();
						if(request.hasField(MobilePB.OutgoingData.getDescriptor().findFieldByName("contentType")))
							contentType = request.getContentType();
						if(request.hasField(MobilePB.OutgoingData.getDescriptor().findFieldByName("contentEncode")))
							contentEncode = request.getContentEncode();
						ByteString contentString = request.getContent();
						if(contentString != null) {
							content = contentString.toByteArray();
						}
						if(request.hasField(MobilePB.OutgoingData.getDescriptor().findFieldByName("needAck")))
							needAck = request.getNeedAck();
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
			MobilePB.OutgoingData.Builder builder = MobilePB.OutgoingData.newBuilder();
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
			MobilePB.OutgoingData incomingMessageRequest = builder.build();
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
		content = message.getData();
		contentType = message.getType();
		contentEncode = message.getEncode();
		id = message.getId();
		service = message.getService();
		time = message.getTime();
		Boolean notSaveOfflineMessage = message.getNotSaveOfflineMessage();
		if(notSaveOfflineMessage == null)
			notSaveOfflineMessage = true;
		needAck = !notSaveOfflineMessage;
	}

	public Boolean getNeedAck() {
		return needAck;
	}

	public void setNeedAck(Boolean needAck) {
		this.needAck = needAck;
	}

	public Integer getContentEncode() {
		return contentEncode;
	}

	public void setContentEncode(Integer contentEncode) {
		this.contentEncode = contentEncode;
	}
}