package script.groovy.servlets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import script.groovy.object.GroovyObjectEx;
import script.groovy.runtime.ClassAnnotationHandler;
import script.groovy.runtime.GroovyRuntime;
import script.groovy.runtime.classloader.MyGroovyClassLoader;
import script.groovy.servlet.annotation.ControllerMapping;
import script.groovy.servlet.annotation.RequestMapping;
import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ChatUtils;
import chat.utils.HashTree;
public class GroovyServletManager extends ClassAnnotationHandler {
	public static final String RESPONSETYPE_JSON = "json";
	public static final String RESPONSETYPE_DOWNLOAD = "download";
	
	public static final String VARIABLE = "VARIABLE";
	public static final String IGNOREPATH = "**.";
	private static final String TAG = GroovyServletManager.class
			.getSimpleName();
	private HashTree<String, RequestURIWrapper> servletTree;
	private HashMap<String, GroovyObjectEx<RequestIntercepter>> interceptorMap;

//	private static GroovyServletManager instance;
	
	private GroovyObjectEx<PermissionIntercepter> permissionIntercepter;

	public String getIp() {
		return null;
	}

	public String getService() {
		return null;
	}

	public Integer getServiceVersion() {
		return null;
	}

	@Override
	public Object getKey() {
		return GroovyServletManager.class;
	}

	@Override
	public void handlerShutdown() {
		permissionIntercepter = null;
		interceptorMap = null;
		servletTree = null;
	}

	public GroovyServletManager() {
//		instance = this;
	}

	public void initAsDefault() {
		GroovyServletDispatcher.setDefaultGroovyServletManager(this);
	}

	public interface PermissionIntercepter {
		public void invoke(String[] perms, String method, HttpServletRequest request, HttpServletResponse response) throws CoreException;
	}

	private void handleRequestUri(String groovyPath, RequestURI requestUri, RequestURIWrapper requestUriWrapper, HashTree<String, RequestURIWrapper> tree, StringBuilder uriLogs) {
		String[] uris = requestUri.getUri();
		String requestMethod = requestUri.getMethod();
		String groovyMethod = requestUri.getGroovyMethod();
		HashTree<String, RequestURIWrapper> theTree = tree;
		if (groovyMethod != null && groovyPath != null
				&& requestMethod != null && uris != null) {
			boolean forceParsingUris = false;
			for (String uri : uris) {
				HashTree<String, RequestURIWrapper> childrenTree = null;
				if (uri.startsWith("{") && uri.endsWith("}")) {
					childrenTree = theTree.getChildren(VARIABLE, true);
					uri = uri.substring(1, uri.length() - 1);
					if(uri.startsWith(IGNOREPATH)) {
						forceParsingUris = true;
					}
					String key = VARIABLE + "_" + requestMethod;
					Object params = childrenTree.getParameter(key);
					HashSet<String> uriSet = null;
					if(params == null) {
						uriSet = new HashSet<String>();
						childrenTree.setParameter(key, uriSet);
					} else if(params instanceof HashSet) {
						uriSet = (HashSet<String>) params;
					}
					if(uriSet != null && !uriSet.contains(uri)) {
						if(!uriSet.isEmpty()) 
							LoggerEx.warn(TAG, uri + " in " + ChatUtils.toString(uris) + " is occupied by other path variables " + ChatUtils.toString(uriSet) + ", please avoid these url design, this may cause bad performance issue.");
						uriSet.add(uri);
					}
				} else {
					childrenTree = theTree.getChildren(uri, true);
				}
				theTree = childrenTree;
				if(forceParsingUris)
					break;
			}
			
			RequestURIWrapper old = theTree.get(requestMethod);
			if (old == null) {
				requestUriWrapper.setMethod(groovyMethod);
				//TODO analyze parameters here.
				theTree.put(requestMethod, requestUriWrapper);
				uriLogs.append("Mapped " + ChatUtils.toString(uris, "/") + "#" + requestMethod + ": " + groovyPath + "#" + groovyMethod + "\r\n");
			} else {
				LoggerEx.error(TAG, "The uri " + ChatUtils.toString(uris)
						+ " has already mapped on " + old.getGroovyPath()
						+ "#" + old.getMethod() + ", the newer "
						+ groovyPath + "#" + requestMethod
						+ " is given up...");
			}
		}		
	}

	public RequestHolder parseUri(HttpServletRequest request,
								  HttpServletResponse response, String[] uriStrs) throws CoreException {
		if(this.servletTree == null)
			throw new CoreException(ChatErrorCodes.ERROR_GROOVYSERVLET_SERVLET_NOT_INITIALIZED, "Groovy servlet is not ready");
		String method = request.getMethod();
		HashTree<String, RequestURIWrapper> theTree = this.servletTree;
		HashMap<String, String> parameters = null;
		boolean forceParsingUris = false;
		for (int i = 0; i < uriStrs.length; i++) {
			String uriStr = uriStrs[i];
			HashTree<String, RequestURIWrapper> children = theTree
					.getChildren(uriStr);
			if (children == null) {
				children = theTree.getChildren(VARIABLE);
				if(children != null) {
					String key = VARIABLE + "_" + method;
					Object params = children.getParameter(key);
					if(params != null && params instanceof HashSet) {
						HashSet<String> uriSet = (HashSet<String>) params;
						if (parameters == null)
							parameters = new HashMap<String, String>();
						for(String variable : uriSet) {
							if(variable.startsWith(IGNOREPATH)) {
								StringBuilder builder = new StringBuilder();
								for (int j = i; j < uriStrs.length; j++) {
									builder.append(uriStrs[j]);
									if(j != uriStrs.length - 1) {
										builder.append("/");
									}
								}
								parameters.put(variable.substring(IGNOREPATH.length()), builder.toString());
								forceParsingUris = true;
							} else {
								parameters.put(variable, uriStr);
							}
						}
					}
				}
			}
			if (children == null)
				return null;
			else
				theTree = children;
			if(forceParsingUris)
				break;
		}
		if (theTree != null) {
			RequestURIWrapper obj = theTree.get(method);
			if(obj != null) {
				GroovyObjectEx<RequestIntercepter> interceptor = null;
				if(interceptorMap != null) {
					interceptor = interceptorMap.get(obj.getGroovyPath());
				}
				return new RequestHolder(obj, request, response, parameters, interceptor, this);
			}
		}
		return null;
	}

	public RequestHolder parseUri(HttpServletRequest request,
			HttpServletResponse response) throws CoreException {
		String uri = request.getRequestURI();
		if (uri == null)
			return null;
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		String[] uriStrs = uri.split("/");
		return parseUri(request, response, uriStrs);
	}

	public HashMap<String, GroovyObjectEx<RequestIntercepter>> getInterceptorMap() {
		return interceptorMap;
	}

	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return ControllerMapping.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		GroovyRuntime groovyRuntime = getGroovyRuntime();
		if(annotatedClassMap != null && !annotatedClassMap.isEmpty()) {
			StringBuilder uriLogs = new StringBuilder("\r\n---------------------------------------\r\n");
			HashTree<String, RequestURIWrapper> tree = new HashTree<String, RequestURIWrapper>();
			HashMap<String, GroovyObjectEx<RequestIntercepter>> iMap = new HashMap<String, GroovyObjectEx<RequestIntercepter>>();
			
			Set<String> keys = annotatedClassMap.keySet();
			for (String key : keys) {
				Class<?> groovyClass = annotatedClassMap.get(key);
				RequestURI requestUri = null;
				GroovyObjectEx<GroovyServlet> groovyServlet = groovyRuntime
						.create(groovyClass);
				
//					Class<GroovyServlet> groovyClass = groovyServlet.getGroovyClass();
				if(groovyClass != null) {
					//Handle RequestIntercepting
					ControllerMapping requestIntercepting = groovyClass.getAnnotation(ControllerMapping.class);
					if(requestIntercepting != null) {
						GroovyObjectEx<RequestIntercepter> groovyInterceptor = null;
						Class<?> clazz = requestIntercepting.intercept();
						if(clazz == null || clazz.equals(Object.class)) {
							String interceptClass = requestIntercepting.interceptClass();
							if(!StringUtils.isBlank(interceptClass)) {
								groovyInterceptor = groovyRuntime
										.create(interceptClass);
							}
						} else {
							groovyInterceptor = groovyRuntime
									.create(clazz);
						}

						if(groovyInterceptor != null) {
							iMap.put(groovyServlet.getGroovyPath(), groovyInterceptor);
						}
					}
					
					//Handle RequestMapping
					Method[] methods = groovyClass.getDeclaredMethods();
					if(methods != null) {
						for(Method method : methods) {
							if(Modifier.isPublic(method.getModifiers())) {
								if(method.isAnnotationPresent(RequestMapping.class)) {
									RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
									if(requestMapping != null) {
										String uri = handleUri(requestMapping.uri(), groovyServlet, method);
										if(uri == null)
											continue;
										requestUri = new RequestURI(uri.trim(), requestMapping.method(), key, method.getName());
										
										RequestURIWrapper requestUriWrapper = new RequestURIWrapper(groovyServlet);
										requestUriWrapper.analyzeMethod(method);
										requestUriWrapper.setResponseType(requestMapping.responseType());
										requestUriWrapper.setPermissions(requestMapping.perms());
										handleRequestUri(key, requestUri, requestUriWrapper, tree, uriLogs);
//											requestUriWrapper.setGroovyObject(groovyServlet);
									}
								}
							}
						}
					}
				}
			}
			this.servletTree = tree;
			this.interceptorMap = iMap;
			uriLogs.append("---------------------------------------");
			LoggerEx.info(TAG, uriLogs.toString());
		}
	}

	public String handleUri(String uri, GroovyObjectEx<GroovyServlet> groovyServlet, Method method) {
		if(uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		return uri;
	}

	public GroovyObjectEx<PermissionIntercepter> getPermissionIntercepter() {
		return permissionIntercepter;
	}

	public void setPermissionIntercepter(GroovyObjectEx<PermissionIntercepter> permissionIntercepter) {
		this.permissionIntercepter = permissionIntercepter;
	}

	public HashTree<String, RequestURIWrapper> getServletTree() {
		return servletTree;
	}

}
