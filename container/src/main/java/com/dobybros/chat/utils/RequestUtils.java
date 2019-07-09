package com.dobybros.chat.utils;

import chat.errors.CoreException;
import chat.logs.LoggerEx;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class RequestUtils {
    final static String SID = "aclsid";
	public static String getRemoteIp(HttpServletRequest request) {
		String remoteAddr = request.getHeader("X-Real-IP");
		if(remoteAddr == null)
			remoteAddr = request.getRemoteAddr();
		return remoteAddr;
	}
	
	public static String getParameter(HttpServletRequest request, String key) {
		String parameter = request.getParameter(key);
		if(parameter == null) {
			parameter = request.getHeader(key);
		}
		if(parameter == null)
			return null;
		return parameter;
	}
	
	public static Integer getHeaderInteger(HttpServletRequest request, String key) {
		try {
			String parameter = request.getHeader(key);
			if(parameter != null)
				return Integer.parseInt(parameter);
		} catch (NumberFormatException e) {
			LoggerEx.error("RequestsUtils", "version parameter is wrong.");
			e.printStackTrace();
		}
		return null;
	}
	
	public static Boolean getParameterBoolean(HttpServletRequest request, String key) {
		String parameter = getParameter(request, key);
		if(parameter != null)
			return Boolean.parseBoolean(parameter);
		return null;
	}
	
	public static Integer getParameterInteger(HttpServletRequest request, String key) {
		String parameter = getParameter(request, key);
		if(parameter != null)
			return Integer.parseInt(parameter);
		return null;
	}
	
	public static Long getParameterLong(HttpServletRequest request, String key) {
		String parameter = getParameter(request, key);
		if(parameter != null)
			return Long.parseLong(parameter);
		return null;
	}
	
	public static int[] getWHFromFieldName(String fieldName) {
		if(fieldName == null)
			return null;
		String[] strs = fieldName.split("_");
		if(strs.length == 3) {
			int[] wh = new int[2];
			try {
				wh[0] = Integer.parseInt(strs[1]);
				wh[1] = Integer.parseInt(strs[2]);
				return wh;
			} catch(NumberFormatException e) {
			}
		}
		return null;
	}
	public static String getHost(HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		String serverName = request.getServerName();
		int pos = url.indexOf(serverName);
		if(pos == -1) {
			return null;
		}
		String fromHost = url.substring(0, pos + serverName.length());
		return fromHost;
	}
	
	/**
	 * unfinished.
	 * 
	 * @param request
	 * @return
	 */
	public static String getSubDomain(HttpServletRequest request) {
		String serverName = request.getServerName();
		int pos = serverName.indexOf(".");
		if(pos == -1) {
			return null;
		}
		String domain = serverName.substring(0, pos);
		return domain;
	}
	
	public static String getSid(HttpServletRequest request){
	    HttpSession session = request.getSession(false);
	    if (session != null) {
	        return session.getId();
	    }
		String sid = request.getParameter(SID);
		if(sid == null){
			sid = request.getHeader(SID);
		}
		if (sid == null) {
		    Cookie[] cookies = request.getCookies();
		    if (cookies != null) {
    		    for (Cookie cookie : cookies) {
    		        if (cookie.getName().equals(SID)) {
    		            return cookie.getValue();
    		        }
    		    }
		    }
		}
		return sid;
	}
	
	public static boolean exceedUploadSize(HttpServletRequest request, int max) {
		int length = request.getContentLength();
		if(length == -1)
			length = Integer.MAX_VALUE;
		if(length > max) {
			return false;
		}
		return true;
	}
	
	public static HttpSession getSession(HttpServletRequest request) {
		if(request == null)
			return null;
		HttpSession session = request.getSession(false);
		return session;
	}
	public static String getFirmId(HttpServletRequest request) {
		HttpSession session = getSession(request);
		if(session != null) {
			String firmId = (String) session.getAttribute(CoreHttpConstants.SESSION_FIRMID);
			return firmId;
		}
		return null;
	}
	public static String getFirmDomain(HttpServletRequest request) {
		HttpSession session = getSession(request);
		if(session != null) {
			String domain = (String) session.getAttribute(CoreHttpConstants.SESSION_DOMAIN);
			return domain;
		}
		return null;
	}
	public static String getTerminal(HttpServletRequest request) {
		String terminal = null;
		HttpSession session = getSession(request);
		if(session != null) {
			terminal = (String) session.getAttribute(CoreHttpConstants.SESSION_TERMINAL);
		}
		if(terminal == null)
			terminal = request.getHeader(CoreHttpConstants.SESSION_TERMINAL);
		return terminal;
	}
	
	public static String getUserId(HttpServletRequest request) {
		HttpSession session = getSession(request);
		if(session != null) {
			String userId = (String) session.getAttribute(CoreHttpConstants.SESSION_USERID);
			return userId;
		} else {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("aclaccount")) {
						return cookie.getValue();
					}
				}
			}
		}
		return null;
	}
	
	public static Integer[] offsetLimitHandler(Integer offset, Integer limit, int defaultLimit, int defaultOffset, int maxRecords) throws CoreException {
		if(offset == null)
			offset = defaultOffset;
		if(limit == null)
			limit = defaultLimit;
		if(offset >= maxRecords)
			return null;
//			throw new CoreException(CoreErrorCodes.ERROR_EXCEEDED_MAX_RECORDS, "Exceeded the max records : " + maxRecords);
		if(offset + limit >= maxRecords) {
			limit = maxRecords - offset;
		}
		return new Integer[]{offset, limit};
	}
	
}
