package com.docker.storage.adapters.impl;

import chat.errors.CoreException;
import com.docker.data.DockerStatus;
import com.docker.data.ServiceVersion;
import com.docker.storage.DBException;
import com.docker.storage.adapters.ServiceVersionService;
import com.docker.storage.mongodb.daos.ServiceVersionDAO;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

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
            List serverTypes = (ArrayList)doc.get("serverType");
            if(serverTypes.contains(serverType)){
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
}
