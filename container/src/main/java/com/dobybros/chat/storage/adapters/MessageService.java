package com.dobybros.chat.storage.adapters;

public interface MessageService extends StorageAdapter {
    public static final String SERVICE ="imagency";
    public void consumeOfflineMessages(String userId, String service);
}
