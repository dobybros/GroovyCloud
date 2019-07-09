package com.dobybros.gateway.channels.data;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.gateway.pack.HailPack;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.mobile.MobilePB;


public class Result extends Data {
	public static int OFFLINE_MESSAGE_RECEIVED_CODE = 11;
	
	private Integer code;
	private String description;
	private String forId;
	private String serverId;
	private Long time;
	private Integer contentEncode;
	private byte[] content;
	
	public Result(){
		super(HailPack.TYPE_OUT_RESULT);
	}
	
	/**
	 * @param code the code to set
	 */
	public void setCode(Integer code) {
		this.code = code;
	}
	/**
	 * @return the code
	 */
	public Integer getCode() {
		return code;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	public String getForId() {
		return forId;
	}

	public void setForId(String forId) {
		this.forId = forId;
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
						MobilePB.Result request = MobilePB.Result.parseFrom(bytes);
						if(request.hasField(MobilePB.Result.getDescriptor().findFieldByName("description")))
							description = request.getDescription();
						if(request.hasField(MobilePB.Result.getDescriptor().findFieldByName("forId")))
							forId = request.getForId();
						if(request.hasField(MobilePB.Result.getDescriptor().findFieldByName("code")))
							code = request.getCode();
						if(request.hasField(MobilePB.Result.getDescriptor().findFieldByName("time")))
							time = request.getTime();
						if(request.hasField(MobilePB.Result.getDescriptor().findFieldByName("serverId")))
							serverId = request.getServerId();
						if(request.hasField(MobilePB.Result.getDescriptor().findFieldByName("contentEncode")))
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
			encode = ENCODE_PB;
//			throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent");
		switch(encode) {
		case ENCODE_PB:
			MobilePB.Result.Builder builder = MobilePB.Result.newBuilder();
			if(code != null)
				builder.setCode(code);
			if(description != null)
				builder.setDescription(description);
			if(forId != null)
				builder.setForId(forId);
			if(time != null)
				builder.setTime(time);
			if(serverId != null) 
				builder.setServerId(serverId);
			if(contentEncode != null)
				builder.setContentEncode(contentEncode);
			if(content != null)
				builder.setContent(ByteString.copyFrom(content));
			MobilePB.Result resultRequest = builder.build();
			byte[] bytes = resultRequest.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			break;
			default:
				throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Integer getContentEncode() {
		return contentEncode;
	}

	public void setContentEncode(Integer contentEncode) {
		this.contentEncode = contentEncode;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}
}
