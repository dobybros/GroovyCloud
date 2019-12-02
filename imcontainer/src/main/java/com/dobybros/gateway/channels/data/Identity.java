package com.dobybros.gateway.channels.data;

import chat.errors.CoreException;
import com.dobybros.chat.binary.data.Data;
import com.dobybros.chat.errors.IMCoreErrorCodes;
import com.dobybros.gateway.pack.HailPack;
import com.pbdata.generated.mobile.MobilePB;

import java.util.Random;


public class Identity extends Data {
    private String id;
    private String sessionId;
    private Integer terminal;
    private String userId;
    private String deviceToken;
    private String service;
    private String key;
    private String appId;
    private String code;
    private Integer sdkVersion;
    private String locale;
    private boolean passive;

    public Identity() {
        super(HailPack.TYPE_IN_IDENTITY);
    }

    public static void main() {
        Random rand = new Random(12);
        for (int i = 0; i < 10; i++) {
            System.out.println(rand.nextInt());
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(sdkVersion);
        buffer.append("/");
        buffer.append(sessionId);
        buffer.append("/");
        buffer.append(userId);
        buffer.append("/");
        buffer.append(terminal);
        buffer.append("/");
        buffer.append(deviceToken);
        buffer.append("/");
        buffer.append(passive);
        buffer.append("/");
        buffer.append(locale);
        return new String(buffer);
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }


    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    @Override
    public void resurrect() throws CoreException {
        byte[] bytes = getData();
        Byte encode = getEncode();
        if (bytes != null) {
            if (encode != null) {
                switch (encode) {
                    case ENCODE_PB:
                        try {
                            MobilePB.Identity request = MobilePB.Identity.parseFrom(bytes);
                            if (MobilePB.Identity.getDescriptor().findFieldByName("userId") != null)
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("userId")))
                                    userId = request.getUserId();
                            if (MobilePB.Identity.getDescriptor().findFieldByName("appId") != null)
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("appId")))
                                    appId = request.getAppId();
                            if (MobilePB.Identity.getDescriptor().findFieldByName("code") != null)
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("code")))
                                    code = request.getCode();
                            if (MobilePB.Identity.getDescriptor().findFieldByName("deviceToken") != null)
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("deviceToken")))
                                    deviceToken = request.getDeviceToken();
                            if (MobilePB.Identity.getDescriptor().findFieldByName("id") != null)
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("id")))
                                    id = request.getId();
                            if (MobilePB.Identity.getDescriptor().findFieldByName("key") != null)
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("key")))
                                    key = request.getKey();
                            if (MobilePB.Identity.getDescriptor().findFieldByName("sessionId") != null)
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("sessionId")))
                                    sessionId = request.getSessionId();
                            if (MobilePB.Identity.getDescriptor().findFieldByName("sdkVersion") != null)
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("sdkVersion")))
                                    sdkVersion = request.getSdkVersion();
                            if (MobilePB.Identity.getDescriptor().findFieldByName("service") != null)
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("service")))
                                    service = request.getService();
                            if (MobilePB.Identity.getDescriptor().findFieldByName("terminal") != null) {
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("terminal")))
                                    terminal = request.getTerminal();
                            }
                            if (MobilePB.Identity.getDescriptor().findFieldByName("passive") != null)
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("passive")))
                                    passive = request.getPassive();
                            if (MobilePB.Identity.getDescriptor().findFieldByName("locale") != null)
                                if (request.hasField(MobilePB.Identity.getDescriptor().findFieldByName("locale")))
                                    locale = request.getLocale();
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new CoreException(IMCoreErrorCodes.ERROR_RPC_ENCODE_PB_PARSE_FAILED, "PB parse data failed, " + e.getMessage());
                        }
                        break;
                    default:
                        throw new CoreException(IMCoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for resurrect");
                }
            }
        }
    }

    @Override
    public void persistent() throws CoreException {
        Byte encode = getEncode();
        if (encode == null)
            encode = ENCODE_PB;//throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent");
        switch (encode) {
            case ENCODE_PB:
                MobilePB.Identity.Builder builder = MobilePB.Identity.newBuilder();
                if (appId != null)
                    builder.setAppId(appId);
                if (code != null)
                    builder.setCode(code);
                if (deviceToken != null)
                    builder.setDeviceToken(deviceToken);
                if (key != null)
                    builder.setKey(key);
                if (sdkVersion != null)
                    builder.setSdkVersion(sdkVersion);
                if (service != null)
                    builder.setService(service);
                if (sessionId != null)
                    builder.setSessionId(sessionId);
                if (terminal != null)
                    builder.setTerminal(terminal);
                if (userId != null)
                    builder.setUserId(userId);
                if (id != null)
                    builder.setId(id);
                if (passive) {
                    builder.setPassive(passive);
                }
                if (locale != null) {
                    builder.setLocale(locale);
                }
                MobilePB.Identity loginRequest = builder.build();
                byte[] bytes = loginRequest.toByteArray();
                setData(bytes);
                setEncode(ENCODE_PB);
                break;
            default:
                throw new CoreException(IMCoreErrorCodes.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(Integer sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isPassive() {
        return passive;
    }

    public void setPassive(boolean passive) {
        this.passive = passive;
    }
}
