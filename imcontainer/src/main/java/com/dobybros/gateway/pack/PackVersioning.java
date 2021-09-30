package com.dobybros.gateway.pack;

import com.dobybros.chat.binary.data.Data;
import com.dobybros.gateway.channels.websocket.data.ChannelContext;


public class PackVersioning {

	public static Pack get(ChannelContext context) {
		Byte version = context.getPackVersion();
		Short encodeVersion = context.getEncodeVersion();
		Byte encode = context.getEncode();
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
