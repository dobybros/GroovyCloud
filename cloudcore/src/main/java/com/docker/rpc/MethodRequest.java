package com.docker.rpc;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.DataInputStreamEx;
import chat.utils.DataOutputStreamEx;
import chat.utils.GZipUtils;
import com.alibaba.fastjson.JSON;
import com.docker.rpc.remote.MethodMapping;
import com.docker.rpc.remote.skeleton.ServiceSkeletonAnnotationHandler;
import com.docker.rpc.remote.stub.RpcCacheManager;
import com.docker.rpc.remote.stub.ServiceStubManager;
import com.docker.script.BaseRuntime;
import com.docker.script.MyBaseRuntime;
import com.docker.script.ScriptManager;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class MethodRequest extends RPCRequest {
	public static final String RPCTYPE = "mthd";
    private static final String TAG = MethodRequest.class.getSimpleName();
    private byte version = 1;

    /**
	 * generated from classname and method name and parameters
	 */
	private Long crc;

	private String service;

	private String fromServerName;

    private Object[] args;

    private Integer argCount;

    private String trackId;
    //客户端的server的ip和port
    private String sourceIp;

    private Integer sourcePort;
    /**
     * 只用于内存, 不错传输序列化
     */
    private String fromService;
    private String argsTmpStr; //Only use for logging

    private ServiceStubManager serviceStubManager;

    private String callbackFutureId;

    @Override
    public String toString() {
        return "MethodRequest crc: " + crc + " service: " + service + " fromServerName: " + fromServerName + " argsSize: " + (args != null ? args.length : 0)
                + " trackId: " + trackId + " sourceIp: " + sourceIp + " sourcePort: " + sourcePort + " fromService: " + fromService;
    }

	public MethodRequest() {
		super(RPCTYPE);
	}
//    public Object invoke() {
//        return methodMapping.invoke(args);
//    }

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
                        version = dis.readByte();
                        crc = dis.readLong();
                        service = dis.readUTF();
                        fromServerName = dis.readUTF();
                        fromService = dis.readUTF();
                        sourceIp = dis.readUTF();
                        sourcePort = dis.readInt();
                        if(crc == null || crc == 0 || crc == -1)
                            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_CRC_ILLEGAL, "CRC is illegal for MethodRequest,crc: " + crc);

                        if(service == null)
                            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SERVICE_NULL, "Service is null for service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(crc));
                        ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");
                        BaseRuntime baseRuntime = scriptManager.getBaseRuntime(service);
                        if(baseRuntime == null)
                            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SERVICE_NOTFOUND, "Service " + service + " not found for service_class_method " + RpcCacheManager.getInstance().getMethodByCrc(crc));
                        ServiceSkeletonAnnotationHandler serviceSkeletonAnnotationHandler = (ServiceSkeletonAnnotationHandler) baseRuntime.getClassAnnotationHandler(ServiceSkeletonAnnotationHandler.class);
                        if(serviceSkeletonAnnotationHandler == null)
                            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SKELETON_NULL, "Skeleton handler is not for service " + service + " on method service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(crc));
                        MethodMapping methodMapping = serviceSkeletonAnnotationHandler.getMethodMapping(crc);
                        if(methodMapping == null)
                            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_METHODNOTFOUND, "Method doesn't be found by service_class_method " + RpcCacheManager.getInstance().getMethodByCrc(crc));

                        argCount = dis.readInt();
                        if(argCount > 0) {
                            Integer length = dis.readInt();
                            byte[] argsData = new byte[length];
                            dis.readFully(argsData);
//                            Class<?>[] parameterTypes = methodMapping.getParameterTypes();
                            Type[] parameterTypes = methodMapping.getGenericParameterTypes();
                            if(parameterTypes != null && parameterTypes.length > 0) {
                                if(parameterTypes.length > argCount) {
                                    LoggerEx.debug(TAG, "Parameter types not equal actual is " + parameterTypes.length + " but expected " + argCount + ". Cut off,service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(crc));
                                    Type[] newParameterTypes = new Type[argCount];
                                    System.arraycopy(parameterTypes, 0, newParameterTypes, 0, argCount);
                                    parameterTypes = newParameterTypes;
                                } else if(parameterTypes.length < argCount){
                                    LoggerEx.debug(TAG, "Parameter types not equal actual is " + parameterTypes.length + " but expected " + argCount + ". Fill with Object.class,service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(crc));
                                    Type[] newParameterTypes = new Type[argCount];
                                    for(int i = parameterTypes.length; i < argCount; i++) {
                                        newParameterTypes[i] = Object.class;
                                    }
                                    parameterTypes = newParameterTypes;
                                }
                                try {
                                    byte[] rawData = argsData;
                                    if(rawData.length > 0) {
                                        byte[] data = GZipUtils.decompress(rawData);
                                        String json = new String(data, "utf8");
                                        argsTmpStr = json;
                                        List<Object> array = JSON.parseArray(json, parameterTypes);
                                        if(array != null)
                                            args = array.toArray();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    LoggerEx.error(TAG, "Parse bytes failed, " + ExceptionUtils.getFullStackTrace(e) + ",service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(crc));
                                }
                            }
                        }

                        trackId = dis.readUTF();
//						boolean hasTrackId = dis.readBoolean();
//						if(hasTrackId) {
//                            trackId = dis.readUTF();
//                        }
					} catch (Throwable e) {
						e.printStackTrace();
						if(e instanceof CoreException) {
						    throw (CoreException)e;
                        }
						throw new CoreException(ChatErrorCodes.ERROR_RPC_DECODE_FAILED, "PB parse data failed, " + ExceptionUtils.getFullStackTrace(e)+ ",service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(crc));
					} finally {
					    IOUtils.closeQuietly(bais);
					    IOUtils.closeQuietly(dis.original());
                    }
                    break;
					default:
						throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for resurrect,service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(crc));
				}
			}
		}
	}

	@Override
	public void persistent() throws CoreException {
		Byte encode = getEncode();
		if(encode == null)
			throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent,service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(crc));
		switch(encode) {
		case ENCODE_JAVABINARY:
            ByteArrayOutputStream baos = null;
            DataOutputStreamEx dis = null;
		    try {
                baos = new ByteArrayOutputStream();
                dis = new DataOutputStreamEx(baos);
                dis.writeByte(version);
                dis.writeLong(crc);
                dis.writeUTF(service);
                dis.writeUTF(fromServerName);
                dis.writeUTF(fromService);
                dis.writeUTF(sourceIp);
                dis.writeInt(sourcePort);

                ServiceStubManager serviceStubManager = this.serviceStubManager;
                if(serviceStubManager == null) {
                    ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");
                    MyBaseRuntime baseRuntime = (MyBaseRuntime) scriptManager.getBaseRuntime(fromService);
                    if(baseRuntime == null)
                        throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SERVICE_NOTFOUND, "Service " + service + " not found for service_class_method " + RpcCacheManager.getInstance().getMethodByCrc(crc));

                    serviceStubManager = baseRuntime.getServiceStubManager();
                }

                MethodMapping methodMapping = serviceStubManager.getMethodMapping(crc);
//                if(methodMapping == null)
//                    throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_METHODNOTFOUND, "Method doesn't be found by crc " + crc);
                if(methodMapping != null) {
                    Class<?>[] parameterTypes = methodMapping.getParameterTypes();
                    if(parameterTypes != null) {
                        argCount = parameterTypes.length;
                    } else {
                        argCount = 0;
                    }
                } else {
                    if(args != null)
                        argCount = args.length;
                    else
                        argCount = 0;
                }
                dis.writeInt(argCount);
                if(argCount > 0) {
                    String json = null;
                    if(argsTmpStr == null)
                        json = JSON.toJSONString(args);
                    else
                        json = argsTmpStr;
                    try {
                        byte[] data = GZipUtils.compress(json.getBytes("utf8"));
                        dis.writeInt(data.length);
                        dis.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LoggerEx.error(TAG, "Generate " + json + " to bytes failed, " + ExceptionUtils.getFullStackTrace(e)+ ",service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(crc));
                    }
                }
                dis.writeUTF(trackId);
//                if(trackId != null) {
//                    dis.writeBoolean(true);
//                    dis.writeUTF(trackId);
//                } else {
//                    dis.writeBoolean(false);
//                }


                byte[] bytes = baos.toByteArray();
                setData(bytes);
                setEncode(ENCODE_JAVABINARY);
                setType(RPCTYPE);
            } catch(Throwable t) {
		        t.printStackTrace();
                throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODE_FAILED, "PB parse data failed, " + ExceptionUtils.getFullStackTrace(t)+ ",service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(crc));
            } finally {
                IOUtils.closeQuietly(baos);
                IOUtils.closeQuietly(dis.original());
            }
            break;
			default:
				throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent,service_class_method: " + RpcCacheManager.getInstance().getMethodByCrc(crc));
		}
	}

    public Long getCrc() {
        return crc;
    }

    public void setCrc(Long crc) {
        this.crc = crc;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Integer getArgCount() {
        return argCount;
    }

    public void setArgCount(Integer argCount) {
        this.argCount = argCount;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getFromServerName() {
        return fromServerName;
    }

    public void setFromServerName(String fromServerName) {
        this.fromServerName = fromServerName;
    }

    public String getFromService() {
        return fromService;
    }

    public void setFromService(String fromService) {
        this.fromService = fromService;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
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

    public String getArgsTmpStr() {
        return argsTmpStr;
    }

    public void setArgsTmpStr(String argsTmpStr) {
        this.argsTmpStr = argsTmpStr;
    }

    public ServiceStubManager getServiceStubManager() {
        return serviceStubManager;
    }

    public void setServiceStubManager(ServiceStubManager serviceStubManager) {
        this.serviceStubManager = serviceStubManager;
        if(this.serviceStubManager.getFromService() != null)
            fromService = this.serviceStubManager.getFromService();
    }

    public String getCallbackFutureId() {
        return callbackFutureId;
    }

    public void setCallbackFutureId(String callbackFutureId) {
        this.callbackFutureId = callbackFutureId;
    }
}
