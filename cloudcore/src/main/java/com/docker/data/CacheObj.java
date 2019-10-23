package com.docker.data;

import java.lang.reflect.Method;

public class CacheObj {
    private String cacheMethod;
    private Method method;
    private Long expired;
    private String prefix;
    private String spelKey;
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

    public String getSpelKey() {
        return spelKey;
    }

    public void setSpelKey(String spelKey) {
        this.spelKey = spelKey;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }

    public Boolean isEmpty() {
        if (spelKey != null && !spelKey.isEmpty() && expired != null && prefix != null) {
            return false;
        }
        return true;
    }
}
