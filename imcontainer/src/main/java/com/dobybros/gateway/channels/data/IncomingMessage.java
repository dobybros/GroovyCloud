package com.dobybros.gateway.channels.data;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.errors.IMCoreErrorCodes;
import com.dobybros.chat.open.data.Message;
import com.dobybros.gateway.pack.HailPack;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * IncomingMessage转换成为Message之后， IncomingMessage的id会是Message的clientId。 
 * 这个转化过程是在Gateway服务器进行的
 * 
 * @author aplombchen
 *
 */
public class IncomingMessage extends Data {
	
	public IncomingMessage() {
		super(HailPack.TYPE_IN_INCOMINGMESSAGE);
	}
	private String id;
	/**
	 * 该消息要发送的业务服务器
	 * singlechat/* 代表发送给单聊服务器的任何一台服务器。 
	 * singlechat/skdfjea 代表发送给单聊服务器的服务器名称为skdfjea的服务器。 
	 */
	private String server;

	/*
	这条消息来源于哪个Service
	 */
	private String service;

	/*
	消息发送给你用户的所属Service
	 */
	private String userService;
	
	private Set<String> userIds;
	
	private String contentType;
	private Integer contentEncode;
	private byte[] content;
	private Boolean notSaveOfflineMessage;
	
	public Message toMessage(String userId) {
		Message msg = new Message();
		msg.setClientId(id);
		msg.setId(script.memodb.ObjectId.get().toString());
		msg.setReceiverIds(userIds);
		msg.setServer(server);
		msg.setService(service);
		msg.setTime(System.currentTimeMillis());
		msg.setType(contentType);
		msg.setData(content);
		msg.setEncode(contentEncode);
		msg.setUserId(userId);
		msg.setReceiverService(userService);
		if(notSaveOfflineMessage == null)
			notSaveOfflineMessage = false;
		msg.setNotSaveOfflineMessage(notSaveOfflineMessage);
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
						com.pbdata.generated.mobile.MobilePB.IncomingMessage request = com.pbdata.generated.mobile.MobilePB.IncomingMessage.parseFrom(bytes);
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.IncomingMessage.getDescriptor().findFieldByName("id")))
							id = request.getId();
						List<String> userIdList = request.getUserIdsList();
						if(userIdList != null)
							userIds = new HashSet<String>(userIdList);
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.IncomingMessage.getDescriptor().findFieldByName("server")))
							server = request.getServer();
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.IncomingMessage.getDescriptor().findFieldByName("service")))
							service = request.getService();
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.IncomingMessage.getDescriptor().findFieldByName("userService")))
							userService = request.getUserService();
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.IncomingMessage.getDescriptor().findFieldByName("contentType")))
							contentType = request.getContentType();
						if(request.hasField(com.pbdata.generated.mobile.MobilePB.IncomingMessage.getDescriptor().findFieldByName("contentEncode")))
							contentEncode = request.getContentEncode();
						notSaveOfflineMessage = request.getNotSaveOfflineMsg();
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
			com.pbdata.generated.mobile.MobilePB.IncomingMessage.Builder builder = com.pbdata.generated.mobile.MobilePB.IncomingMessage.newBuilder();
			if(server != null)
				builder.setServer(server);
			if(service != null)
				builder.setService(service);
			if(userService != null)
				builder.setUserService(userService);
			if(content != null)
				builder.setContent(ByteString.copyFrom(content));
			if(contentType != null)
				builder.setContentType(contentType);
			if(contentEncode != null)
				builder.setContentEncode(contentEncode);
			if(userIds != null)
				builder.addAllUserIds(userIds);
			if(id != null)
				builder.setId(id);
			if(notSaveOfflineMessage != null)
				builder.setNotSaveOfflineMsg(notSaveOfflineMessage);
			com.pbdata.generated.mobile.MobilePB.IncomingMessage incomingMessageRequest = builder.build();
			byte[] bytes = incomingMessageRequest.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			break;
			default:
				throw new CoreException(IMCoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Set<String> getUserIds() {
		return userIds;
	}

	public void setUserIds(Set<String> userIds) {
		this.userIds = userIds;
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

	public String getUserService() {
		return userService;
	}

	public void setUserService(String userService) {
		this.userService = userService;
	}

	public Boolean getNotSaveOfflineMessage() {
		return notSaveOfflineMessage;
	}

	public void setNotSaveOfflineMessage(Boolean notSaveOfflineMessage) {
		this.notSaveOfflineMessage = notSaveOfflineMessage;
	}
}