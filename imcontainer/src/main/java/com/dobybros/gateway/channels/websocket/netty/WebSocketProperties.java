package com.dobybros.gateway.channels.websocket.netty;

/**
 * Created by hzj on 2021/9/28 下午2:55 
 */
public class WebSocketProperties {

    /**
     * 对外端口
     */
    private Integer publicPort;

    /**
     * 监听端口
     */
    private Integer port;

    /**
     * 读取空闲时间，单位：分钟
     */
    private int readIdleTime = 5;

    /**
     * 写入空闲时间，单位：分钟
     */
    private int writeIdleTime = 5;

    /**
     * 读和写空闲时间，单位：分钟
     */
    private int allIdleTime = 5;

    /**
     * 连接队列长度
     */
    private int backlog = 1024;

    /**
     * 是否启用SSL
     */
    private boolean ssl = false;

    public WebSocketProperties(Integer publicPort, Integer port) {
        this.publicPort = publicPort;
        this.port = port;
    }

    public Integer getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(Integer publicPort) {
        this.publicPort = publicPort;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public int getReadIdleTime() {
        return readIdleTime;
    }

    public void setReadIdleTime(int readIdleTime) {
        this.readIdleTime = readIdleTime;
    }

    public int getWriteIdleTime() {
        return writeIdleTime;
    }

    public void setWriteIdleTime(int writeIdleTime) {
        this.writeIdleTime = writeIdleTime;
    }

    public int getAllIdleTime() {
        return allIdleTime;
    }

    public void setAllIdleTime(int allIdleTime) {
        this.allIdleTime = allIdleTime;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }
}
