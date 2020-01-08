package com.docker.data;

import com.docker.storage.mongodb.CleanDocument;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Service {
    public static final String FIELD_SERVICE_SERVICE = "service";
    public static final String FIELD_SERVICE_VERSION = "version";
    public static final String FIELD_SERVICE_MINVERSION = "minVersion";
    public static final String FIELD_SERVICE_UPLOADTIME = "uploadTime";
    public static final String FIELD_SERVICE_TYPE = "type";
    public static final String FIELD_SERVICE_SERVICEANNOTATION = "serviceAnnotations";
    public static final String FIELD_MAXUSERNUMBER = "maxUserNumber";

    public static final int FIELD_SERVER_TYPE_NORMAL = 1;
    public static final int FIELD_SERVER_TYPE_DEPLOY_FAILED = 2;

    private String service;
    private Integer version;
    private Integer minVersion;
    private Long uploadTime;
    private Integer type;
    private Long maxUserNumber;
    private List<ServiceAnnotation> serviceAnnotations;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(Integer minVersion) {
        this.minVersion = minVersion;
    }

    public Long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getMaxUserNumber() {
        return maxUserNumber;
    }

    public void setMaxUserNumber(Long maxUserNumber) {
        this.maxUserNumber = maxUserNumber;
    }

    public void fromDocument(Document dbObj) {
        service = (String) dbObj.get(FIELD_SERVICE_SERVICE);
        version = dbObj.getInteger(FIELD_SERVICE_VERSION);
        minVersion = dbObj.getInteger(FIELD_SERVICE_MINVERSION);
        uploadTime = dbObj.getLong(FIELD_SERVICE_UPLOADTIME);
        type = dbObj.getInteger(FIELD_SERVICE_TYPE);
        maxUserNumber = dbObj.getLong(FIELD_MAXUSERNUMBER);
        List<Document> anDocs = (List<Document>) dbObj.get(FIELD_SERVICE_SERVICEANNOTATION);
        if(anDocs != null) {
            serviceAnnotations = new ArrayList<>();
            for(Document doc : anDocs) {
                ServiceAnnotation serviceAnnotation = new ServiceAnnotation();
                serviceAnnotation.fromDocument(doc);
                serviceAnnotations.add(serviceAnnotation);
            }
        }
    }

    public Document toDocument() {
        Document dbObj = new CleanDocument();
        dbObj.put(FIELD_SERVICE_SERVICE, service);
        dbObj.put(FIELD_SERVICE_MINVERSION, minVersion);
        dbObj.put(FIELD_SERVICE_VERSION, version);
        dbObj.put(FIELD_SERVICE_UPLOADTIME, uploadTime);
        dbObj.put(FIELD_SERVICE_TYPE, type);
        if(maxUserNumber != null){
            dbObj.put(FIELD_MAXUSERNUMBER, maxUserNumber);
        }
        if(serviceAnnotations != null) {
            List<Document> annotationList = new ArrayList<>();
            for(ServiceAnnotation serviceAnnotation : serviceAnnotations) {
                Document anDoc = serviceAnnotation.toDocument();
                annotationList.add(anDoc);
            }
            dbObj.put(FIELD_SERVICE_SERVICEANNOTATION, annotationList);
        }
        return dbObj;
    }

    public List<ServiceAnnotation> getServiceAnnotations(){
        return serviceAnnotations;
    }

    public void appendServiceAnnotation(List<ServiceAnnotation> annotations) {
        if(serviceAnnotations == null)
            serviceAnnotations = new ArrayList<>();
        serviceAnnotations.addAll(annotations);
    }
}
