package com.docker.utils;

import chat.errors.CoreException;


public abstract class WaitForListener {
	public abstract boolean waitFor() throws Throwable;

	public void timeout() {};
	
	private long checkPeriod = 1000L;

	public WaitForListener() {
	}

	public WaitForListener(long checkPeriod) {
		this.checkPeriod = checkPeriod;
	}

	public void startWaiting(long timeout) throws Throwable {
		final long startTime = System.currentTimeMillis();
		while ((System.currentTimeMillis() - startTime) < timeout) {
			if (waitFor())
				return;
			try {
				Thread.sleep(checkPeriod);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		timeout();
		throw new CoreException("Wait timeout");
	}

	public long getCheckPeriod() {
		return checkPeriod;
	}

	public void setCheckPeriod(long checkPeriod) {
		this.checkPeriod = checkPeriod;
	}
}