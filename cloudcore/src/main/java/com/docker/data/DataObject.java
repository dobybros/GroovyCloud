package com.docker.data;

import com.docker.storage.mongodb.CleanDocument;
import org.bson.Document;
import org.bson.types.ObjectId;


public abstract class DataObject extends connectors.mongodb.codec.DataObject{
	
	public boolean isIdGenerated(){
		if(id == null){
			return false;
		}
		return true;
	}
	
	public void generateId(){
//		CRC32 crc = new CRC32();
//		crc.update(UUID.randomUUID().toString().getBytes());
//		id = colName + "_" + crc.getValue();
//		id = colName + "_" + UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
		if(id == null)
			id = ObjectId.get().toString();
	}
	public Document toDocument(){
		Document dbObj1 = new CleanDocument();//TODO need CleanDocument implementation like CleanDBObject
//		Document dbObj1 = new Document();
		if(id != null)
			dbObj1.put(FIELD_ID, id);
		return dbObj1;
	}
	
	public void fromDocument(Document dbObj){
		Object idObj = (Object) dbObj.get(FIELD_ID);
		if(idObj != null) 
			id = idObj.toString();
	}
	
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataObject other = (DataObject) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
