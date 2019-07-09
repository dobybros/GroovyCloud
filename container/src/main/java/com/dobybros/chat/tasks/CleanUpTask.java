package com.dobybros.chat.tasks;

import chat.logs.LoggerEx;
import com.dobybros.chat.utils.CommonUtils;
import com.docker.tasks.Task;
import script.file.FileAdapter;
import script.file.FileAdapter.PathEx;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingQueue;

public class CleanUpTask extends Task {
	private static final String TAG = CleanUpTask.class.getSimpleName();

	private LinkedBlockingQueue<CleanUp> queue;
	
	@Resource
	private FileAdapter fileAdapter;
	
	private boolean isStarted = true;
	
	public static class CleanUp {
		public static final int TYPE_RESOURCEID = 1;
		private int type;
		private String id;
		public static int RETRY_MAX = 3;
		private int retry = RETRY_MAX;
		
		public CleanUp(int type, String id) {
			this.type = type;
			this.id = id;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public int getRetry() {
			return retry;
		}

		public void setRetry(int retry) {
			this.retry = retry;
		}
	}
	
	@Override
	public void execute() {
		while(isStarted) {
			LoggerEx.info(TAG, CleanUpTask.class.getSimpleName() + " is started, " + Thread.currentThread());
			CleanUp task = null;
			try {
				try {
					task = queue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(task != null) {
					if(task instanceof ShutdownCleanUp) 
						break;
					switch(task.type) {
					case CleanUp.TYPE_RESOURCEID:
						boolean bool = fileAdapter.deleteFile(new PathEx(CommonUtils.getDocumentPath(task.id),
	                    		task.id, null));
						task.retry--;
						if(!bool) {
							if(task.retry >= 0) 
								queue.add(task);
							else 
								LoggerEx.fatal(TAG, "Delete resource return false after retry " + CleanUp.RETRY_MAX + " times");
						}
						break;
					}
				}
			}catch(Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "Task sending failed, " + t.getMessage());
				if(task != null) {
					task.retry--;
					if(task.retry >= 0) 
						queue.add(task);
					else 
						LoggerEx.fatal(TAG, "Delete resource failed " + t.getMessage() + " after retry " + CleanUp.RETRY_MAX + " times");
				}
			}
		}
		LoggerEx.info(TAG, CleanUpTask.class.getSimpleName() + " is shutted down, " + Thread.currentThread());
	}
	
	public void addCleanUp(CleanUp cleanUp) {
		try {
			queue.put(cleanUp);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void init() throws Throwable {
		queue = new LinkedBlockingQueue<>();
	}

	class ShutdownCleanUp extends CleanUp {

		public ShutdownCleanUp() {
			super(CleanUp.TYPE_RESOURCEID, null);
		}
		
	}
	
	@Override
	public void shutdown() {
		LoggerEx.info(TAG, CleanUpTask.class.getSimpleName() + " is shutting down... " + queue.size() + " still left in queue");
		isStarted = false;
		Integer numOfThread = getNumOfThreads();
		if(numOfThread == null) 
			numOfThread = 1;
		for(int i = 0; i < numOfThread; i++) {
			addCleanUp(new ShutdownCleanUp());
		}
	}

}
