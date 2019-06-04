package com.docker.storage.adapters;

import chat.errors.CoreException;
import com.docker.data.ServiceVersion;

import java.util.List;

/**
 * Created by lick on 2019/6/3.
 * Description：
 */
public interface ServiceVersionService {
    List<ServiceVersion> getServiceVersions(String serverType) throws CoreException;
}
