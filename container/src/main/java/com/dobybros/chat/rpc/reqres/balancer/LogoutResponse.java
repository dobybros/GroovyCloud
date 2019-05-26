package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.BalancerPB;

public class LogoutResponse extends RPCResponse {
	public LogoutResponse() {
		super(LogoutRequest.RPCTYPE);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(LogoutResponse.class.getSimpleName());
//		builder.append(": ").append(message);
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
						com.pbdata.generated.balancer.BalancerPB.LogoutResponse response = BalancerPB.LogoutResponse.parseFrom(bytes);
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
			BalancerPB.LogoutResponse.Builder builder = BalancerPB.LogoutResponse.newBuilder();
			com.pbdata.generated.balancer.BalancerPB.LogoutResponse logoutResponse = builder.build();
			byte[] bytes = logoutResponse.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			setType(LogoutRequest.RPCTYPE);
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

}
