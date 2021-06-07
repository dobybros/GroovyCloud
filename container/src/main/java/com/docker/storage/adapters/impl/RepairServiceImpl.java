package com.docker.storage.adapters.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.docker.data.DataObject;
import com.docker.data.RepairData;
import com.docker.storage.DBException;
import com.docker.storage.adapters.RepairService;
import com.docker.storage.mongodb.daos.RepairDAO;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bson.Document;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lick on 2020/4/14.
 * Description：
 */
public class RepairServiceImpl implements RepairService {
    private final String TAG = RepairServiceImpl.class.getSimpleName();
    @Resource
    private RepairDAO repairDAO;

    @Override
    public List<RepairData> getAllRepairDatas() throws CoreException {
        try {
            Document query = new Document();
            FindIterable<Document> iterable = repairDAO.query(query);
            //TODO : 这里的sort为后面的排序字段，并没有多字段排序的结果，如果想使用多字段排序写法应该是：
            // iterable.sort(new Document(RepairData.FIELD_SERVERNAME, -1).append(RepairData.FIELD_CREATETIME, -1));
            iterable.sort(new Document(RepairData.FIELD_SERVERNAME, -1));
            iterable.sort(new Document(RepairData.FIELD_CREATETIME, -1));
            MongoCursor<Document> cursor = iterable.iterator();
            List<RepairData> RepairDatas = new ArrayList<>();
            while(cursor.hasNext()) {
                Document doc = cursor.next();
                RepairData repairData = new RepairData();
                repairData.fromDocument(doc);
                RepairDatas.add(repairData);
            }
            return RepairDatas;
        } catch (DBException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Query lans failed, " + ExceptionUtils.getFullStackTrace(e));
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query repairData failed, " + e.getMessage());
        }
    }

    @Override
    public void updateRepairData(RepairData repairData) throws CoreException {
        try {
            Document update = repairData.toDocument();
            update.remove(DataObject.FIELD_ID);
            repairDAO.updateOne(new Document().append(RepairData.FIELD_ID, repairData.getId()), new Document().append("$set", update), false);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Update repairData " + repairData.toString() + " failed, " + e.getMessage());
        }
    }

    @Override
    public void addRepairData(RepairData repairData) throws CoreException {
        try {
            repairDAO.add(repairData);
        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_ADD_FAILED, "Add repairData " + repairData.toString() + " failed, " + e.getMessage());
        }
    }

    @Override
    public RepairData getRepairData(String id) throws CoreException {
        try {
            Document query = new Document().append(RepairData.FIELD_ID, id);
            RepairData repairData = (RepairData) repairDAO.findOne(query);
            return repairData;
        } catch (DBException e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Query repairData " + id + " failed, " + ExceptionUtils.getFullStackTrace(e));
            throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query repairData " + id + " failed, " + e.getMessage());
        }
    }
}
