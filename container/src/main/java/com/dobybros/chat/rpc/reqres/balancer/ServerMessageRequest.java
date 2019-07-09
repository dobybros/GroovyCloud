package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.chat.open.data.Message;
import com.docker.rpc.RPCRequest;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.ChatPB;

import java.util.HashSet;
import java.util.List;

public class ServerMessageRequest extends RPCRequest {
	public static final String RPCTYPE = "smsg";
	
	private Message message;

	public String toString() {
		StringBuilder builder = new StringBuilder(ServerMessageRequest.class.getSimpleName());
		builder.append(": ").append(message);
		return builder.toString();
	}

	public ServerMessageRequest() {
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
						com.pbdata.generated.balancer.ChatPB.ServerMessageRequest request = ChatPB.ServerMessageRequest.parseFrom(bytes);
						com.pbdata.generated.balancer.ChatPB.Message msg = request.getMessage();
						if(msg != null) {
							message = new Message();
							if(msg.hasField(com.pbdata.generated.balancer.ChatPB.Message.getDescriptor().findFieldByName("id")))
								message.setId(msg.getId());
							if(msg.hasField(com.pbdata.generated.balancer.ChatPB.Message.getDescriptor().findFieldByName("server")))
								message.setServer(msg.getServer());
							if(msg.hasField(com.pbdata.generated.balancer.ChatPB.Message.getDescriptor().findFieldByName("service")))
								message.setService(msg.getService());
							if(msg.hasField(com.pbdata.generated.balancer.ChatPB.Message.getDescriptor().findFieldByName("time")))
								message.setTime(msg.getTime());
							if(msg.hasField(com.pbdata.generated.balancer.ChatPB.Message.getDescriptor().findFieldByName("type")))
								message.setType(msg.getType());
							if(msg.hasField(com.pbdata.generated.balancer.ChatPB.Message.getDescriptor().findFieldByName("userId")))
								message.setUserId(msg.getUserId());
							if(msg.hasField(com.pbdata.generated.balancer.ChatPB.Message.getDescriptor().findFieldByName("receiverService")))
								message.setReceiverService(msg.getReceiverService());
							List<String> receiverIdList = msg.getReceiverIdsList();
							if(receiverIdList != null)
								message.setReceiverIds(new HashSet<String>(receiverIdList));
							ByteString dataString = msg.getData();
							if(dataString != null)
								message.setData(dataString.toByteArray());
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
			ChatPB.ServerMessageRequest.Builder builder = ChatPB.ServerMessageRequest.newBuilder();
			if(message != null) {
				ChatPB.Message.Builder msgBuilder = ChatPB.Message.newBuilder();
				if(message.getData() != null)
					msgBuilder.setData(ByteString.copyFrom(message.getData()));
				if(message.getId() != null)
					msgBuilder.setId(message.getId());
				if(message.getReceiverIds() != null)
					msgBuilder.addAllReceiverIds(message.getReceiverIds());
				if(message.getServer() != null)
					msgBuilder.setServer(message.getServer());
				if(message.getService() != null)
					msgBuilder.setService(message.getService());
				if(message.getTime() != null)
					msgBuilder.setTime(message.getTime());
				if(message.getType() != null)
					msgBuilder.setType(message.getType());
				if(message.getUserId() != null)
					msgBuilder.setUserId(message.getUserId());
				if(message.getReceiverService() != null)
					msgBuilder.setReceiverService(message.getReceiverService());
				builder.setMessage(msgBuilder);
			}
			com.pbdata.generated.balancer.ChatPB.ServerMessageRequest serverMessageRequest = builder.build();
			byte[] bytes = serverMessageRequest.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			setType(RPCTYPE);		
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

}
