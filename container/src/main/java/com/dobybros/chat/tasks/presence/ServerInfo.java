package com.dobybros.chat.tasks.presence;

import chat.utils.ConcurrentHashSet;

import java.util.concurrent.atomic.AtomicInteger;

public class ServerInfo {
	private ConcurrentHashSet<String> targetIds;
	private AtomicInteger targetCount;

	public ServerInfo() {
		targetCount = new AtomicInteger(0);
		targetIds = new ConcurrentHashSet<String>();
	}

	public boolean addTargetId(String targetId) {
		boolean bool = targetIds.add(targetId);
		if(bool) {
			targetCount.incrementAndGet();
		}
		return bool;
	}
	
	public boolean removeTargetId(String targetId) {
		boolean bool = targetIds.remove(targetId);
		if(bool) {
			targetCount.decrementAndGet();
		}
		return bool;
	}
	
	public int size() {
		if(targetCount == null)
			return 0;
		return targetCount.get();
	}
	
	public ConcurrentHashSet<String> getTargetIds() {
		return targetIds;
	}

	public void setTargetIds(ConcurrentHashSet<String> targetIds) {
		this.targetIds = targetIds;
	}

	public AtomicInteger getTargetCount() {
		return targetCount;
	}

	public void setTargetCount(AtomicInteger targetCount) {
		this.targetCount = targetCount;
	}
	
	public void clear() {
		if(targetIds != null) {
			targetIds.clear();
		}
		if(targetCount != null) {
			targetCount.set(0);
		}
	}
}
