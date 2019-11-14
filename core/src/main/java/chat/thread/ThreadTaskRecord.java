package chat.thread;

import chat.logs.LoggerEx;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lick on 2019/10/17.
 * Description： Execute the custom task by most threads
 */
public class ThreadTaskRecord {
    private final String TAG = ThreadTaskRecord.class.getSimpleName();
    private static ThreadTaskRecord instance;
    private Map<Object, SpecifyThreads> specifyThreadsMap = new ConcurrentHashMap<>();

    public void execute(ThreadPoolExecutor threadPoolExecutor, Integer countThreads, Runnable t, Object key, List<ThreadTaskRecordListener> threadTaskRecordListeners) {
        SpecifyThreads specifyThreads = specifyThreadsMap.get(key);
        if(specifyThreads == null){
            specifyThreads = new SpecifyThreads(threadPoolExecutor, countThreads, t, key, threadTaskRecordListeners);
            SpecifyThreads specifyThreadsOld = specifyThreadsMap.putIfAbsent(key, specifyThreads);
            if(specifyThreadsOld != null){
                specifyThreads = specifyThreadsOld;
            }
        }
        specifyThreads.execute();
    }

    public static synchronized ThreadTaskRecord getInstance() {
        if (instance == null) {
            instance = new ThreadTaskRecord();
        }
        return instance;
    }

    private class SpecifyThreads {
        private ThreadPoolExecutor threadPoolExecutor;
        private Integer countThreads;
        private Runnable t;
        private Object key;
        private AtomicInteger counter = new AtomicInteger(0);
        private List<ThreadTaskRecordListener> threadTaskRecordListeners;
        private SpecifyThreads(ThreadPoolExecutor threadPoolExecutor, Integer countThreads, Runnable t, Object key, List<ThreadTaskRecordListener> threadTaskRecordListeners) {
            this.threadPoolExecutor = threadPoolExecutor;
            if (countThreads == null) {
                countThreads = 1;
            }
            this.countThreads = countThreads;
            this.t = t;
            this.key = key;
            this.threadTaskRecordListeners = threadTaskRecordListeners;
        }

        public void execute() {
            if (counter.get() >= this.countThreads) {
                if(threadTaskRecordListeners != null && !threadTaskRecordListeners.isEmpty()){
                    for (ThreadTaskRecordListener threadTaskRecordListener : threadTaskRecordListeners){
                        threadTaskRecordListener.executeFailed();
                    }
                }
                return;
            }
            counter.incrementAndGet();
            this.threadPoolExecutor.execute(() -> {
                try {
                    this.t.run();
                } catch (Throwable t) {
                    LoggerEx.error(TAG, "Excute timer task error, err: " + ExceptionUtils.getFullStackTrace(t));
                } finally {
                    counter.decrementAndGet();
                    if(counter.get() == 0){
                        specifyThreadsMap.remove(this.key);
                    }
                }
            });
        }
    }
}
