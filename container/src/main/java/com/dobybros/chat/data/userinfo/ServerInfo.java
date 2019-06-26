package com.dobybros.chat.data.userinfo;
/**
 * Created by zhanjing on 2017/9/11.
 *
 */

public class ServerInfo {

    private String server;
    private String ip;

    private Integer rpcPort;

    private String publicDomain;

    private Integer sslRpcPort;

    private Integer httpPort;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(Integer rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getPublicDomain() {
        return publicDomain;
    }

    public void setPublicDomain(String publicDomain) {
        this.publicDomain = publicDomain;
    }

    public Integer getSslRpcPort() {
        return sslRpcPort;
    }

    public void setSslRpcPort(Integer sslRpcPort) {
        this.sslRpcPort = sslRpcPort;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }
}
