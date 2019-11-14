package com.docker.storage.adapters.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import com.alibaba.fastjson.JSON;
import com.docker.storage.DBException;
import com.docker.storage.adapters.ServersService;
import com.docker.storage.mongodb.daos.ServersDAO;
import com.docker.utils.SpringContextUtil;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class ServersServiceImpl implements ServersService {
    @Resource
    private ServersDAO serversDAO;
    @Override
    public Document getServerConfig(String serverType) throws CoreException {
        try {
            FindIterable<Document> iterable = serversDAO.query(new Document().append("_id", serverType));
            MongoCursor<Document> cursor = iterable.iterator();
            if(cursor.hasNext())
                return cursor.next();
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query server config for " + serverType + " failed, " + e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteServerConfig(Bson bson) throws CoreException{
        try {
            serversDAO.delete(bson);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_DELETE_FAILED, "Delete server config failed, bson: " + JSON.toJSONString(bson));
        }
    }

    @Override
    public void addServerConfig(Document document) throws CoreException {
        try {
            serversDAO.add(document);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_ADD_FAILED, "Add server config failed, bson: " + JSON.toJSONString(document));
        }
    }

    @Override
    public List<Document> getServerConfigs() throws CoreException {
        try {
            FindIterable<Document> iterable = serversDAO.find();
            MongoCursor<Document> cursor = iterable.iterator();
            List list = new ArrayList();
            while (cursor.hasNext()){
                list.add(cursor.next());
            }
            return list;
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query server config failed, " + e.getMessage());
        }
    }
}
