package com.dobybros.chat.properties;

import com.docker.utils.AutoReloadProperties;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginProperties extends AutoReloadProperties {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, LoginPara> loginMap;
//	private LoginPara loginPara;
	private String shutdownPassword;
	
	private String internalKey;
	
	public LoginProperties() {
		
	}
	
	class LoginPara {
		private String account;
		private String password;
		private String role;
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
		public String getRole() {
			return role;
		}
		public void setRole(String role) {
			this.role = role;
		}
	}
	
	@Override
	protected void onLoad() {
		ConcurrentHashMap<String, LoginPara> newLoginMap = null;
		String accounts = getProperty("accounts");
		if(accounts != null) {
			String[] accountStrings = accounts.split("#");
			if(accountStrings != null && accountStrings.length > 0) {
				newLoginMap = new ConcurrentHashMap<String, LoginPara>();
				for(String accountString : accountStrings) {
					String[] accountPassword = accountString.split(":");
					if(accountPassword != null && accountPassword.length == 3) {
						LoginPara lp = new LoginPara();
						lp.setAccount(accountPassword[0]);
						lp.setPassword(accountPassword[1]);
						lp.setRole(accountPassword[2]);
						newLoginMap.put(accountPassword[0], lp);
					}
				}
			}
		}
		if(newLoginMap != null)
			loginMap = newLoginMap;
		String newShutdownPassword = getProperty("shutdown.password");
		if(!StringUtils.isBlank(newShutdownPassword))
			shutdownPassword = newShutdownPassword;
		String internalKey = getProperty("internal.key");
		if(!StringUtils.isBlank(internalKey))
			this.internalKey = internalKey;
	}

	public String login(String account, String password) {
		if(account != null && password != null)
			if(loginMap != null) {
				if(loginMap.get(account) != null) {
					String thePassword = loginMap.get(account).getPassword();
					if(thePassword != null) 
						if(password.equals(thePassword))
							return loginMap.get(account).getRole();
				}
			}
		return null;
	}
	
	public boolean verifyShutdownPassword(String password) {
		if(password != null && shutdownPassword != null) {
			if(password.equals(shutdownPassword))
				return true;
		}
		return false;
	}

	public String getInternalKey() {
		return internalKey;
	}

}
