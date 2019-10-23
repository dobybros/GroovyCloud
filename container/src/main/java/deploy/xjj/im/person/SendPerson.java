package deploy.xjj.im.person;

import com.alibaba.fastjson.JSON;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.utils.JsonObjectEx;
import com.dobybros.gateway.channels.data.*;
import com.dobybros.gateway.pack.HailPack;
import com.docker.rpc.BinaryCodec;
import com.pbdata.generated.mobile.MessagePB;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SendPerson extends Person {
	DataOutputStream dos = null;

	String server;
	String sessionId;
	String language;
	String appId = "jj";

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String toString() {
		return account;
	}
	
	@Override
	public void run() {
		try {

			// access token
			String apiKey = "5ad5e354f8d7041e0f53bdd7";
			String privateKey = "6472ac46-5b83-4471-8cd3-a76a7b5610b2";
			HttpClient accesshttpClient = new DefaultHttpClient();
			HttpPost accessTokenPost = new HttpPost("http://" + accountsHost + "/rest/acuim_v1/accesstoken");
			JsonObjectEx accessObj = new JsonObjectEx();
			accessObj.writeString("apiKey", apiKey);
			accessObj.writeString("privateKey", privateKey);
			accessTokenPost.setEntity(new StringEntity(accessObj.toString(), "utf8"));
			HttpResponse accessres = accesshttpClient.execute(accessTokenPost);
			HttpEntity accessresponseEntity = accessres.getEntity();
			String accessresult = EntityUtils.toString(accessresponseEntity);
			JsonObjectEx accessresponseObj = parseResponse(accessresult);
			JsonObjectEx accessData = accessresponseObj.getJsonObjectEx("data");
			String imApiToken = accessData.getString("imapitoken");


			// im login
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost loginPost = new HttpPost("http://" + accountsHost + "/rest/acuim_v1/login");
			loginPost.setHeader("imapitoken", imApiToken);
			JsonObjectEx loginObj = new JsonObjectEx();
			loginObj.writeString("account", account);
			loginObj.writeInteger("terminal", (int)terminal);
			loginObj.writeString("appId", appId);
			loginObj.writeString("service", service);
			loginPost.setEntity(new StringEntity(loginObj.toString(), "utf8"));
			HttpResponse res = httpClient.execute(loginPost);
			HttpEntity responseEntity = res.getEntity();
			String result = EntityUtils.toString(responseEntity);
			JsonObjectEx responseObj = parseResponse(result);
			JsonObjectEx loginData = responseObj.getJsonObjectEx("data");
			tcpHost = loginData.getString("host");
			Integer wsport = loginData.getInteger("wsport");
			Integer tcpport = loginData.getInteger("tcpport");
			server = loginData.getString("s");
			sessionId = loginData.getString("sid");


			// tcp connection
//			SSLContext sslContext = new SSLContextGenerator().getSslContext();
//			SSLSocket socket = (SSLSocket)sslContext.getSocketFactory().createSocket(tcpHost, port);
			Socket socket;
//			socket = new Socket("119.254.231.9", Integer.parseInt(upstreamPort));
			socket = new Socket(tcpHost, tcpport);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			dos = new DataOutputStream(os);
			info("server length " + server.getBytes().length);
			dos.write(server.getBytes());
			dos.flush();
			Identity identity = new Identity();
			identity.setUserId(account);
			identity.setTerminal(terminal);
			identity.setAppId(appId);
			identity.setCode(sessionId);
			identity.setSessionId(sessionId);
			identity.setDeviceToken(deviceToken);
			identity.setKey("mykey");
			identity.setService(service);
			identity.setSdkVersion(1);
			short ev = 1;
			identity.setEncodeVersion(ev);
			identity.setEncode(BinaryCodec.ENCODE_PB);
			
			HailPack pack = new HailPack(identity);
			pack.setVersion((byte) 1);
			
			dos.writeByte(pack.getVersion());
			dos.writeShort(pack.getEncodeVersion());

			dos.writeByte(pack.getType());
			dos.writeByte(pack.getEncode());
			dos.writeInt(pack.getLength());
			dos.write(pack.getContent());
		
			dos.flush();
			
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					while(true) {
//						try {
//							Thread.sleep(10000L);
//						} catch (InterruptedException e1) {
//							e1.printStackTrace();
//						}
//						try {
//							ReceivedMessage message = new ReceivedMessage();
//							message.setContentType(Topic.CONTENTTYPE_TEXT);
//							message.setClientId(ObjectId.get().toString());
////							message.setParentTopicId("123å");
//							message.setContent(new Document().append("content", "hello world[smile] " + message.getClientId()));
//							List<String> participantIds = new ArrayList<>();
//							//ucloud server
//							participantIds.add("54ba6d5ae51064b5b49ff012");
//							//local server
////							participantIds.add("548c23cd0364cb154f6c7c09");
//							message.setParticipantIds(participantIds);
//							message.setUploadResourceIds(new ArrayList<String>());
//							
//							HailPack pack = message.toHailPack(HailPack.ENCODE_JSON);
//							dos.writeInt(pack.getContent().length);
//							dos.writeShort(pack.getType());
//							dos.writeShort(pack.getEncode());
//							dos.write(pack.getContent());
//							
//							dos.flush();
//						} catch (Throwable e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}).start();
			info("identity = " + new String(pack.getContent()));
			int count = 0;
			DataInputStream dis = new DataInputStream(is);
			boolean started = true;
			byte encode = pack.getEncode();
			while(started){
//				dos.writeByte(pack.getVersion());
//				dos.writeShort(pack.getEncodeVersion());
//				
//				dos.writeByte(pack.getType());
//				dos.writeByte(pack.getEncode());
//				dos.writeInt(pack.getLength());
//				dos.write(pack.getContent());
				
//				byte version = dis.readByte();
//				short encodeVersion = dis.readShort();
				
				byte type = dis.readByte();
//				byte encode = dis.readByte();
				int length = dis.readInt();
				byte[] data1 = new byte[length];
				dis.read(data1);
				
				HailPack resultPack = new HailPack();
				resultPack.setContent(data1);
				resultPack.setEncode(encode);
				resultPack.setEncodeVersion((short) 1);
				resultPack.setLength(length);
				resultPack.setType(type);
				resultPack.setVersion((byte) 1);
				Data resultData = DataVersioning.get(resultPack);
				
				info("data == " + resultData + "; len = " + data1.length + "; type = " + type + "; encode = " + encode + "; count = " + count++);
				if(type == HailPack.TYPE_OUT_RESULT) {
					Result r = (Result) resultData;
					info("Result is code " + r.getCode() + " desp " + r.getDescription() + " forId " + r.getForId());
					if (r.getContentEncode() != null && r.getContentEncode() == HailPack.ENCODE_JSON) {
						byte[] content = r.getContent();
						String jsonString = new String(content, "utf-8");
						info("I have received result, result: " + jsonString);
					}
					if(r.getCode() == 11) {
						synchronized (this) {
							this.notify();
						}
					}
				}
//				JsonObjectEx resultObj = new JsonObjectEx(resultJson);
				
				if(type == HailPack.TYPE_OUT_OUTGOINGMESSAGE) {
					OutgoingMessage message = (OutgoingMessage) resultData;
					String id = message.getId();
					if (message.getEncode() == HailPack.ENCODE_JSON) {
						byte[] content = message.getContent();
						String jsonString = new String(content, "utf-8");
						info("I have received message, messageType: " + message.getType() + ", message: " + jsonString);
					}
					// 发送ack
					Acknowledge ack = new Acknowledge();
					ack.setService(service);
					ack.setMsgIds(new HashSet<String>(Arrays.asList(id)));
					HailPack ackPack = new HailPack(ack);
//					dos.writeByte(ackPack.getVersion());
//					dos.writeShort(ackPack.getEncodeVersion());
					
					dos.writeByte(ackPack.getType());
//					dos.writeByte(ackPack.getEncode());
					dos.writeInt(ackPack.getLength());
					dos.write(ackPack.getContent());
					dos.flush();
				}

				if (type == HailPack.TYPE_OUT_OUTGOINGDATA) {
					OutgoingData data = (OutgoingData) resultData;
					String id = data.getId();
					if (data.getEncode() == HailPack.ENCODE_JSON) {
						byte[] content = data.getContent();
						String jsonString = new String(content, "utf-8");
						info("I have received message, messageType: " + data.getType() + ", message: " + jsonString);
					}
				}
				started = true;
			}
			socket.close();

		} catch(Throwable t) {
			dos = null;
			t.printStackTrace();
			error(t);
			System.exit(-1);
		} finally {
			if(logFileWriter != null)
				try {
					logFileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public void logout() {
		HttpClient httpClient = new DefaultHttpClient();
		HttpDelete delete = new HttpDelete("http://" + tcpHost + ":" + httpPort + "/rest/apis/chat/logout");
		String cookie = "account=" + account + ";s=" + server + ";sid=" + sessionId + ";terminal=4;service=" + service;
		delete.addHeader("Cookie" , cookie);

		try {
			HttpResponse response = httpClient.execute(delete);
			HttpEntity getEntity = response.getEntity();
			String getResult = EntityUtils.toString(getEntity);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	static AtomicInteger count = new AtomicInteger(0);
	
	public void sendMessage(String service, String... userId) {
		if(dos == null)
			return;
		try {
			IncomingMessage message = new IncomingMessage();
			
			MessagePB.TextMessage.Builder builder = MessagePB.TextMessage.newBuilder();
			builder.setText("hello world " + count.incrementAndGet() + " from " + account);
			message.setContent(builder.build().toByteArray());
			message.setContentType("text");
			message.setContentEncode((int) BinaryCodec.ENCODE_PB);
			
//			message.setContent(("hello world " + count.incrementAndGet() + " from " + account).getBytes());
			message.setId(UUID.randomUUID().toString());
			Set<String> participantIds = new HashSet<>();
			for(String uid : userId) {
				participantIds.add(uid);
			}
			message.setUserIds(participantIds);
			message.setUserService(service);
			message.setService(this.service);
			message.setEncodeVersion((short) 1);
			
			HailPack pack = new HailPack(message);
//			dos.writeByte(pack.getVersion());
//			dos.writeShort(pack.getEncodeVersion());
			
			dos.writeByte(pack.getType());
//			dos.writeByte(pack.getEncode());
			dos.writeInt(pack.getLength());
			dos.write(pack.getContent());
			dos.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	public void sendTextMessageJson(String service, String... userId) {
		if(dos == null)
			return;
		try {
			IncomingMessage message = new IncomingMessage();

			String text = "hello world " + count.incrementAndGet() + " from " + account;
			Map content = new HashMap();
			content.put("text", text);
			String jsonString = JSON.toJSONString(content);
			byte[] data = jsonString.getBytes();
			message.setContent(data);
			message.setContentType("text");
			message.setContentEncode((int) BinaryCodec.ENCODE_JSON);

//			message.setContent(("hello world " + count.incrementAndGet() + " from " + account).getBytes());
			message.setId(UUID.randomUUID().toString());
			Set<String> participantIds = new HashSet<>();
			for(String uid : userId) {
				participantIds.add(uid);
			}
			message.setUserIds(participantIds);
			message.setUserService(service);
			message.setService(this.service);
			message.setEncodeVersion((short) 1);

			HailPack pack = new HailPack(message);
//			dos.writeByte(pack.getVersion());
//			dos.writeShort(pack.getEncodeVersion());

			dos.writeByte(pack.getType());
//			dos.writeByte(pack.getEncode());
			dos.writeInt(pack.getLength());
			dos.write(pack.getContent());
			dos.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void sendMessageIncomingData(String service, String... userId) {
		if(dos == null)
			return;
		try {

			IncomingData message = new IncomingData();

			MessagePB.TextMessage.Builder builder = MessagePB.TextMessage.newBuilder();
			builder.setText("hello world " + count.incrementAndGet() + " from " + account);
			message.setContent(builder.build().toByteArray());
			message.setContentType("text");
			message.setContentEncode((int) BinaryCodec.ENCODE_PB);

//			message.setContent(("hello world " + count.incrementAndGet() + " from " + account).getBytes());
			message.setId(UUID.randomUUID().toString());
			Set<String> participantIds = new HashSet<>();
			for(String uid : userId) {
				participantIds.add(uid);
			}
			message.setService(this.service);
			message.setEncodeVersion((short) 1);

			HailPack pack = new HailPack(message);
//			dos.writeByte(pack.getVersion());
//			dos.writeShort(pack.getEncodeVersion());

			dos.writeByte(pack.getType());
//			dos.writeByte(pack.getEncode());
			dos.writeInt(pack.getLength());
			dos.write(pack.getContent());
			dos.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void sendReadtimeMessageJson(String sessionId, Long readTime, String service, String... userId) {
		if(dos == null)
			return;
		try {
			IncomingMessage message = new IncomingMessage();

			Map contentMap = new HashMap();
			contentMap.put("readTime", readTime);
			contentMap.put("sessionId", sessionId);
			String jsonString = JSON.toJSONString(contentMap);
			byte[] data = jsonString.getBytes();
			message.setContent(data);
			message.setContentType("msgupread");
			message.setContentEncode((int) BinaryCodec.ENCODE_JSON);

			message.setId(UUID.randomUUID().toString());
			Set<String> participantIds = new HashSet<>();
			for(String uid : userId) {
				participantIds.add(uid);
			}
			message.setUserIds(participantIds);
			message.setUserService(service);
			message.setService(this.service);
			message.setEncodeVersion((short) 1);

			HailPack pack = new HailPack(message);
//			dos.writeByte(pack.getVersion());
//			dos.writeShort(pack.getEncodeVersion());

			dos.writeByte(pack.getType());
//			dos.writeByte(pack.getEncode());
			dos.writeInt(pack.getLength());
			dos.write(pack.getContent());
			dos.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void getHistoryMessageJson(String sessionId, Long lastMessageTime, String service, String... userId) {
		if(dos == null)
			return;
		try {
			IncomingMessage message = new IncomingMessage();

			Map contentMap = new HashMap();
			contentMap.put("lastMessageTime", lastMessageTime);
			contentMap.put("sessionId", sessionId);
			contentMap.put("limit", 10);
			contentMap.put("desc", true);
			String jsonString = JSON.toJSONString(contentMap);
			byte[] data = jsonString.getBytes();
			message.setContent(data);
			message.setContentType("msghis");
			message.setContentEncode((int) BinaryCodec.ENCODE_JSON);

			message.setId(UUID.randomUUID().toString());
			Set<String> participantIds = new HashSet<>();
			for(String uid : userId) {
				participantIds.add(uid);
			}
			message.setUserIds(participantIds);
			message.setUserService(service);
			message.setService(this.service);
			message.setEncodeVersion((short) 1);

			HailPack pack = new HailPack(message);
//			dos.writeByte(pack.getVersion());
//			dos.writeShort(pack.getEncodeVersion());

			dos.writeByte(pack.getType());
//			dos.writeByte(pack.getEncode());
			dos.writeInt(pack.getLength());
			dos.write(pack.getContent());
			dos.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void deleteSessionJson(String sessionId, String service, String... userId) {
		if(dos == null)
			return;
		try {
			IncomingMessage message = new IncomingMessage();

			Map contentMap = new HashMap();
			contentMap.put("sessionId", sessionId);
			String jsonString = JSON.toJSONString(contentMap);
			byte[] data = jsonString.getBytes();
			message.setContent(data);
			message.setContentType("usdel");
			message.setContentEncode((int) BinaryCodec.ENCODE_JSON);

			message.setId(UUID.randomUUID().toString());
			Set<String> participantIds = new HashSet<>();
			for(String uid : userId) {
				participantIds.add(uid);
			}
			message.setUserIds(participantIds);
			message.setUserService(service);
			message.setService(this.service);
			message.setEncodeVersion((short) 1);

			HailPack pack = new HailPack(message);
//			dos.writeByte(pack.getVersion());
//			dos.writeShort(pack.getEncodeVersion());

			dos.writeByte(pack.getType());
//			dos.writeByte(pack.getEncode());
			dos.writeInt(pack.getLength());
			dos.write(pack.getContent());
			dos.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void sendOrdewrMessage(String service, String... userId) {
		if(dos == null)
			return;
		try {
			IncomingMessage message = new IncomingMessage();

			MessagePB.OrderMessage.Builder builder = MessagePB.OrderMessage.newBuilder();
			builder.setText("order " + count.incrementAndGet() + " from " + account).setOrderId("21342");
			message.setContent(builder.build().toByteArray());
//			message.setContentType("text");
			message.setContentType("order");
			message.setContentEncode((int) BinaryCodec.ENCODE_PB);

//			message.setContent(("hello world " + count.incrementAndGet() + " from " + account).getBytes());
			message.setId(UUID.randomUUID().toString());
			Set<String> participantIds = new HashSet<>();
			for(String uid : userId) {
				participantIds.add(uid);
			}
			message.setUserIds(participantIds);
			message.setUserService(service);
			message.setService(this.service);
			message.setEncodeVersion((short) 1);

			HailPack pack = new HailPack(message);
//			dos.writeByte(pack.getVersion());
//			dos.writeShort(pack.getEncodeVersion());

			dos.writeByte(pack.getType());
//			dos.writeByte(pack.getEncode());
			dos.writeInt(pack.getLength());
			dos.write(pack.getContent());
			dos.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	public void sendKickMessage(String service, String... userId) {
		if(dos == null)
			return;
		try {
			IncomingMessage message = new IncomingMessage();

			MessagePB.UserKickedEvent.Builder builder = MessagePB.UserKickedEvent.newBuilder();
			builder.setCode(321);
			builder.setReason("就想踢你， 咋滴");
			message.setContent(builder.build().toByteArray());
			message.setContentType("kick");
			message.setContentEncode((int) BinaryCodec.ENCODE_PB);

			message.setId(UUID.randomUUID().toString());
			Set<String> participantIds = new HashSet<>();
			for(String uid : userId) {
				participantIds.add(uid);
			}
			message.setUserIds(participantIds);
			message.setUserService(service);
			message.setService(this.service);
			message.setEncodeVersion((short) 1);

			HailPack pack = new HailPack(message);
//			dos.writeByte(pack.getVersion());
//			dos.writeShort(pack.getEncodeVersion());

			dos.writeByte(pack.getType());
//			dos.writeByte(pack.getEncode());
			dos.writeInt(pack.getLength());
			dos.write(pack.getContent());
			dos.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
//	private void sendA(byte[] bs, Identity id) throws UnknownHostException, IOException, CoreException {
//		Socket socket;
//		socket = new Socket("localhost", 7890);
//		OutputStream os = socket.getOutputStream();
//		DataOutputStream dos = new DataOutputStream(os);
//		
//		if(bs != null)
//			dos.write(bs);
//			
//		HailPack pack = id.toHailPack(HailPack.ENCODE_JSON);
//		
//		dos.writeInt(pack.getLength());
//		dos.write(pack.getType());
//		dos.write(pack.getEncode());
//		dos.write(pack.getContent());
//		dos.flush();
//	}
	
	private StringBuffer setCookie(HttpRequestBase entity) {
		StringBuffer buffer = new StringBuffer();
		if(cookies != null) {
			for(Header cookie : cookies) {
				entity.addHeader(cookie);
				buffer.append(cookie.getName() + ": " + cookie.getValue());
			}
//			output(entity.getURI().toString(), "Cookies = " + buffer.toString());
		}
		return buffer;
	}
	
	private String head() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(new Date().toString()).append("|");
		buffer.append(account);
		buffer.append(": ");
		return buffer.toString();
	}
	
	private synchronized boolean writeFile(String str) {
		if(logPath == null)
			return false;
		if(logFileWriter == null) {
			File file = new File(FilenameUtils.concat(logPath, toString() + ".txt"));
			if(file.exists())
				FileUtils.deleteQuietly(file);
			try {
				logFileWriter = new FileWriter(file, true);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		try {
			logFileWriter.write(str + "\r\n");
			logFileWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	private void info(String str) {
		System.out.println(head() + str);
		writeFile(head() + str);
	}
	private void warn(String str, Throwable t) {
		System.out.println(head() + str + " warn " + t.getMessage());
		writeFile(head() + str + " warn " + t.getMessage());
	}
	private void error(Throwable t) {
		System.out.println(head() + "error " + t.getMessage());
		writeFile(head() + "error " + t.getMessage());
	}
	private void outputRequest(String url, String cookies) {
//		System.out.println(head() + "url " + url + " || response " + str);
		writeFile(head() + "url " + url + " || cookies " + cookies);
	}
	private void outputResponse(String url, String str) {
//		System.out.println(head() + "url " + url + " || response " + str);
		writeFile(head() + "url " + url + " || response " + str);
	}
	
	JsonObjectEx parseResponse(String str) throws Exception {
		JsonObjectEx json = new JsonObjectEx(str);
		if(json != null) {
			int code = json.getInteger("code");
			if(code == 1) {
				return json;
			} else if(code >= 4000 && code < 5000) {
				throw new IllegalAccessError("parseResponse failed, " + str);
			}
		}
		throw new Exception("parseResponse failed, " + str);
	}
	
}
