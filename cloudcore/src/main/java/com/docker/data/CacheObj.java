package com.docker.data;

import java.lang.reflect.Method;

public class CacheObj {
    private String cacheMethod;
    private Long expired;
    private String prefix;
    private CacheKeyObj cacheKeyObj;
    private Object value;

    public class CacheKeyObj {
        private Integer index;
        private String key;
        private String field;
        private Method method;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        public Boolean isEmpty() {
            if (key != "") {
                return false;
            }
            return true;
        }
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

    public CacheKeyObj getCacheKeyObj() {
        return cacheKeyObj;
    }

    public void setCacheKeyObj(CacheKeyObj cacheKeyObj) {
        this.cacheKeyObj = cacheKeyObj;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }


    public Boolean isEmpty() {
        if (cacheKeyObj != null && !cacheKeyObj.isEmpty() && expired != null && prefix != null && value != null) {
            return false;
        }
        return true;
    }
}
