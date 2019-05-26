package com.dobybros.chat.services.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.IteratorEx;
import com.dobybros.chat.data.DataObject;
import com.dobybros.chat.services.IConsumeQueueService;
import com.dobybros.chat.storage.DBException;
import com.dobybros.chat.storage.mongodb.BaseDAO;
import com.dobybros.chat.storage.mongodb.MongoHelper;
import org.bson.Document;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumeQueueService implements IConsumeQueueService {
	private static final String TAG = ConsumeQueueService.class.getSimpleName();
	// var cursor = db.mycoll.find({"ts" : {"$gt" :
	// 33}}).addOption(DBQuery.Option.awaitData).addOption(DBQuery.Option.tailable).addOption(DBQuery.Option.oplogReplay)
	private BaseDAO dao;
	private AtomicBoolean isShutdown;
	private Object shutDownLock = new int[0];
	private Integer idleTime = 30000;
	private boolean shutdownAlready = false;
	
	public ConsumeQueueService() {
		isShutdown = new AtomicBoolean(false);
	}

	@Override
	public void add(DataObject dataObject) throws CoreException {
		try {
			dao.add(dataObject);
		} catch (DBException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_MESSAGEADD_FAILED, new String[]{dataObject.toString()},"Message " + dataObject + " add failed, " + e.getMessage());
		}
	}
	
	@Override
	public void consumeMessages(IteratorEx<DataObject> iterator) throws CoreException {
		if(iterator == null)
			throw new CoreException(ChatErrorCodes.ERROR_ITERATOR_NULL, "Iterator is null");
		while(!isShutdown.get()) {
			try {
				Document dbObj = dao.findAndDelete(new Document());
//				DBObject dbObj = dao.findAndModify(new BasicDBObject(), null, null, true, null, false, false);
				if(dbObj != null) {
					Class<? extends DataObject> dataObjectClass = dao.getDataObjectClass(dbObj);
					if(dataObjectClass != null) {
						DataObject dataObject = dataObjectClass.newInstance();
						if(dataObject != null) {
							dataObject.fromDocument(dbObj);
							if(!iterator.iterate(dataObject)) 
								break;
						}
					}
				} else {
					synchronized (isShutdown) {
						isShutdown.wait(idleTime);
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
				LoggerEx.fatal(TAG, "Consume message failed, " + e.getMessage());
			} 
		}
		if(isShutdown.get()) {
			synchronized (shutDownLock) {
				shutdownAlready = true;
				shutDownLock.notify();
			}
		}
		LoggerEx.info(TAG, "Consume queue thread " + getDao() + " is terminated. " + Thread.currentThread());
	}
	
	@Override
	public void shutdownImmediately() {
		isShutdown.set(true);
		synchronized (isShutdown) {
			isShutdown.notify();
		}
	}
	
	@Override
	public void shutdown() {
		if(!isShutdown.getAndSet(true)) {
			synchronized (isShutdown) {
				isShutdown.notify();
			}
			synchronized (shutDownLock) {
				if(!shutdownAlready) {
					try {
						LoggerEx.info(TAG, "Waiting for consume queue thread " + getDao() + " termination..." + Thread.currentThread());
						shutDownLock.wait(TimeUnit.MINUTES.toMillis(30));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
	public BaseDAO getDao() {
		return dao;
	}

	public void setDao(BaseDAO dao) {
		this.dao = dao;
	}
}
