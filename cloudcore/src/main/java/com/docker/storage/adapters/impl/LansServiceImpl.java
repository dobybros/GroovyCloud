package com.docker.storage.adapters.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.data.Lan;
import com.docker.storage.DBException;
import com.docker.storage.adapters.LansService;
import com.docker.storage.mongodb.daos.LansDAO;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bson.Document;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class LansServiceImpl implements LansService {
    private static final String TAG = LansServiceImpl.class.getSimpleName();

    @Resource
    private LansDAO lansDAO;

    @Override
    public Lan getLan(String lanId) throws CoreException {
        try {
            Document query = new Document().append(Lan.FIELD_ID, lanId);
            Lan lan = (Lan) lansDAO.findOne(query);
            return lan;
        } catch (DBException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Query lan " + lanId + " failed, " + ExceptionUtils.getFullStackTrace(e));
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query lan " + lanId + " failed, " + e.getMessage());
        }
    }

    @Override
    public List<Lan> getLans() throws CoreException {
        try {
            Document query = new Document();
            FindIterable<Document> iterable = lansDAO.query(query);
            MongoCursor<Document> cursor = iterable.iterator();
            List<Lan> lans = new ArrayList<>();
            while(cursor.hasNext()) {
                Document doc = cursor.next();
                Lan lan = new Lan();
                lan.fromDocument(doc);
                lans.add(lan);
            }
            return lans;
        } catch (DBException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Query lans failed, " + ExceptionUtils.getFullStackTrace(e));
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query lans failed, " + e.getMessage());
        }
    }
}
