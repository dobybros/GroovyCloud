package script.groovy.servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import chat.errors.CoreException;
import chat.logs.LoggerEx;

public class GroovyServletDispatcher extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7785123445517306608L;
	private static final String TAG = GroovyServletManager.class.getSimpleName();
	private static GroovyServletManager groovyServletManager = null;

	private static ConcurrentHashMap<String, GroovyServletManager> groovyServletMap = new ConcurrentHashMap<>();

	public static void setDefaultGroovyServletManager(GroovyServletManager servletManager) {
		groovyServletManager = servletManager;
	}

	public static void addGroovyServletManagerEx(String rootPath, GroovyServletManager servletManager) {
		GroovyServletManager oldGroovyServletMap = groovyServletMap.put(rootPath, servletManager);
		//TODO whether need clear memory for oldGroovyServletMap
	}

	public static void removeGroovyServletManagerEx(String rootPath) {
		groovyServletMap.remove(rootPath);
	}

	private void servletDispatch(HttpServletRequest request, HttpServletResponse response) {
		try {
			RequestHolder holder = null;
			String uri = request.getRequestURI();
			LoggerEx.info(TAG, "RequestURI " + uri + " method " + request.getMethod() + " from " + request.getRemoteAddr());
			if (uri.startsWith("/")) {
				uri = uri.substring(1);
			}
			String[] uriStrs = uri.split("/");
			String matchStr = uriStrs.length > 1 ? uriStrs[1] : null;
			if(matchStr != null) {
				GroovyServletManager servletManagerEx = groovyServletMap.get(matchStr);
				if(servletManagerEx != null) {
					int index = matchStr.lastIndexOf("_v");
					if(index > 0)
						uriStrs[1] = matchStr.substring(0, index);
					holder = servletManagerEx.parseUri(request, response, uriStrs);
				}
			}
			if(holder == null) {
				if(groovyServletManager != null) {
					holder = groovyServletManager.parseUri(request, response, uriStrs);
				} else {
					LoggerEx.error(TAG, "No handler for uri " + request.getRequestURI() + " method " + request.getMethod() + " from " + request.getRemoteAddr());
				}
			}
			if(holder == null) {
				try {
					response.sendError(404, "Url not found");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				holder.handleRequest();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				response.sendError(500, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doHead(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doOptions(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
	
	@Override
	public void doTrace(HttpServletRequest request, HttpServletResponse response) {
		servletDispatch(request, response);
	}
}
