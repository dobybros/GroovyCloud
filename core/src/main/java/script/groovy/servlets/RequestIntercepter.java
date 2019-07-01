package script.groovy.servlets;

import script.groovy.object.GroovyObjectEx;
import chat.errors.CoreException;

public abstract class RequestIntercepter {
	protected Object proceed(RequestHolder holder) throws CoreException {
		RequestURIWrapper requestUriWrapper = holder.getRequestUriWrapper();
		GroovyObjectEx<GroovyServlet> servletObj = requestUriWrapper.getGroovyObject();
		String groovyMethod = requestUriWrapper.getMethod();
//		servletObj.invokeMethod(groovyMethod, holder);
		return holder.invokeMethod(groovyMethod, servletObj);
	}
	
	void invokeInternal(RequestHolder holder) {
		try {
			invoke(holder);
		} catch (Throwable t) {
			t.printStackTrace();
			try {
				invokeError(t, holder);
			} catch (Throwable t1) {
				t1.printStackTrace();
			}
		}
	}
	
	public abstract void invoke(RequestHolder holder) throws CoreException;
	public abstract void invokeError(Throwable t, RequestHolder holder);
}