package com.dobybros.chat.open.data;

/**
 * Created by zhanjing on 2017/11/22.
 */
public class MsgResult {
    private Integer code;
    private byte[] data;
    private Integer dataEncode;
    private boolean shouldIntercept = true;

    public MsgResult(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Integer getDataEncode() {
        return dataEncode;
    }

    public void setDataEncode(Integer dataEncode) {
        this.dataEncode = dataEncode;
    }

    public boolean isShouldIntercept() {
        return shouldIntercept;
    }

    public void setShouldIntercept(boolean shouldIntercept) {
        this.shouldIntercept = shouldIntercept;
    }
}
