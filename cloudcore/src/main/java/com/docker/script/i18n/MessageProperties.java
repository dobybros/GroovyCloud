package com.docker.script.i18n;

import com.docker.utils.AutoReloadProperties;

/**
 * Created by zhanjing on 2017/7/17.
 */
public class MessageProperties extends AutoReloadProperties {
    public String getMessage(String key) {
        return getMessage(key, null, null);
    }
    public String getMessage(String key, String[] parameters) {
        return getMessage(key, parameters, null);
    }
    public String getMessage(String key, String[] parameters, String defaultValue) {

        String value = this.getProperty(key, defaultValue);
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (value.contains("#{" + i + "}"))
                    value = value.replace("#{" + i + "}", parameters[i]);
            }
        }
        return value;
    }

}
