package com.docker.storage.adapters;

import chat.errors.CoreException;
import com.docker.data.RepairData;

import java.util.List;

/**
 * Created by lick on 2020/4/14.
 * Description：
 */
public interface RepairService {
    public List<RepairData> getAllRepairDatas() throws CoreException;

    public void updateRepairData(RepairData repairData) throws CoreException;

    public void addRepairData(RepairData repairData) throws CoreException;

    public RepairData getRepairData(String id) throws CoreException;
}
