package com.docker.storage.mongodb.daos;

import com.docker.data.DataObject;
import com.docker.data.RepairData;
import com.docker.storage.mongodb.BaseDAO;
import org.bson.Document;

/**
 * Created by lick on 2020/4/14.
 * Description：
 */
public class RepairDAO extends BaseDAO {
    public static final String COLLECTION = "repairs";
    public RepairDAO(){
        setCollectionName(COLLECTION);
    }
    @Override
    public Class<? extends DataObject> getDataObjectClass(Document obj) {
        return RepairData.class;
    }

    @Override
    public Document[] getIndexes() {
        return new Document[0];
    }

    @Override
    public Document getShardKeys() {
        return null;
    }
}
