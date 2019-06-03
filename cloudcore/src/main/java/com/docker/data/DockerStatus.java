package com.docker.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用于存储服务器的在线信息， 上线时写入该对象， 下线时删除该对象。 
 * 
 * @author aplombchen
 *
 */
public class DockerStatus extends DataObject {
	public static final String FIELD_DOCKERSTATUS_IP = "ip";
	public static final String FIELD_DOCKERSTATUS_HTTPPORT = "httpPort";
	public static final String FIELD_DOCKERSTATUS_RPCPORT = "rpcPort";
	public static final String FIELD_DOCKERSTATUS_SSLRPCPORT = "sslRpcPort";
	public static final String FIELD_DOCKERSTATUS_PUBLICDOMAIN = "publicDomain";
	public static final String FIELD_DOCKERSTATUS_LANID = "lanId";
	public static final String FIELD_DOCKERSTATUS_SERVER = "server";
	public static final String FIELD_DOCKERSTATUS_STATUS = "status";
	public static final String FIELD_DOCKERSTATUS_SERVICES = "services";
	public static final String FIELD_DOCKERSTATUS_HEALTH = "health";
	public static final String FIELD_DOCKERSTATUS_SERVERTYPE = "serverType";
	public static final String FIELD_DOCKERSTATUS_INFO = "serverInfo";

	public static final String FIELD_SERVERSTATUS_RPCPORT = "rp";
	public static final String FIELD_SERVERSTATUS_SSLRPCPORT = "srp";
	public static final String FIELD_SERVERSTATUS_TCPPORT = "tp";
	public static final String FIELD_SERVERSTATUS_WEBSOCKETPORT = "ws";
	/**
	 * 服务器的类型， login， gateway， presence等
	 */
	private String serverType;

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

	private List<Service> services;

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
	 * 状态， OK是可以正常工作， Standby是服务器刚启动后的待命状态， 此时还不能提供服务器， 到OK状态之后开始服务。
	 */
	private Integer status;
	public static final int STATUS_OK = 1;
	public static final int STATUS_PREPARING = 50;
	public static final int STATUS_STANDBY = 100;
	public static final int STATUS_DELETED = 200;


	/**
	 * 一台服务器的健康值， 0分是最健康的， 100分是最不健康的。 100分封顶。
	 *
	 * 这个值的计算需要综合各方面因素， 定期刷新到服务器中， 例如每10秒刷一次。
	 */
	private Integer health;
	public static final int HEALTH_MAX = 100;

	/**
	 * 其他字段，例如gateway的sslRpcPort、tcpPort、webSocketPort、rpcPort
	 */
	public Map info;

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

	public void setHttpPort(Integer port) {
		this.httpPort = port;
	}

	public String getPublicDomain() {
		return publicDomain;
	}
	public void setPublicDomain(String publicDomain) {
		this.publicDomain = publicDomain;
	}
	public Integer getRpcPort() {
		return rpcPort;
	}
	public void setRpcPort(Integer rpcPort) {
		this.rpcPort = rpcPort;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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

    public Integer getSslRpcPort() {
        return sslRpcPort;
    }

    public void setSslRpcPort(Integer sslRpcPort) {
        this.sslRpcPort = sslRpcPort;
    }

	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}

	public Map getInfo() {
		return info;
	}

	public void setInfo(Map info) {
		this.info = info;
	}

	@Override
	public void fromDocument(Document dbObj) {
		super.fromDocument(dbObj);
		server = (String) dbObj.get(FIELD_DOCKERSTATUS_SERVER);
		publicDomain = (String) dbObj.get(FIELD_DOCKERSTATUS_PUBLICDOMAIN);
		ip = dbObj.getString(FIELD_DOCKERSTATUS_IP);
		httpPort = dbObj.getInteger(FIELD_DOCKERSTATUS_HTTPPORT);
		rpcPort = dbObj.getInteger(FIELD_DOCKERSTATUS_RPCPORT);
        sslRpcPort = dbObj.getInteger(FIELD_DOCKERSTATUS_SSLRPCPORT);
		publicDomain = dbObj.getString(FIELD_DOCKERSTATUS_PUBLICDOMAIN);
		lanId = dbObj.getString(FIELD_DOCKERSTATUS_LANID);
		status = dbObj.getInteger(FIELD_DOCKERSTATUS_STATUS);
		health = dbObj.getInteger(FIELD_DOCKERSTATUS_HEALTH);
		serverType = (String) dbObj.get(FIELD_DOCKERSTATUS_SERVERTYPE);
		List<Document> servicesList = (List<Document>) dbObj.get(FIELD_DOCKERSTATUS_SERVICES);
		if(servicesList != null) {
			services = new ArrayList<>();
			for(Document serviceDoc : servicesList) {
				Service service = new Service();
				service.fromDocument(serviceDoc);
				services.add(service);
			}
		}
		String jsonInfo = dbObj.getString(FIELD_DOCKERSTATUS_INFO);
		if (jsonInfo != null) {
			info = JSONObject.parseObject(jsonInfo);
		}
	}
	
	@Override
	public Document toDocument() {
		Document dbObj = null; 
		dbObj = super.toDocument();
		if(server != null)
			dbObj.put(FIELD_DOCKERSTATUS_SERVER, server);
		if(serverType != null)
			dbObj.put(FIELD_DOCKERSTATUS_SERVERTYPE, serverType);
		if(publicDomain != null)
			dbObj.put(FIELD_DOCKERSTATUS_PUBLICDOMAIN, publicDomain);
		if(ip != null)
			dbObj.put(FIELD_DOCKERSTATUS_IP, ip);
		if(httpPort != null)
			dbObj.put(FIELD_DOCKERSTATUS_HTTPPORT, httpPort);
		if(rpcPort != null)
			dbObj.put(FIELD_DOCKERSTATUS_RPCPORT, rpcPort);
        if(sslRpcPort != null)
            dbObj.put(FIELD_DOCKERSTATUS_SSLRPCPORT, sslRpcPort);
		if(publicDomain != null)
			dbObj.put(FIELD_DOCKERSTATUS_PUBLICDOMAIN, publicDomain);
		if(lanId != null)
			dbObj.put(FIELD_DOCKERSTATUS_LANID, lanId);
		if(status != null)
			dbObj.put(FIELD_DOCKERSTATUS_STATUS, status);
		if(health != null)
			dbObj.put(FIELD_DOCKERSTATUS_HEALTH, health);
		if(services != null) {
			List<Document> serviceList = new ArrayList<>();
			for(Service service : services) {
				Document serviceDoc = service.toDocument();
				serviceList.add(serviceDoc);
			}
			dbObj.put(FIELD_DOCKERSTATUS_SERVICES, serviceList);
		}
		if (info != null) {
			dbObj.put(FIELD_DOCKERSTATUS_INFO, JSON.toJSONString(info));
		}
		return dbObj;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
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
}