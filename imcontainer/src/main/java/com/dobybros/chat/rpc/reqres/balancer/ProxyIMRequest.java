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
 * @date 2019/11/15
 */
public class ProxyIMRequest extends RPCRequest {
    private final String TAG = ProxyIMRequest.class.getSimpleName();
    static final String RPCTYPE = "proxyim";
    public static final Integer CONNECTSTATUS_CONNECT = 1;
    public static final Integer CONNECTSTATUS_SESSIONERROR = -1;
    public static final Integer CONNECTSTATUS_PINGTIMEOUT = -2;
    private String channelId;
    private String channelIp;
    private String userId;
    private String service;
    private Integer terminal;
    private byte theType; //PACK.TYPE
    private Integer connectStatus = 1;  //1.connect  2.disconnect  3.pingTimeout
    private String sourceIp;
    private Integer sourcePort;
    private String sourceServer;
    private byte[] theData;
    private Short imEncodeVersion;

    //存内存
    private String forId;

    public ProxyIMRequest(){
        super(RPCTYPE);
    }
    @Override
    public void resurrect() throws CoreException {
        byte[] bytes = getData();
        Byte encode = getEncode();
        if(bytes != null) {
            if(encode != null) {
                switch(encode) {
                    case ENCODE_JAVABINARY:
                        ByteArrayInputStream bais = null;
                        DataInputStreamEx dis = null;
                        try {
                            bais = new ByteArrayInputStream(bytes);
                            dis = new DataInputStreamEx(bais);
                            channelId = dis.readUTF();
                            userId = dis.readUTF();
                            service = dis.readUTF();
                            terminal = dis.readInt();
                            theType = dis.readByte();
                            connectStatus = dis.readInt();
                            sourceIp = dis.readUTF();
                            sourcePort = dis.readInt();
                            sourceServer = dis.readUTF();
                            imEncodeVersion = dis.readShort();
                            channelIp = dis.readUTF();
                            int theDataCount = dis.readInt();
                            if(theDataCount > 0){
                                theData = new byte[theDataCount];
                                dis.readFully(theData);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            throw new CoreException(ChatErrorCodes.ERROR_RPC_DECODE_FAILED, "ProxyIMRequest PB parse data failed, " + ExceptionUtils.getFullStackTrace(e)+ ",service_userId: " + service + "_" + userId);
                        } finally {
                            IOUtils.closeQuietly(bais);
                            IOUtils.closeQuietly(dis.original());
                        }
                        break;
                    default:
                        throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "ProxyIMRequest Encoder type doesn't be found for resurrect,service_userId: " + service + "_" + userId);
                }
            }
        }
    }

    @Override
    public void persistent() throws CoreException {
        Byte encode = getEncode();
        if(encode == null)
            throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NULL, "ProxyIMRequest Encoder is null for persistent,service_userId: " + service + "_" + userId);
        switch(encode) {
            case ENCODE_JAVABINARY:
                ByteArrayOutputStream baos = null;
                DataOutputStreamEx dis = null;
                try {
                    baos = new ByteArrayOutputStream();
                    dis = new DataOutputStreamEx(baos);
                    dis.writeUTF(channelId);
                    dis.writeUTF(userId);
                    dis.writeUTF(service);
                    dis.writeInt(terminal);
                    dis.writeByte(theType);
                    dis.writeInt(connectStatus);
                    dis.writeUTF(sourceIp);
                    dis.writeInt(sourcePort);
                    dis.writeUTF(sourceServer);
                    dis.writeShort(imEncodeVersion);
                    dis.writeUTF(channelIp);
                    if(theData != null){
                        dis.writeInt(theData.length);
                        dis.write(theData);
                    } else {
                        dis.writeInt(0);
                    }
                    byte[] bytes = baos.toByteArray();
                    setData(bytes);
                    setEncode(ENCODE_JAVABINARY);
                    setType(RPCTYPE);
                } catch(Throwable t) {
                    t.printStackTrace();
                    throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODE_FAILED, "ProxyIMRequest PB parse data failed, " + ExceptionUtils.getFullStackTrace(t)+ ",service_userId: " + service + "_" + userId);
                } finally {
                    IOUtils.closeQuietly(baos);
                    IOUtils.closeQuietly(dis.original());
                }
                break;
            default:
                throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "ProxyIMRequest Encoder type doesn't be found for persistent,service_userId: " + service + "_" + userId);
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

    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
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

    public Integer getConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(Integer connectStatus) {
        this.connectStatus = connectStatus;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public Integer getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getSourceServer() {
        return sourceServer;
    }

    public void setSourceServer(String sourceServer) {
        this.sourceServer = sourceServer;
    }

    public String getForId() {
        return forId;
    }

    public void setForId(String forId) {
        this.forId = forId;
    }

    public Short getImEncodeVersion() {
        return imEncodeVersion;
    }

    public void setImEncodeVersion(Short imEncodeVersion) {
        this.imEncodeVersion = imEncodeVersion;
    }

    public String getChannelIp() {
        return channelIp;
    }

    public void setChannelIp(String channelIp) {
        this.channelIp = channelIp;
    }

    public boolean checkParamsNotNull(){
        if(theType == 0){
            return userId != null && service != null && terminal != null && imEncodeVersion != null;
        }else {
            return channelId != null && userId != null && service != null && terminal != null && imEncodeVersion != null;
        }
    }
}
