package com.docker.utils;

import chat.utils.ChatUtils;
import org.apache.commons.collections.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class FixedSizeLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 6918023506928428613L;
	private int maxEntries;
	
	public FixedSizeLinkedHashMap(int maxEntries) {
		this.maxEntries = maxEntries;
	}
	/**
	 * 获得允许存放的最大容量
	 * 
	 * @return int
	 */
	public int getMaxEntries() {
		return maxEntries;
	}

	/**
	 * 设置允许存放的最大容量
	 * 
	 * @param int max_entries
	 */
	public void setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
	}

	/**
	 * 如果Map的尺寸大于设定的最大长度，返回true，再新加入对象时删除最老的对象
	 * 
	 * @param Map
	 *            .Entry eldest
	 * @return int
	 */
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxEntries;
	}
	
	public static void main(String[] args) {
		Map<Integer,  String> map = MapUtils.synchronizedMap(new FixedSizeLinkedHashMap<String, String>(1));
		for(int i = 0; i < 100; i++) {
			 map.put(i, Integer.toString(i));
		}
		
		System.out.println(" size = " + map.size() + " map " + ChatUtils.toString(map.values()));
	}
}