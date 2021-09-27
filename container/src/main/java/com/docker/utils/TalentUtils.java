package com.docker.utils;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TalentUtils {
	private static final String TAG = TalentUtils.class.getSimpleName();
	
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSSS");
	static {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8")); 
	}
	public static String dateString(Date date) {
		return sdf.format(date);
	}
	public static String dateString(long time) {
		return sdf.format(new Date(time));
	}
	public static String dateString() {
		return sdf.format(new Date());
	}
	
	public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
	    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
	    String query = url.getQuery();
	    if(query == null)
	    	return null;
	    String[] pairs = query.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
	    }
	    return query_pairs;
	}
	public static final String PROPERTY_PATH = "props"; 
	public static Properties loadProperty(String fileName) throws CoreException {
		ClassPathResource resource = new ClassPathResource(PROPERTY_PATH + "/" + fileName);
		Properties props  = null;
		try {
			props  = PropertiesLoaderUtils.loadProperties(resource);
			return props;
		} catch (IOException e) {
			LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(e));
			throw new CoreException(e.getMessage());
		}
	}
	
	public static String toString(String[] strs) {
		if(strs == null)
			return null;
		if(strs.length == 0)
			return "";
		StringBuffer buffer = new StringBuffer();
		for(String str : strs) {
			buffer.append(str).append(",");
		}
		return buffer.substring(0, buffer.length() - 1);
	}
	public static String[] fromString(String str) {
		if(str == null)
			return null;
		return str.split(",");
	}
	public static String toString(Collection<String> strs, String sperator) {
		if(strs == null || strs.size() == 0)
			return "";
		StringBuffer buffer = new StringBuffer();
		for(String str : strs) {
			buffer.append(str).append(sperator);
		}
		return buffer.substring(0, buffer.length() - 1);
	}
	public static String toString(Collection<String> strs) {
		return toString(strs, ",");
	}
	/*
	 * get the local host ip
	 */
	private static String getLocalHostIpPrivate(){
		try{
			InetAddress addr=InetAddress.getLocalHost();
			return addr.getHostAddress();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getLocalHostIp() {
		return getLocalHostIp(null, null);
	}
	
	public static String getLocalHostIp(String ipStartWith, String faceStartWith) {
		NetworkInterface iface = null;
		String ethr;
		String myip = null;
		
		if(ipStartWith == null && faceStartWith == null) {
			myip = getLocalHostIpPrivate();
			if(myip != null) 
				return myip;
		}
		try
		{
//			String anyIp = null;
			for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();ifaces.hasMoreElements();)
			{
				iface = (NetworkInterface)ifaces.nextElement();
				ethr = iface.getDisplayName();

				if (faceStartWith == null || ethr.startsWith(faceStartWith))
				{
					InetAddress ia = null;
					for(Enumeration<InetAddress> ips = iface.getInetAddresses();ips.hasMoreElements();)
					{
						ia = (InetAddress)ips.nextElement();
						String anyIp = ia.getHostAddress();
						if (ipStartWith == null || anyIp.startsWith(ipStartWith))
						{
							myip = ia.getHostAddress();
							return myip;
						}
					}
				}
			}
		}
		catch (SocketException e){}
		return myip;
	}
	
	public static String getRuntimePath() {
		return System.getProperty("user.dir");
	}
	
	public static String getUserLanguage() {
		return System.getProperty("user.language");
	}
	
	/*
	 * get the local host name
	 */
	public static String getLocalHostName(){
		try{
			InetAddress addr=InetAddress.getLocalHost();
			return addr.getHostName();
		}catch(Exception e){
			return "";
		}
	}
	
	public static void copyStream(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[8192]; // Adjust if you want
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}
	
	/**
	 * 该方法使用了InputStream的skip方法，效率不高，尽量不要在生产环境使用
	 * @param input
	 * @param output
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public static void copyStream(InputStream input, OutputStream output, Integer offset, Integer length)
			throws IOException {
		final int BUFFERSIZE = 8192;
		byte[] buffer = null; // Adjust if you want
		int bytesRead;
		int lengthTemp = length;
		if(offset != null && offset > 0) {
			int actualSkipped = (int) input.skip(offset);
			if(actualSkipped != offset)
				throw new IOException();
		}
		if(length < BUFFERSIZE) 
			buffer = new byte[length];
		else 
			buffer = new byte[BUFFERSIZE];
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
			lengthTemp -= bytesRead;
			if(lengthTemp == 0) 
				break;
			else if(lengthTemp < 0) 
				throw new IOException("Unexpected lengthTemp " + lengthTemp);
			else if(lengthTemp < buffer.length) 
				buffer = new byte[lengthTemp];
		}
	}

	public static String[] joinStringArray(String[] array, String[] t) {
		if(array == null && t == null)
			return new String[0];
		else if (array == null && t != null)
			return t;
		else if (array != null && t == null)
			return array;
		String[] newArray = new String[array.length + t.length];
		System.arraycopy(array, 0, newArray, 0, array.length);
		System.arraycopy(t, 0, newArray, array.length, t.length);
		return newArray;
	}
	
	public static Collection<String> joinStringArray(Collection<String> array, String t) {
		if(array == null)
			return null;
		if(t == null)
			return array;
		Collection<String> strs = new ArrayList<>();
		strs.addAll(array);
		strs.add(t);
		return strs;
	}
	public static List<String> joinStringList(String[] array, String t) {
		if(array == null)
			return null;
		List<String> strs = new ArrayList<>();
		strs.addAll(Arrays.asList(array));
		strs.add(t);
		return strs;
	}
	public static String[] joinStringArray(String[] array, String t) {
		if(array == null)
			return null;
		if(t == null)
			return array;
		String[] newArray = new String[array.length + 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[newArray.length - 1] = t;
		return newArray;
	}
	
	public static String formatSpringSensitiveString(String str) {
		if(str == null)
			return null;
		return str.replaceAll("[/\\\\;:]", "_").replace("<", "(").replace(">", ")");
	}
	
	private static String specialCharacters = "^()./+[]-"; 
	private static String s3specialCharacters = "~"; 
	private static String fileNameSpecialCharacters = "*:?<>|^()./+[]-\""; 
	public static String formatForRegularExpression(String str) {
		if(str == null)
			return "";
		str = str.replace("\\", "\\\\");
		for(int i = 0; i < specialCharacters.length(); i++) {
			String c = specialCharacters.substring(i, i + 1);
			str = str.replace(c, "\\" + c);
		}
		return str;
	}
	public static String formatAsRegularExpressionForName(String str) {
		if(str == null)
			return "";
		str = str.replace("\\", "\\\\");
		for(int i = 0; i < fileNameSpecialCharacters.length(); i++) {
			String c = fileNameSpecialCharacters.substring(i, i + 1);
			str = str.replace(c, "_");
		}
		return str;
	}
	public static String formatS3Name(String str) {
		if(str == null)
			return "";
		for(int i = 0; i < s3specialCharacters.length(); i++) {
			String c = s3specialCharacters.substring(i, i + 1);
			str = str.replace(c, "_");
		}
		return str;
	}
	
	public static String formatTxt2Html(String str) {
		if (str == null || str.trim().length() == 0) return str;
		return str.replaceAll("(\r\n|\n\r|\r|\n)", "<br/>").replaceAll("(\t)", "&nbsp; &nbsp; ").replaceAll("   ", "&nbsp; &nbsp;").replaceAll("  ", "&nbsp; ");
	}
	
	public static String formatSecureString(String str) {
		if (str == null || str.trim().length() == 0) return str;
		return str.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	public static void destroyProcess(Process process) {
		if(process == null)
			return;
		process.destroy();
		while(true) {
			try {
				process.exitValue();
				process = null;
				break;
			} catch(IllegalThreadStateException e) {
				try {
					Thread.sleep(200L);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				LoggerEx.debug(TAG, "Waiting for process really destroyed. " + process);
			}
		}		
	}
	
	public static boolean isProcessExist(String name) throws IOException {
		String[] command = new String[]{"tasklist", "/fo", "table", "/nh"};
		ProcessBuilder pb = new ProcessBuilder(command);
		Process tasks = pb.start();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(tasks.getInputStream()));
		String info = null;
		while ((info = reader.readLine()) != null) {
			String pname = info.split(" ")[0];
			
			if (pname.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	public static String escapeAngleBracket(String asString) {
		return asString == null ? null : asString.replaceAll("<", "&lt;");
				//replaceAll(">", "&gt;");
	}

	public static String filterCarriageAndWrapChar(String wh) {
		return wh == null ? null : wh.replaceAll("(\r\n|\n\r|\r|\n)", "");
	}

    public static String getLocaleMessage(String msgKey, HttpServletRequest request){
        return getLocaleMessage(msgKey, null, request);
    }
    
    public static String getLocaleMessage(String msgKey, String[] parameters, HttpServletRequest request){
        if(request == null) {
            return getI18NMessage(msgKey, parameters, "");
        } else {
            HttpSession httpSession = request.getSession(false);
            String localeStr = null;
            if (httpSession == null)
                localeStr = getLocalFromRequest(request);
            else
                localeStr = (String)httpSession.getAttribute(CoreHttpConstants.SESSION_LOCALE);
            return getI18NMessage(msgKey, parameters, localeStr);
        }
    }
    
    public static String getI18NMessageEx(String message, String localeStr) throws CoreException {
    	String[] messageArray = message.split("#");
    	String msgKey = messageArray[0];
    	String[] nnnStrings = new String[messageArray.length - 1];
    	System.arraycopy(messageArray, 1, nnnStrings, 0, messageArray.length - 1);
    	String result = getI18NMessage(msgKey, nnnStrings, localeStr);
		return result;
	}
    
    private static ResourceBundle getBundle(Locale locale) {
    	ResourceBundle bundle = null;
    	try {
    		bundle = ResourceBundle.getBundle("message", locale);
		} catch (Exception e) {
		}
    	return bundle;
    }
    
    public static String getI18NMessage(String msgKey, Object[] parameters, String localeStr){
        ResourceBundle bundle = null;
        if (StringUtils.isBlank(localeStr)) {
            bundle = getBundle(Locale.ENGLISH);
        } else {
			Locale locale = new Locale(localeStr);

			try {
				bundle = getBundle(locale);
			} catch (Exception e) {
				e.printStackTrace();
				LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(e));
				bundle = getBundle(Locale.ENGLISH);
			}
        }
        
        try{
            String value = bundle.getString(msgKey);
//原来多语言的更换字符标是${}，后发现系统有自己的一套更换机制，如下所示：
            String result = MessageFormat.format(value, parameters);
//            if(value != null && parameters != null) {
//                for(int i = 0; i < parameters.length; i++) {
//                    String key = "${" + i + "}";
//                    if(parameters[i] != null) 
//                    	value = value.replace(key, parameters[i]);
//                }
//            }
            return result;
        } catch(MissingResourceException e){
            LoggerEx.error(TAG, "Missing resource " + msgKey);
            return msgKey;
        }
    }
    
    public static String getLocalFromRequest(HttpServletRequest request) {
        String locale = request.getParameter(CoreHttpConstants.SESSION_LOCALE);
        if (locale == null) {
            Cookie[] cookies = request.getCookies();
            if(cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(CoreHttpConstants.SESSION_LOCALE)) {
                        locale = cookie.getValue();
                        break;
                    }
                }
            }
        } 
        return locale;
    }
	
}