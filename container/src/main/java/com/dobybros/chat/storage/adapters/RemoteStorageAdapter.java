package com.dobybros.chat.storage.adapters;

/**
 * 集成这个类的子类， 代表着如果跨Lan， 该Adapter方法需要使用RPC的方式， 在Lan内， 使用数据库直连的方式。 
 * 
 * @author aplombchen
 *
 */
public interface RemoteStorageAdapter extends StorageAdapter {
}
