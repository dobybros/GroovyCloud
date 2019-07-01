package com.dobybros.chat.script.i18n;

import java.util.Map;

/**
 * Created by zhanjing on 2017/7/17.
 */
public class I18nHandler {

    private Map<String, MessageProperties> msgPropertyMap;

    public String getI18nMessage(String locale, String key, String[] parameters, String defaultValue) {
        if (key != null) {
            locale = locale == null ? "en_US" : locale;
            MessageProperties messageProperties = this.getMsgPropertyMap().get(locale);
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
