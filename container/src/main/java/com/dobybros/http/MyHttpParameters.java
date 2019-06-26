package com.dobybros.http;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;

public class MyHttpParameters extends BasicHttpParams {  
  
    public MyHttpParameters() {  
        setSocketTimeout(120 * 1000);  
        setCharset("UTF8");  
        setConnectionTimeout(120 * 1000);  
    }  
    public void setConnectionTimeout(int i) {  
        setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, i);  
    }  
  
    public void setCharset(String string) {  
        setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, string);  
    }  
    public void setSocketTimeout(int t) {  
        setParameter(CoreConnectionPNames.SO_TIMEOUT, t);  
    }  
  
    private static final long serialVersionUID = 1L;  
} 