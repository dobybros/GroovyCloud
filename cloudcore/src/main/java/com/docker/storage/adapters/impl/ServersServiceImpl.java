package com.docker.storage.adapters.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import com.docker.storage.DBException;
import com.docker.storage.adapters.ServersService;
import com.docker.storage.mongodb.daos.ServersDAO;
import com.docker.utils.SpringContextUtil;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import javax.annotation.Resource;

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
}
