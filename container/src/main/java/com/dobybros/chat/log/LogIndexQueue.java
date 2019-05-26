package com.dobybros.chat.log;

import com.dobybros.chat.services.IConsumeQueueService;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogIndexQueue {
    private BlockingQueue<LogIndex> queue = new LinkedBlockingQueue<>();
    private boolean isStarted = false;
    @Resource
	private IConsumeQueueService bulkLogQueueService;
	private int taskCount = 2;
	private Long flushPeriod = 30000L;
	private Integer maxLogCount = 100;
	
	public static final String FIELD_TASKCOUNT = "taskCount";
	public static final String FIELD_FLUSHPERIOD = "flushPeriod";
	public static final String FIELD_MAXLOGCOUNT = "maxLogCount";
	
	
	public void init() {
		if(!isStarted) {
			isStarted = true;
			
//			if(logsProperties != null) {
//				AutoReloadProperties.PropertiesReloadListener reloadListener = new AutoReloadProperties.PropertiesReloadListener() {
//					@Override
//					public void reloaded() {
//						taskCount = ChatUtils.parseInt(logsProperties.getProperty(FIELD_TASKCOUNT), taskCount);
//						flushPeriod = ChatUtils.parseLong(logsProperties.getProperty(FIELD_FLUSHPERIOD), flushPeriod);
//						maxLogCount = ChatUtils.parseInt(logsProperties.getProperty(FIELD_MAXLOGCOUNT), maxLogCount);
//					}
//				};
//				logsProperties.setReloadListener(reloadListener);
//				reloadListener.reloaded();
//			}
			
			for(int i = 0; i < taskCount; i++) {
				LogTask logTask = new LogTask(queue, bulkLogQueueService);
				logTask.setFlushPeriod(flushPeriod);
				logTask.setMaxLogCount(maxLogCount);
				logTask.start();
			}
		} 
	}
	
	public void add(LogIndex logIndex) {
		try {
		    if (logIndex != null) {
		        queue.put(logIndex);
		    }
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void addAll(List<LogIndex> logIndexes) {
	    try {
	        if (logIndexes != null) {
    	        for (LogIndex index : logIndexes) {
    	            queue.put(index);
    	        }
	        }
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
	}

	public int getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
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
