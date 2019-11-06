package com.docker.storage.mongodb.daos;

import com.docker.data.DataObject;
import com.docker.storage.mongodb.BaseDAO;
import org.bson.Document;

public class ScheduledTaskDAO extends BaseDAO {
    public static final String COLLECTION = "scheduledtasks";
    public ScheduledTaskDAO(){
        setCollectionName(COLLECTION);
    }
    @Override
    public Class<? extends DataObject> getDataObjectClass(Document obj) {
        return DataObject.class;
    }

    @Override
    public Document[] getIndexes() {
        return new Document[]{};
    }

    @Override
    public Document getShardKeys() {
        return null;
    }
}
