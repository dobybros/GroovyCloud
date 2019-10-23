package com.dobybros.chat.open.data;

import java.util.Map;

/**
 *
 * Created by zhanjing on 2017/7/27.
 */
public class PNInfo {

    private Integer terminal;

    private boolean needBadge;

    private String soundFile;

    // 自定义badge的显示， 如果这个不为空， 忽略离线数的badge
    private Integer customBadge;

    // 苹果apn可带参数
    private Map<String, String> customPropertyMap;


    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
    }

    public boolean isNeedBadge() {
        return needBadge;
    }

    public void setNeedBadge(boolean needBadge) {
        this.needBadge = needBadge;
    }

    public String getSoundFile() {
        return soundFile;
    }

    public void setSoundFile(String soundFile) {
        this.soundFile = soundFile;
    }

    public Integer getCustomBadge() {
        return customBadge;
    }

    public void setCustomBadge(Integer customBadge) {
        this.customBadge = customBadge;
    }

    public Map<String, String> getCustomPropertyMap() {
        return customPropertyMap;
    }

    public void setCustomPropertyMap(Map<String, String> customPropertyMap) {
        this.customPropertyMap = customPropertyMap;
    }
}
