package com.docker.storage.adapters;

import chat.errors.CoreException;
import com.docker.data.DockerStatus;
import com.docker.data.Service;

import java.util.List;


/**
 * 管理服务器在线状态的接口
 * 
 * 使用Lan里的MongoDB数据库
 * 
 * @author aplombchen
 *
 */
public interface DockerStatusService {

	void deleteDockerStatus(String server) throws CoreException;

	void addDockerStatus(DockerStatus serverStatus)
			throws CoreException;

	void addService(String server, Service service)
			throws CoreException;

	void updateServiceUpdateTime(String server, String serviceName, Integer serviceVersion, Long updateTime)
			throws CoreException;

	void updateServiceType(String server, String serviceName, Integer serviceVersion, Integer type)
			throws CoreException;

	void deleteService(String server, String service, Integer version) throws CoreException;

	void update(String server, DockerStatus serverStatus)
			throws CoreException;

	DockerStatus getDockerStatusByServer(String server)
			throws CoreException;

	List<DockerStatus> getDockerStatusByServerType(String serverType) throws CoreException;

	List<DockerStatus> getDockerStatus(String serverType, Integer status) throws CoreException;

	List<DockerStatus> getDockerStatusByServerTypes(List<String> serverTypes) throws CoreException;

	List<DockerStatus> getAllDockerStatus() throws CoreException;
}
