package com.docker.storage.mongodb.daos;

import com.docker.data.DataObject;
import com.docker.storage.mongodb.BaseDAO;
import org.bson.Document;

/**
 * Created by lick on 2019/6/3.
 * Description：
 */
public class DeployServiceVersionDAO extends BaseDAO {
    public static final String COLLECTION = "deployserviceversion";

    public DeployServiceVersionDAO(){
        setCollectionName(COLLECTION);
    }

    @Override
    public Document[] getIndexes() {
        return new Document[] {};
    }

    @Override
    public Class<? extends DataObject> getDataObjectClass(Document dbObj) {
        return DataObject.class;
    }
    @Override
    public Document getShardKeys() {
        return null;
    }
}
