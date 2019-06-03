package com.docker.storage.mongodb.daos;

import com.docker.data.DataObject;
import com.docker.storage.mongodb.BaseDAO;
import org.bson.Document;


/**
 * db.serverstatus.ensureIndex({"server" : 1}, {"unique" : true})
 * 
 * @author aplombchen
 *
 */
public class ServersDAO extends BaseDAO {
	public static final String COLLECTION = "servers";

	public ServersDAO(){
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
