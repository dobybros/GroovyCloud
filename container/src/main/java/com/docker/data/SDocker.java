package com.docker.data;

import org.bson.Document;

public class SDocker extends DataObject {
    public static final String FIELD_SDOCKER_SERVERTYPE = "serverType";
    public static final String FIELD_SDOCKER_IP = "ip";
    public static final String FIELD_SDOCKER_PORT = "port";
    /**
     * 服务器的类型， login， gateway， presence等
     */
    private String ip;
    private Integer port;
    private String serverType;

    @Override
    public void fromDocument(Document dbObj) {
        super.fromDocument(dbObj);
        serverType = (String) dbObj.get(FIELD_SDOCKER_SERVERTYPE);
        ip = (String) dbObj.get(FIELD_SDOCKER_IP);
        port = (Integer) dbObj.get(FIELD_SDOCKER_PORT);
    }

    @Override
    public Document toDocument() {
        Document dbObj = super.toDocument();
        if (ip != null)
            dbObj.put(FIELD_SDOCKER_IP, ip);
        if (port != null)
            dbObj.put(FIELD_SDOCKER_PORT, port);
        if (serverType != null)
            dbObj.put(FIELD_SDOCKER_SERVERTYPE, serverType);
        return dbObj;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

}
