package com.dobybros.chat.log;

import com.dobybros.chat.data.DataObject;
import com.dobybros.chat.log.sublogindex.MessageLog;
import org.bson.Document;


/**
 * This class provides common fields of log of need indexing
 * @author Aplomb
 *
 */
public abstract class LogIndex extends DataObject {
	private static final String FIELD_LOGTYPE_MESSAGE = "message";
	public static String FIELD_USERID = "uid";
    public static String FIELD_INDEXTYPE = "itype";
    public static String FIELD_TIME = "time";
    public static String FIELD_LOGTYPE = "log";
    public static String FIELD_CLIENTID = "cid";
    /**
     * 打点位置源(发起改动作的页面)
     */
    public static String FIELD_SOURCE = "src";
    /**
     * 打点动作源(程序行为/用户行为)
     */
    public static String FIELD_BEHAVIOR = "beh";
	public static String FIELD_SERVICE = "ser";
    private String userId;
    private String indexType;
    private Long time;
    private String source;
    private String behavior;
	private LogType logType;
	private String service;

    public LogIndex() {
    }

	@Override
    public void fromDocument(Document dbObj) {
    	super.fromDocument(dbObj);
        time = (Long) dbObj.get(FIELD_TIME);
        userId = (String) dbObj.get(FIELD_USERID);
        indexType = (String) dbObj.get(FIELD_INDEXTYPE);
        source = (String) dbObj.get(FIELD_SOURCE);
        behavior = (String) dbObj.get(FIELD_BEHAVIOR);
		service = (String) dbObj.get(FIELD_SERVICE);
        String logTypeStr = (String) dbObj.get(FIELD_LOGTYPE);
        if(logTypeStr != null) {
        	logType = LogType.getLogType(logTypeStr);
        }
    }

	@Override
    public Document toDocument() {
    	Document dbObj = super.toDocument();
    	dbObj.put(FIELD_INDEXTYPE, indexType);
    	dbObj.put(FIELD_USERID, userId);
    	dbObj.put(FIELD_TIME, time);
    	dbObj.put(FIELD_SOURCE, source);
    	dbObj.put(FIELD_BEHAVIOR, behavior);
		dbObj.put(FIELD_SERVICE, service);
		if (logType != null)
			dbObj.put(FIELD_LOGTYPE, logType.name);
    	return dbObj;
    }

    public String getIndexType() {
        return indexType;
    }

	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

	public LogType getLogType() {
		return logType;
	}

	public void setLogType(LogType logType) {
		this.logType = logType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getBehavior() {
		return behavior;
	}

	public void setBehavior(String behavior) {
		this.behavior = behavior;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public static enum LogType {
		Message(FIELD_LOGTYPE_MESSAGE, MessageLog.class);

		private String name;
		private Class<? extends LogIndex> dataObjectClass;

		LogType(String name, Class<? extends LogIndex> dataObjectClass) {
			this.name = name;
			this.dataObjectClass = dataObjectClass;
		}

		public static LogType getLogType(String str) {
			if (str != null) {
				switch (str) {
					case FIELD_LOGTYPE_MESSAGE:
						return LogType.Message;
				}
			}
			return null;
		}

		public static LogType getLogType(Document dbObj) {
			String type = (String) dbObj.get(LogIndex.FIELD_LOGTYPE);
			if (type != null) {
				LogType logType = getLogType(type);
				return logType;
			}
			return null;
		}

		public String getName() {
			return name;
		}

		public Class<? extends LogIndex> getDataObjectClass() {
			return dataObjectClass;
		}
	}
}
