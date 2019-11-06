package com.docker.storage.adapters.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import com.docker.data.SDocker;
import com.docker.data.ScheduleTask;
import com.docker.storage.DBException;
import com.docker.storage.adapters.ScheduledTaskService;
import com.docker.storage.mongodb.daos.ScheduledTaskDAO;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bson.Document;

import javax.annotation.Resource;

public class ScheduledTaskServiceImpl implements ScheduledTaskService {
    @Resource
    ScheduledTaskDAO scheduledTaskDAO;

    @Override
    public ScheduleTask getSchedeuleTask(String id) throws CoreException{
        try {
            FindIterable<Document> iterable = scheduledTaskDAO.query(new Document().append(ScheduleTask.FIELD_ID, id));
            MongoCursor<Document> cursor = iterable.iterator();
            if (cursor.hasNext()){
                ScheduleTask scheduleTask = new ScheduleTask();
                scheduleTask.fromDocument(cursor.next());
                return scheduleTask;
            }

        } catch (DBException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_SDOCKER_QUERY_FAILED, "Query scheduletask errr, errMsg: " + ExceptionUtils.getFullStackTrace(e));
        }
        return null;

    }

    public void setScheduledTaskDAO(ScheduledTaskDAO scheduledTaskDAO) {
        this.scheduledTaskDAO = scheduledTaskDAO;
    }
}
