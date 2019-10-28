package deploy.xjj.im.person;

import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.utils.JsonObjectEx;
import com.dobybros.gateway.channels.data.*;
import com.dobybros.gateway.pack.HailPack;
import com.docker.rpc.BinaryCodec;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class ReceivePerson extends Person {
	
	public String toString() {
		return account;
	}
	
	@Override
	public void run() {
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://" + accountsHost + "/rest/oauth/login");
			post.setHeader("Referer", "http://" + accountsHost + "/rest/oauth/login?client_id=gateway&response_type=code&scope=*");
			JsonObjectEx loginObj = new JsonObjectEx();
			loginObj.writeString("account", account);
			loginObj.writeInteger("terminal", 1);
			loginObj.writeString("key", "asdfasdf");
			loginObj.writeString("appId", "com.ss");
			loginObj.writeString("service", "SS");
			
			post.setEntity(new StringEntity(loginObj.toString(), "utf8"));
			HttpResponse res = httpClient.execute(post);
			HttpEntity responseEntity = res.getEntity();
			
			String result = EntityUtils.toString(responseEntity);
			outputResponse(post.getURI().toString(), result);
			JsonObjectEx responseObj = parseResponse(result);
			
			JsonObjectEx sessionObj = responseObj.getJsonObjectEx("session");
//			Session session = new Session();
//			session.fromDocument(sessionObj);
			String code = sessionObj.getString("code");
			String userId = sessionObj.getString("userId");
			String uri = sessionObj.getString("redirect");
			
			HttpGet get = new HttpGet(uri + "?code=" + code + "&u=" + userId + "&t=2&m=true&p=false&d=androiddevicetoken");
			HttpResponse response = httpClient.execute(get);
			String sid = null;
			String server = null;
			Header[] setCookies = response.getHeaders("Set-Cookie");
			if(setCookies != null) {
				cookies = new Header[setCookies.length];
				for(int i = 0; i < setCookies.length; i++) {
					cookies[i] = new BasicHeader("Cookie", setCookies[i].getValue());
					String name = cookies[i].getName();
					String value = cookies[i].getValue();
					if(value != null) {
						String[] strs = value.split(";");
						if(strs.length > 0) {
							String str = strs[0];
							String[] realStrs = str.split("=");
							if(realStrs.length > 1) {
								switch(realStrs[0]) {
								case "s":
									server = realStrs[1];
									break;
								case "sid":
									sid = realStrs[1];
									break;
								}
							}
						}
					}
				}
			}
			HttpEntity getEntity = response.getEntity();
			
			String getResult = EntityUtils.toString(getEntity);
			outputResponse(get.getURI().toString(), getResult);
			JsonObjectEx getObj = parseResponse(getResult);
			Integer port = getObj.getInteger("tcpport");
			if(port == null) 
				throw new Exception("port is null, " + userId + ", " + userName);
			
			Socket socket;
//			socket = new Socket("119.254.231.9", Integer.parseInt(upstreamPort));
			socket = new Socket(tcpHost, port);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			final DataOutputStream dos = new DataOutputStream(os);
			System.out.println("server length " + server.getBytes().length);
			dos.write(server.getBytes());
			dos.flush();
			Identity identity = new Identity();
			identity.setUserId(userId);
			identity.setTerminal(Person.TERMINAL_ANDROID);
			identity.setAppId("com.ss");
//			identity.setCode(code);
			identity.setSessionId(sid);
			identity.setDeviceToken("androiddevicetoken");
			identity.setKey("mykey");
			identity.setService("SS");
			identity.setSdkVersion(1);
			identity.setEncodeVersion((short) 1);
			identity.setEncode(BinaryCodec.ENCODE_PB);
			
			HailPack pack = new HailPack(identity);
			pack.setVersion((byte) 1);
			
			dos.writeByte(pack.getVersion());
			dos.writeShort(pack.getEncodeVersion());
			dos.writeByte(pack.getEncode());
			
			dos.writeByte(pack.getType());
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
			System.out.println("identity = " + new String(pack.getContent()));
			int count = 0;
			DataInputStream dis = new DataInputStream(is);
			boolean started = true;
			byte encode = pack.getEncode();
			short encodeVersion = pack.getEncodeVersion();
			byte version = pack.getVersion();
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
//				byte encode = dis.readByte();
				
				byte type = dis.readByte();
				int length = dis.readInt();
				byte[] data1 = new byte[length];
				dis.read(data1);
				
				HailPack resultPack = new HailPack();
				resultPack.setContent(data1);
				resultPack.setEncode(encode);
				resultPack.setEncodeVersion(encodeVersion);
				resultPack.setLength(length);
				resultPack.setType(type);
				resultPack.setVersion(version);
				Data resultData = DataVersioning.get(resultPack);
				
				System.out.println("data == " + resultData + "; len = " + data1.length + "; type = " + type + "; encode = " + encode + "; count = " + count++);
				if(type == HailPack.TYPE_OUT_RESULT) {
					Result r = (Result) resultData;
					System.out.println("Result is code " + r.getCode() + " desp " + r.getDescription() + " forId " + r.getForId());
				}
//				JsonObjectEx resultObj = new JsonObjectEx(resultJson);
				
				if(type == HailPack.TYPE_OUT_OUTGOINGMESSAGE) {
					OutgoingMessage message = (OutgoingMessage) resultData;
					String id = message.getId();
					Acknowledge ack = new Acknowledge();
					ack.setMsgIds(new HashSet<String>(Arrays.asList(id)));
					HailPack ackPack = new HailPack(ack);
//					dos.writeByte(ackPack.getVersion());
//					dos.writeShort(ackPack.getEncodeVersion());
//					dos.writeByte(ackPack.getEncode());
					
					dos.writeByte(ackPack.getType());
					dos.writeInt(ackPack.getLength());
					dos.write(ackPack.getContent());
					dos.flush();
				}
			}
			socket.close();
		} catch(Throwable t) {
			t.printStackTrace();
			error(t);
		} finally {
			if(logFileWriter != null)
				try {
					logFileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
