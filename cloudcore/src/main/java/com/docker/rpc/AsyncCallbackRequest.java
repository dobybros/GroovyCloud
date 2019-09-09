package com.docker.rpc;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.DataInputStreamEx;
import chat.utils.DataOutputStreamEx;
import chat.utils.GZipUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.docker.rpc.remote.MethodMapping;
import com.docker.rpc.remote.stub.ServerCacheManager;
import com.docker.rpc.remote.stub.ServiceStubManager;
import com.docker.script.MyBaseRuntime;
import com.docker.script.ScriptManager;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by lick on 2019/8/22.
 * Description：
 */
public class AsyncCallbackRequest extends RPCRequest {
    private static final String TAG = AsyncCallbackRequest.class.getSimpleName();
    public static final String RPCTYPE = "asyncCallback";
    private String callbackFutureId;
    private Object dataObject;
    private CoreException exception;
    private Long crc;
    private String fromService;

    public AsyncCallbackRequest() {
        super(RPCTYPE);
    }

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
                            callbackFutureId = dis.readUTF();
                            crc = dis.readLong();
                            fromService = dis.readUTF();
                            ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");
                            MyBaseRuntime baseRuntime = (MyBaseRuntime) scriptManager.getBaseRuntime(fromService);
                            MethodMapping methodMapping = null;
                            if (baseRuntime != null){
                                ServiceStubManager serviceStubManager = baseRuntime.getServiceStubManager();
                                methodMapping = serviceStubManager.getMethodMapping(crc);
                            }
//                                throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SERVICE_NOTFOUND, "Service " + fromService + " not found for service_class_method " + ServerCacheManager.getInstance().getCrcMethodMap().get(crc));

//						if(methodMapping == null)
//							throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_METHODNOTFOUND, "Method doesn't be found by crc " + crc);

                            int returnLength = dis.readInt();
                            if (returnLength > 0) {
                                byte[] returnBytes = new byte[returnLength];
                                dis.readFully(returnBytes);
                                try {
                                    byte[] data = GZipUtils.decompress(returnBytes);
                                    String json = new String(data, "utf8");
                                    if (methodMapping == null || methodMapping.getReturnClass().equals(Object.class)) {
                                        dataObject = JSON.parse(json);
                                    } else {
                                        if(methodMapping.getGenericReturnClass().getTypeName().contains(CompletableFuture.class.getTypeName())){
                                           if(methodMapping.getGenericReturnActualTypeArguments() != null && methodMapping.getGenericReturnActualTypeArguments().length > 0){
                                               dataObject = JSON.parseObject(json, methodMapping.getGenericReturnActualTypeArguments()[0]);
                                           }else {
                                               dataObject = JSON.parseObject(json);
                                           }
                                        }else {
                                            dataObject = JSON.parseObject(json, methodMapping.getGenericReturnClass());
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    LoggerEx.error(TAG, "Parse return bytes failed, " + e.getMessage()+ ",service_class_method: " + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)));
                                }
                            }

                            int execeptionLength = dis.readInt();
                            if (execeptionLength > 0) {
                                byte[] exceptionBytes = new byte[execeptionLength];
                                dis.readFully(exceptionBytes);
                                try {
                                    byte[] data = GZipUtils.decompress(exceptionBytes);
                                    String json = new String(data, "utf8");
                                    JSONObject jsonObj = (JSONObject) JSON.parse(json);
                                    if (jsonObj != null) {
                                        exception = new CoreException(jsonObj.getInteger("code"), jsonObj.getString("message")+ (crc != null ? ",service_class_method: " + ServerCacheManager.getInstance().getCrcMethodMap().get(crc) : ""));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    LoggerEx.error(TAG, "Parse exception bytes failed, " + e.getMessage()+ ",service_class_method: " + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)));
                                }
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODE_FAILED, "PB parse data failed when callback, " + e.getMessage()+ ",service_class_method: " + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)));
                        }finally {
                            IOUtils.closeQuietly(bais);
                            IOUtils.closeQuietly(dis.original());
                        }
                        break;
                    default:
                        throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for resurrect when callback,service_class_method: " + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)));
                }
            }
        }
    }

    @Override
    public void persistent() throws CoreException{
        Byte encode = getEncode();
        if (encode == null) {
            LoggerEx.error(TAG, "Encoder is null for persistent when callback,service_class_method: " + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)));
            throw  new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent when callback for service_class_method" + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)));
        }
        switch (encode) {
            case ENCODE_JAVABINARY:
                ByteArrayOutputStream baos = null;
                DataOutputStreamEx dis = null;
                try {
                    baos = new ByteArrayOutputStream();
                    dis = new DataOutputStreamEx(baos);
                    dis.writeUTF(callbackFutureId);
                    dis.writeLong(crc);
                    dis.writeUTF(fromService);
                    byte[] returnBytes = null;
                    String returnStr = null;
                    if (dataObject != null) {
                        returnStr = JSON.toJSONString(dataObject);
                        try {
                            returnBytes = GZipUtils.compress(returnStr.getBytes("utf8"));
                        } catch (IOException e) {
                            e.printStackTrace();
                            LoggerEx.error(TAG, "Generate return " + returnStr + " to bytes failed when callback, service_class_method: " + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)) + ",err: " + e.getMessage());
                        }
                    }
                    if (returnBytes != null) {
                        dis.writeInt(returnBytes.length);
                        dis.write(returnBytes);
                    } else {
                        dis.writeInt(0);
                    }

                    byte[] exceptionBytes = null;
                    if (exception != null) {
                        JSONObject json = new JSONObject();
                        json.put("code", exception.getCode());
                        json.put("message", exception.getMessage());
                        String errorStr = json.toJSONString();//JSON.toJSONString(exception);
                        try {
                            exceptionBytes = GZipUtils.compress(errorStr.getBytes("utf8"));
                        } catch (IOException e) {
                            e.printStackTrace();
                            LoggerEx.error(TAG, "Generate error " + errorStr + " to bytes failed, " + e.getMessage() + ",service_class_method: " + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)));
                        }
                    }
                    if (exceptionBytes != null) {
                        dis.writeInt(exceptionBytes.length);
                        dis.write(exceptionBytes);
                    } else {
                        dis.writeInt(0);
                    }

                    byte[] bytes = baos.toByteArray();
                    setData(bytes);
                    setEncode(ENCODE_JAVABINARY);
                    setType(RPCTYPE);
                    dataObject = null;
                    exception = null;
                } catch (Throwable t) {
                    t.printStackTrace();
                    LoggerEx.error(TAG, "PB parse data failed when callback, " + t.getMessage()+ ",service_class_method: " + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)));
                    throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODE_FAILED, "PB parse data failed when call back, " + t.getMessage()+ ",service_class_method: " + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)));
                } finally {
                    IOUtils.closeQuietly(baos);
                    IOUtils.closeQuietly(dis.original());
                }
                break;
            default:
                LoggerEx.error(TAG, "Encoder type doesn't be found for persistent when callback, service_class_method: " + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)));
                throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent when callback,service_class_method: " + (crc == null ? null : ServerCacheManager.getInstance().getCrcMethodMap().get(crc)));
        }
    }

    public String getCallbackFutureId() {
        return callbackFutureId;
    }

    public void setCallbackFutureId(String callbackFutureId) {
        this.callbackFutureId = callbackFutureId;
    }

    public Object getDataObject() {
        return dataObject;
    }

    public void setDataObject(Object dataObject) {
        this.dataObject = dataObject;
    }

    public Long getCrc() {
        return crc;
    }

    public void setCrc(Long crc) {
        this.crc = crc;
    }

    public String getFromService() {
        return fromService;
    }

    public void setFromService(String fromService) {
        this.fromService = fromService;
    }

    public CoreException getException() {
        return exception;
    }

    public void setException(CoreException exception) {
        this.exception = exception;
    }
}
