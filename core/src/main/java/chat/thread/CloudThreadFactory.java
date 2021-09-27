package chat.thread;

import org.apache.commons.lang.StringUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lick on 2019/9/29.
 * Description：
 */
public class CloudThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNum = new AtomicInteger(1);
    private final AtomicInteger threadNum = new AtomicInteger(1);
    //set name
    private final String prefix;
    private final boolean daemoThread;
    public CloudThreadFactory(){
        this(null);
    }
    public CloudThreadFactory(String prefix){
        this(prefix, false);
    }
    public CloudThreadFactory(String prefix, boolean daemo){
        this.prefix = (StringUtils.isNotBlank(prefix) ? prefix : "cloudThread") + "-" + poolNum.incrementAndGet() + "-thread-";
        daemoThread = daemo;
    }
    @Override
    public Thread newThread(Runnable runnable) {
        String name = prefix + threadNum.getAndIncrement();
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(daemoThread);
        return thread;
    }
}
