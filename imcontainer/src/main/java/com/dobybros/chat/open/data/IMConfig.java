package com.dobybros.chat.open.data;

import java.util.concurrent.TimeUnit;

/**
 * @author lick
 * @date 2019/11/19
 */
public class IMConfig {
    private Long pingInterval = TimeUnit.SECONDS.toMillis(8);  //ms
    private Long maxInactiveInterval = TimeUnit.SECONDS.toMillis(60);//ms

    public Long getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(Long pingInterval) {
        this.pingInterval = pingInterval;
    }

    public Long getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(Long maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }
}
