package com.docker.data;

import com.docker.storage.mongodb.CleanDocument;
import org.bson.Document;

import java.util.Map;

/**
 * Created by wenqi on 2018/12/4
 */
public class ServiceAnnotation {
    public static final String TYPE = "type";
    public static final String ANNOTATIONPARAMS = "params";
    public static final String CLASSNAME = "classname";
    public static final String METHODNAME = "methodname";
    public static final String ASYNC = "async";
    private String type; //serviceAnnotationType, name of Annotation, like TransactionTry
    private Map<String, Object> annotationParams;
    private String className;
    private String methodName;
    private boolean async;

    public void fromDocument(Document document) {
        type = document.getString(TYPE);
        annotationParams = document.get(ANNOTATIONPARAMS, Map.class);
        className = document.getString(CLASSNAME);
        methodName = document.getString(METHODNAME);
        async = document.getBoolean(ASYNC);
    }

    public Document toDocument() {
        CleanDocument document = new CleanDocument();
        document.append(TYPE, type)
                .append(ANNOTATIONPARAMS, annotationParams)
                .append(CLASSNAME, className)
                .append(METHODNAME, methodName)
                .append(ASYNC, async);
        return document;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getAnnotationParams() {
        return annotationParams;
    }

    public void setAnnotationParams(Map<String, Object> annotationParams) {
        this.annotationParams = annotationParams;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
}
