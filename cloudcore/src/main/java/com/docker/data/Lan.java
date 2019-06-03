package com.docker.data;

import org.bson.Document;

/**
 *
 * @author aplombchen
 *
 */
public class Lan extends DataObject {
	public static final String FIELD_LAN_PORT = "port";
	public static final String FIELD_LAN_DOMAIN = "domain";
	public static final String FIELD_LAN_PROTOCOL = "protocol";

	private String domain;

	private Integer port;

	private String protocol;

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public void fromDocument(Document dbObj) {
		super.fromDocument(dbObj);
		domain = dbObj.getString(FIELD_LAN_DOMAIN);
		protocol = dbObj.getString(FIELD_LAN_PROTOCOL);
		port = dbObj.getInteger(FIELD_LAN_PORT);
	}

	@Override
	public Document toDocument() {
		Document dbObj = super.toDocument();
		if(domain != null)
			dbObj.put(FIELD_LAN_DOMAIN, domain);
		if(protocol != null)
			dbObj.put(FIELD_LAN_PROTOCOL, protocol);
		if(port != null)
			dbObj.put(FIELD_LAN_PORT, port);
		return dbObj;
	}
}