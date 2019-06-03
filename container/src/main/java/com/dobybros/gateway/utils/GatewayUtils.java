package com.dobybros.gateway.utils;

import java.util.Random;

public class GatewayUtils {
	private static final String TAG = GatewayUtils.class.getSimpleName();

//	public static Integer checkTerminal(Integer terminal) {
//		if (terminal != null) {
//			switch(terminal) {
//			case DeviceInfo.TERMINAL_ANDROID:
//			case DeviceInfo.TERMINAL_ANDROID_PAD:
//			case DeviceInfo.TERMINAL_DESKTOP:
//			case DeviceInfo.TERMINAL_DESKTOP_LINUX:
//			case DeviceInfo.TERMINAL_DESKTOP_MAC:
//			case DeviceInfo.TERMINAL_DESKTOP_WINDOWS:
//			case DeviceInfo.TERMINAL_IOS:
//			case DeviceInfo.TERMINAL_IOS_PAD:
//			case DeviceInfo.TERMINAL_WEB:
//			case DeviceInfo.TERMINAL_WEB_MOBILE:
//			case DeviceInfo.TERMINAL_WEB_PC:
//				return terminal;
//			}
//		}
//		return null;
//	}
	
	/**
	 * 返回0-99(闭区间)内的随机整数
	 * @return
	 */
	public static Integer getRandomNumberIn100() {
		Random random = new Random();
		return random.nextInt(100);
	}
	
}
