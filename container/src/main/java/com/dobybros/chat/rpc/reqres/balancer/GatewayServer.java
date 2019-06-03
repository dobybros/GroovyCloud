package com.dobybros.chat.rpc.reqres.balancer;

import com.pbdata.generated.balancer.ChatPB;

public class GatewayServer {
	private String ip;
	private Integer port;//tcp port
	private Integer httpPort;
	private String server;
	private String lanId;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getLanId() {
		return lanId;
	}
	public void setLanId(String lanId) {
		this.lanId = lanId;
	}
	public Integer getHttpPort() {
		return httpPort;
	}
	public void setHttpPort(Integer httpPort) {
		this.httpPort = httpPort;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(GatewayServer.class.getSimpleName());
		builder.append(": ip=").append(ip).append(" port=").append(port).append(" server=").append(server).append(" lanId=").append(lanId);
		return builder.toString();
	}

	public static GatewayServer fromPB(ChatPB.GatewayServer gatewayServer) {
		if(gatewayServer == null)
			return null;
		GatewayServer gateway = null;
		if(gatewayServer.hasField(com.pbdata.generated.balancer.ChatPB.GatewayServer.getDescriptor().findFieldByName("ip"))) {
			if(gateway == null)
				gateway = new GatewayServer();
			gateway.setIp(gatewayServer.getIp());
		}
		if(gatewayServer.hasField(com.pbdata.generated.balancer.ChatPB.GatewayServer.getDescriptor().findFieldByName("port"))) {
			if(gateway == null)
				gateway = new GatewayServer();
			gateway.setPort(gatewayServer.getPort());
		}
		if(gatewayServer.hasField(com.pbdata.generated.balancer.ChatPB.GatewayServer.getDescriptor().findFieldByName("server"))) {
			if(gateway == null)
				gateway = new GatewayServer();
			gateway.setServer(gatewayServer.getServer());
		}
		if(gatewayServer.hasField(com.pbdata.generated.balancer.ChatPB.GatewayServer.getDescriptor().findFieldByName("lanId"))) {
			if(gateway == null)
				gateway = new GatewayServer();
			gateway.setLanId(gatewayServer.getLanId());
		}
		if(gatewayServer.hasField(com.pbdata.generated.balancer.ChatPB.GatewayServer.getDescriptor().findFieldByName("httpPort"))) {
			if(gateway == null)
				gateway = new GatewayServer();
			gateway.setHttpPort(gatewayServer.getHttpPort());
		}
		return gateway;
	}

	public static ChatPB.GatewayServer.Builder toPB(GatewayServer gateway) {
		if(gateway == null)
			return null;
		ChatPB.GatewayServer.Builder gatewayBuilder = ChatPB.GatewayServer.newBuilder();
		if(gateway.getIp() != null)
			gatewayBuilder.setIp(gateway.getIp());
		if(gateway.getPort() != null)
			gatewayBuilder.setPort(gateway.getPort());
		if(gateway.getServer() != null)
			gatewayBuilder.setServer(gateway.getServer());
		if(gateway.getLanId() != null)
			gatewayBuilder.setLanId(gateway.getLanId());
		if(gateway.getHttpPort() != null)
			gatewayBuilder.setHttpPort(gateway.getHttpPort());
		return gatewayBuilder;
	}
}