package com.dobybros.chat.storage.mongodb.daos;

import com.dobybros.chat.data.DataObject;
import com.dobybros.chat.log.BulkLog;
import com.dobybros.chat.storage.mongodb.BaseDAO;
import org.bson.Document;


/**
 * db.offlinemessage.ensureIndex({"msg.rids" : 1, "time" : 1})
 *
 * @author aplombchen
 *
 */
public class BulkLogDAO extends BaseDAO {
	public static final String COLLECTION = "logs";


	public BulkLogDAO(){
		setCollectionName(COLLECTION);
	}

	@Override
	public Document[] getIndexes() {
		return new Document[] {};
	}

	@Override
	public Class<? extends DataObject> getDataObjectClass(Document dbObj) {
		return BulkLog.class;
	}
	
	@Override
	public Document getShardKeys() {
		return null;
	}
}
