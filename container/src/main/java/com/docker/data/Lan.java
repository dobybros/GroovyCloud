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
	public static final String FIELD_LAN_TYPE = "type";
	private String type;
	public static final Integer TYPE_RPC = 0;
	public static final Integer TYPE_http = 1;
	private String domain;

	private String port;

	private String protocol;

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void fromDocument(Document dbObj) {
		super.fromDocument(dbObj);
		domain = dbObj.getString(FIELD_LAN_DOMAIN);
		protocol = dbObj.getString(FIELD_LAN_PROTOCOL);
		port = dbObj.getString(FIELD_LAN_PORT);
		type = dbObj.getString(FIELD_LAN_TYPE);
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
		if(type != null){
			dbObj.put(FIELD_LAN_TYPE, type);
		}
		return dbObj;
	}
}