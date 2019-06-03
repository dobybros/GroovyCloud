package com.docker.storage.mongodb.daos;

import com.docker.data.DataObject;
import com.docker.data.DockerStatus;
import com.docker.storage.mongodb.BaseDAO;
import org.bson.Document;


/**
 * db.serverstatus.ensureIndex({"server" : 1}, {"unique" : true})
 * 
 * @author aplombchen
 *
 */
public class DockerStatusDAO extends BaseDAO {
	public static final String COLLECTION = "dockers";
	
	public static final Document INDEX_ONLINESERVER_SERVER = new Document().append(DockerStatus.FIELD_DOCKERSTATUS_SERVER, 1).append(UNIQUE_KEY, true);
	
	public DockerStatusDAO(){
		setCollectionName(COLLECTION);
	}

	@Override
	public Document[] getIndexes() {
		return new Document[] {INDEX_ONLINESERVER_SERVER};
	}

	@Override
	public Class<? extends DataObject> getDataObjectClass(Document dbObj) {
		return DockerStatus.class;
	}
	@Override
	public Document getShardKeys() {
		return null;
	}
}
