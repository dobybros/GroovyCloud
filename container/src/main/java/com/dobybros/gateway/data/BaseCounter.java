package com.dobybros.gateway.data;
/**
 * 计数基类
 * @author Baihua
 *
 */
public class BaseCounter {
	
	protected int count;	//次数
	
	protected long firstTime;	//首次时间
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public long getFirstTime() {
		return firstTime;
	}
	public void setFirstTime(long firstTime) {
		this.firstTime = firstTime;
	}
}
