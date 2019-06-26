package com.dobybros.gateway.pack;

import com.dobybros.chat.binary.data.Data;
import org.apache.mina.core.buffer.IoBuffer;


public class HailPack extends Pack {
	
	public static final byte ENCODE_PB = 5;
	public static final byte ENCODE_JSON = 1;
	public static final byte ENCODE_JSON_GZIP = 2;
	public static final byte ENCODE_CHUNKED = 100;

	private static final String TAG = HailPack.class.getSimpleName();
	/**
	 * content的编码方式
	 */
	private byte encode;
	/**
	 * content的版本
	 */
	private short encodeVersion;
	
	public HailPack() {
	}
	
	public HailPack(Data data) {
		super(data);
//		setData(data);
	}
	
	@Override
	public void setData(Data data) {
		super.setData(data);
		encode = data.getEncode();
		encodeVersion = data.getEncodeVersion();
	}

//	public HailPack(byte[] bs) {
//		if (bs != null && bs.length >= 4) {
//			length = GFCommon.bytes2Int(bs, 0, 4, true);
//			content = new byte[bs.length - 4];
//			System.arraycopy(bs, 4, content, 0, content.length);
//		}
//	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(" Len:").append(getLength());
//		sb.append(" Type:").append(getType());
//		sb.append(" Encode:").append(getEncode());
//		sb.append(" content:").append(content);
		return sb.toString();
	}

	public short getEncodeVersion() {
		return encodeVersion;
	}

	public void setEncodeVersion(short encodeVersion) {
		this.encodeVersion = encodeVersion;
	}

	public byte getEncode() {
		return encode;
	}

	public void setEncode(byte encode) {
		this.encode = encode;
	}

	public static int getPackHeadLength() {
		//1 + 4
		return 5;
	}

	@Override
	public void readHeadFromIoBuffer(IoBuffer buf) {
		//version = buf.get(); 在解包的地方已经读取。 
		//encodeVersion = buf.getShort();
//		encode = buf.get();
		type = buf.get();
		length = buf.getInt();
	}

	@Override
	public void persistent(IoBuffer buf) {
//		buf.put(version);
//		buf.putShort(encodeVersion);
//		buf.put(encode);
		buf.put(type);
		buf.putInt(length);
		if(length > 0)
			buf.put(content);
	}
}
