package com.docker.data;

import org.bson.Document;

/**
 * Created by lick on 2020/4/14.
 * Description：
 */
public class RepairData extends DataObject {
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_CREATETIME = "createTime";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_LASTEXECUTETIME = "lastExecuteTime";
    public static final String FIELD_SERVERURI = "serverUri";
    public static final String FIELD_EXECUTERESULT = "executeResult";

    private String description;
    private String createTime;
    private Integer type;
    private String lastExecuteTime;
    private String serverUri;
    private String executeResult;

    public RepairData() {
    }

    public RepairData(String id, String description, String createTime, Integer type,String lastExecuteTime, String serverUri) {
        this.id = id;
        this.description = description;
        this.createTime = createTime;
        this.type = type;
        this.lastExecuteTime = lastExecuteTime;
        this.serverUri = serverUri;
    }

    @Override
    public void fromDocument(Document dbObj) {
        super.fromDocument(dbObj);
        try {
            description = dbObj.getString(FIELD_DESCRIPTION);
        } catch (Throwable t) {
        }
        try {
            type = dbObj.getInteger(FIELD_TYPE);
        } catch (Throwable t) {
        }
        try {
            createTime = dbObj.getString(FIELD_CREATETIME);
        } catch (Throwable t) {
        }
        try {
            lastExecuteTime = dbObj.getString(FIELD_LASTEXECUTETIME);
        } catch (Throwable t) {
        }
        try {
            serverUri = dbObj.getString(FIELD_SERVERURI);
        } catch (Throwable t) {
        }
        try {
            executeResult = dbObj.getString(FIELD_EXECUTERESULT);
        } catch (Throwable t) {
        }
    }

    @Override
    public String toString() {
        return "id: " + id + ",type: " + type + ",description: " + description + ",createTime: " + createTime + ",lastExecuteTime: " + lastExecuteTime;
    }

    @Override
    public Document toDocument() {
        Document dbDocument = super.toDocument();
        if (type != null) {
            dbDocument.put(FIELD_TYPE, type);
        }
        if (description != null) {
            dbDocument.put(FIELD_DESCRIPTION, description);
        }
        if (createTime != null) {
            dbDocument.put(FIELD_CREATETIME, createTime);
        }
        if (lastExecuteTime != null) {
            dbDocument.put(FIELD_LASTEXECUTETIME, lastExecuteTime);
        }
        if(serverUri != null){
            dbDocument.put(FIELD_SERVERURI, serverUri);
        }
        if(executeResult != null){
            dbDocument.put(FIELD_EXECUTERESULT, executeResult);
        }
        return dbDocument;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getLastExecuteTime() {
        return lastExecuteTime;
    }

    public void setLastExecuteTime(String lastExecuteTime) {
        this.lastExecuteTime = lastExecuteTime;
    }

    public String getServerUri() {
        return serverUri;
    }

    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }

    public String getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(String executeResult) {
        this.executeResult = executeResult;
    }
}
