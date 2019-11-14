package com.docker.storage.mongodb;

import com.docker.data.DataObject;
import com.docker.storage.DBException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Collection;


public interface DAO {
    public DAO init() throws DBException;

    public void add(DataObject dObj) throws DBException;

    public void add(Document document) throws DBException;

    public void addAll(Collection<DataObject> dObjs) throws DBException;

    public DeleteResult delete(Bson match) throws DBException;

    public <T extends DataObject> T findOne(Bson query, Class<T> clazz) throws DBException;

    public Iterable<Document> aggregate(Bson... ops) throws DBException;

    FindIterable<Document> query(Bson query, String... keys)
            throws DBException;

    UpdateResult updateOne(Bson match, Bson update, boolean upsert)
            throws DBException;

    UpdateResult updateMany(Bson match, Bson update, boolean upsert)
            throws DBException;

    DataObject findOne(Bson query) throws DBException;

    FindIterable<Document> find()
            throws DBException;

    Document findAndUpdate(Bson query, Bson projection, Bson sort, Bson update,
                           boolean returnNew, boolean upsert) throws DBException;

    Document findAndDelete(Bson query) throws DBException;

    Document findAndReplace(Bson query, Document replacement,
                            boolean returnNew, boolean upsert) throws DBException;

    <TResult> FindIterable<TResult> query(Bson query, Class<TResult> resultClass)
            throws DBException;

    FindIterable<Document> query(Bson query) throws DBException;

    <TResult> MongoCursor<TResult> distinct(String fieldName, Bson query,
                                            Class<TResult> resultClass) throws DBException;

    long count(Bson query);

    UpdateResult replace(DataObject dObj) throws DBException;

    long count(Bson query, CountOptions options);
}
