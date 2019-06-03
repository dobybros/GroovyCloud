package com.docker.storage.mongodb;

import com.docker.data.DataObject;
import com.docker.storage.DBException;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public abstract class BaseDAO implements DAO {
	public static final String UNIQUE_KEY = "mongodb_unique";
	public static final String SPARSE = "mongodb_sparse";
	
	protected MongoHelper mongoHelper;
	protected MongoCollection<Document> col;
	protected String collectionName;
	
	public BaseDAO() {
	}
	
	protected int getType(MongoException me) {
		if(me instanceof DuplicateKeyException) 
			return DBException.ERRORTYPE_DUPLICATEKEY;
		if(me instanceof MongoSocketException)
			return DBException.ERRORTYPE_NETWORK;
		if(me instanceof MongoCursorNotFoundException)
			return DBException.ERRORTYPE_CURSORNOTFOUND;
		return DBException.ERRORTYPE_UNKNOWN;
	}
	
	@Override
	public Iterable<Document> aggregate(Bson... ops) throws DBException {
		AggregateIterable<Document> output = null;
		try {
			List<Bson> pipeline = Arrays.asList(ops);
			output = col.aggregate(pipeline);
		} catch (Throwable t) {
			if(t instanceof MongoException) {
				MongoException me = (MongoException) t;
				throw new DBException(getType(me), me.getCode(), getCollectionName() + " aggregate failed, " + Arrays.deepToString(ops) + ". Error " + t.getMessage());
			}
			throw new DBException(DBException.ERRORTYPE_UNKNOWN, 0, getCollectionName() + " aggregate failed, " + Arrays.deepToString(ops) + ". Error " + t.getMessage());
		}
		if(output == null) 
			throw new DBException(DBException.ERRORTYPE_UNKNOWN, 0, getCollectionName() + " aggregate output is null, " + Arrays.deepToString(ops));
//		CommandResult result = output.getCommandResult();
//		MongoException me = result.getException();
//		if(me != null)
//			throw new DBException(getType(me), me.getCode(), getCollectionName() + " aggregate failed, " + Arrays.deepToString(ops) + ". Error " + me.getMessage());
//		String errorMessage = result.getErrorMessage();
//		if(errorMessage != null)
//			throw new DBException(DBException.ERRORTYPE_UNKNOWN, 0, getCollectionName() + " aggregate failed, " + Arrays.deepToString(ops) + ". Error " + errorMessage);
		return output;
	}
	
	@Override
	public FindIterable<Document> query(Bson query, String... keys) throws DBException {
		FindIterable<Document> iterable = col.find(query);
		if(keys != null) {
			iterable.projection(Projections.include(keys));
		}
		return iterable;
	}
	
	@Override
	public void add(DataObject dObj) throws DBException {
		dObj.generateId();
		try{
			col.insertOne(dObj.toDocument());
		} catch (MongoException me) {
			throw new DBException(getType(me), me.getCode(), getCollectionName() + " save failed", me.getMessage() + "; obj = " + dObj.toDocument());
		}
	}
	
	@Override
	public UpdateResult replace(DataObject dObj) throws DBException {
		dObj.generateId();
		try{
			UpdateOptions updateOptions = new UpdateOptions();
			updateOptions.upsert(true);
			UpdateResult result = col.replaceOne(Filters.eq(DataObject.FIELD_ID, dObj.getId()), dObj.toDocument(), updateOptions);
			return result;
		} catch (MongoException me) {
			throw new DBException(getType(me), me.getCode(), getCollectionName() + " save failed", me.getMessage() + "; obj = " + dObj.toDocument());
		}
	}
	
	@Override
	public void addAll(Collection<DataObject> dObjs) throws DBException {
	    try{
	    	List<Document> objs = new ArrayList<Document>();
	        for (DataObject dob : dObjs) {
	        	objs.add(dob.toDocument());
	        }
	        col.insertMany(objs);
	    } catch (MongoException me) {
	        throw new DBException(getType(me), me.getCode(), getCollectionName() + " save failed", me.getMessage());
	    }
	}

	@Override
	public DeleteResult delete(Bson match) throws DBException {
		try{
			DeleteResult result = col.deleteMany(match);
			return result;
		} catch (MongoException me) {
			throw new DBException(getType(me), me.getCode(), getCollectionName() + " delete failed", me.getMessage() + "; match = " + match);
		}
	}

	@Override
	public UpdateResult updateMany(Bson match, Bson update, boolean upsert) throws DBException {
		try {
			UpdateOptions updateOptions = new UpdateOptions();
			updateOptions.upsert(upsert);
			UpdateResult result = null;
			result = col.updateMany(match, update, updateOptions);
			return result;
		} catch (MongoException me) {
			throw new DBException(getType(me), me.getCode(), getCollectionName() + " udpate failed", me.getMessage() + "; match = " + match + "; result = " + update);
		}
	}
	
	@Override
	public UpdateResult updateOne(Bson match, Bson update, boolean upsert) throws DBException {
		try {
			UpdateOptions updateOptions = new UpdateOptions();
			updateOptions.upsert(upsert);
			UpdateResult result = null;
			result = col.updateOne(match, update, updateOptions);
			return result;
		} catch (MongoException me) {
			throw new DBException(getType(me), me.getCode(), getCollectionName() + " udpate failed", me.getMessage() + "; match = " + match + "; result = " + update);
		}
	}
	
//	@Override
//	public WriteResult update(DBObject match, DBObject dObj) throws DBException{
//		return update(match, dObj, false, false);
//	}
	
	@Override
	
	public BaseDAO init() throws DBException {
		col = mongoHelper.getDBCollection(getCollectionName());
		col.withWriteConcern(WriteConcern.SAFE);
//		ensureIndexes(col); Don't ensureIndexes in application. 
		return this;
	}
	
	@Override
	public DataObject findOne(Bson query) throws DBException {
		FindIterable<Document> obj = col.find(query);
		if(obj != null){
			Document doc = obj.first();
			if(doc != null) {
				DataObject dataObject;
				try {
					dataObject = newDataObjectInstance(doc);
					return dataObject;
				} catch (Exception e) {
					e.printStackTrace();
					throw new DBException(DBException.ERRORTYPE_UNKNOWN, 0, "FindOne failed in DAO, " + this + ", query = " + query + ", error = " + e.getMessage());
				} 
			}
		}
		return null;
	}
	
    @Override
	public <T extends DataObject> T findOne(Bson query, Class<T> clazz) throws DBException {
    	FindIterable<Document> obj = col.find(query);
		if(obj != null){
			Document doc = obj.first();
			if(doc != null) {
				try {
					T dataObj = clazz.newInstance();
					dataObj.fromDocument(doc);
					return dataObj;
				} catch (Exception e) {
					e.printStackTrace();
					throw new DBException(DBException.ERRORTYPE_UNKNOWN, 0, "FindOne failed in DAO, " + this + ", query = " + query + ", error = " + e.getMessage());
				} 
			}
	    }
	    return null;
	}

	@Override
	public Document findAndUpdate(Bson query, Bson projection,
			Bson sort, Bson update, boolean returnNew,
			boolean upsert) throws DBException {
		try {
			FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
			options.upsert(upsert);
			options.sort(sort);
			options.projection(projection);
			options.returnDocument(returnNew ? ReturnDocument.AFTER : ReturnDocument.BEFORE);
			Document doc = col.findOneAndUpdate(query, update, options);
			return doc;
		} catch (MongoException me) {
			me.printStackTrace();
			throw new DBException(getType(me), me.getCode(), "findAndModify failed in DAO, " + this + ", query = " + query + ", error = " + me.getMessage());
		} 
	}
	
	@Override
	public Document findAndDelete(Bson query) throws DBException {
		try {
			Document doc = col.findOneAndDelete(query);
			return doc;
		} catch (MongoException me) {
			me.printStackTrace();
			throw new DBException(getType(me), me.getCode(), "findAndModify failed in DAO, " + this + ", query = " + query + ", error = " + me.getMessage());
		} 
	}
	
	@Override
	public Document findAndReplace(Bson query, Document replacement, boolean returnNew, boolean upsert) throws DBException {
		try {
			FindOneAndReplaceOptions options = new FindOneAndReplaceOptions();
			options.returnDocument(returnNew ? ReturnDocument.AFTER : ReturnDocument.BEFORE);
			options.upsert(upsert);
			Document doc = col.findOneAndReplace(query, replacement, options);
			return doc;
		} catch (MongoException me) {
			me.printStackTrace();
			throw new DBException(getType(me), me.getCode(), "findAndModify failed in DAO, " + this + ", query = " + query + ", error = " + me.getMessage());
		} 
	}
	
	public DataObject newDataObjectInstance(Document doc) throws Exception {
		Class<? extends DataObject> dataObjectClass = getDataObjectClass(doc);
		if(dataObjectClass != null) {
			DataObject dataObj = dataObjectClass.newInstance();
			dataObj.fromDocument(doc);
			return dataObj;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return super.toString() + " ,collectionName = " + getCollectionName() + " ,dataObjectClass = " + getDataObjectClass(null);
	}
	
	@Override
	public <TResult> FindIterable<TResult> query(Bson query, Class<TResult> resultClass) throws DBException {
		return col.find(query, resultClass);
	}
	
	@Override
	public FindIterable<Document> query(Bson query) throws DBException {
		return col.find(query);
	}
	
	@Override
	public long count(Bson query) {
		return col.count(query);
	}
	
	@Override
	public long count(Bson query, CountOptions options) {
		return col.count(query, options);
	}
	
	@Override
	public <TResult> MongoCursor<TResult> distinct(String fieldName, Bson query, Class<TResult> resultClass) throws DBException {
		DistinctIterable<TResult> iterator = col.distinct(fieldName, resultClass);
		iterator.filter(query);
		MongoCursor<TResult> cursor = iterator.iterator();
		return cursor;
	}

	/**
	 * @param mongoHelper the mongoHelper to set
	 */
	
	public void setMongoHelper(MongoHelper mongoHelper) {
		this.mongoHelper = mongoHelper;
	}

	/**
	 * @return the mongoHelper
	 */
	
	public MongoHelper getMongoHelper() {
		return mongoHelper;
	}
	
	
	public String getCollectionName() {
		return collectionName;
	}
	
	
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}
	
	public abstract Class<? extends DataObject> getDataObjectClass(Document obj);
	
	public abstract Document[] getIndexes();
	
	public abstract Document getShardKeys();
	
}
