package com.dobybros.gateway.onlineusers;


import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.docker.server.OnlineServer;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class OnlineUsersHolder {
    private ConcurrentHashMap<String, OnlineUser> onlineUserMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, LongAdder> serviceUserCountMap = new ConcurrentHashMap<>();
    //	private EventReceivingTaskEx eventReceivingTask;
    private OnlineUserManager onlineUserManager;
    private OnlineServer onlineServer;

    public OnlineUsersHolder() {
    }

    public void init() {
        TimerEx.schedule(new TimerTaskEx("RefreshServiceUserCount") {
            @Override
            public void execute() {
                if (!serviceUserCountMap.isEmpty() && onlineUserMap != null) {
                    Map<String, Long> map = new HashMap<>();
                    Long total = 0L;
                    for (OnlineUser onlineUser : onlineUsers()) {
                        for (String service : onlineUser.getServiceUserMap().keySet()) {
                            OnlineServiceUser onlineServiceUser = onlineUser.getOnlineServiceUser(service);
                            Long serviceSize = map.get(service);
                            if (serviceSize != null) {
                                map.put(service, (long) onlineServiceUser.getChannelMap().size() + serviceSize);
                            } else {
                                map.put(service, (long) onlineServiceUser.getChannelMap().size());
                            }
                        }
                    }
                    for (String service : serviceUserCountMap.keySet()) {
                        Long serviceOnlineUserCount = map.get(service);
                        if (serviceOnlineUserCount != null) {
                            LongAdder longAdder = serviceUserCountMap.get(service);
                            if (longAdder != null) {
                                longAdder.reset();
                                longAdder.add(serviceOnlineUserCount);
                            }
                        }
                    }
                }
            }
        }, 10000L, 600000L);
    }

    public void destroy() {
    }

    public OnlineServer getOnlineServer() {
        return onlineServer;
    }

    public void setOnlineServer(OnlineServer acuServer) {
        this.onlineServer = acuServer;
    }

    void initServiceUserCount(String service) {
        serviceUserCountMap.putIfAbsent(service, new LongAdder());
    }

    void addServiceUserCount(String service) {
        LongAdder longAdder = serviceUserCountMap.get(service);
        if (longAdder != null) {
            longAdder.increment();
        }

    }

    void decrementServiceUserCount(String service) {
        LongAdder longAdder = serviceUserCountMap.get(service);
        if (longAdder != null) {
            longAdder.decrement();
        }
    }

    public OnlineUser addOnlineUserIfAbsent(OnlineUser onlineUser) {
        OnlineUser user = onlineUserMap.putIfAbsent(onlineUser.getUserId(), onlineUser);
//		if(user == null)
//			serverPresentTask.addTarget(serverPresentTask.new TargetAction(onlineUser.getUserId(), TargetAction.ACTION_ADD));
        return user;
    }

    public OnlineUser addOnlineUser(OnlineUser onlineUser) {
        OnlineUser user = onlineUserMap.put(onlineUser.getUserId(), onlineUser);
//		if(user == null)
//			serverPresentTask.addTarget(serverPresentTask.new TargetAction(onlineUser.getUserId(), TargetAction.ACTION_ADD));
        return user;
    }

    public boolean deleteOnlineUser(OnlineUser removedUser) {
        boolean bool = onlineUserMap.remove(removedUser.getUserId(), removedUser);
//		if(bool)
//			serverPresentTask.addTarget(serverPresentTask.new TargetAction(removedUser.getUserId(), TargetAction.ACTION_DELETE));
        return bool;
    }

    public OnlineUser deleteOnlineUser(String userId) {
        OnlineUser onlineUser = onlineUserMap.remove(userId);
//		if(onlineUser != null)
//			serverPresentTask.addTarget(serverPresentTask.new TargetAction(userId, TargetAction.ACTION_DELETE));
        return onlineUser;
    }

    public OnlineUser getOnlineUser(String userId) {
        OnlineUser user = onlineUserMap.get(userId);
        return user;
    }

    public Integer onlineUserCount() {
        return onlineUserMap.size();
    }

    public Collection<OnlineUser> onlineUsers() {
        return onlineUserMap.values();
    }

    public Map<String, Long> getServiceUserNumber() {
        if (!serviceUserCountMap.isEmpty()) {
            Map<String, Long> serviceUserNumberMap = new ConcurrentHashMap<>();
            for (String service : serviceUserCountMap.keySet()) {
                serviceUserNumberMap.put(service, serviceUserCountMap.get(service).longValue());
            }
            return serviceUserNumberMap;
        }
        return null;
    }

    public Boolean available(String service, Long serviceMaxUserNumber) {
        long serverCountTotal = 0L;
        for (String theService : serviceUserCountMap.keySet()){
            LongAdder longAdder = serviceUserCountMap.get(theService);
            if(longAdder != null){
                serverCountTotal += longAdder.longValue();
            }
        }
        Long maxUserNumber = onlineServer.getMaxUserNumber();
        if (isAvailable(serverCountTotal, maxUserNumber)) {
            LongAdder longAdder = serviceUserCountMap.get(service);
            if (longAdder != null) {
                Long serviceUserNumber = longAdder.longValue();
                return isAvailable(serviceUserNumber, serviceMaxUserNumber);
            } else {
                return true;
            }
        }
        return false;
    }

    public Collection<String> onlineUserIds() {
        return onlineUserMap.keySet();
    }

    public OnlineUserManager getOnlineUserManager() {
        return onlineUserManager;
    }

    public void setOnlineUserManager(OnlineUserManager onlineUserManager) {
        this.onlineUserManager = onlineUserManager;
    }

    private boolean isAvailable(Long userNumber, Long maxUserNumber) {
        if (maxUserNumber != null) {
            if (maxUserNumber == 0) {
                return false;
            } else {
                if (userNumber != null) {
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    float proportion = Float.parseFloat(decimalFormat.format((float) userNumber / maxUserNumber));
                    return !(proportion > 0.9);
                }
            }
        }
        return true;
    }

    public ConcurrentHashMap<String, OnlineUser> getOnlineUserMap() {
        return onlineUserMap;
    }

    public ConcurrentHashMap<String, LongAdder> getServiceUserCountMap() {
        return serviceUserCountMap;
    }
}
