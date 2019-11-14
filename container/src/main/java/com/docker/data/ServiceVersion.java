package com.docker.data;

import com.alibaba.fastjson.JSONObject;
import com.docker.storage.mongodb.CleanDocument;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lick on 2019/5/30.
 * Description：
 */
public class ServiceVersion {
    private String _id;
    private List<String> serverType;
    private String type;
    private Map<String, String> serviceVersions;

    public List<String> getServerType() {
        return serverType;
    }

    public void setServerType(List<String> serverType) {
        this.serverType = serverType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getServiceVersions() {
        return serviceVersions;
    }

    public void setServiceVersions(Map<String, String> serviceVersions) {
        this.serviceVersions = serviceVersions;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
    public void fromDocument(Document dbObj) {
        _id = dbObj.getString("_id");
        type = dbObj.getString("type");
        serverType = (List<String>) dbObj.get("serverType");
        serviceVersions = (Map<String, String>)dbObj.get("serviceVersions");

    }
    public Document toDocument() {
        Document dbObj = new CleanDocument();
       dbObj.append("_id", _id)
               .append("type", type)
               .append("serverType", serverType)
               .append("serviceVersions", serviceVersions);
        return dbObj;
    }
}
