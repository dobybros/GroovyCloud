package chat.errors;


import chat.logs.LoggerEx;

import java.util.HashMap;
import java.util.List;


public class CoreException extends Exception {
    private static final long serialVersionUID = -3101325177490138661L;
    private List<CoreException> moreExceptions;
    private HashMap<String, Object> infoMap;
    private Object data;
    public final static String LEVEL_INFO = "INFO";
    public final static String LEVEL_WARN = "WARN";
    public final static String LEVEL_ERROR = "ERROR";
    public final static String LEVEL_FATAL = "FATAL";

    public CoreException() {
    }

    public CoreException(int code) {
        this.code = code;
    }

    public CoreException(int code, String message, String logLevel) {
        this(code, message);
        this.logLevel = logLevel;
    }

    public CoreException(String logLevel, int code){
        this.code = code;
        this.logLevel = logLevel;
    }

    public CoreException(int code, String[] parameters, String message) {
        this(code, message);
        this.parameters = parameters;
    }

    public CoreException(int code, Object data, String message) {
        this(code, message);
        this.data = data;
    }

    public CoreException(int code, String message) {
        super(message);
        this.code = code;
    }

    public CoreException(int code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }

    public CoreException(String message) {
        super(message);
    }

    public CoreException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    private int code;
    private String logLevel = LEVEL_ERROR;
    private String[] parameters;

    /**
     *
     */
    public String toString() {
        return "code: " + code + " | message: " + this.getMessage();
    }

    public List<CoreException> getMoreExceptions() {
        return moreExceptions;
    }

    public void setMoreExceptions(List<CoreException> moreExceptions) {
        this.moreExceptions = moreExceptions;
    }

    public CoreException setInfo(String key, Object value) {
        if (infoMap == null)
            infoMap = new HashMap<String, Object>();
        infoMap.put(key, value);
        return this;
    }

    public Object getInfo(String key) {
        if (infoMap == null)
            return null;
        return infoMap.get(key);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void log(String TAG, String message){
        switch (logLevel){
            case LEVEL_INFO:
                LoggerEx.info(TAG, message);
                break;
            case LEVEL_WARN:
                LoggerEx.warn(TAG, message);
                break;
            case LEVEL_FATAL:
                LoggerEx.fatal(TAG, message);
                break;
            default:
                LoggerEx.error(TAG, message);
                break;
        }
    }
}