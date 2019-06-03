package script.groovy.runtime;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

import chat.utils.ConcurrentHashSet;
import script.groovy.annotation.RedeployMain;
import script.groovy.object.GroovyObjectEx;
import chat.logs.LoggerEx;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

public class GroovyRedeployMainHandler extends ClassAnnotationHandler {

	private static final String TAG = GroovyRedeployMainHandler.class.getSimpleName();
	private ConcurrentHashSet<GroovyObjectEx> redeploySet = new ConcurrentHashSet<>();
	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return RedeployMain.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if(annotatedClassMap != null) {
			ConcurrentHashSet<GroovyObjectEx> newRedeploySet = new ConcurrentHashSet<>();
			Collection<Class<?>> values = annotatedClassMap.values();
			for(Class<?> groovyClass : values) {
				GroovyObjectEx<?> groovyObj = getGroovyRuntime().create(groovyClass);
				try {
					groovyObj.invokeRootMethod("main");
				} catch (Throwable t) {
					t.printStackTrace();
					LoggerEx.warn(TAG, "Execute redeploy main for " + groovyClass + " failed, " + t.getMessage());
				}
				newRedeploySet.add(groovyObj);
			}
			redeploySet = newRedeploySet;
		}
	}

	@Override
	public void handlerShutdown() {
		for(GroovyObjectEx<?> obj : redeploySet) {
			try {
				obj.invokeRootMethod("shutdown");
			} catch (Throwable e) {
//				e.printStackTrace();
				LoggerEx.warn(TAG, "Execute redeploy shutdown for " + obj.getGroovyPath() + " failed, " + e.getMessage());
			}
		}
	}
}
