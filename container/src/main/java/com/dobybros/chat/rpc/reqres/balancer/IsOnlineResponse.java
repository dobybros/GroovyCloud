package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.ChatPB;

public class IsOnlineResponse extends RPCResponse {
	private String server;
	
	public IsOnlineResponse() {
		super(IsOnlineRequest.RPCTYPE);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(IsOnlineResponse.class.getSimpleName());
		builder.append(": ").append(server);
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
						ChatPB.IsOnlineResponse request = ChatPB.IsOnlineResponse.parseFrom(bytes);
						if(request.hasField(ChatPB.IsOnlineResponse.getDescriptor().findFieldByName("server"))) {
							server = request.getServer();
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
			ChatPB.IsOnlineResponse.Builder builder = ChatPB.IsOnlineResponse.newBuilder();
			if(server != null)
				builder.setServer(server);
			ChatPB.IsOnlineResponse loginResponse = builder.build();
			byte[] bytes = loginResponse.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			setType(IsOnlineRequest.RPCTYPE);
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

}
