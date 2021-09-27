package com.docker.storage.adapters.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import com.alibaba.fastjson.JSON;
import com.docker.data.DeployServiceVersion;
import com.docker.storage.DBException;
import com.docker.storage.adapters.DeployServiceVersionService;
import com.docker.storage.mongodb.daos.DeployServiceVersionDAO;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lick on 2019/6/3.
 * Description：
 */
public class DeployServiceVersionServiceImpl implements DeployServiceVersionService {
    private DeployServiceVersionDAO deployServiceVersionDAO;

    @Override
    public DeployServiceVersion getServiceVersion(String serverType) throws CoreException {
        try {
            Document query = new Document();
            query.append(DeployServiceVersion.DEPLOY_SERVERTYPE, serverType);
            FindIterable iterable = deployServiceVersionDAO.query(query);
            MongoCursor<Document> cursor = iterable.iterator();
            if(cursor.hasNext()){
                Document document = cursor.next();
                DeployServiceVersion deployServiceVersion = new DeployServiceVersion();
                deployServiceVersion.fromDocument(document);
                return deployServiceVersion;
            }
            return null;
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query server config failed, " + e.getMessage());
        }
    }

    @Override
    public void deleteServiceVersion(String id) throws CoreException {
        try {
            deployServiceVersionDAO.delete(new Document().append(DeployServiceVersion.ID, id));
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_DELETE_FAILED, "Delete service version  failed, id: " + id);
        }
    }

    @Override
    public DeployServiceVersion getDeployServiceVersion(String id) throws CoreException {
        try {
            Document query = new Document();
            query.append(DeployServiceVersion.ID, id);
            FindIterable iterable = deployServiceVersionDAO.query(query);
            MongoCursor<Document> cursor = iterable.iterator();
            if(cursor.hasNext()){
                Document document = cursor.next();
                DeployServiceVersion deployServiceVersion = new DeployServiceVersion();
                deployServiceVersion.fromDocument(document);
                return deployServiceVersion;
            }
            return null;
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query server config failed, " + e.getMessage());
        }
    }

    @Override
    public DeployServiceVersion getDeployServiceVersionByDeployId(String deployId) throws CoreException {
        try {
            Document query = new Document();
            query.append(DeployServiceVersion.DEPLOY_ID, deployId);
            FindIterable iterable = deployServiceVersionDAO.query(query);
            MongoCursor<Document> cursor = iterable.iterator();
            if(cursor.hasNext()){
                Document document = cursor.next();
                DeployServiceVersion deployServiceVersion = new DeployServiceVersion();
                deployServiceVersion.fromDocument(document);
                return deployServiceVersion;
            }
            return null;
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query server config failed, " + e.getMessage());
        }
    }

    public DeployServiceVersionDAO getDeployServiceVersionDAO() {
        return deployServiceVersionDAO;
    }

    public void setDeployServiceVersionDAO(DeployServiceVersionDAO deployServiceVersionDAO) {
        this.deployServiceVersionDAO = deployServiceVersionDAO;
    }

    @Override
    public List<DeployServiceVersion> getServiceVersionsAll() throws CoreException {
        try {
            FindIterable<Document> iterable = deployServiceVersionDAO.find();
            MongoCursor<Document> cursor = iterable.iterator();
            List<DeployServiceVersion> list = new ArrayList<>();
            while (cursor.hasNext()) {
                Document document = cursor.next();
                DeployServiceVersion serviceVersion = new DeployServiceVersion();
                serviceVersion.fromDocument(document);
                list.add(serviceVersion);
            }
            return list;
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query server config failed, " + e.getMessage());
        }
    }

    @Override
    public void addServiceVersion(DeployServiceVersion serviceVersion) throws CoreException {
        if (serviceVersion.get_id() == null) {
            serviceVersion.set_id(ObjectId.get().toString());
        }
        try {
            deployServiceVersionDAO.updateOne(new Document().append("_id", serviceVersion.get_id()), new Document().append("$set", serviceVersion.toDocument()), true);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_ADD_FAILED, "Add service version failed, serviceVersion: " + JSON.toJSONString(serviceVersion));
        }
    }

    @Override
    public void updateTheServiceVersion(String service, String version) throws CoreException{
        try {
            Document query = new Document();
            query.append("serviceVersions." + service, new Document().append("$ne", null));
            Document update = new Document();
            update.append("$set", new Document().append("serviceVersions." + service, version));
            deployServiceVersionDAO.updateMany(query, update, false);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Update service version  failed, servie: " + service + ",version: " + version);
        }
    }

    @Override
    public void updateServiceVersionDeployId(String id, String deployId) throws CoreException {
        try {
            Document query = new Document();
            query.append(DeployServiceVersion.ID, id);
            Document update = new Document();
            update.append("$set", new Document().append(DeployServiceVersion.DEPLOY_ID, deployId));
            deployServiceVersionDAO.updateOne(query, update, false);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Update deployId  failed, id : " + id + ",deployId: " + deployId);
        }
    }

    @Override
    public void deleteService(String serverType, String service) throws CoreException {
        try {
            Document query = new Document();
            query.append(DeployServiceVersion.DEPLOY_SERVERTYPE, serverType);
            Document update = new Document();
            update.append("$unset", new Document().append("serviceVersions." + service, "0"));
            deployServiceVersionDAO.updateOne(query, update, false);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_DELETE_FAILED, "Delete service version  failed, servie: " + service);
        }
    }

    @Override
    public void addService(String serverType, String service, String version) throws CoreException{
        try {
            Document query = new Document();
            query.append(DeployServiceVersion.DEPLOY_SERVERTYPE, serverType);
            Document update = new Document();
            update.append("$set", new Document().append("serviceVersions." + service, version));
            deployServiceVersionDAO.updateOne(query, update, false);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_DELETE_FAILED, "Add service version  failed, servie: " + service);
        }
    }

    @Override
    public void updateBaseJarVersion(String id, String baseJar, String version) throws CoreException {
        try {
            Document query = new Document();
            query.append("_id", id);
            Document update = new Document();
            update.append("$set", new Document().append(DeployServiceVersion.DEPLOY_BASE_JAR_VERSIONS + "." + baseJar, version));
            deployServiceVersionDAO.updateOne(query, update, false);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Delete service version  failed, baseJar: " + baseJar);
        }
    }

    @Override
    public void updateBaseJarVersions(String id, Map<String, String> baseJarVersions) throws CoreException {
        try {
            Document query = new Document();
            query.append("_id", id);
            Document update = new Document();
            update.append("$set", new Document().append(DeployServiceVersion.DEPLOY_BASE_JAR_VERSIONS, baseJarVersions));
            deployServiceVersionDAO.updateOne(query, update, false);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Delete service version  failed, baseJarVersions: " + JSON.toJSONString(baseJarVersions));
        }
    }

    @Override
    public void updateServers(String id, Map<String, Map<String, Object>> servers) throws CoreException {
        try {
            Document query = new Document();
            query.append("_id", id);
            Document update = new Document();
            update.append("$set", new Document().append(DeployServiceVersion.DEPLOY_SERVERS, servers));
            deployServiceVersionDAO.updateOne(query, update, false);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Delete service version  failed, servers: " + JSON.toJSONString(servers));
        }
    }
}
