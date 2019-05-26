package com.docker.storage.adapters.impl;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.ChatUtils;
import com.docker.data.DataObject;
import com.docker.data.DockerStatus;
import com.docker.data.Service;
import com.docker.storage.DBException;
import com.docker.storage.adapters.DockerStatusService;
import com.docker.storage.mongodb.daos.DockerStatusDAO;
import com.docker.utils.SpringContextUtil;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class DockerStatusServiceImpl implements DockerStatusService {
	private static final String TAG = DockerStatusServiceImpl.class.getSimpleName();
//	@Resource
	private DockerStatusDAO dockerStatusDAO = (DockerStatusDAO) SpringContextUtil.getBean("dockerStatusDAO");
	@Override
	public void deleteDockerStatus(String server) throws CoreException {
		try {
			DeleteResult result = dockerStatusDAO.delete(new Document().append(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server));
			if(result.getDeletedCount() <= 0)
				throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_NOT_FOUND, "OnlineServer " + server + " doesn't be found while delete " + server);
		} catch (DBException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_DELETE_FAILED, "Delete online server failed, " + e.getMessage());
		}
	}

	@Override
	public void addDockerStatus(DockerStatus serverStatus)
			throws CoreException {
		try {
			dockerStatusDAO.add(serverStatus);
		} catch (DBException e) {
			e.printStackTrace();
			if(e.getType() == DBException.ERRORTYPE_DUPLICATEKEY) {
				String serverStr = ChatUtils.generateFixedRandomString();
				LoggerEx.error(TAG, "Duplicated key while adding present server s = " + serverStatus.getServer() + " regenerating... new s = " + serverStr);
				serverStatus.setServer(serverStr);
				addDockerStatus(serverStatus);
			}
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_ADD_FAILED, "Add onlineServer " + serverStatus + " failed, " + e.getMessage());
		}
	}

	@Override
	public void addService(String server, Service service)
			throws CoreException {
		try {
			dockerStatusDAO.updateOne(Filters.eq(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server), Updates.addToSet(DockerStatus.FIELD_DOCKERSTATUS_SERVICES, service.toDocument()), false);
		} catch (DBException e) {
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Add service " + service + " to server " + server + " failed, " + e.getMessage());
		}
	}

	@Override
	public void updateServiceUpdateTime(String server, String serviceName, Integer serviceVersion, Long updateTime) throws CoreException {
		try {
			Bson query1 = Filters.eq(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server);
			Bson query21 = Filters.eq(Service.FIELD_SERVICE_SERVICE, serviceName);
			Bson query22 = Filters.eq(Service.FIELD_SERVICE_VERSION, serviceVersion);
			Bson query2 = Filters.elemMatch(DockerStatus.FIELD_DOCKERSTATUS_SERVICES, Filters.and(query21, query22));
			dockerStatusDAO.updateOne(Filters.and(query1, query2), Updates.set(DockerStatus.FIELD_DOCKERSTATUS_SERVICES + ".$." + Service.FIELD_SERVICE_UPLOADTIME, updateTime), false);
		} catch (DBException e) {
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Update service " + serviceName + ", version " + serviceVersion + " to server " + server + " failed, " + e.getMessage());
		}
	}

	@Override
	public void updateServiceType(String server, String serviceName, Integer serviceVersion, Integer type) throws CoreException {
		try {
			Bson query1 = Filters.eq(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server);
			Bson query21 = Filters.eq(Service.FIELD_SERVICE_SERVICE, serviceName);
			Bson query22 = Filters.eq(Service.FIELD_SERVICE_VERSION, serviceVersion);
			Bson query2 = Filters.elemMatch(DockerStatus.FIELD_DOCKERSTATUS_SERVICES, Filters.and(query21, query22));
//			Bson query3 = Filters.elemMatch(DockerStatus.FIELD_DOCKERSTATUS_SERVICES, Filters.eq(Service.FIELD_SERVICE_VERSION, serviceVersion));
			UpdateResult result = dockerStatusDAO.updateOne(Filters.and(query1, query2), Updates.set(DockerStatus.FIELD_DOCKERSTATUS_SERVICES + ".$." + Service.FIELD_SERVICE_TYPE, type), false);
			System.out.print(result);
		} catch (DBException e) {
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Update service " + serviceName + ", version " + serviceVersion + " to server " + server + " failed, " + e.getMessage());
		}
	}

	@Override
	public void deleteService(String server, String service, Integer version) throws CoreException {
		try {
			dockerStatusDAO.updateOne(Filters.eq(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server), new Document().append("$pull", new Document().append(DockerStatus.FIELD_DOCKERSTATUS_SERVICES, new Document().append(Service.FIELD_SERVICE_SERVICE, service).append(Service.FIELD_SERVICE_VERSION, version))), false);
		} catch (DBException e) {
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Delete service " + service + " to server " + server + " failed, " + e.getMessage());
		}
	}

	@Override
	public void update(String server, DockerStatus serverStatus)
			throws CoreException {
		try {
			Document update = serverStatus.toDocument();
			update.remove(DataObject.FIELD_ID);
			dockerStatusDAO.updateOne(new Document().append(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server), new Document().append("$set", update), false);
//			serverPresentDAO.update(new BasicDBObject().append(ServerPresent.FIELD_SERVERPRESENT_SERVER, server), new BasicDBObject().append("$set", presentServer.toDocument()), false, false);
		} catch (DBException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_UPDATE_FAILED, "Update onlineServer " + server + " failed, " + e.getMessage());
		}
	}

	@Override
	public DockerStatus getDockerStatusByServer(String server)
			throws CoreException {
		try {
			Document query = new Document().append(DockerStatus.FIELD_DOCKERSTATUS_SERVER, server);
			DockerStatus serverPresent = (DockerStatus) dockerStatusDAO.findOne(query);
			return serverPresent;
		} catch (DBException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query server present server failed, " + e.getMessage());
		}
	}

	@Override
	public List<DockerStatus> getDockerStatusByServerType(String serverType) throws CoreException {
		try {
			List<DockerStatus> dockerStatuses = new ArrayList<>();
			Document query = new Document().append(DockerStatus.FIELD_DOCKERSTATUS_SERVERTYPE, serverType);
			FindIterable<Document> iterable = dockerStatusDAO.query(query);
			MongoCursor<Document> cursor = iterable.iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				DockerStatus dockerStatus = new DockerStatus();
				dockerStatus.fromDocument(doc);
				dockerStatuses.add(dockerStatus);
			}
			return dockerStatuses;
		} catch (DBException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query server present server failed, " + e.getMessage());
		}
	}

	@Override
	public List<DockerStatus> getDockerStatus(String serverType, Integer status) throws CoreException {
		try {
			List<DockerStatus> dockerStatuses = new ArrayList<>();
			Document query = new Document();
			if (serverType != null) {
				query.append(DockerStatus.FIELD_DOCKERSTATUS_SERVERTYPE, serverType);
			}
			if (status != null) {
				query.append(DockerStatus.FIELD_DOCKERSTATUS_STATUS, status);
			}
			FindIterable<Document> iterable = dockerStatusDAO.query(query);
			MongoCursor<Document> cursor = iterable.iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				DockerStatus dockerStatus = new DockerStatus();
				dockerStatus.fromDocument(doc);
				dockerStatuses.add(dockerStatus);
			}
			return dockerStatuses;
		} catch (DBException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query server present server failed, " + e.getMessage());
		}
	}

	@Override
	public List<DockerStatus> getDockerStatusByServerTypes(List<String> serverTypes) throws CoreException {

		try {
			List<DockerStatus> dockerStatuses = new ArrayList<>();
			Document query = new Document();
			if (serverTypes != null) {
				List<Bson> list = new ArrayList<Bson>();
				for (String serverType : serverTypes) {
					list.add(new Document().append(DockerStatus.FIELD_DOCKERSTATUS_SERVERTYPE, serverType.trim()));
				}
				query.append("$or", list);
			}
			FindIterable<Document> iterable = dockerStatusDAO.query(query);
			MongoCursor<Document> cursor = iterable.iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				DockerStatus dockerStatus = new DockerStatus();
				dockerStatus.fromDocument(doc);
				dockerStatuses.add(dockerStatus);
			}
			return dockerStatuses;
		} catch (DBException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query server present server failed, " + e.getMessage());
		}

	}

	@Override
	public List<DockerStatus> getAllDockerStatus() throws CoreException {
		try {
			List<DockerStatus> dockerStatuses = new ArrayList<>();
			Document query = new Document();
			FindIterable<Document> iterable = dockerStatusDAO.query(query);
			MongoCursor<Document> cursor = iterable.iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				DockerStatus dockerStatus = new DockerStatus();
				dockerStatus.fromDocument(doc);
				dockerStatuses.add(dockerStatus);
			}
			return dockerStatuses;
		} catch (DBException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_ONLINESERVER_QUERY_FAILED, "Query all docker servers failed, " + e.getMessage());
		}
	}

}
