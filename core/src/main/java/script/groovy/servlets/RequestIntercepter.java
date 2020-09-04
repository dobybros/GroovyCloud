package script.groovy.servlets;

import chat.errors.CoreException;
import script.groovy.object.GroovyObjectEx;

import java.io.IOException;

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
//			t.printStackTrace();
			try {
				invokeError(t, holder);
			} catch (Throwable t1) {
				t1.printStackTrace();
			}
		}finally {
			try {
				if(holder.getRequest().getInputStream() != null){
					try {
						holder.getRequest().getInputStream().close();
					}catch (Throwable t){
						t.printStackTrace();
					}
				}
				if(holder.getResponse().getOutputStream() != null){
					try {
						holder.getResponse().getOutputStream().close();
					}catch(Throwable t){
						t.printStackTrace();
					}
				}
			}catch (IOException e){
				e.printStackTrace();
			}
		}

	}
	
	public abstract void invoke(RequestHolder holder) throws CoreException;
	public abstract void invokeError(Throwable t, RequestHolder holder);
}