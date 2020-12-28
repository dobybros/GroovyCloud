package com.dobybros.gateway.channels.tcp.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HailProtocalCodecFactory implements ProtocolCodecFactory {
	private final HailProtocalEncoder encoder;
	private final HailProtocalDecoder decoder;

	public HailProtocalCodecFactory(){
		this(StandardCharsets.UTF_8);
	}
	
	public HailProtocalCodecFactory(Charset charset) {
		encoder = new HailProtocalEncoder(charset);
		decoder = new HailProtocalDecoder(charset);
	}

	public ProtocolEncoder getEncoder(IoSession session) {
		return encoder;
	}

	public ProtocolDecoder getDecoder(IoSession session) {
		return decoder;
	}

}
