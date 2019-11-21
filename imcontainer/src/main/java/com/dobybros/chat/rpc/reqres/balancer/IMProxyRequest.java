package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.utils.DataInputStreamEx;
import chat.utils.DataOutputStreamEx;
import com.docker.rpc.RPCRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author lick
 * @date 2019/11/18
 */
public class IMProxyRequest extends RPCRequest {
    public static final String RPCTYPE = "improxy";

    public IMProxyRequest() {
        super(RPCTYPE);
    }

    private String channelId;
    private String userId;
    private String service;
    public static final Integer CHANNELSTATUS_CLOSE = 1;
    public static final Integer CHANNELSTATUS_NORMAL = 2;
    private Integer channelStatus = 2;  //1.关闭  2.正常
    private Integer[] includeTerminals;
    private Integer[] excludeTerminals;
    private Byte theType;
    private byte[] theData;

    @Override
    public void resurrect() throws CoreException {
        byte[] bytes = getData();
        Byte encode = getEncode();
        if (bytes != null) {
            if (encode != null) {
                switch (encode) {
                    case ENCODE_JAVABINARY:
                        ByteArrayInputStream bais = null;
                        DataInputStreamEx dis = null;
                        try {
                            bais = new ByteArrayInputStream(bytes);
                            dis = new DataInputStreamEx(bais);
                            channelId = dis.readUTF();
                            userId = dis.readUTF();
                            service = dis.readUTF();
                            channelStatus = dis.readInt();
                            includeTerminals = dis.readIntegerArray();
                            excludeTerminals = dis.readIntegerArray();
                            theType = dis.readByte();
                            int theDataCount = dis.readInt();
                            if (theDataCount > 0) {
                                theData = new byte[theDataCount];
                                dis.readFully(theData);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            throw new CoreException(ChatErrorCodes.ERROR_RPC_DECODE_FAILED, "IMProxyRequest PB parse data failed, " + ExceptionUtils.getFullStackTrace(e) + ",service_userId: " + service + "_" + userId);
                        } finally {
                            IOUtils.closeQuietly(bais);
                            IOUtils.closeQuietly(dis.original());
                        }
                        break;
                    default:
                        throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "IMProxyRequest Encoder type doesn't be found for resurrect,service_userId: " + service + "_" + userId);
                }
            }
        }
    }

    @Override
    public void persistent() throws CoreException {
        Byte encode = getEncode();
        if (encode == null)
            throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NULL, "IMProxyRequest Encoder is null for persistent,service_userId: " + service + "_" + userId);
        switch (encode) {
            case ENCODE_JAVABINARY:
                ByteArrayOutputStream baos = null;
                DataOutputStreamEx dis = null;
                try {
                    baos = new ByteArrayOutputStream();
                    dis = new DataOutputStreamEx(baos);
                    dis.writeUTF(channelId);
                    dis.writeUTF(userId);
                    dis.writeUTF(service);
                    dis.writeInt(channelStatus);
                    dis.writeIntegerArray(includeTerminals);
                    dis.writeIntegerArray(excludeTerminals);
                    dis.writeByte(theType);
                    if (theData != null) {
                        dis.writeInt(theData.length);
                        dis.write(theData);
                    } else {
                        dis.writeInt(0);
                    }
                    byte[] bytes = baos.toByteArray();
                    setData(bytes);
                    setEncode(ENCODE_JAVABINARY);
                    setType(RPCTYPE);
                } catch (Throwable t) {
                    t.printStackTrace();
                    throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODE_FAILED, "IMProxyRequest PB parse data failed, " + ExceptionUtils.getFullStackTrace(t) + ",service_userId: " + service + "_" + userId);
                } finally {
                    IOUtils.closeQuietly(baos);
                    IOUtils.closeQuietly(dis.original());
                }
                break;
            default:
                throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "IMProxyRequest Encoder type doesn't be found for persistent,service_userId: " + service + "_" + userId);
        }
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer[] getIncludeTerminals() {
        return includeTerminals;
    }

    public void setIncludeTerminals(Integer[] includeTerminals) {
        this.includeTerminals = includeTerminals;
    }

    public Integer[] getExcludeTerminals() {
        return excludeTerminals;
    }

    public void setExcludeTerminals(Integer[] excludeTerminals) {
        this.excludeTerminals = excludeTerminals;
    }

    public byte getTheType() {
        return theType;
    }

    public void setTheType(byte theType) {
        this.theType = theType;
    }

    public byte[] getTheData() {
        return theData;
    }

    public void setTheData(byte[] theData) {
        this.theData = theData;
    }
    public boolean checkParamsNotNull(){
        return userId != null && service != null;
    }

    public Integer getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(Integer channelStatus) {
        this.channelStatus = channelStatus;
    }

    public void setTheType(Byte theType) {
        this.theType = theType;
    }
}
