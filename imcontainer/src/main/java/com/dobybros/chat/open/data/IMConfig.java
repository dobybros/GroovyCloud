package com.dobybros.chat.open.data;

/**
 * @author lick
 * @date 2019/11/19
 */
public class IMConfig {
    private Integer pingInterval = 8;  //s

    public Integer getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(Integer pingInterval) {
        this.pingInterval = pingInterval;
    }
}
