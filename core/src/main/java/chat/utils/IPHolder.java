package chat.utils;

import chat.logs.LoggerEx;

public class IPHolder {
	private static final String TAG = IPHolder.class.getSimpleName();
	private String ipPrefix;
	private String ethPrefix;
	
	private String ip;

	public void init() {
		ip = ChatUtils.getLocalHostIp(ipPrefix, ethPrefix);
		if(ip == null)
			ip = "127.0.0.1";
		LoggerEx.info(TAG, "Server ip is " + ip + " by ipPrefix " + ipPrefix + " ethPrefix " + ethPrefix);
	}
	public String getIpPrefix() {
		return ipPrefix;
	}

	public void setIpPrefix(String ipPrefix) {
		this.ipPrefix = ipPrefix;
	}

	public String getEthPrefix() {
		return ethPrefix;
	}

	public void setEthPrefix(String ethPrefix) {
		this.ethPrefix = ethPrefix;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
