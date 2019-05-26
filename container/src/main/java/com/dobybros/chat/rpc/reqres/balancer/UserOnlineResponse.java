package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCResponse;
import com.pbdata.generated.balancer.ChatPB;

public class UserOnlineResponse extends RPCResponse {

	public UserOnlineResponse() {
		super(UserOnlineRequest.RPCTYPE);
	}

	@Override
	public void resurrect() throws CoreException {
		byte[] bytes = getData();
		Byte encode = getEncode();
		if(bytes != null) {
			if(encode != null) {
				switch(encode) {
				case ENCODE_PB:
//					try {
//						ChatPB.UserOnlineResponse userPresenceResponse = ChatPB.UserOnlineResponse.parseFrom(bytes);
//					} catch (InvalidProtocolBufferException e) {
//						e.printStackTrace();
//						throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODE_PB_PARSE_FAILED, "PB parse data failed, " + e.getMessage());
//					}
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
			ChatPB.UserOnlineResponse.Builder builder = ChatPB.UserOnlineResponse.newBuilder();
			ChatPB.UserOnlineResponse response = builder.build();
			byte[] bytes = response.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			setType(UserPresenceRequest.RPCTYPE);
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

}
