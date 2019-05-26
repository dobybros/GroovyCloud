package com.dobybros.gateway.channels.tcp.codec;

import chat.logs.LoggerEx;
import chat.utils.ChatUtils;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.dobybros.chat.server.OnlineServer;
import com.dobybros.gateway.channels.data.DataVersioning;
import com.dobybros.gateway.pack.Pack;
import com.dobybros.gateway.pack.PackVersioning;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.nio.charset.Charset;

public class HailProtocalDecoder implements ProtocolDecoder {
	private final static String TAG = "DECODER";
	private final AttributeKey CONTEXT = new AttributeKey(getClass(), "context");
	private static final AttributeKey VERSION = new AttributeKey(HailProtocalDecoder.class, "version");
	private static final AttributeKey ENCODEVERSION = new AttributeKey(HailProtocalDecoder.class, "encodeVersion");
	private static final AttributeKey ENCODE = new AttributeKey(HailProtocalDecoder.class, "encode");
	private final Charset charset;
	private int maxPackLength = 48 * 1024;

	public HailProtocalDecoder() {
		this(Charset.defaultCharset());
		//Charset.forName("UTF-8")
	}

	public HailProtocalDecoder(Charset charset) {
		this.charset = charset;
	}

	public int getMaxLineLength() {
		return maxPackLength;
	}

	public void setMaxLineLength(int maxLineLength) {
		if (maxLineLength <= 0) {
			throw new IllegalArgumentException("maxLineLength: "
					+ maxLineLength);
		}
		this.maxPackLength = maxLineLength;
	}

	private Context getContext(IoSession session) {
		Context ctx;
		ctx = (Context) session.getAttribute(CONTEXT);
		if (ctx == null) {
			ctx = new Context();
			session.setAttribute(CONTEXT, ctx);
		}
		return ctx;
	}
	
	public static Byte getEncode(IoSession session) {
		return (Byte) session.getAttribute(ENCODE);
	}
	
	private void setEncode(IoSession session, Byte encode) {
		session.setAttributeIfAbsent(ENCODE, encode);
	}
	
	public static Byte getVersion(IoSession session) {
		return (Byte) session.getAttribute(VERSION);
	}
	
	private void setVersion(IoSession session, Byte version) {
		session.setAttributeIfAbsent(VERSION, version);
	}
	
	public static Short getEncodeVersion(IoSession session) {
		return (Short) session.getAttribute(ENCODEVERSION);
	}
	
	private void setEncodeVersion(IoSession session, Short encodeVersion) {
		session.setAttributeIfAbsent(ENCODEVERSION, encodeVersion);
	}

	static final int balanceLength = 6 + 1 + 2 + 1;
//	static final int packHeadLength = 4 + 2 + 2;
//	static final int packHeadLength = 1 + 1 + 1 + 2 + 4;
	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
			throws Exception {
//		LoggerEx.info(TAG, "decode buffer " + in);
		// 先获取上次的处理上下文，其中可能有未处理完的数据
		Context ctx = getContext(session);
		// 先把当前buffer中的数据追加到Context的buffer当中
		ctx.append(in);
		// 把position指向0位置，把limit指向原来的position位置
		IoBuffer buf = ctx.getBuffer();
		buf.flip();
		if(ctx.consumeBalanceHead > 0) {
			if(buf.remaining() >= balanceLength) {
				byte[] content1 = new byte[6];
				buf.get(content1);//[50, 107, 110, 57, 56, 118]
				String server = new String(content1);
				OnlineServer onlineServer = OnlineServer.getInstance();
				if(onlineServer == null || onlineServer.getServer() == null || !onlineServer.getServer().equals(server)) {
					LoggerEx.error(TAG, "Session closed, consume server " + new String(content1) + " failed, current " + onlineServer.getServer());
//					synchronized (session) {
						session.close(true);
//					}
					return;
				}
				byte v = buf.get();
				setVersion(session, v);
				short ev = buf.getShort();
				setEncodeVersion(session, ev);
				byte e = buf.get();
				setEncode(session, e);
				ctx.consumeBalanceHead = 0;
			} else {
				IoBuffer temp = IoBuffer.allocate(maxPackLength)
						.setAutoExpand(true);
				temp.put(buf);
				temp.flip();
				buf.clear();
				buf.put(temp);
				return;
			}
		} 
		Byte version = getVersion(session);
		Short encodeVersion = getEncodeVersion(session);
		Byte encode = getEncode(session);
		if(version == null || encodeVersion == null) {
			LoggerEx.error(TAG, "Session closed, version " + version + " encodeVersion " + encodeVersion + " is illegal");
//			synchronized (session) {
				session.close(true);
				return;
//			}
		}
		
		int headLength = PackVersioning.getHeadLength(version);
		// 然后按数据包的协议进行读取
		while (buf.remaining() >= headLength) {
			buf.mark();
			// 读取消息头部分
			//http://blog.csdn.net/jxncwzb/article/details/6694425
			//在 ios 下实现 DataOutputStream 和 DataInputStream
			/*
			buf.put(value.getVersion());
			buf.put(value.getType());
			buf.put(value.getEncode());
			buf.putShort(value.getEncodeVersion());
			buf.putInt(value.getLength());
			if (value.getContent() != null)
			 */
			
//			com.dobybros.gateway.pack.v1.HailPack pack = (HailPack) PackVersioning.get(version);
			Pack pack = PackVersioning.get(version, encode, encodeVersion);
			if(pack == null) {
				LoggerEx.error(TAG, "Session closed, version " + version + ", failed to find supported Pack");
//				synchronized (session) {
					session.close(true);
//				}
				return;
			}
			pack.readHeadFromIoBuffer(buf);
			int length = pack.getLength();
//			ChatUtils.generateSecret(sid, seed, sequence);
			
			// 检查读取的包头是否正常，不正常的话清空buffer
			if (length < 0 || length > maxPackLength) {
				LoggerEx.warn(TAG, "Read inproper length of pack, ignore the whole buffer and wait for next arrive. length = " + length + "; maxLength = " + maxPackLength);
				buf.clear();
				
				Pack resPack = DataVersioning.getResult(version, encode, encodeVersion, CoreErrorCodes.ERROR_CHARACTER_OVER_MAXIMUM_LIMITS, null, null);
//				Result result = new Result();
//				result.setCode(CoreErrorCodes.ERROR_CHARACTER_OVER_MAXIMUM_LIMITS);
//				HailPack resPack = new HailPack(result);
//				synchronized (session) {
					session.write(resPack);
					session.close(false);
//				}
				return;
			}
			// 读取正常的消息包，并写入输出流中，以便IoHandler进行处理
			else if (length <= buf.remaining()) {
				//HeapBuffer[pos=5 lim=5 cap=80: empty]
				byte[] content1 = new byte[length];
				buf.get(content1);
				pack.setContent(content1);
				
				Data data = DataVersioning.get(pack);
				out.write(data);
			} else {
				// 如果消息包不完整
				// 将指针重新移动消息头的起始位置
				buf.reset();
				break;
			}
		}
		if (buf.hasRemaining()) {
			// 将数据移到buffer的最前面
			IoBuffer temp = IoBuffer.allocate(maxPackLength)
					.setAutoExpand(true);
			temp.put(buf);
			temp.flip();
			buf.clear();
			buf.put(temp);
		} else {// 如果数据已经处理完毕，进行清空
			buf.clear();
		}

	}

	public void finishDecode(IoSession session, ProtocolDecoderOutput out)
			throws Exception {
	}

	public void dispose(IoSession session) throws Exception {
		Context ctx = (Context) session.getAttribute(CONTEXT);
		if (ctx != null) {
			session.removeAttribute(CONTEXT);
		}
	}

	// 记录上下文，因为数据触发没有规模，很可能只收到数据包的一半
	// 所以，需要上下文拼起来才能完整的处理
	private class Context {
		private IoBuffer buf;
		private int consumeBalanceHead;

		private Context() {
			consumeBalanceHead = balanceLength;
			buf = IoBuffer.allocate(maxPackLength).setAutoExpand(true);
		}

		public IoBuffer getBuffer() {
			return buf;
		}

		public void append(IoBuffer in) {
			buf.put(in);
		}
	}
	public static void main(String[] args) {
		String str = "dkfaj324kadf11f";
//		for(int i = Integer.MAX_VALUE - 100; i < Integer.MAX_VALUE ;i++) {
		for(int i = 1000; i < 1200 ;i++) {
			System.out.println(ChatUtils.generateSecret(str, 16, i));
		}
	}
	
}
