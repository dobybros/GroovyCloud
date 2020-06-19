package com.dobybros.chat.script.annotations.gateway

import chat.utils.TimerTaskEx
import com.docker.server.OnlineServer
/**
 * Created by lick on 2020/6/19.
 * Description：
 */
class DataServiceUserSessionListener extends ServiceUserSessionListener {
    private TimerTaskEx storeDataTimer = null
    private final String SERVERSERVICESEPARATOR = "###"

    public void restoreData() {
        cancelStoreDataTimer()
        storeDataTimer = () -> {
            Object data = super.getRoomData()
            List dataList = new ArrayList()
            dataList.add(getUserId())
            dataList.add(getService() + SERVERSERVICESEPARATOR + OnlineServer.getInstance().getServer())
            dataList.add(data)
        }

    }

    @Override
    void sessionCreated() {
        super.sessionCreated()
    }

    @Override
    void sessionClosed(int close) {
        super.sessionClosed(close)
    }
    private void cancelStoreDataTimer(){
        if(storeDataTimer != null){
            storeDataTimer.cancel()
            storeDataTimer = null
        }
    }
}
