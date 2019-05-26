package com.dobybros.chat.storage.adapters;

public interface MessageService extends StorageAdapter {
    public void consumeOfflineMessages(String userId, String service);
}
