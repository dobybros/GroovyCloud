package com.docker.data;

import com.docker.storage.mongodb.CleanDocument;
import org.bson.Document;

import java.util.Map;

/**
 * Created by lick on 2019/5/30.
 * Description：
 */
public class DeployServiceVersion {
    public static final String ID = "_id";
    public static final String SERVERTYPE = "serverType";
    public static final String TYPE = "type";
    public static final String GROOVYCLOUTYPE = "groovyCloudType";
    public static final String SERVICEVERSIONS = "serviceVersions";
    public static final String BASEJARVERSIONS = "baseJarVersions";
    public static final String SERVERS = "servers";
    public static final String DEPLOYID = "deployId";
    private String _id;
    private String serverType;
    private String groovyCloudType;
    public static String TYPE_DEFAULT = "default";
    private String type;
    private String deployId;
    private Map<String, String> serviceVersions;
    private Map<String, String> baseJarVersions;
    private Map<String, Map<String, Object>> servers;

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

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getGroovyCloudType() {
        return groovyCloudType;
    }

    public void setGroovyCloudType(String groovyCloudType) {
        this.groovyCloudType = groovyCloudType;
    }

    public Map<String, String> getBaseJarVersions() {
        return baseJarVersions;
    }

    public void setBaseJarVersions(Map<String, String> baseJarVersions) {
        this.baseJarVersions = baseJarVersions;
    }

    public Map<String, Map<String, Object>> getServers() {
        return servers;
    }

    public void setServers(Map<String, Map<String, Object>> servers) {
        this.servers = servers;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getDeployId() {
        return deployId;
    }

    public void setDeployId(String deployId) {
        this.deployId = deployId;
    }

    public void fromDocument(Document dbObj) {
        _id = dbObj.getString(ID);
        type = dbObj.getString(TYPE);
        serverType =  dbObj.getString(SERVERTYPE);
        serviceVersions = (Map<String, String>)dbObj.get(SERVICEVERSIONS);
        groovyCloudType = dbObj.getString(GROOVYCLOUTYPE);
        baseJarVersions = (Map<String, String>) dbObj.get(BASEJARVERSIONS);
        servers = (Map<String, Map<String, Object>>) dbObj.get(SERVERS);
        deployId = dbObj.getString(DEPLOYID);
    }
    public Document toDocument() {
        Document dbObj = new CleanDocument();
       dbObj.append(ID, _id)
               .append(TYPE, type)
               .append(SERVERTYPE, serverType)
               .append(SERVICEVERSIONS, serviceVersions)
               .append(GROOVYCLOUTYPE, groovyCloudType)
               .append(DEPLOYID, deployId)
               .append(BASEJARVERSIONS, baseJarVersions)
               .append(SERVERS, servers);
        return dbObj;
    }
}
