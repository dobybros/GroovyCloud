package com.docker.storage.adapters;

import chat.errors.CoreException;
import com.docker.data.ServiceVersion;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * Created by lick on 2019/6/3.
 * Description：
 */
public interface ServiceVersionService {
    List<ServiceVersion> getServiceVersions(String serverType) throws CoreException;

    List<ServiceVersion> getServiceVersionsAll() throws CoreException;

    void addServiceVersion(ServiceVersion serviceVersion) throws CoreException;

    void deleteServiceVersion(Bson bson) throws CoreException;
}
