package chat.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import chat.logs.LoggerEx;

public class ThreadPoolExecutorEx {
	private static final String TAG = ThreadPoolExecutorEx.class.getSimpleName();
	private Integer corePoolSize;
	private Integer maxPoolSize;
	private Integer queueCapacity;
	private Integer keepAliveSeconds;
	private LinkedBlockingQueue<Runnable> queue;
	private ThreadPoolExecutor threadPoolExecutor;
	public ThreadPoolExecutorEx() {
	}
	
	public void init() {
		if(threadPoolExecutor == null) {
			if(queueCapacity != null) {
				queue = new LinkedBlockingQueue<>(queueCapacity);
			} else {
				queue = new LinkedBlockingQueue<>();
			}
				
			if(keepAliveSeconds == null)
				keepAliveSeconds = 30;
			if(corePoolSize == null)
				corePoolSize = 10;
			if(maxPoolSize == null)
				maxPoolSize = 1000;
			threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 30, TimeUnit.SECONDS, queue, new RejectedExecutionHandler() {
				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					LoggerEx.error(TAG, "rejectedExecution " + r.toString());
				}
			});
		}
	}
	
	public void execute(RunnableEx runnable) {
		if(threadPoolExecutor == null)
			throw new NullPointerException("AcuThreadPoolExecutor hasn't been initialized.");
		threadPoolExecutor.execute(runnable);
	}
	
	public Integer getQueueCapacity() {
		return queueCapacity;
	}
	
	public void setQueueCapacity(Integer queueCapacity) {
		this.queueCapacity = queueCapacity;
	}
	
	public Integer getMaxPoolSize() {
		return maxPoolSize;
	}
	
	public void setMaxPoolSize(Integer maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}
	
	public Integer getCorePoolSize() {
		return corePoolSize;
	}
	
	public void setCorePoolSize(Integer corePoolSize) {
		this.corePoolSize = corePoolSize;
	}
	
	public Integer getKeepAliveSeconds() {
		return keepAliveSeconds;
	}
	
	public void setKeepAliveSeconds(Integer keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}

}
