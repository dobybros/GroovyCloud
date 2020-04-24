package com.docker.storage.zookeeper;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.WatcherRemoveCuratorFramework;
import org.apache.curator.framework.api.*;
import org.apache.curator.framework.api.transaction.CuratorMultiTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.schema.SchemaSet;
import org.apache.curator.framework.state.ConnectionStateErrorPolicy;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.server.quorum.flexible.QuorumVerifier;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by lick on 2020/4/22.
 * Description：
 */
public class ZookeeperClient {
    private Map<String, TreeCache> treeCacheMap = new ConcurrentHashMap<>();
    private Map<String, PathChildrenCache> pathChildrenCacheMap = new ConcurrentHashMap<>();
    private Map<String, NodeCache> nodeCacheMap = new ConcurrentHashMap<>();

    private CuratorFramework curatorFramework;

    public void start() {
        curatorFramework.start();
    }


    public void close() {
        curatorFramework.close();
    }


    public CuratorFrameworkState getState() {
        return curatorFramework.getState();
    }


    public boolean isStarted() {
        return curatorFramework.isStarted();
    }


    public CreateBuilder create() {
        return curatorFramework.create();
    }


    public DeleteBuilder delete() {
        return curatorFramework.delete();
    }


    public ExistsBuilder checkExists() {
        return curatorFramework.checkExists();
    }


    public GetDataBuilder getData() {
        return curatorFramework.getData();
    }


    public SetDataBuilder setData() {
        return curatorFramework.setData();
    }


    public GetChildrenBuilder getChildren() {
        return curatorFramework.getChildren();
    }


    public GetACLBuilder getACL() {
        return curatorFramework.getACL();
    }


    public SetACLBuilder setACL() {
        return curatorFramework.setACL();
    }


    public ReconfigBuilder reconfig() {
        return curatorFramework.reconfig();
    }


    public GetConfigBuilder getConfig() {
        return curatorFramework.getConfig();
    }


    public CuratorTransaction inTransaction() {
        return curatorFramework.inTransaction();
    }


    public CuratorMultiTransaction transaction() {
        return curatorFramework.transaction();
    }


    public TransactionOp transactionOp() {
        return curatorFramework.transactionOp();
    }


    public void sync(String s, Object o) {
        curatorFramework.sync(s, o);
    }


    public void createContainers(String s) throws Exception {
        curatorFramework.createContainers(s);
    }


    public SyncBuilder sync() {
        return curatorFramework.sync();
    }


    public RemoveWatchesBuilder watches() {
        return curatorFramework.watches();
    }


    public Listenable<ConnectionStateListener> getConnectionStateListenable() {
        return curatorFramework.getConnectionStateListenable();
    }


    public Listenable<CuratorListener> getCuratorListenable() {
        return curatorFramework.getCuratorListenable();
    }


    public Listenable<UnhandledErrorListener> getUnhandledErrorListenable() {
        return curatorFramework.getUnhandledErrorListenable();
    }


    public CuratorFramework nonNamespaceView() {
        return curatorFramework.nonNamespaceView();
    }


    public CuratorFramework usingNamespace(String s) {
        return curatorFramework.usingNamespace(s);
    }


    public String getNamespace() {
        return curatorFramework.getNamespace();
    }


    public CuratorZookeeperClient getZookeeperClient() {
        return curatorFramework.getZookeeperClient();
    }


    public EnsurePath newNamespaceAwareEnsurePath(String s) {
        return curatorFramework.newNamespaceAwareEnsurePath(s);
    }


    public void clearWatcherReferences(Watcher watcher) {
        curatorFramework.clearWatcherReferences(watcher);
    }


    public boolean blockUntilConnected(int i, TimeUnit timeUnit) throws InterruptedException {
        return curatorFramework.blockUntilConnected(i, timeUnit);
    }


    public void blockUntilConnected() throws InterruptedException {
        curatorFramework.blockUntilConnected();
    }


    public WatcherRemoveCuratorFramework newWatcherRemoveCuratorFramework() {
        return curatorFramework.newWatcherRemoveCuratorFramework();
    }


    public ConnectionStateErrorPolicy getConnectionStateErrorPolicy() {
        return curatorFramework.getConnectionStateErrorPolicy();
    }


    public QuorumVerifier getCurrentConfig() {
        return curatorFramework.getCurrentConfig();
    }


    public SchemaSet getSchemaSet() {
        return curatorFramework.getSchemaSet();
    }


    public boolean isZk34CompatibilityMode() {
        return curatorFramework.isZk34CompatibilityMode();
    }


    public CompletableFuture<Void> runSafe(Runnable runnable) {
        return curatorFramework.runSafe(runnable);
    }

    public void setCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        this.start();
    }

    public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }

    public boolean checkTreeCache(String path) {
        return treeCacheMap.containsKey(path);
    }

    public boolean addTreeCache(String path, TreeCache treeCache) {
        TreeCache treeCacheOld = treeCacheMap.putIfAbsent(path, treeCache);
        return treeCacheOld == null;
    }
    public boolean checkPathChildrenCache(String path) {
        return pathChildrenCacheMap.containsKey(path);
    }

    public boolean addPathChildrenCache(String path, PathChildrenCache pathChildrenCache) {
        PathChildrenCache pathChildrenCacheOld = pathChildrenCacheMap.putIfAbsent(path, pathChildrenCache);
        return pathChildrenCacheOld == null;
    }
    public boolean checkNodeCache(String path) {
        return nodeCacheMap.containsKey(path);
    }

    public boolean addNodeCache(String path, NodeCache nodeCache) {
        NodeCache nodeCacheOld = nodeCacheMap.putIfAbsent(path, nodeCache);
        return nodeCacheOld == null;
    }
}
