package com.dobybros.chat.log;

import chat.logs.LoggerEx;
import com.dobybros.chat.services.IConsumeQueueService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LogTask extends Thread {
    private static final String TAG = LogTask.class.getSimpleName();
    private BlockingQueue<LogIndex> queue;
    private IConsumeQueueService bulkLogQueueService;
    private List<LogIndex> cachedLogs = new ArrayList<>();
    private Long flushPeriod = 15000L;
    private Integer maxLogCount = 100;
    private Long time = 0L;
    private ExecutorService executorService;

    public LogTask(BlockingQueue<LogIndex> queue, IConsumeQueueService consumeQueueService) {
        this.queue = queue;
        this.bulkLogQueueService = consumeQueueService;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void run() {
        while (true) {
            if (System.currentTimeMillis() - time > flushPeriod) {
                if (!cachedLogs.isEmpty()) {
                    time = System.currentTimeMillis();
                    sendLog(cachedLogs);
                    cachedLogs = new ArrayList<>();
                }
            } else if (cachedLogs.size() > maxLogCount) {
                sendLog(cachedLogs);
                cachedLogs = new ArrayList<>();
            }
            LogIndex log = null;
            if(cachedLogs.isEmpty()) {
            	try {
					log = queue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            } else {
            	log = queue.poll();
            }
            
            if (log != null) {
            	cachedLogs.add(log);
            } else {
        		synchronized (cachedLogs) {
        			try {
        				cachedLogs.wait(TimeUnit.SECONDS.toMillis(30));
        			} catch (InterruptedException e) {
        				e.printStackTrace();
        			}
        		}
            }
        }
    }

    public void sendLog(final List<LogIndex> logs) {
        CreateIndex createIndex = new CreateIndex(executorService, logs);
        createIndex.start();
    }

    class CreateIndex implements Runnable {
        int retryCount = 1;
        BulkLog bulkLog;
        ExecutorService executorService;

        CreateIndex(ExecutorService executorService, List<LogIndex> logs) {
            bulkLog = new BulkLog();
            bulkLog.setLogIndexes(logs);
            this.executorService = executorService;
        }

        @Override
        public void run() {
            try {
            	bulkLogQueueService.add(bulkLog);
            } catch (Throwable e) {
                LoggerEx.error(TAG, " Create log index failed, " + e.getMessage() + " --Retry "
                        + retryCount);
                if (retryCount > 0) {
                    retryCount--;
                    executorService.execute(this);
                }
            }
        }

        void start() {
            executorService.execute(this);
        }
    }

    public Long getFlushPeriod() {
        return flushPeriod;
    }

    public void setFlushPeriod(Long flushPeriod) {
        this.flushPeriod = flushPeriod;
    }

    public Integer getMaxLogCount() {
        return maxLogCount;
    }

    public void setMaxLogCount(Integer maxLogCount) {
        this.maxLogCount = maxLogCount;
    }

}
