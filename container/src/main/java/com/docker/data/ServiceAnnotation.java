package com.docker.data;

import com.docker.storage.mongodb.CleanDocument;
import org.bson.Document;

import java.util.Map;

/**
 * Created by wenqi on 2018/12/4
 */
public class ServiceAnnotation {
    private final String TAG = ServiceAnnotation.class.getSimpleName();
    public static final String ANNOTATION_TYPE = "type";
    public static final String ANNOTATION_PARAMS = "params";
    public static final String ANNOTATION_CLASSNAME = "classname";
    public static final String ANNOTATION_METHODNAME = "methodname";
    public static final String ANNOTATION_ASYNC = "async";
    private String type; //serviceAnnotationType, name of Annotation, like TransactionTry
    private Map<String, Object> annotationParams;
    private String className;
    private String methodName;
    private boolean async = false;

    public void fromDocument(Document document) {

        type = document.getString(ANNOTATION_TYPE);
        annotationParams = document.get(ANNOTATION_PARAMS, Map.class);
        className = document.getString(ANNOTATION_CLASSNAME);
        methodName = document.getString(ANNOTATION_METHODNAME);
        if(document.get(ANNOTATION_ASYNC) != null){
            async = document.getBoolean(ANNOTATION_ASYNC);
        }
    }

    public Document toDocument() {
        CleanDocument document = new CleanDocument();
        document.append(ANNOTATION_TYPE, type)
                .append(ANNOTATION_PARAMS, annotationParams)
                .append(ANNOTATION_CLASSNAME, className)
                .append(ANNOTATION_METHODNAME, methodName)
                .append(ANNOTATION_ASYNC, async);
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
