package com.dobybros.chat.data.serverstatus;

import com.dobybros.chat.data.DataObject;

import java.util.Set;

/**
 * 用于存储服务器的在线信息， 上线时写入该对象， 下线时删除该对象。 
 * 
 * @author aplombchen
 *
 */
public class ServerStatus extends DataObject {

	public static final String FIELD_SERVERSTATUS_SERVER = "s";
	public static final String FIELD_SERVERSTATUS_SERVERTYPE = "stype";
	public static final String FIELD_SERVERSTATUS_IP = "ip";
	public static final String FIELD_SERVERSTATUS_HTTPPORT = "hp";
	public static final String FIELD_SERVERSTATUS_RPCPORT = "rp";
	public static final String FIELD_SERVERSTATUS_SSLRPCPORT = "srp";
	public static final String FIELD_SERVERSTATUS_TCPPORT = "tp";
	public static final String FIELD_SERVERSTATUS_PUBLICDOMAIN = "pub";
	public static final String FIELD_SERVERSTATUS_LANID = "lan";
	public static final String FIELD_SERVERSTATUS_TYPE = "type";
	public static final String FIELD_SERVERSTATUS_STATUS = "stats";
	public static final String FIELD_SERVERSTATUS_PRESENCESERVER = "ps";
	public static final String FIELD_SERVERSTATUS_HEALTH = "hlth";
	public static final String FIELD_SERVERSTATUS_WEBSOCKETPORT = "ws";

	/**
	 * 服务器的类型， 例如Presence， Gateway， SingleChat等
	 */
	public static final String SERVERTYPE_PRESENCE = "presence";
	public static final String SERVERTYPE_GATEWAY = "gateway";
	public static final String SERVERTYPE_LOGIN = "login";


	/**
	 * 区分是推送系统内的服务器还是集成进来的业务服务器
	 */
	public static final int TYPE_MSGSERVERS = 1;
	public static final int TYPE_APPSERVER = 10;

	/**
	 * 状态， OK是可以正常工作， Standby是服务器刚启动后的待命状态， 此时还不能提供服务器， 到OK状态之后开始服务。
	 */
	public static final int STATUS_OK = 1;
	public static final int STATUS_PREPARING = 50;
	public static final int STATUS_STANDBY = 100;
	public static final int STATUS_DELETED = 200;

	public static final int HEALTH_MAX = 100;


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
	 * Gateway服务器才会有tcpPort
	 */
	private Integer tcpPort;

	/**
	 * Gateway服务器才会有sslTcpPort
	 */
	private Integer sslTcpPort;

	/**
	 * Gateway服务器才会有wsPort
	 */
	private Integer wsPort;

	/**
	 * ip有可能对应的是内网IP， 如果这台服务器需要对外， 就需要通过IM的管理后台配置外网IP。
	 */
	private String publicDomain;

	/**
	 * 相同lanId的服务器可以通过ip直接通信， 说明他们在同一内网。
	 * 不同lanId的服务器只能通过publicDomain访问， 说明他们不在同一内网， 可以是跨越国家或者大洲的不同部署。
	 */
	private String lanId;

	/**
	 * 服务器的类型， 例如Presence， Gateway， SingleChat等
	 */
	private String serverType;

	/**
	 * 区分是推送系统内的服务器还是集成进来的业务服务器
	 */
	private Integer type;

	/**
	 * 状态， OK是可以正常工作， Standby是服务器刚启动后的待命状态， 此时还不能提供服务器， 到OK状态之后开始服务。
	 */
	private Integer status;

	/**
	 * 如果这是一台Gateway服务器， PresenceServer指明的是它的在线服务器名称。 在Gateway还没有激活时， 该字段为空。
	 */
	private String presenceServer;

	/**
	 * 一台服务器的健康值， 0分是最健康的， 100分是最不健康的。 100分封顶。
	 *
	 * 这个值的计算需要综合各方面因素， 定期刷新到服务器中， 例如每10秒刷一次。
	 */
	private Integer health;

	private Set<String> services;

	private Integer userCount;

	public Integer getUserCount() {
		return userCount;
	}

	public void setUserCount(Integer userCount) {
		this.userCount = userCount;
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

	public Integer getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(Integer tcpPort) {
		this.tcpPort = tcpPort;
	}

	public Integer getSslTcpPort() {
		return sslTcpPort;
	}

	public void setSslTcpPort(Integer sslTcpPort) {
		this.sslTcpPort = sslTcpPort;
	}

	public Integer getWsPort() {
		return wsPort;
	}

	public void setWsPort(Integer wsPort) {
		this.wsPort = wsPort;
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

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getPresenceServer() {
		return presenceServer;
	}

	public void setPresenceServer(String presenceServer) {
		this.presenceServer = presenceServer;
	}

	public Integer getHealth() {
		return health;
	}

	public void setHealth(Integer health) {
		this.health = health;
	}

	Set<String> getServices() {
		return services;
	}

	void setServices(Set<String> services) {
		this.services = services;
	}

}