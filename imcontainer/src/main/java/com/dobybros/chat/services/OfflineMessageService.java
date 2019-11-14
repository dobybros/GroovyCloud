package com.dobybros.chat.services;

import com.dobybros.chat.data.OfflineMessage;

import java.util.Collection;
import java.util.List;

/**
 * Created by zhanjing on 2017/9/12.
 *
 */
public interface OfflineMessageService {

    public void saveOfflineMessage(OfflineMessage offlineMessage);

    public long removeOfflineMessages(String userId, Collection<String> offlineMessageIds);

    public List<OfflineMessage> readOfflineMessages(String userId, String service, Integer offset, Integer limit);

    public long removeOverdueOfflineMessages(Long overdueTime);

}
