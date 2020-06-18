package com.docker.storage.adapters;

import chat.errors.CoreException;
import com.docker.data.DeployServiceVersion;

import java.util.List;
import java.util.Map;

/**
 * Created by lick on 2019/6/3.
 * Description：
 */
public interface DeployServiceVersionService {
    DeployServiceVersion getServiceVersion(String serverType) throws CoreException;

    DeployServiceVersion getDeployServiceVersion(String id) throws CoreException;

    DeployServiceVersion getDeployServiceVersionByDeployId(String deployId) throws CoreException;

    List<DeployServiceVersion> getServiceVersionsAll() throws CoreException;

    void addServiceVersion(DeployServiceVersion serviceVersion) throws CoreException;

    void deleteServiceVersion(String id) throws CoreException;

    void updateTheServiceVersion(String service, String version) throws CoreException;

    void updateServiceVersionDeployId(String id, String deployId) throws CoreException;

    void deleteService(String serverType, String service) throws CoreException;

    void addService(String serverType, String service, String version) throws CoreException;

    void updateBaseJarVersion(String id, String baseJar, String version) throws CoreException;

    void updateBaseJarVersions(String id, Map<String, String> baseJarVersions) throws CoreException;

    void updateServers(String id, Map<String, Map<String, Object>> servers) throws CoreException;
}
