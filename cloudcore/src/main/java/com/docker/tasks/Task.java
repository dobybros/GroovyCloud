package com.docker.tasks;

import chat.logs.LoggerEx;
import com.docker.server.OnlineServer;

public abstract class Task implements Runnable{
	public static final int STATUS_INACTIVE = 0;
	public static final int STATUS_ACTIVE = 1;
	public static final int STATUS_DEAD = -1;
	private static final String TAG = Task.class.getSimpleName();
	
	private int status = STATUS_INACTIVE;
	protected OnlineServer onlineServer;
	
	private Integer numOfThreads = 1;
	
	/**
	 * Init method of Task shall never failed. if so, stop whole server.
	 * 
	 * @throws Throwable
	 */
	public abstract void init() throws Throwable;
	public abstract void shutdown();
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String taskDescription() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public final void run() {
		status = STATUS_ACTIVE;
		try {
			execute();
		} catch (Throwable t) {
			t.printStackTrace();
			LoggerEx.error(TAG, "Task " + this.getClass().getSimpleName() + " execute failed, " + t.getMessage());
		}
		status = STATUS_INACTIVE;
	}
	
	public abstract void execute();
	public OnlineServer getOnlineServer() {
		return onlineServer;
	}
	public void setOnlineServer(OnlineServer onlineServer) {
		this.onlineServer = onlineServer;
	}
	public Integer getNumOfThreads() {
		return numOfThreads;
	}
	public void setNumOfThreads(Integer numOfThreads) {
		this.numOfThreads = numOfThreads;
	}
}
