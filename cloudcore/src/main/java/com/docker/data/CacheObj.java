package com.docker.data;

import java.lang.reflect.Method;

public class CacheObj {
    private String cacheMethod;
    private Method method;
    private Long expired;
    private String prefix;
    private Object value;
    private String spelKey;
    private String key;
    private String[] paramNames;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getCacheMethod() {
        return cacheMethod;
    }

    public void setCacheMethod(String cacheMethod) {
        this.cacheMethod = cacheMethod;
    }

    public Long getExpired() {
        return expired;
    }

    public void setExpired(Long expired) {
        this.expired = expired;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSpelKey() {
        return spelKey;
    }

    public void setSpelKey(String spelKey) {
        this.spelKey = spelKey;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }

    public Boolean isEmpty() {
        if (spelKey != null && !spelKey.isEmpty() && expired != null && prefix != null && value != null) {
            return false;
        }
        return true;
    }
}
