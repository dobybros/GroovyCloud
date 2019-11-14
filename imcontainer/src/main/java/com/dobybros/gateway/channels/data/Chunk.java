package com.dobybros.gateway.channels.data;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.errors.IMCoreErrorCodes;
import com.dobybros.gateway.pack.HailPack;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.mobile.MobilePB;

public class Chunk extends Data {

	public Chunk() {
		super(HailPack.TYPE_INOUT_CHUNK);
	}
	private String id;
	private Integer originalType;
	private byte[] content;
	private Integer offset;
	private Integer totalSize;
	private Integer chunkNum;
	
	@Override
	public void resurrect() throws CoreException {
		byte[] bytes = getData();
		Byte encode = getEncode();
		if(bytes != null) {
			if(encode != null) {
				switch(encode) {
				case ENCODE_PB:
					try {
						MobilePB.Chunk request = MobilePB.Chunk.parseFrom(bytes);
						if(request.hasField(MobilePB.Chunk.getDescriptor().findFieldByName("id")))
							id = request.getId();
						originalType = request.getType();
						offset = request.getOffset();
						totalSize = request.getTotalSize();
						ByteString contentString = request.getContent();
						if(contentString != null) {
							content = contentString.toByteArray();
						}
						chunkNum = request.getChunkNum();
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
			MobilePB.Chunk.Builder builder = MobilePB.Chunk.newBuilder();
			if(originalType != null)
				builder.setType(originalType);
			if(content != null)
				builder.setContent(ByteString.copyFrom(content));
			if(offset != null)
				builder.setOffset(offset);
			if(totalSize != null)
				builder.setTotalSize(totalSize);
			if(id != null)
				builder.setId(id);
			if(chunkNum != null)
				builder.setChunkNum(chunkNum);
			MobilePB.Chunk chunk = builder.build();
			byte[] bytes = chunk.toByteArray();
			setData(bytes);
			setEncode(ENCODE_PB);
			break;
			default:
				throw new CoreException(IMCoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Integer getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(Integer totalSize) {
		this.totalSize = totalSize;
	}

	public Integer getOriginalType() {
		return originalType;
	}

	public void setOriginalType(Integer originalType) {
		this.originalType = originalType;
	}

	public Integer getChunkNum() {
		return chunkNum;
	}

	public void setChunkNum(Integer chunkNum) {
		this.chunkNum = chunkNum;
	}
}