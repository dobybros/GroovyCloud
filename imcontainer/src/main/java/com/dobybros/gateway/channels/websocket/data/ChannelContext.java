package com.dobybros.gateway.channels.websocket.data;

import chat.logs.LoggerEx;
import chat.utils.TimerEx;
import chat.utils.TimerTaskEx;
import com.dobybros.gateway.pack.Pack;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hzj on 2021/9/28 下午6:28
 */
public abstract class ChannelContext {

    public static final String CONTEXT_TAG = ChannelContext.class.getSimpleName();

    public static final String ATTRIBUTE_VERSION = "VERSION";

    private TimerTaskEx closeChannelTask;
    private Long closeInterval = 8000L;

    private String userId;
    private String service;
    private Integer terminal;
    private String ip;
    private String channelId;

    private Byte packVersion;
    private Short encodeVersion;
    private Byte encode;

    private ConcurrentHashMap<String, Object> attributeMap = new ConcurrentHashMap<>();

    public abstract void write(Pack pack);
    public abstract void write(byte[] data, byte type);
    public abstract void write(int code, String description, String forId);
    public abstract void close();
    public abstract String getContextIp();
    public abstract Boolean channelIsActive();

    public Boolean checkParamsNotNull() {
        return StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(service) && terminal != null;
    }

    public Boolean checkAllParamsNotNull() {
        return StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(service) && terminal != null && StringUtils.isNotBlank(channelId);
    }

    /**
     * add attribute
     */
    public void setAttribute(String key, Object value) {
        if (StringUtils.isNotBlank(key) && value != null)
            attributeMap.put(key, value);
    }

    /**
     * get attribute
     */
    public Object getAttribute(String key) {
        return StringUtils.isNotBlank(key) ? attributeMap.get(key) : null;
    }

    /**
     * remove addtribute
     */
    public void removeAttribute(String key) {
        if (StringUtils.isNotBlank(key))
            attributeMap.remove(key);
    }

    /**
     * 如果已没有收到identity，8s后关闭channel
     */
    public void startCloseChannelTask() {
        releaseCloseChannelTask();
        if (closeChannelTask == null) {
            final ChannelContext ctx = this;
            closeChannelTask = new TimerTaskEx(CONTEXT_TAG) {
                @Override
                public void execute() {
                    LoggerEx.info(TAG, "Session closed by timeout after ws session created, " + ctx);
                    ctx.close();
                }
            };
        }
        TimerEx.schedule(closeChannelTask, closeInterval);
    }

    /**
     * 释放timer
     */
    public void releaseCloseChannelTask() {
        if (closeChannelTask != null) {
            try {
                closeChannelTask.cancel();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            closeChannelTask = null;
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
    }

    public String getIp() {
        if (StringUtils.isBlank(ip))
            ip = getContextIp();
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Byte getPackVersion() {
        return packVersion;
    }

    public void setPackVersion(Byte packVersion) {
        this.packVersion = packVersion;
    }

    public Short getEncodeVersion() {
        return encodeVersion;
    }

    public void setEncodeVersion(Short encodeVersion) {
        this.encodeVersion = encodeVersion;
    }

    public Byte getEncode() {
        return encode;
    }

    public void setEncode(Byte encode) {
        this.encode = encode;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        return "userId='" + userId + '\'' +
                ", service='" + service + '\'' +
                ", terminal=" + terminal +
                ", channelId='" + channelId + '\'' +
                ", ip='" + ip;
    }
}
