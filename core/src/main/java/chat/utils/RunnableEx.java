package chat.utils;

import chat.logs.LoggerEx;


public abstract class RunnableEx implements Runnable {
	private final String TAG = RunnableEx.class.getSimpleName();
	private Long startTime;
	private Long endTime;
	private Throwable error;
	private String description;
	public RunnableEx(String description) {
		this.description = description;
	}
	@Override
	public final void run() {
		startTime = System.currentTimeMillis();
		try {
			execute();
		} catch (Throwable t) {
			error = t;
		} finally {
			endTime = System.currentTimeMillis();
		}
		LoggerEx.info(TAG, this.toString());
	}
	
	public abstract void execute();
	
	public String toString() {
		StringBuffer buffer = new StringBuffer("AcuRunnable (");
		buffer.append(description).append(")");
		if(startTime == null) {
			buffer.append(" not started.");
		} else {
			buffer.append(" started at ").append(ChatUtils.dateString(startTime));
			if(endTime == null) {
				buffer.append(" running for ").append((System.currentTimeMillis() - startTime) / 1000).append(" seconds.");
			} else {
				buffer.append(" ended after ").append((endTime - startTime) / 1000).append(" seconds.").append(" Ended at ").append(ChatUtils.dateString(endTime));
				if(error != null)
					buffer.append(" Occured error ").append(error.getMessage());
			}
		}
		return buffer.toString();
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
