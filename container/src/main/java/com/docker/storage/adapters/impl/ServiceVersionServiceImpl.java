package com.docker.storage.adapters.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import com.alibaba.fastjson.JSON;
import com.docker.data.ServiceVersion;
import com.docker.storage.DBException;
import com.docker.storage.adapters.ServiceVersionService;
import com.docker.storage.mongodb.daos.ServiceVersionDAO;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lick on 2019/6/3.
 * Description：
 */
public class ServiceVersionServiceImpl implements ServiceVersionService {
    private ServiceVersionDAO serviceVersionDAO;

    @Override
    public List<ServiceVersion> getServiceVersions(String serverType) throws CoreException {
        List<ServiceVersion> serviceVersions = new ArrayList<>();
//        Document query = new Document().append("serverType", new Document().append("$in", new ArrayList<>().add(serverType)));
        Document query = new Document();
//                .append("serverType." + serverType, new Document().append("$exists", true));
        FindIterable<Document> iterable = null;
        try {
            iterable = serviceVersionDAO.query(query);
        } catch (DBException e) {
            e.printStackTrace();
        }
        MongoCursor<Document> cursor = iterable.iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            List serverTypes = (ArrayList) doc.get("serverType");
            if (serverTypes.contains(serverType)) {
                ServiceVersion serviceVersion = new ServiceVersion();
                serviceVersion.fromDocument(doc);
                serviceVersions.add(serviceVersion);
            }
        }
        return serviceVersions;
    }

    public ServiceVersionDAO getServiceVersionDAO() {
        return serviceVersionDAO;
    }

    public void setServiceVersionDAO(ServiceVersionDAO serviceVersionDAO) {
        this.serviceVersionDAO = serviceVersionDAO;
    }

    @Override
    public List<ServiceVersion> getServiceVersionsAll() throws CoreException {
        try {
            FindIterable<Document> iterable = serviceVersionDAO.find();
            MongoCursor<Document> cursor = iterable.iterator();
            List<ServiceVersion> list = new ArrayList<>();
            while (cursor.hasNext()) {
                Document document = cursor.next();
                ServiceVersion serviceVersion = new ServiceVersion();
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
    public void addServiceVersion(ServiceVersion serviceVersion) throws CoreException {
        if (serviceVersion.get_id() == null) {
            serviceVersion.set_id(ObjectId.get().toString());
        }
        try {
            serviceVersionDAO.updateOne(new Document().append("_id", serviceVersion.get_id()), new Document().append("$set", serviceVersion.toDocument()), true);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_ADD_FAILED, "Add service version failed, serviceVersion: " + JSON.toJSONString(serviceVersion));
        }
    }

    @Override
    public void deleteServiceVersion(Bson bson) throws CoreException {
        try {
            serviceVersionDAO.delete(bson);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_DELETE_FAILED, "Delete service version  failed, bson: " + JSON.toJSONString(bson));
        }
    }
}
