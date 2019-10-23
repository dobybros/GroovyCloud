package com.docker.utils;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import com.jcraft.jsch.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class SSHChannel {
	private static final String TAG = SSHChannel.class.getSimpleName();
	private String ip;
	private int port;
	private JSch jsch;
	private Session session;
	
	private String username;
	private String password;
	
	private boolean isConnected = false;
	
	private String permPath;
	
	public static final int MAX_RETRY = 3;
	private int retryCount;
	public synchronized void reconnect() throws CoreException {
		isConnected = false;
		connect();
	}
	public synchronized void connect() throws CoreException {
		if(isConnected)
			return;
		try {
			jsch = new JSch();
			if(permPath != null)
				jsch.addIdentity(permPath);
			session = jsch.getSession(username, ip, port);
			if(password != null)
				session.setPassword(password);
			Properties config = new Properties();
			config.setProperty("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			isConnected = true;
			retryCount = MAX_RETRY;
		} catch (JSchException e) {
			LoggerEx.error(TAG, "connect " + ip + " by " + username + " failed : "
					+ ExceptionUtils.getFullStackTrace(e));
			throw new CoreException(ChatErrorCodes.ERROR_SSH_CONNECT_FAILED,new String[]{ip, username},
					"connect " + ip + " by " + username + " failed : "
							+ e.getMessage());
		}
	}
	
	public synchronized void disconnnect() {
		session.disconnect();
	}
	
	public void execCmds(List<String> commands) throws CoreException {
		Channel channel = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			channel = session.openChannel("shell");
			channel.setOutputStream(baos);
			PrintStream shellStream = new PrintStream(channel.getOutputStream());
			channel.connect();
			for(String command : commands) {
				shellStream.println(command);
				shellStream.flush();
				Thread.sleep(3000);
			}
		} catch(InterruptedException | IOException | JSchException e) {
			LoggerEx.error(TAG, "execCmds " + ArrayUtils.toString(commands) + " error " + ExceptionUtils.getFullStackTrace(e));
			throw new CoreException("execCmds " + ArrayUtils.toString(commands) + " error " + e.getMessage());
		} finally {
			if (channel != null)
				channel.disconnect();
			System.out.println(baos.toString());
			try {
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized String exec(String command) throws CoreException {
		BufferedReader reader = null;
		Channel channel = null;
		StringBuffer commandInfo = new StringBuffer();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (command != null) {
				channel = session.openChannel("exec");
				((ChannelExec) channel).setCommand(command);
				channel.setInputStream(null);
				((ChannelExec) channel).setErrStream(baos);
				((ChannelExec) channel).setPty(true);
				channel.connect();
				InputStream in = channel.getInputStream();
				reader = new BufferedReader(new InputStreamReader(in));
				String buf = null;
				while ((buf = reader.readLine()) != null) {
					commandInfo.append(buf);
				}
			}
		} catch (IOException | JSchException e) {
			while(retryCount > 0) {
				try {
					Thread.sleep((MAX_RETRY - retryCount) * 1000L);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} 
				retryCount--;
				reconnect();
				exec(command);
			}
			LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(e));
			throw new CoreException(ChatErrorCodes.ERROR_SSH_EXEC_FAILED, new String[]{command}, e.getMessage());
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				LoggerEx.error(TAG, ExceptionUtils.getFullStackTrace(e));
				throw new CoreException(e.getMessage());
			}
			if (channel != null)
				channel.disconnect();
			System.out.println(command + " === " + baos.toString());
		}
		return commandInfo.toString(); 
	}
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public JSch getJsch() {
		return jsch;
	}
	public void setJsch(JSch jsch) {
		this.jsch = jsch;
	}
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	public String getPermPath() {
		return permPath;
	}
	public void setPermPath(String permPath) {
		this.permPath = permPath;
	}
}
