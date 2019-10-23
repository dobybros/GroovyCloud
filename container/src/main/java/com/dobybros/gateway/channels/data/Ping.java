package com.dobybros.gateway.channels.data;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.gateway.pack.HailPack;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.mobile.MobilePB;


public class Ping extends Data {
	private String id;
	
	public Ping(){
		super(HailPack.TYPE_IN_PING);
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
						MobilePB.Ping request = MobilePB.Ping.parseFrom(bytes);
						if(request.hasField(MobilePB.Ping.getDescriptor().findFieldByName("id")))
							id = request.getId();
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
			MobilePB.Ping.Builder builder = MobilePB.Ping.newBuilder();
			if(id != null)
				builder.setId(id);
			MobilePB.Ping loginRequest = builder.build();
			byte[] bytes = loginRequest.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	

}
