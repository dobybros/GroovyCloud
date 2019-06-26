package com.docker.storage.mongodb.daos;

import com.docker.data.DataObject;
import com.docker.data.Lan;
import com.docker.storage.mongodb.BaseDAO;
import org.bson.Document;


/**
 * db.serverstatus.ensureIndex({"server" : 1}, {"unique" : true})
 * 
 * @author aplombchen
 *
 */
public class LansDAO extends BaseDAO {
	public static final String COLLECTION = "lans";

	public LansDAO(){
		setCollectionName(COLLECTION);
	}

	@Override
	public Document[] getIndexes() {
		return new Document[] {};
	}

	@Override
	public Class<? extends DataObject> getDataObjectClass(Document dbObj) {
		return Lan.class;
	}

	@Override
	public Document getShardKeys() {
		return null;
	}
}
