package script.groovy.runtime;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import script.groovy.annotation.TimerTask;
import script.groovy.object.GroovyObjectEx;
import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import script.groovy.runtime.classloader.MyGroovyClassLoader;

public class GroovyTimerTaskHandler extends ClassAnnotationHandler {

	private static final String TAG = GroovyTimerTaskHandler.class.getSimpleName();
	private ConcurrentHashMap<String, MyTimerTask> timerTasks;

	@Override
	public void handlerShutdown() {
		if(timerTasks != null) {
			Collection<MyTimerTask> theTimerTasks = timerTasks.values();
			for(MyTimerTask timerTask : theTimerTasks) {
				timerTask.cancel();
			}
			timerTasks.clear();
		}
	}

	@Override
	public Class<? extends Annotation> handleAnnotationClass(GroovyRuntime groovyRuntime) {
		return TimerTask.class;
	}

	@Override
	public void handleAnnotatedClasses(Map<String, Class<?>> annotatedClassMap,
			MyGroovyClassLoader classLoader) {
		if(annotatedClassMap != null) {
			ConcurrentHashMap<String, MyTimerTask> newTimerTasks = new ConcurrentHashMap<>();
			Collection<Class<?>> values = annotatedClassMap.values();
			for(Class<?> groovyClass : values) {
				TimerTask timerTask = groovyClass.getAnnotation(TimerTask.class);
				if(timerTask != null) {
					long period = timerTask.period();
					String key = timerTask.key();
					if(period > 10) {
						GroovyObjectEx<?> groovyObj = getGroovyRuntime().create(groovyClass);
						MyTimerTask task = new MyTimerTask(key, period, groovyObj);
						MyTimerTask existTask = newTimerTasks.putIfAbsent(key, task);
						if(existTask != null) {
							LoggerEx.error(TAG, "Groovy TimerTask " + groovyClass + " was ignored because of duplicated key " + key);
						}
					} else {
						LoggerEx.warn(TAG, "Groovy TimerTask " + groovyClass + " was ignored because of small period " + (period / 1000));
					}
				}
			}
			
			if(timerTasks != null) {
				Collection<String> keys = timerTasks.keySet();
				for(String key : keys) {
					MyTimerTask timerTask = timerTasks.get(key);
					if(timerTask != null) {
						MyTimerTask newTimerTask = newTimerTasks.get(key);
						if(newTimerTask != null && timerTask.nextExecutionTime != null) {
							long time = timerTask.nextExecutionTime - System.currentTimeMillis();
							if(time > 2000) {
								newTimerTask.nextExecutionTime = timerTask.nextExecutionTime;
							}
						}
						timerTask.cancel();
					}
				}
			}
			if(newTimerTasks != null) {
				timerTasks = newTimerTasks;
				int count = 0;
				Collection<MyTimerTask> tasks = timerTasks.values();
				for(MyTimerTask timerTask : tasks) {
					long delay = (++count) * 100L;
					if(timerTask.nextExecutionTime != null) {
						long time = timerTask.nextExecutionTime - System.currentTimeMillis();
						if(time > 1000) {
							delay = time;
						}
					}
					LoggerEx.info(TAG, "Redeploy scheduled Groovy timer task " + timerTask.groovyObj.getGroovyPath() + " delay " + (delay / 1000) + "s to execute. Period " + (timerTask.period / 1000) + (timerTask.nextExecutionTime != null ? " resume from last key " + timerTask.key : ""));
					TimerEx.schedule(timerTask, delay, timerTask.getPeriod());
				}
			}
		}
	}

	public class MyTimerTask extends TimerTaskEx {
		private long period;
		private GroovyObjectEx<?> groovyObj;
		private Long nextExecutionTime;
		private String key;
		
		public MyTimerTask(String key, long period, GroovyObjectEx<?> groovyObj) {
			this.key = key;
			this.period = period;
			this.groovyObj = groovyObj;
		}
		@Override
		public void execute() {
//			LoggerEx.info(TAG, "Scheduled Groovy timer task " + groovyObj.getGroovyPath() + " to execute. Period " + (period / 1000) + "s");
			try {
 				groovyObj.invokeRootMethod("main");
			} catch (Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "Execute execute main for " + groovyObj + " failed, " + t.getMessage());
			}
			nextExecutionTime = System.currentTimeMillis() + period;
		}
		public long getPeriod() {
			return period;
		}
	}
}
