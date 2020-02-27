package chat.main;

import chat.thread.CloudThreadFactory;
import org.apache.tomcat.util.threads.TaskQueue;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerStart {
    private static String coreSize;
    private static String maximumPoolSize;
    private static String gatewayCoreSize;
    private static String gatewayMaximumPoolSize;
    private static String timerCoreSize;
    private static String timerMaximumPoolSize;
    private static String asyncCoreSize;
    private static String asyncMaximumPoolSize;
    private static String keepAliveTime;
    private static String queueCapacity;
    private ThreadPoolExecutor threadPoolExecutor;
    private ThreadPoolExecutor timerThreadPoolExecutor;
    private ThreadPoolExecutor asyncThreadPoolExecutor;
    private ThreadPoolExecutor gatewayThreadPoolExecutor;

    private static volatile ServerStart instance;

    public static ServerStart getInstance() {

        if (instance == null) {
            synchronized (ServerStart.class) {
                if (instance == null) {
                    ClassPathResource configResource = new ClassPathResource("groovycloud.properties");
                    Properties properties = new Properties();
                    try {
                        properties.load(configResource.getInputStream());
                        coreSize = properties.getProperty("thread.coreSize");
                        maximumPoolSize = properties.getProperty("thread.maximumPoolSize");
                        gatewayCoreSize = properties.getProperty("thread.gateway.coreSize");
                        gatewayMaximumPoolSize = properties.getProperty("thread.gateway.maximumPoolSize");
                        timerCoreSize = properties.getProperty("thread.timer.coreSize");
                        timerMaximumPoolSize = properties.getProperty("thread.timer.maximumPoolSize");
                        asyncCoreSize = properties.getProperty("thread.async.coreSize");
                        asyncMaximumPoolSize = properties.getProperty("thread.async.maximumPoolSize");
                        keepAliveTime = properties.getProperty("thread.keepAliveTime");
                        queueCapacity = properties.getProperty("thread.queueCapacity");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            configResource.getInputStream().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    instance = new ServerStart();
                    if (coreSize == null) {
                        coreSize = "30";
                    }
                    if (maximumPoolSize == null) {
                        maximumPoolSize = "300";
                    }
                    if (timerCoreSize == null) {
                        timerCoreSize = "20";
                    }
                    if (timerMaximumPoolSize == null) {
                        timerMaximumPoolSize = "100";
                    }
                    if (gatewayCoreSize == null) {
                        gatewayCoreSize = "10";
                    }
                    if (gatewayMaximumPoolSize == null) {
                        gatewayMaximumPoolSize = "100";
                    }

                    if (asyncCoreSize == null) {
                        asyncCoreSize = "5";
                    }
                    if (asyncMaximumPoolSize == null) {
                        asyncMaximumPoolSize = "100";
                    }
                    if (keepAliveTime == null) {
                        keepAliveTime = "30";
                    }
                    if (queueCapacity == null) {
                        queueCapacity = "20000";
                    }
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {
        File f = new File("./tmp");
        System.out.println(f.getAbsolutePath());
    }

    //业务使用
    public ThreadPoolExecutor getThreadPool() {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(Integer.valueOf(coreSize), Integer.valueOf(maximumPoolSize), Integer.valueOf(keepAliveTime), TimeUnit.SECONDS, new TaskQueue(Integer.valueOf(queueCapacity)), new CloudThreadFactory("Normal"));
        }
        return threadPoolExecutor;
    }
    //gateway专用
    public ThreadPoolExecutor getGatewayThreadPoolExecutor() {
        if (gatewayThreadPoolExecutor == null) {
            gatewayThreadPoolExecutor = new ThreadPoolExecutor(Integer.valueOf(gatewayCoreSize), Integer.valueOf(gatewayMaximumPoolSize), Integer.valueOf(keepAliveTime), TimeUnit.SECONDS, new TaskQueue(Integer.valueOf(queueCapacity)), new CloudThreadFactory("Gateway"));
        }
        return gatewayThreadPoolExecutor;
    }
    //定时器专用
    public ThreadPoolExecutor getTimerThreadPoolExecutor() {
        if (timerThreadPoolExecutor == null) {
            timerThreadPoolExecutor = new ThreadPoolExecutor(Integer.valueOf(timerCoreSize), Integer.valueOf(timerMaximumPoolSize), Integer.valueOf(keepAliveTime), TimeUnit.SECONDS, new TaskQueue(Integer.valueOf(queueCapacity)), new CloudThreadFactory("Timer"));
        }
        return timerThreadPoolExecutor;
    }

    //异步专用
    public ThreadPoolExecutor getAsyncThreadPoolExecutor() {
        if (asyncThreadPoolExecutor == null) {
            asyncThreadPoolExecutor = new ThreadPoolExecutor(Integer.valueOf(asyncCoreSize), Integer.valueOf(asyncMaximumPoolSize), Integer.valueOf(keepAliveTime), TimeUnit.SECONDS, new TaskQueue(Integer.valueOf(queueCapacity)), new CloudThreadFactory("Async"));
        }
        return asyncThreadPoolExecutor;
    }

    /**普通的线程池：
     * 1、如果线程池的当前大小还没有达到基本大小(poolSize < corePoolSize)，那么就新增加一个线程处理新提交的任务；
     *
     * 2、如果当前大小已经达到了基本大小，就将新提交的任务提交到阻塞队列排队，等候处理workQueue.offer(command)；
     *
     * 3、如果队列容量已达上限，并且当前大小poolSize没有达到maximumPoolSize，那么就新增线程来处理任务；
     *
     * 4、如果队列已满，并且当前线程数目也已经达到上限，那么意味着线程池的处理能力已经达到了极限，此时需要拒绝新增加的任务。至于如何拒绝处理新增的任务，取决于线程池的饱和策略RejectedExecutionHandler。
     * tomcat的taskqueue:会先把线程池起到maximumPoolSize,再往队列中放
     */
}
