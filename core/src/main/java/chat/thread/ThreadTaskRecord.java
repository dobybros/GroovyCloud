package chat.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lick on 2019/10/17.
 * Description： Execute the custom task by most threads
 */
public class ThreadTaskRecord {
    private static ThreadTaskRecord instance;
    private Map<Object, Integer> threadTaskMap = new ConcurrentHashMap<>();
    private Lock lock = new ReentrantLock();

    private boolean access(Object o, Integer maxThreads) {
        lock.lock();
        boolean flag = false;
        Integer count = threadTaskMap.get(o);
        if (count == null)
            count = 0;
        if (count < maxThreads) {
            threadTaskMap.put(o, ++count);
            flag = true;
        }
        lock.unlock();
        return flag;
    }

    public void removeTask(Object o) {
        lock.lock();
        Integer count = threadTaskMap.get(o);
        if (count != null) {
            if (count > 1) {
                threadTaskMap.put(o, --count);
            } else {
                threadTaskMap.remove(o);
            }
        }
        lock.unlock();
    }

    public void execute(ThreadPoolExecutor threadPoolExecutor, Integer countThreads, Runnable t, Object key) {
       new SpecifyThreads(threadPoolExecutor, countThreads, t, key).execute();
    }

    public static synchronized ThreadTaskRecord getInstance() {
        if (instance == null) {
            instance = new ThreadTaskRecord();
        }
        return instance;
    }

    private class SpecifyThreads {
        private ThreadPoolExecutor threadPoolExecutor;
        private Integer countTHreads;
        private Runnable t;
        private Object key;

        private SpecifyThreads(ThreadPoolExecutor threadPoolExecutor, Integer countThreads, Runnable t, Object key) {
            this.threadPoolExecutor = threadPoolExecutor;
            if (countThreads == null) {
                countThreads = 1;
            }
            this.countTHreads = countThreads;
            this.t = t;
            this.key = key;
        }

        public void execute() {
            if (access(this.key, this.countTHreads)) {
                this.threadPoolExecutor.execute(t);
            }
        }
    }
}
