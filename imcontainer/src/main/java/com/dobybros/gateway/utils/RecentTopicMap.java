package com.dobybros.gateway.utils;

import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.dobybros.chat.utils.FixedSizeLinkedHashMap;
import com.dobybros.gateway.channels.data.Result;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RecentTopicMap {
	
	private long inactiveCheckPeriod = TimeUnit.MINUTES.toMillis(30);
//	private long inactiveCheckPeriod = TimeUnit.SECONDS.toMillis(30);
	
	private Map<String, Result> topicMap = Collections.synchronizedMap(new FixedSizeLinkedHashMap<String, Result>(100));
	private SendingTopicExpireTimer expireTimerTask = new SendingTopicExpireTimer();
	
	class SendingTopicExpireTimer extends TimerTaskEx {

		@Override
		public void execute() {
//			for(int i = 0; i < maxTopicSize; i++) {
//				if(topicMap.get(i) != null && topicMap.get(i).getSequence() < System.currentTimeMillis() - inactiveCheckPeriod) {
//					topicMap.remove(i);
//				} else {
//					break;
//				}
			//TODO 可能有性能问题
			Integer pos = 0;
			Long lastRemoveTime = System.currentTimeMillis() - inactiveCheckPeriod;
			List<String> deletedKeys = new ArrayList<String>();
			for (Iterator<String> iterator = topicMap.keySet().iterator(); iterator.hasNext(); pos++) {
				String cid = iterator.next();
				Long time = topicMap.get(cid).getTime();
				if(time == null || time < lastRemoveTime)
					deletedKeys.add(cid);
				else
					break;
			}
			if(!deletedKeys.isEmpty()) {
				for(String key : deletedKeys) {
					topicMap.remove(key);
				}
			}
		}
//		}
	}
	
	public synchronized void init() {
		TimerEx.schedule(expireTimerTask, TimeUnit.SECONDS.toMillis(10), inactiveCheckPeriod);
	}
	
	public void add(Result resultEvent) {
		topicMap.put(resultEvent.getForId(), resultEvent);
	}
	
	public Result getExistEvent(String clientId) {
		if(topicMap.keySet() != null)
			return topicMap.get(clientId);
		return null;
	}
	
	public void destroy() {
		topicMap.clear();
		expireTimerTask.cancel();
	}
	
	public Map<String, Result> getTopicMap() {
		return topicMap;
	}

	public void setTopicMap(Map<String, Result> topicMap) {
		this.topicMap = topicMap;
	}

}
