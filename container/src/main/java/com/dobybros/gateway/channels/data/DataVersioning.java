package com.dobybros.gateway.channels.data;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.gateway.channels.tcp.codec.HailProtocalDecoder;
import com.dobybros.gateway.pack.HailPack;
import com.dobybros.gateway.pack.Pack;
import org.apache.mina.core.session.IoSession;

public class DataVersioning{
	private static final String TAG = DataVersioning.class.getSimpleName();

	public static Pack getDataPack(IoSession session, Data data) {
		Byte version = HailProtocalDecoder.getVersion(session);
		Short encodeVersion = HailProtocalDecoder.getEncodeVersion(session);
		Byte encode = HailProtocalDecoder.getEncode(session);
		data.setEncodeVersion(encodeVersion);
		data.setEncode(encode);
		HailPack hailPack = new HailPack(data);
		hailPack.setVersion(version);
		return hailPack;
	}
	
	public static Result getResultData(IoSession session, int code, String description, String forId) {
		Short encodeVersion = HailProtocalDecoder.getEncodeVersion(session);
		Result data = (Result) get(encodeVersion, Pack.TYPE_OUT_RESULT);
		data.setCode(code);
		data.setDescription(description);
		data.setForId(forId);
		return data;
	}
	
	public static Pack getResult(IoSession session, int code, String description, String forId) {
		Byte version = HailProtocalDecoder.getVersion(session);
		Short encodeVersion = HailProtocalDecoder.getEncodeVersion(session);
		Byte encode = HailProtocalDecoder.getEncode(session);
		return getResult(version, encode, encodeVersion, code, description, forId);
	}
	
	public static Data get(Pack pack) {
		HailPack hailPack = (HailPack) pack;
		Data data = get(hailPack.getEncodeVersion(), hailPack.getType());
		if(data != null) {
			data.setData(pack.getContent());
			data.setEncode(hailPack.getEncode());
			try {
				data.resurrect();
				return data;
			} catch (CoreException e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "Resurrect data failed, " + e.getMessage() + " for data " + data);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Data get(short encodeVersion, int type) {
		//new Acknowledge() 比Acknowledge.class.newInstance快1.5倍
		Data data = null;
		switch(type) {
		case HailPack.TYPE_IN_ACKNOWLEDGE:
			data = new Acknowledge();
			break;
		case HailPack.TYPE_IN_IDENTITY:
			data = new Identity();
			break;
		case HailPack.TYPE_IN_INCOMINGMESSAGE:
			data = new IncomingMessage();
			break;
		case HailPack.TYPE_OUT_OUTGOINGMESSAGE:
			data = new OutgoingMessage();
			break;
		case HailPack.TYPE_OUT_RESULT:
			data = new Result();
			break;
		case HailPack.TYPE_IN_PING:
			data = new Ping();
			break;
		case HailPack.TYPE_IN_INCOMINGDATA:
			data = new IncomingData();
			break;
		case HailPack.TYPE_OUT_OUTGOINGDATA:
			data = new OutgoingData();
			break;
		}
		if(data != null)
			data.setEncodeVersion(encodeVersion);
		return data;
	}
	
	public static void main(String[] args) {
		int count = 1000000;
		long time = System.currentTimeMillis();
		for(int i = 0; i < count; i++) {
			DataVersioning.get((short) 1, HailPack.TYPE_IN_ACKNOWLEDGE);
//			new Acknowledge(); //40ms
//			try {
//				Acknowledge.class.newInstance(); //100ms
//			} catch (InstantiationException | IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		System.out.println(System.currentTimeMillis() - time);
	}

	public static Pack getResult(Byte version, Byte encode, Short encodeVersion, int code, String description, String forId) {
		if(version == null || encodeVersion == null)
			return null;
		Result data = (Result) get(encodeVersion, Pack.TYPE_OUT_RESULT);
		data.setCode(code);
		data.setDescription(description);
		data.setForId(forId);
		HailPack hailPack = new HailPack(data);
		hailPack.setVersion(version);
		hailPack.setEncode(encode);
		return hailPack;
	}
	
}
