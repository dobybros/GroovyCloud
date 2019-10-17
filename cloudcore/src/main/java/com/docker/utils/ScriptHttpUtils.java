package com.docker.utils;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.json.Result;
import chat.logs.LoggerEx;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreConnectionPNames;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by lick on 2019/5/30.
 * Description：
 */
public class ScriptHttpUtils {
    private static final String TAG = ScriptHttpUtils.class.getSimpleName();
    public static Result post(String data, String url, Map headers, Class c) {
        HttpPost post = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间(单位毫秒)
                    .setConnectTimeout(5000)
                    // 设置请求超时时间(单位毫秒)
                    .setConnectionRequestTimeout(5000)
                    // socket读写超时时间(单位毫秒)
                    .setSocketTimeout(5000)
                    // 设置是否允许重定向(默认为true)
                    .setRedirectsEnabled(true).build();
            post = new HttpPost(url);
            post.setConfig(requestConfig);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            if(headers != null && !headers.isEmpty()){
                for (Object key : headers.keySet()){
                    post.setHeader(key.toString(), headers.get(key).toString());
                }
            }
            // 构建消息实体
            StringEntity entity = new StringEntity(data, Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            post.setEntity(entity);

            response = httpClient.execute(post);
            int code = response.getStatusLine().getStatusCode();
            if (code == 200) {
                HttpEntity responseEntity = response.getEntity();
                String str = IOUtils.toString(responseEntity.getContent());
                Result result = (Result) JSON.parseObject(str, c);
                if (result != null && result.getCode() == 1) {
                    return result;
                } else {
                    throw new CoreException(ChatErrorCodes.ERROR_POST_FAILED, "Connect to server failed, " + result.getMsg());
                }
            } else {
                throw new CoreException(ChatErrorCodes.ERROR_POST_FAILED, "Connect to server http failed, " + code);
            }

        } catch (Exception e) {
            LoggerEx.error(TAG, "Http post failed, err: " + ExceptionUtils.getFullStackTrace(e));
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Result get(String url, Class c) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间(单位毫秒)
                    .setConnectTimeout(5000)
                    // 设置请求超时时间(单位毫秒)
                    .setConnectionRequestTimeout(5000)
                    // socket读写超时时间(单位毫秒)
                    .setSocketTimeout(5000)
                    // 设置是否允许重定向(默认为true)
                    .setRedirectsEnabled(true).build();
            get.setConfig(requestConfig);
            response = httpClient.execute(get);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity responseEntity = response.getEntity();
                String str = IOUtils.toString(responseEntity.getContent());
                Result result = (Result) JSON.parseObject(str, c);
                if (result != null && result.getCode() == 1) {
                    return result;
                } else {
                    throw new CoreException(ChatErrorCodes.ERROR_GET_FAILED, "Connect to server failed, " + result.getMsg() + "url: " + url);

                }
            }else {
                throw new CoreException(ChatErrorCodes.ERROR_GET_FAILED, "Connect to server failed, url: " + url);
            }
        } catch (Exception e) {
            LoggerEx.error(TAG, "Http get failed, the url is unavailable,will retry 60s again,url: " + url + ", err: " + ExceptionUtils.getFullStackTrace(e));
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
