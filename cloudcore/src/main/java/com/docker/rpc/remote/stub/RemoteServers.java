package com.docker.rpc.remote.stub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lick on 2019/5/30.
 * Description：
 */
public class RemoteServers {
    private ConcurrentHashMap<String, Server> servers = new ConcurrentHashMap<>();
    private List<Server> sortedServers = new ArrayList<>();

    private String TAG = RemoteServers.class.getSimpleName();



    public void stop() {

    }

    public ConcurrentHashMap<String, Server> getServers() {
        return servers;
    }

    public void setServers(ConcurrentHashMap<String, Server> servers) {
        this.servers = servers;
    }

    public Server getServer(String server) {
        return servers.get(server);
    }

    public List<Server> getSortedServers() {
        return sortedServers;
    }

    public void setSortedServers(List<Server> sortedServers) {
        this.sortedServers = sortedServers;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(RemoteServers.class.getSimpleName() + ": ");
        builder.append("Servers " + Arrays.toString(servers.values().toArray())).append("; sorted servers " + Arrays.toString(sortedServers.toArray()));
        return builder.toString();
    }
    public static class Server {
        /**
         * 6位服务器名称， 随机生成的， 在数据库中是唯一字段
         */
        private String server;

        /**
         * 这是网卡上能取到的IP， 一般是内网IP， 可以用于相同局域网的服务器间通信。
         */
        private String ip;
        /**
         * 通过msg.jar包装jetty提供http服务，
         * 这样的设计需要提供两个jar， 一个msg.jar提供元数据和接口， 以及使用classloader启动msgimpl.jar， 在msgimpl.jar里包含所有的依赖包， 例如jetty的。
         */
        private Integer httpPort;
        /**
         * 通过msg.jar提供的rpc端口
         */
        private Integer rpcPort;
        /*
         * ssl rpc port
         */
        private Integer sslRpcPort;
        /**
         * ip有可能对应的是内网IP， 如果这台服务器需要对外， 就需要通过IM的管理后台配置外网IP。
         */
        private String publicDomain;
        /**
         * 相同lanId的服务器可以通过ip直接通信， 说明他们在同一内网。
         * 不同lanId的服务器只能通过publicDomain访问， 说明他们不在同一内网， 可以是跨越国家或者大洲的不同部署。
         */
        private String lanId;

        private Integer version;
        private Integer minVersion;

        /**
         * 一台服务器的健康值， 0分是最健康的， 100分是最不健康的。 100分封顶。
         *
         * 这个值的计算需要综合各方面因素， 定期刷新到服务器中， 例如每10秒刷一次。
         */
        private Integer health;
        public static final int HEALTH_MAX = 100;

        private int score = 100;

        public static class Service {
            private String service;
            private Integer version;
            private Integer minVersion;

            public String getService() {
                return service;
            }

            public void setService(String service) {
                this.service = service;
            }

            public Integer getVersion() {
                return version;
            }

            public void setVersion(Integer version) {
                this.version = version;
            }

            public Integer getMinVersion() {
                return minVersion;
            }

            public void setMinVersion(Integer minVersion) {
                this.minVersion = minVersion;
            }
        }


        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getHttpPort() {
            return httpPort;
        }

        public void setHttpPort(Integer httpPort) {
            this.httpPort = httpPort;
        }

        public Integer getRpcPort() {
            return rpcPort;
        }

        public void setRpcPort(Integer rpcPort) {
            this.rpcPort = rpcPort;
        }

        public Integer getSslRpcPort() {
            return sslRpcPort;
        }

        public void setSslRpcPort(Integer sslRpcPort) {
            this.sslRpcPort = sslRpcPort;
        }

        public String getPublicDomain() {
            return publicDomain;
        }

        public void setPublicDomain(String publicDomain) {
            this.publicDomain = publicDomain;
        }

        public String getLanId() {
            return lanId;
        }

        public void setLanId(String lanId) {
            this.lanId = lanId;
        }

        public Integer getHealth() {
            return health;
        }

        public void setHealth(Integer health) {
            this.health = health;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public Integer getMinVersion() {
            return minVersion;
        }

        public void setMinVersion(Integer minVersion) {
            this.minVersion = minVersion;
        }
    }
}
