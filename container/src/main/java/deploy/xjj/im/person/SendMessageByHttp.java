package deploy.xjj.im.person;

import com.alibaba.fastjson.JSONObject;
import com.dobybros.chat.utils.JsonObjectEx;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SendMessageByHttp {
	DataOutputStream dos = null;

	public static final String HOST = "http://xjj";

	public static final void main(String[] args) {

		try {
			// access token
			String apiKey = "5ad5e354f8d7041e0f53bdd7";
			String privateKey = "6472ac46-5b83-4471-8cd3-a76a7b5610b2";
			HttpClient accesshttpClient = new DefaultHttpClient();
			HttpPost accessTokenPost = new HttpPost(HOST + "/rest/acuim/accesstoken");
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


			// send message
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost loginPost = new HttpPost(HOST + "/rest/acuim/message");
			loginPost.setHeader("imapitoken", imApiToken);
//			JsonObjectEx sendObj = new JsonObjectEx();
//			sendObj.writeString("id", "messageId1");
//			sendObj.writeString("clientId", "001");
//			JsonArray array = new JsonArray();
//			array.add("5c46c4f140402121f4c45365");
//			sendObj.writeJsonArray("receiverIds", array);
//			sendObj.writeString("receiverService", "gwtuitionroom");
//			sendObj.writeString("userId", "test1");
//			sendObj.writeString("service", "gwtuitionroom");
//			sendObj.writeString("type", "text");

			JSONObject json = new JSONObject();
			json.put("id", "messageId1");
			json.put("clientId", "001");
			List array = new ArrayList();
			array.add("5c46c4f140402121f4c45365");
			json.put("receiverIds", array);
			json.put("receiverService", "gwtuitionroom");
			json.put("userId", "test1");
			json.put("service", "gwtuitionroom");
			json.put("type", "text");
			JSONObject jsonc = new JSONObject();
			jsonc.put("content", "This is an apple!");
			json.put("data", jsonc.toJSONString().getBytes());
			loginPost.setEntity(new StringEntity(json.toJSONString(), "utf8"));
			HttpResponse res = httpClient.execute(loginPost);
			HttpEntity responseEntity = res.getEntity();
			String result = EntityUtils.toString(responseEntity);
			info(result);
		} catch (Throwable t) {

		}
	}

	private static void info(String str) {
		System.out.println(str);
	}
	private void warn(String str, Throwable t) {
		System.out.println(str + " warn " + t.getMessage());
	}
	private void error(Throwable t) {
		System.out.println("error " + t.getMessage());
	}
	
	static JsonObjectEx parseResponse(String str) throws Exception {
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
