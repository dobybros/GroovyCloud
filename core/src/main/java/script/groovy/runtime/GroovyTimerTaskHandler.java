package script.groovy.runtime;

import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.StringUtils;
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
        if (timerTasks != null) {
            Collection<MyTimerTask> theTimerTasks = timerTasks.values();
            for (MyTimerTask timerTask : theTimerTasks) {
                try {
                    timerTask.cancel();
                } catch (Throwable t) {
                }
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
        if (annotatedClassMap != null) {
            ConcurrentHashMap<String, MyTimerTask> newTimerTasks = new ConcurrentHashMap<>();
            Collection<Class<?>> values = annotatedClassMap.values();
            for (Class<?> groovyClass : values) {
                TimerTask timerTask = groovyClass.getAnnotation(TimerTask.class);
                if (timerTask != null) {
                    String key = getGroovyRuntime().processAnnotationString(timerTask.key());
                    long period = timerTask.period();
                    String cron = getGroovyRuntime().processAnnotationString(timerTask.cron());
                    GroovyObjectEx<?> groovyObj = ((GroovyBeanFactory) getGroovyRuntime().getClassAnnotationHandler(GroovyBeanFactory.class)).getClassBean(groovyClass);
                    MyTimerTask task = new MyTimerTask(key, groovyObj);
                    task.setId(key);
                    if (!StringUtils.isEmpty(cron)) {
                        task.setCron(cron);
                    } else if (period > 10) {
                        task.setPeriod(period);
                    } else {
                        LoggerEx.warn(TAG, "Groovy TimerTask " + groovyClass + " was ignored because of small period " + (period / 1000));
                    }
                    MyTimerTask existTask = newTimerTasks.putIfAbsent(key, task);
                    if (existTask != null) {
                        LoggerEx.error(TAG, "Groovy TimerTask " + groovyClass + " was ignored because of duplicated key " + key);
                    }

                }
            }

            if (timerTasks != null) {
                Collection<String> keys = timerTasks.keySet();
                for (String key : keys) {
                    MyTimerTask timerTask = timerTasks.get(key);
                    if (timerTask != null) {
                        timerTask.cancel();
                    }
                }
            }
            if (newTimerTasks.size() > 0) {
                timerTasks = newTimerTasks;
                Collection<MyTimerTask> tasks = timerTasks.values();
                for (MyTimerTask timerTask : tasks) {
                    if (timerTask.getPeriod() != null) {
                        TimerEx.schedule(timerTask, null, timerTask.getPeriod());
                    } else if (timerTask.getCron() != null) {
                        TimerEx.schedule(timerTask, timerTask.getCron());
                    }
                }
            }
        }
    }

    public class MyTimerTask extends TimerTaskEx {
        private GroovyObjectEx<?> groovyObj;
        private String key;

        public MyTimerTask(String key, GroovyObjectEx<?> groovyObj) {
            this.key = key;
            this.groovyObj = groovyObj;
        }

        @Override
        public void execute() {
            try {
                groovyObj.invokeRootMethod("main");
            } catch (Throwable t) {
                t.printStackTrace();
                LoggerEx.error(TAG, "Execute execute main for " + groovyObj + " failed, " + t.getMessage());
            }
        }
    }
}
