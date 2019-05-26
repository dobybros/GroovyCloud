package com.dobybros.chat.log;

import chat.logs.LoggerEx;
import com.dobybros.chat.data.DataObject;
import com.mongodb.BasicDBList;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class BulkLog extends DataObject {
	private List<LogIndex> logIndexes; 
	private Integer retryTimes;
	public static final String FIELD_BULKLOG_LOGS = "logs";
	public static final String FIELD_BULKLOG_RETRYTIMES = "rtry";

	private static final String TAG = BulkLog.class.getSimpleName();
	
	@Override
    public void fromDocument(Document dbObj) {
        super.fromDocument(dbObj);
        List<?> logs = (List<?>) dbObj.get(FIELD_BULKLOG_LOGS);
        if(logs != null) {
        	logIndexes = new ArrayList<>();
        	for(int i = 0; i < logs.size(); i++) {
        		Document logObj = (Document) logs.get(i);
        		LogIndex.LogType logType = LogIndex.LogType.getLogType(logObj);
        		if(logType != null) {
        			Class<? extends LogIndex> dataObjectClass = logType.getDataObjectClass();
        			if(dataObjectClass != null) {
        				LogIndex dataObject = null;
        				try {
							dataObject = dataObjectClass.newInstance();
						} catch (InstantiationException
								| IllegalAccessException e) {
							e.printStackTrace();
						}
        				if(dataObject != null) {
        					try {
        						dataObject.fromDocument(logObj);
        						logIndexes.add(dataObject);
							} catch (Throwable t) {
								LoggerEx.error(TAG, "Create bulk log failed, " + t.getMessage() + " the log is dropped...");
							}
        				}
        			}
        		} else {
        			LoggerEx.warn(TAG, "Unrecognized logtype for " + logObj + " ignored...");
        		}
        	}
        }
        retryTimes = (Integer) dbObj.get(FIELD_BULKLOG_RETRYTIMES);
    }
	
	@Override
    public Document toDocument() {
		Document dbObj = super.toDocument();
    	BasicDBList logs = new BasicDBList();
    	if(logIndexes != null && !logIndexes.isEmpty()) {
    		for(LogIndex logIndex : logIndexes) {
    			logs.add(logIndex.toDocument());
    		}
    	}
    	dbObj.put(FIELD_BULKLOG_LOGS, logs);
    	dbObj.put(FIELD_BULKLOG_RETRYTIMES, retryTimes);
    	return dbObj;
    }

	public List<LogIndex> getLogIndexes() {
		return logIndexes;
	}

	public void setLogIndexes(List<LogIndex> logIndexes) {
		this.logIndexes = logIndexes;
	}

	public Integer getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(Integer retryTimes) {
		this.retryTimes = retryTimes;
	}
}
