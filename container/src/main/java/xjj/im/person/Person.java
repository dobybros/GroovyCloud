package xjj.im.person;

import org.apache.http.Header;

import java.io.FileWriter;

public abstract class Person implements Runnable{
	public static final int TERMINAL_ANDROID_PAD = 1;
	public static final int TERMINAL_ANDROID = 2;
	public static final int TERMINAL_IOS_PAD = 3;
	public static final int TERMINAL_IOS = 4;
	public static final int TERMINAL_WEB_MOBILE = 5;
	public static final int TERMINAL_WEB_PC = 6;
	public static final int TERMINAL_WEB = 7;
	public static final int TERMINAL_DESKTOP_WINDOWS = 8;
	public static final int TERMINAL_DESKTOP_MAC = 9;
	public static final int TERMINAL_DESKTOP_LINUX = 10;
	public static final int TERMINAL_DESKTOP = 11;

	protected String logPath;
	protected FileWriter logFileWriter;
	
	protected String account;
	protected String password;
	
	protected String service;
	protected String userId;
	protected String userName;
	protected Header[] cookies;
	protected Integer terminal;
	protected String deviceToken;
	
	
	protected String tcpHost;
	protected String accountsHost;
	protected String acucomHost;
	protected Integer httpPort;


	public String toString() {
		return account;
	}
	
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getAccountsHost() {
		return accountsHost;
	}

	public void setAccountsHost(String accountsHost) {
		this.accountsHost = accountsHost;
	}

	public String getAcucomHost() {
		return acucomHost;
	}

	public void setAcucomHost(String acucomHost) {
		this.acucomHost = acucomHost;
	}

	public Integer getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(Integer httpPort) {
		this.httpPort = httpPort;
	}

	public String getUserId() {
		return userId;
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	public String getTcpHost() {
		return tcpHost;
	}

	public void setTcpHost(String tcpHost) {
		this.tcpHost = tcpHost;
	}

	public Integer getTerminal() {
		return terminal;
	}

	public void setTerminal(Integer terminal) {
		this.terminal = terminal;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}
	
}
