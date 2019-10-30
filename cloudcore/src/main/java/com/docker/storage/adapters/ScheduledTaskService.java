package com.docker.storage.adapters;

import chat.errors.CoreException;
import com.docker.data.ScheduleTask;

public interface ScheduledTaskService {
    public ScheduleTask getSchedeuleTask(String id) throws CoreException;
}
