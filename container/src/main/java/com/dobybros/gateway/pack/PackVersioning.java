package com.dobybros.gateway.pack;

import com.dobybros.chat.binary.data.Data;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalDecoder;
import org.apache.mina.core.session.IoSession;


public class PackVersioning {

	public static Pack get(IoSession session) {
		Byte version = HailProtocalDecoder.getVersion(session);
		Short encodeVersion = HailProtocalDecoder.getEncodeVersion(session);
		Byte encode = HailProtocalDecoder.getEncode(session);
		return get(version, encode, encodeVersion);
	}
	
	public static int getHeadLength(int version) {
		switch(version) {
		case 1:
			return com.dobybros.gateway.pack.HailPack.getPackHeadLength();
		}
		return -1;
	}
	
	public static Pack get(Byte version, Byte encode, Short encodeVersion) {
		HailPack hailPack = new HailPack();
		hailPack.setVersion(version);
		hailPack.setEncode(encode);
		hailPack.setEncodeVersion(encodeVersion);
		return hailPack;
	}
	
	public static Pack get(Byte version, Data data) {
		HailPack hailPack = new HailPack();
		hailPack.setVersion(version);
		hailPack.setData(data);
		return hailPack;
	}

}
