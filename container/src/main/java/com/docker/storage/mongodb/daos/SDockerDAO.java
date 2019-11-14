package com.docker.storage.mongodb.daos;

import com.docker.data.DataObject;
import com.docker.data.SDocker;
import com.docker.storage.mongodb.BaseDAO;
import org.bson.Document;


/**
 * db.serverstatus.ensureIndex({"server" : 1}, {"unique" : true})
 *
 * @author aplombchen
 */
public class SDockerDAO extends BaseDAO {
    public static final String COLLECTION = "sdockers";

    public SDockerDAO() {
        setCollectionName(COLLECTION);
    }

    @Override
    public Class<? extends DataObject> getDataObjectClass(Document dbObj) {
        return SDocker.class;
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
