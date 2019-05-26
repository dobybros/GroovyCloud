package com.dobybros.chat.rpc.reqres.balancer;

import chat.errors.CoreException;
import com.dobybros.chat.errors.CoreErrorCodes;
import com.docker.rpc.RPCRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pbdata.generated.balancer.ChatPB;

import java.util.List;

/**
 * Created by zhanjing on 2017/7/25.
 *
 * 踢通道时删除device
 */

public class DeviceDeleteRequest extends RPCRequest {

    // 用户id
    private String userId;

    // 要删除的用户所在的service
    private String service;
    private List<Integer> terminals;

    public static final String RPCTYPE = "deviceDel";

    public String toString() {
        StringBuilder builder = new StringBuilder(DeviceUpdateRequest.class.getSimpleName());
        builder.append(": ").append(userId).append(": ").append(service);
        return builder.toString();
    }

    public DeviceDeleteRequest() {
        super(RPCTYPE);
    }

    @Override
    public void resurrect() throws CoreException {
        byte[] bytes = getData();
        Byte encode = getEncode();
        if(bytes != null) {
            if(encode != null) {
                switch(encode) {
                    case ENCODE_PB:
                        try {
                            ChatPB.DeviceDeleteRequest request = ChatPB.DeviceDeleteRequest.parseFrom(bytes);
                            if(request.hasField(ChatPB.DeviceDeleteRequest.getDescriptor().findFieldByName("userId")))
                                userId = request.getUserId();
                            if(request.hasField(ChatPB.DeviceDeleteRequest.getDescriptor().findFieldByName("service")))
                                service = request.getService();
                            List<Integer> terminalsList = request.getTerminalsList();
                            terminals = terminalsList;

                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODE_PB_PARSE_FAILED, "PB parse data failed, " + e.getMessage());
                        }
                        break;
                    default:
                        throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for resurrect");
                }
            }
        }
    }

    @Override
    public void persistent() throws CoreException {
        Byte encode = getEncode();
        if(encode == null)
            throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent");
        switch(encode) {
            case ENCODE_PB:
                ChatPB.DeviceDeleteRequest.Builder builder = ChatPB.DeviceDeleteRequest.newBuilder();
                if(userId != null)
                    builder.setUserId(userId);
                if(service != null)
                    builder.setService(service);
                if(terminals != null)
                    builder.addAllTerminals(terminals);

                ChatPB.DeviceDeleteRequest loginRequest = builder.build();
                byte[] bytes = loginRequest.toByteArray();
                setData(bytes);
                setEncode(ENCODE_PB);
                setType(RPCTYPE);
                break;
            default:
                throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
        }
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

    public List<Integer> getTerminals() {
        return terminals;
    }

    public void setTerminals(List<Integer> terminals) {
        this.terminals = terminals;
    }
}
