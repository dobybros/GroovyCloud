package chat.thread;

import chat.logs.LoggerEx;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by lick on 2019/10/17.
 * Description： Execute the custom task by most threads
 */
public class MultipleFixedThreadManager {
    private final String TAG = MultipleFixedThreadManager.class.getSimpleName();
    private static volatile MultipleFixedThreadManager instance;
    private Map<Object, SpecifyThreads> specifyThreadsMap = new ConcurrentHashMap<>();

    public void execute(ThreadPoolExecutor threadPoolExecutor, Integer countThreads, Runnable t, Object key, List<FixedThreadListener> fixedThreadListeners) {
        SpecifyThreads specifyThreads = specifyThreadsMap.get(key);
        if (specifyThreads == null) {
            specifyThreads = new SpecifyThreads(threadPoolExecutor, countThreads, t, key, fixedThreadListeners);
            SpecifyThreads specifyThreadsOld = specifyThreadsMap.putIfAbsent(key, specifyThreads);
            if (specifyThreadsOld != null) {
                specifyThreads = specifyThreadsOld;
            }
        }
        specifyThreads.execute();
    }

    public static MultipleFixedThreadManager getInstance() {
        if (instance == null) {
            synchronized (MultipleFixedThreadManager.class){
                if(instance == null){
                    instance = new MultipleFixedThreadManager();
                }
            }
        }
        return instance;
    }

    private class SpecifyThreads {
        private ThreadPoolExecutor threadPoolExecutor;
        private Integer countThreads;
        private Runnable t;
        private Object key;
        private int counter = 0;
        private final int[] lock = new int[0];
        private List<FixedThreadListener> threadTaskRecordListeners;

        private SpecifyThreads(ThreadPoolExecutor threadPoolExecutor, Integer countThreads, Runnable t, Object key, List<FixedThreadListener> threadTaskRecordListeners) {
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
            boolean exceed = false;
            synchronized (lock) {
                if (counter >= this.countThreads) {
                    exceed = true;
                }
            }
            if (exceed) {
                if (threadTaskRecordListeners != null && !threadTaskRecordListeners.isEmpty()) {
                    for (FixedThreadListener fixedThreadListener : threadTaskRecordListeners) {
                        try {
                            fixedThreadListener.threadExceeded();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                            LoggerEx.error(TAG, "FixedThreadListener excute threadExceeded failed, errMsg: " + ExceptionUtils.getFullStackTrace(throwable));
                        }
                    }
                }
                return;
            }
            boolean canExecute = false;
            synchronized (lock) {
                if (counter < countThreads) {
                    counter++;
                    canExecute = true;
                }
            }
            if (canExecute) {
                this.threadPoolExecutor.execute(() -> {
                    try {
                        this.t.run();
                    } catch (Throwable t) {
                        LoggerEx.error(TAG, "Excute SpecifyThreads error,key: "+ key +" err: " + ExceptionUtils.getFullStackTrace(t));
                    } finally {
                        synchronized (lock) {
                            counter --;
                            if(counter == 0){
                                specifyThreadsMap.remove(this.key);
                            }
                        }
                    }
                });
            }else {
                LoggerEx.error(TAG, "Task exceed countThreads, task: " + key + ",countThreads: " + countThreads);
            }
        }
    }
}
