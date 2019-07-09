package com.docker.rpc;

import chat.errors.ChatErrorCodes;
import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.utils.GZipUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.docker.rpc.remote.MethodMapping;
import com.docker.rpc.remote.stub.ServiceStubManager;
import com.docker.script.MyBaseRuntime;
import com.docker.script.ScriptManager;
import com.docker.utils.SpringContextUtil;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class MethodResponse extends RPCResponse {
	private static final String TAG = MethodResponse.class.getSimpleName();
	private byte version = 1;
	private Long crc;
	private Object returnObject;
	private CoreException exception;

	private String returnTmpStr;

	public static final String FIELD_RETURN = "return";
	public static final String FIELD_ERROR = "error";

	public MethodResponse() {
		super(MethodRequest.RPCTYPE);
	}

	public MethodResponse(Object returnObj, CoreException exception) {
		super(MethodRequest.RPCTYPE);
		this.returnObject = returnObj;
		this.exception = exception;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(MethodResponse.class.getSimpleName());
//		builder.append(": ").append(server);
		return builder.toString();
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
					DataInputStream dis = null;
					try {
						bais = new ByteArrayInputStream(bytes);
						dis = new DataInputStream(bais);
						version = dis.readByte();
						crc = dis.readLong();
						if(crc == null || crc == 0 || crc == -1)
							throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_CRC_ILLEGAL, "CRC is illegal for MethodRequest");

						ServiceStubManager serviceStubManager = null;
						MethodRequest methodRequest = (MethodRequest) request;
						if(request != null) {
							serviceStubManager = methodRequest.getServiceStubManager();
						}
						if(serviceStubManager == null) {
							ScriptManager scriptManager = (ScriptManager) SpringContextUtil.getBean("scriptManager");
							MyBaseRuntime baseRuntime = (MyBaseRuntime) scriptManager.getBaseRuntime(methodRequest.getFromService());
							if(baseRuntime == null)
								throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SERVICE_NOTFOUND, "Service " + methodRequest.getFromService() + " not found for crc " + crc);
							serviceStubManager = baseRuntime.getServiceStubManager();
						}

						MethodMapping methodMapping = serviceStubManager.getMethodMapping(crc);
//						if(methodMapping == null)
//							throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_METHODNOTFOUND, "Method doesn't be found by crc " + crc);

						int returnLength = dis.readInt();
						if(returnLength > 0) {
							byte[] returnBytes = new byte[returnLength];
							dis.readFully(returnBytes);
							try {
								byte[] data = GZipUtils.decompress(returnBytes);
								String json = new String(data, "utf8");
								returnTmpStr = json;
								if(methodMapping == null || methodMapping.getReturnClass().equals(Object.class)) {
									returnObject = JSON.parse(json);
								} else {
									returnObject = JSON.parseObject(json, methodMapping.getGenericReturnClass());
								}
							} catch (IOException e) {
								e.printStackTrace();
								LoggerEx.error(TAG, "Parse return bytes failed, " + e.getMessage());
							}
						}

						int execeptionLength = dis.readInt();
						if(execeptionLength > 0) {
							byte[] exceptionBytes = new byte[execeptionLength];
							dis.readFully(exceptionBytes);
							try {
								byte[] data = GZipUtils.decompress(exceptionBytes);
								String json = new String(data, "utf8");
								JSONObject jsonObj = (JSONObject) JSON.parse(json);
								if(jsonObj != null) {
									exception = new CoreException(jsonObj.getInteger("code"), jsonObj.getString("message"));
								}
							} catch (IOException e) {
								e.printStackTrace();
								LoggerEx.error(TAG, "Parse exception bytes failed, " + e.getMessage());
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
						throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODE_FAILED, "PB parse data failed, " + e.getMessage());
					}
					break;
					default:
						throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for resurrect");
				}
			}
		}
	}

	@Override
	public void persistent() throws CoreException {
		Byte encode = getEncode();
		if(encode == null)
			throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent");
		switch(encode) {
		case ENCODE_JAVABINARY:
			ByteArrayOutputStream baos = null;
			DataOutputStream dis = null;
			try {
				baos = new ByteArrayOutputStream();
				dis = new DataOutputStream(baos);
				dis.writeByte(version);
				dis.writeLong(crc);

				byte[] returnBytes = null;
				if(returnObject != null) {
					String returnStr = null;
					if(returnTmpStr == null)
						returnStr = JSON.toJSONString(returnObject);
					else
						returnStr = returnTmpStr;
					try {
						returnBytes = GZipUtils.compress(returnStr.getBytes("utf8"));
					} catch (IOException e) {
						e.printStackTrace();
						LoggerEx.error(TAG, "Generate return " + returnStr + " to bytes failed, " + e.getMessage());
					}
				}
				if(returnBytes != null) {
					dis.writeInt(returnBytes.length);
					dis.write(returnBytes);
				} else {
					dis.writeInt(0);
				}

				byte[] exceptionBytes = null;
				if(exception != null) {
					JSONObject json = new JSONObject();
					json.put("code", exception.getCode());
					json.put("message", exception.getMessage());
					String errorStr = json.toJSONString();//JSON.toJSONString(exception);
					try {
						exceptionBytes = GZipUtils.compress(errorStr.getBytes("utf8"));
					} catch (IOException e) {
						e.printStackTrace();
						LoggerEx.error(TAG, "Generate error " + errorStr + " to bytes failed, " + e.getMessage());
					}
				}
				if(exceptionBytes != null) {
					dis.writeInt(exceptionBytes.length);
					dis.write(exceptionBytes);
				} else {
					dis.writeInt(0);
				}

				byte[] bytes = baos.toByteArray();
				setData(bytes);
				setEncode(ENCODE_JAVABINARY);
				setType(MethodRequest.RPCTYPE);
			} catch (Throwable t) {
				t.printStackTrace();
				throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODE_FAILED, "PB parse data failed, " + t.getMessage());
			} finally {
				IOUtils.closeQuietly(baos);
				IOUtils.closeQuietly(dis);
			}
			break;
			default:
				throw new CoreException(ChatErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
		}
	}

	public Long getCrc() {
		return crc;
	}

	public void setCrc(Long crc) {
		this.crc = crc;
	}

	public Object getReturnObject() {
		return returnObject;
	}

	public void setReturnObject(Object returnObject) {
		this.returnObject = returnObject;
	}

	public CoreException getException() {
		return exception;
	}

	public void setException(CoreException exception) {
		this.exception = exception;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public String getReturnTmpStr() {
		return returnTmpStr;
	}

	public void setReturnTmpStr(String returnTmpStr) {
		this.returnTmpStr = returnTmpStr;
	}
}
