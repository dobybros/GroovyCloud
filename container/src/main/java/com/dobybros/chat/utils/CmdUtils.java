package com.dobybros.chat.utils;

import org.apache.commons.lang.StringUtils;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class CmdUtils {
	/**
	 * 执行
	 * @param cmd
	 * @return
	 */
	public static List<String> exec(String cmd) {
		try {
			String[] cmdA = { "/bin/sh", "-c", cmd };
			Process process = Runtime.getRuntime().exec(cmdA);
			LineNumberReader br = new LineNumberReader(new InputStreamReader(
					process.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			List<String> logList = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
//				System.out.println(line);
//				sb.append(line).append("\n");
				logList.add(new String(line.getBytes("utf-8"), "utf-8") + "\r");
			}
			return logList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		String grep = "";
		String cmd = "tail -n100 /home/Baihua/workspace/TalentChat/logs/server.log";
		if(!StringUtils.isEmpty(grep)) {
			cmd = cmd + " | grep " + grep;
		}
		List<String> logList = exec(cmd);
		System.out.println(logList);
	}
}
