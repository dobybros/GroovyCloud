package com.dobybros.gateway.channels.tcp.codec;

public class GFCommon {
	public static int bytes2Int(byte[] bys, int start, int len,
			boolean isBigEndian) {
		int n = 0;
		for (int i = start, k = start + len % (Integer.SIZE / Byte.SIZE + 1); i < k; i++) {
			n |= (bys[i] & 0xff) << ((isBigEndian ? (k - i - 1) : i) * Byte.SIZE);
		}
		return n;
	}
}
