package com.docker.data;

import org.bson.Document;

import java.util.List;
import java.util.Map;

public class ScheduleTask {
    public static final String FIELD_ID = "_id";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_REASON = "reason";
    private String _id;
    private Integer status;
    private String reason;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    public void fromDocument(Document dbObj) {
        _id = dbObj.getString("_id");
        status = dbObj.getInteger("status");
        reason = dbObj.getString("reason");
    }
}
