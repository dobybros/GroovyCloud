package com.dobybros.gateway.utils;

import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class MomentLikeMap {
	
	private long inactiveCheckPeriod = TimeUnit.MINUTES.toMillis(60);
//	private long inactiveCheckPeriod = TimeUnit.SECONDS.toMillis(10);
	private int maxMapSize = 100;
	
	private Map<String, Long> likeMap = Collections.synchronizedMap(new LinkedHashMap<String, Long>());
	private MomentLikeExpireTimer expireTimerTask = new MomentLikeExpireTimer();
	
	class MomentLikeExpireTimer extends TimerTaskEx {
		private final String TAG = MomentLikeExpireTimer.class.getSimpleName();

		@Override
		public void execute() {
			for(int i = 0; i < maxMapSize; i++) {
				Integer pos = 0;
				Long lastRemoveTime = System.currentTimeMillis() - inactiveCheckPeriod;
				List<String> deletedKeys = new ArrayList<String>();
				for (Iterator<String> iterator = likeMap.keySet().iterator(); iterator.hasNext(); pos++) {
					String mid = iterator.next();
					if(likeMap.get(mid) < lastRemoveTime)
						deletedKeys.add(mid);
					else
						break;
				}
				if(!deletedKeys.isEmpty()) {
					for(String key : deletedKeys) {
						likeMap.remove(key);
					}
				}
			}
		}
	}
	
	public synchronized void init() {
		TimerEx.schedule(expireTimerTask, TimeUnit.SECONDS.toMillis(10), inactiveCheckPeriod);
	}
	
	/**
	 * 
	 * @param momentId
	 * @return 一小时内是否已经like过
	 */
	public boolean add(String momentId) {
		Long likeTime = likeMap.put(momentId, System.currentTimeMillis());
		if(likeMap.size() > maxMapSize) {
			Integer needRemoveCount = likeMap.size() - maxMapSize;
			Integer pos = 0;
			List<String> deletedKeys = new ArrayList<String>();
			for (Iterator<String> iterator = likeMap.keySet().iterator(); iterator.hasNext() && needRemoveCount > 0 ; pos++, needRemoveCount--) {
				String cid = iterator.next();
				deletedKeys.add(cid);
			}
			if(!deletedKeys.isEmpty()) {
				for(String key : deletedKeys) {
					likeMap.remove(key);
				}
			}
		}
		return likeTime != null;		//为空表示1小时内从未like过该条moment，不为空表示like过
	}
	
	public Long getLikeTime(String momentId) {
		if(likeMap.keySet() != null)
			return likeMap.get(momentId);
		return null;
	}
	
	public void destroy() {
		likeMap.clear();
		expireTimerTask.cancel();
	}
	
	public Map<String, Long> getLikeMap() {
		return likeMap;
	}

	public void setLikeMap(Map<String, Long> likeMap) {
		this.likeMap = likeMap;
	}
	
}
