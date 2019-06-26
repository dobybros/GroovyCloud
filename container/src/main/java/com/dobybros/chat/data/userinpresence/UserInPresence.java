package com.dobybros.chat.data.userinpresence;

/**
 * Created by zhanjing on 2017/9/12.
 *
 */
public class UserInPresence {

    /**
     * Server's lanId
     */
    private String lanId;

    public UserInPresence() {
    }

    public UserInPresence(String lanId) {
        this.lanId = lanId;
    }
    public String getLanId() {
        return lanId;
    }
    public void setLanId(String lanId) {
        this.lanId = lanId;
    }

    public String toString() {
        return lanId;
    }

}
