package com.dobybros.chat.props;

import chat.logs.LoggerEx;
import com.docker.utils.AutoReloadProperties;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalLansProperties extends AutoReloadProperties {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Lan> lanMap;

	public static class Lan {
		private String lanId;

		public static final Integer TYPE_RPC = 0;
		public static final Integer TYPE_http = 1;
		private Integer type;

		private String host;

		public Lan(String lan, String host, Integer type) {
			this.lanId = lan;
			this.host = host;
			this.type = type;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public String getLanId() {
			return lanId;
		}

		public void setLanId(String lanId) {
			this.lanId = lanId;
		}

		public Integer getType() {
			return type;
		}

		public void setType(Integer type) {
			this.type = type;
		}

		public String toString() {
			return lanId + "#" + host;
		}
	}


	@Override
	protected void onLoad() {
		ConcurrentHashMap<String, Lan> newLanMap = null;
		String lans = getProperty("lans");
		if(lans != null) {
			String[] lanArray = lans.split(",");
			if(lanArray != null && lanArray.length > 0) {
				newLanMap = new ConcurrentHashMap<>();
				for(String lan : lanArray) {
//					newLanSet.add(lan);
					String host = getProperty("host." + lan);
					String typeStr = getProperty("type." + lan);
					if(StringUtils.isNotBlank(host)) {
						Integer type = 1;
						if(typeStr != null){
							type = Integer.valueOf(typeStr);
						}
						newLanMap.put(lan, new Lan(lan, host, type));
					} else {
						LoggerEx.error(TAG, "host." + lan + " doesn't be found, please check your gateway.properties");
					}
				}
			}
		}
		if(newLanMap != null)
			lanMap = newLanMap;
	}

	public Map<String, Lan> getLanMap() {
		return lanMap;
	}
}
