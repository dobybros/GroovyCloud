package com.dobybros.chat.constants;
public interface BalancerConstants {
	/**
	 * Server status is standby, need start working by Balancer server. 
	 */
	public static final int STATUS_STANDBY = -1;
	/**
	 * Server status is good to use. 
	 */
	public static final int STATUS_RUNNING = 1;
	/**
	 * Server status is busy, please don't route users into it.
	 */
	public static final int STATUS_BUSY = 20;
	/**
	 * Server status is suspended, means it is manually suspended, may caused by maintenance. 
	 */
	public static final int STATUS_SUSPEND = 50;
	/**
	 * Server status is disconnected.
	 */
	public static final int STATUS_DISCONNECTED = 100;
	/**
	 * Server status is down.
	 */
	public static final int STATUS_DOWN = 200;
	/**
	 * Server status is don't accept more users online. 
	 */
	public static final int STATUS_PAUSE = 300;
	/**
	 * Server status is stop service, all online users will go offline.
	 */
	public static final int STATUS_STOP = 400;
	/**
	 * Server is shutted down manually
	 */
	public static final int STATUS_SHUTDOWN = 500;
	/**
	 * Server is working...
	 */
	public static final int STATUS_WORKING = 1000;
	
	public static final String FIELD_HEALTHSCORE = "healthScore";
	public static final String FIELD_VERSION = "version";
	public static final String FIELD_HEALTHDESCRIPTION = "healthDescription";
	public static final String FIELD_STATUS = "status";
	public static final String FIELD_CHANGING = "changing";
	public static final String FIELD_ONLINECOUNT = "onlineCount";
	public static final String FIELD_MAXONLINE = "maxOnline";
	public static final String FIELD_DOMAIN = "domain";
	public static final String FIELD_TARGETIDS = "targetIds";
	public static final String FIELD_CLIENTID = "clientId";

	public static final String PENDING_SERVER = "pending_server";
}
