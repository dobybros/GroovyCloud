package com.docker.script.i18n;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhanjing on 2017/7/17.
 */
public class I18nHandler {

    private Map<String, MessageProperties> msgPropertyMap = new ConcurrentHashMap<>();

    public String getI18nMessage(String language, String key, String[] parameters, String defaultValue) {
        if (key != null) {
            language = language == null ? "en_US" : language;
            MessageProperties messageProperties = this.getMsgPropertyMap().get(language);
            if (messageProperties == null) {
                messageProperties = this.getMsgPropertyMap().get("en_US");
            }
            if (messageProperties != null)
                return messageProperties.getMessage(key, parameters, defaultValue);
            else
                return defaultValue;
        } else
            return defaultValue;
    }

    public Map<String, MessageProperties> getMsgPropertyMap() {
        return msgPropertyMap;
    }

    public void setMsgPropertyMap(Map<String, MessageProperties> msgPropertyMap) {
        this.msgPropertyMap = msgPropertyMap;
    }
}
