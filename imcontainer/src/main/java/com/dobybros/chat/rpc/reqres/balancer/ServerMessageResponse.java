package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import chat.utils.ChatUtils;
import com.dobybros.chat.errors.IMCoreErrorCodes;
import com.docker.rpc.RPCResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ProtocolStringList;
import com.pbdata.generated.balancer.ChatPB;

import java.util.HashSet;
import java.util.Set;

public class ServerMessageResponse extends RPCResponse {
	private Set<String> notReceivedIds;
	
	public ServerMessageResponse() {
		super(ServerMessageRequest.RPCTYPE);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(ServerMessageResponse.class.getSimpleName());
		builder.append(": ").append(ChatUtils.toString(notReceivedIds));
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
						com.pbdata.generated.balancer.ChatPB.ServerMessageResponse request = ChatPB.ServerMessageResponse.parseFrom(bytes);
						ProtocolStringList notReceivedIdList = request.getNotReceivedIdsList();
						if(notReceivedIdList != null && !notReceivedIdList.isEmpty()) {
							notReceivedIds = new HashSet<String>(notReceivedIdList);
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
			throw new CoreException(IMCoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent");
		switch(encode) {
		case ENCODE_PB:
			ChatPB.ServerMessageResponse.Builder builder = ChatPB.ServerMessageResponse.newBuilder();
			if(notReceivedIds != null) {
//				for(String targetId : notReceivedIds) {
//					builder.addNotReceivedIds(targetId);
//				}
				builder.addAllNotReceivedIds(notReceivedIds);
			}
			com.pbdata.generated.balancer.ChatPB.ServerMessageResponse response = builder.build();
			byte[] bytes = response.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			setType(ServerMessageRequest.RPCTYPE);
			break;
			default:
				throw new CoreException(IMCoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public Set<String> getNotReceivedIds() {
		return notReceivedIds;
	}

	public void setNotReceivedIds(Set<String> notReceivedIds) {
		this.notReceivedIds = notReceivedIds;
	}

}
