package script.groovy.servlets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import script.groovy.object.GroovyObjectEx;
import script.groovy.object.GroovyObjectEx.GroovyObjectListener;
import script.groovy.servlet.annotation.PathVariable;
import script.groovy.servlet.annotation.RequestHeader;
import script.groovy.servlet.annotation.RequestParam;
import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ChatUtils;

public class RequestURIWrapper implements GroovyObjectListener{
	private static final String TAG = RequestURIWrapper.class.getSimpleName();
	private GroovyObjectEx<GroovyServlet> groovyObject;
	private String method;
	private String responseType;
	private LinkedHashMap<Parameter, HandleRequest> parameters = new LinkedHashMap<>();
	private String[] permissions;
	
	public static interface HandleRequest {
	}
	
	private static Map<Class<? extends Annotation>, HandleRequestAnnotation> annotationHandlerMap = new HashMap<>();
	public static interface HandleRequestAnnotation extends HandleRequest{
		public Object handle(Parameter param, Annotation annotation, RequestHolder requestHolder) throws CoreException;
		public Class<? extends Annotation> getAnnotationClass();
	}
	static {
		annotationHandlerMap.put(RequestParam.class, new RequestParamHandler());
		annotationHandlerMap.put(RequestHeader.class, new RequestHeaderHandler());
		annotationHandlerMap.put(PathVariable.class, new PathVariableHandler());
	}
	
	private static Map<Class<?>, HandleRequestParameterClass> paramClassHandlerMap = new HashMap<>();
	public static interface HandleRequestParameterClass extends HandleRequest {
		public Object handle(Parameter param, RequestHolder requestHolder) throws CoreException;
	}
	static {
		paramClassHandlerMap.put(HttpServletRequest.class, new HttpServletRequestHandler());
		paramClassHandlerMap.put(HttpServletResponse.class, new HttpServletResponseHandler());
		paramClassHandlerMap.put(RequestHolder.class, new RequestHolderHandler());
	}
	
	public static class HttpServletRequestHandler implements HandleRequestParameterClass {
		@Override
		public Object handle(Parameter param, RequestHolder requestHolder) throws CoreException {
			return requestHolder.getRequest();
		}
	}
	
	public static class HttpServletResponseHandler implements HandleRequestParameterClass {
		@Override
		public Object handle(Parameter param, RequestHolder requestHolder) throws CoreException {
			return requestHolder.getResponse();
		}
	}
	
	public static class RequestHolderHandler implements HandleRequestParameterClass {
		@Override
		public Object handle(Parameter param, RequestHolder requestHolder) throws CoreException {
			return requestHolder;
		}
	}
	
	public static class RequestParamHandler implements HandleRequestAnnotation {
		@Override
		public Object handle(Parameter param, Annotation annotation,
				RequestHolder requestHolder) throws CoreException {
			RequestParam requestParam = (RequestParam) annotation;
			String key = requestParam.key();
			String value = null;
			if(!StringUtils.isBlank(key)) {
				value = requestHolder.getParam(key, requestParam.required());
//				if(value == null) {
//					value = requestParam.defaultValue();
//				}
			}
			Class<?> typeClass = param.getType();
			if(value != null) {
				return ChatUtils.typeCost(value, typeClass);
			}
			return value;
		}
		
		@Override
		public Class<? extends Annotation> getAnnotationClass() {
			return RequestParam.class;
		}
	}
	
	public static class RequestHeaderHandler implements HandleRequestAnnotation {
		@Override
		public Object handle(Parameter param, Annotation annotation,
				RequestHolder requestHolder) throws CoreException {
			RequestHeader requestHeader = (RequestHeader) annotation;
			String key = requestHeader.key();
			String value = null;
			if(!StringUtils.isBlank(key)) {
				HttpServletRequest request = requestHolder.getRequest();
				value = request.getHeader(key);
				if(value == null && requestHeader.required()) {
					throw new CoreException(ChatErrorCodes.ERROR_URL_HEADER_NULL, "URL header " + key + " is required but null.");
				}
			}
			Class<?> typeClass = param.getType();
			if(value != null) {
				return ChatUtils.typeCost(value, typeClass);
			}
			return value;
		}
		@Override
		public Class<? extends Annotation> getAnnotationClass() {
			return RequestHeader.class;
		}
	}
	
	public static class PathVariableHandler implements HandleRequestAnnotation {
		@Override
		public Object handle(Parameter param, Annotation annotation,
				RequestHolder requestHolder) throws CoreException {
			PathVariable pathVariable = (PathVariable) annotation;
			String value = requestHolder.getPathVariable(pathVariable.key(), true);
			Class<?> typeClass = param.getType();
			return ChatUtils.typeCost(value, typeClass);
		}
		@Override
		public Class<? extends Annotation> getAnnotationClass() {
			return PathVariable.class;
		}
	}

	public RequestURIWrapper(GroovyObjectEx<GroovyServlet> object) {
		groovyObject = object;
	}
	
	public String getMethod() {
		return method; 
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
	public void analyzeMethod(Method method) {
		if(method != null) {
			Parameter[] parameters = method.getParameters();
			for(int i = 0; i < parameters.length; i++) {
				boolean handled = false;
				Parameter param = parameters[i];
				Class<?> typeClass = param.getType();
				Annotation[] annotations = param.getAnnotations();
				if(annotations != null && annotations.length > 0) {
					for(Annotation annotation : annotations) {
						Class<? extends Annotation> annotationClass = null;
						if(annotation instanceof Proxy) {
							Proxy proxy = (Proxy) annotation;
							InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
							if(invocationHandler != null) {
								try {
									Field typeField = invocationHandler.getClass().getDeclaredField("type");
									typeField.setAccessible(true);
									annotationClass = (Class<? extends Annotation>) typeField.get(invocationHandler);
								} catch (NoSuchFieldException
										| SecurityException | IllegalArgumentException | IllegalAccessException e) {
									e.printStackTrace();
									LoggerEx.error(TAG, "Get annotationClass for method " + method + " failed, " + e.getMessage());
								}
							}
						} else {
							annotationClass = annotation.getClass();
						}
						HandleRequestAnnotation annotationHandler = annotationHandlerMap.get(annotationClass);
						if(annotationHandler != null) {
							this.parameters.put(param, annotationHandler);
							handled = true;
							break;
						}
					}
				} else {
					HandleRequestParameterClass paramClassHandler = paramClassHandlerMap.get(typeClass);
					if(paramClassHandler != null) {
						this.parameters.put(param, paramClassHandler);
						handled = true;
					}
				}
				if(!handled) {
					this.parameters.put(param, null);
				}
			}
		}
 	}

	public Object[] getActualParameters(RequestHolder requestHolder) throws CoreException {
		Set<Parameter> parameters = this.parameters.keySet();
		Object[] args = new Object[parameters.size()];
		int i = 0;
		for(Parameter param : parameters) {
			HandleRequest handler = this.parameters.get(param);
			if(handler != null) {
				if(handler instanceof HandleRequestAnnotation) {
					HandleRequestAnnotation annotationHandler = (HandleRequestAnnotation) handler;
					Annotation annotation = param.getAnnotation(annotationHandler.getAnnotationClass());
					args[i] = annotationHandler.handle(param, annotation, requestHolder);
				} else if(handler instanceof HandleRequestParameterClass) {
					args[i] = ((HandleRequestParameterClass) handler).handle(param, requestHolder);
				}
			} else {
				RequestHolder.ParameterHandler parameterHandler = requestHolder.getParameterHandler();
				if(parameterHandler != null) {
					args[i] = parameterHandler.valueForParameter(param);
				}
			}
			i++;
		}
		return args;
	}
	
	public GroovyObjectEx<GroovyServlet> getGroovyObject() {
		return groovyObject;
	}

	public void setGroovyObject(GroovyObjectEx<GroovyServlet> groovyObject) {
		this.groovyObject = groovyObject;
	}
	
	public String getGroovyPath() {
		return this.groovyObject.getGroovyPath();
	}
	
//	public GroovyServlet getObject() throws CoreException {
//		Object obj = this.groovyObject.getObject();
//		if(obj == null) 
//			return null;
//		if(obj instanceof GroovyServlet) {
//			GroovyServlet groovyServlet = (GroovyServlet) obj;
//			groovyServlet.groovyRuntime = groovyObject.getGroovyRuntime();
//			return groovyServlet;
//		} else
//			throw new CoreException(ChatErrorCodes.ERROR_GROOY_CLASSCAST, "Must implement GroovyServlet for " + obj.getClass().getName());
//	}

	@Override
	public void objectPrepared(Object obj) throws CoreException {
		if(obj instanceof GroovyServlet) {
			GroovyServlet groovyServlet = (GroovyServlet) obj;
//			groovyServlet.groovyRuntime = groovyObject.getGroovyRuntime();
		} else
			throw new CoreException(ChatErrorCodes.ERROR_GROOY_CLASSCAST, "Must implement GroovyServlet for " + obj.getClass().getName());
	}

	public String getResponseType() {
		return responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public String[] getPermissions() {
		return permissions;
	}

	public void setPermissions(String[] permissions) {
		if(permissions != null && permissions.length > 0) {
			if (permissions.length == 1 && permissions[0].equals("")) {
				return;
			}
			this.permissions = permissions;
		}
	}
}