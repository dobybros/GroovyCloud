package com.dobybros.chat.services;

import chat.errors.CoreException;
import chat.utils.IteratorEx;
import com.docker.data.DataObject;

public interface IConsumeQueueService {

	void shutdown();

	void add(DataObject dataObject) throws CoreException;

	void consumeMessages(IteratorEx<DataObject> iterator) throws CoreException;

	void shutdownImmediately();

}
