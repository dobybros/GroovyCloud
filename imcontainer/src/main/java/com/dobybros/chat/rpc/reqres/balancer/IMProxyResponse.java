package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.utils.DataInputStreamEx;
import chat.utils.DataOutputStreamEx;
import com.docker.rpc.RPCResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author lick
 * @date 2019/11/15
 */
public class IMProxyResponse extends RPCResponse {
    public static final Integer CHANNELSTATUS_CLOSE = 1;
    public static final Integer CHANNELSTATUS_NORMAL = 2;
    private Integer channelStatus = CHANNELSTATUS_NORMAL;
    private Byte returnType;
    private byte[] returnData;  //RESULT

    public IMProxyResponse(){
        super(IMProxyRequest.RPCTYPE);
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
                            channelStatus = dis.readInt();
                            returnType = dis.readByte();
                            int returnDataCount = dis.readInt();
                            if(returnDataCount > 0){
                                returnData = new byte[returnDataCount];
                                dis.readFully(returnData);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            throw new CoreException(ChatErrorCodes.ERROR_RPC_DECODE_FAILED, "ProxyIMRequest PB parse data failed, " + ExceptionUtils.getFullStackTrace(e));
                        } finally {
                            IOUtils.closeQuietly(bais);
                            if (dis != null)
                                IOUtils.closeQuietly(dis.original());
                        }
                        break;
                    default:
                        throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "ProxyIMRequest Encoder type doesn't be found for resurrect");
                }
            }
        }
    }

    @Override
    public void persistent() throws CoreException {
        Byte encode = getEncode();
        if (encode == null)
            throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NULL, "IMProxyResponse Encoder is null for persistent");
        switch (encode) {
            case ENCODE_JAVABINARY:
                ByteArrayOutputStream baos = null;
                DataOutputStreamEx dis = null;
                try {
                    baos = new ByteArrayOutputStream();
                    dis = new DataOutputStreamEx(baos);
                    dis.writeInt(channelStatus);
                    dis.writeByte(returnType);
                    dis.writeInt(returnData.length);
                    dis.write(returnData);

                    byte[] bytes = baos.toByteArray();
                    setData(bytes);
                    setEncode(ENCODE_JAVABINARY);
                    setType(ProxyIMRequest.RPCTYPE);
                } catch (Throwable t) {
                    t.printStackTrace();
                    throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODE_FAILED, " IMProxyResponse PB parse data failed, " + ExceptionUtils.getFullStackTrace(t));
                } finally {
                    IOUtils.closeQuietly(baos);
                    if (dis != null)
                        IOUtils.closeQuietly(dis.original());
                }
                break;
            default:
                throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "IMProxyResponse Encoder type doesn't be found for persistent ");
        }
    }

    public Integer getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(Integer channelStatus) {
        this.channelStatus = channelStatus;
    }

    public Byte getReturnType() {
        return returnType;
    }

    public void setReturnType(Byte returnType) {
        this.returnType = returnType;
    }

    public byte[] getReturnData() {
        return returnData;
    }

    public void setReturnData(byte[] returnData) {
        this.returnData = returnData;
    }
}
