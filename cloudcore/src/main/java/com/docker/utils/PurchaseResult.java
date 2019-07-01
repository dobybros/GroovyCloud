package com.docker.utils;

/**
 * 从第三方平台返回回来的结果
 * 
 * @author Baihua
 *
 */
public class PurchaseResult {
	public PurchaseResult() {
	}

	/**
	 * 第三方订单id
	 */
	String orderId;
	/**
	 * 第三方商品id
	 */
	String itemId;
	/**
	 * 过期时间
	 */
	Long expirationTime;
	/**
	 * 付款时间
	 */
	Long purchaseTime;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public Long getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(Long expirationTime) {
		this.expirationTime = expirationTime;
	}

	public Long getPurchaseTime() {
		return purchaseTime;
	}

	public void setPurchaseTime(Long purchaseTime) {
		this.purchaseTime = purchaseTime;
	}

	public String toString() {
        return " PurchaseResult	--	OrderId : " + orderId + " | ExpirationTime : " + expirationTime + " | ItemId : " + itemId + " | purchaseTime : " + purchaseTime;
    }
	
}