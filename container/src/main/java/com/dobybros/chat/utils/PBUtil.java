package com.dobybros.chat.utils;

import com.dobybros.chat.open.data.DeviceInfo;
import com.dobybros.chat.open.data.PNInfo;
import com.pbdata.generated.balancer.ChatPB;

/**
 * Created by zhanjing on 2017/7/27.
 *
 * 个别pb与实例对象的转化
 */
public class PBUtil {

    public static DeviceInfo fromDevicePB(ChatPB.Device device) {
        if(device == null)
            return null;
        DeviceInfo deviceInfo = null;
        if(device.hasField(com.pbdata.generated.balancer.ChatPB.Device.getDescriptor().findFieldByName("terminal"))) {
            if(deviceInfo == null)
                deviceInfo = new DeviceInfo();
            Integer tt = device.getTerminal();
            deviceInfo.setTerminal(tt);
        }
        if(device.hasField(com.pbdata.generated.balancer.ChatPB.Device.getDescriptor().findFieldByName("deviceToken"))) {
            if(deviceInfo == null)
                deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceToken(device.getDeviceToken());
        }
        if(device.hasField(com.pbdata.generated.balancer.ChatPB.Device.getDescriptor().findFieldByName("loginTime"))) {
            if(deviceInfo == null)
                deviceInfo = new DeviceInfo();
            deviceInfo.setLoginTime(device.getLoginTime());
        }
        if(device.hasField(com.pbdata.generated.balancer.ChatPB.Device.getDescriptor().findFieldByName("locale"))) {
            if(deviceInfo == null)
                deviceInfo = new DeviceInfo();
            deviceInfo.setLocale(device.getLocale());
        }
        return deviceInfo;
    }

    public static ChatPB.Device.Builder toDevicePB(DeviceInfo device, boolean needDeviceToken) {
        if(device == null)
            return null;
        ChatPB.Device.Builder deviceBuilder = ChatPB.Device.newBuilder();
        if(device.getTerminal() != null)
            deviceBuilder.setTerminal(device.getTerminal());
        if (needDeviceToken) {
            if(device.getDeviceToken() != null)
                deviceBuilder.setDeviceToken(device.getDeviceToken());
        }
        if(device.getLocale() != null)
            deviceBuilder.setLocale(device.getLocale());
        if(device.getLoginTime() != null)
            deviceBuilder.setLoginTime(device.getLoginTime());
        return deviceBuilder;
    }

    public static PNInfo fromPNInfoPB(ChatPB.PNInfo pnInfoPB) {
        if(pnInfoPB == null)
            return null;
        PNInfo pnInfo = new PNInfo();
        if(pnInfoPB.hasField(com.pbdata.generated.balancer.ChatPB.PNInfo.getDescriptor().findFieldByName("terminal"))) {
            Integer tt = pnInfoPB.getTerminal();
            pnInfo.setTerminal(tt);
        }
        if(pnInfoPB.hasField(com.pbdata.generated.balancer.ChatPB.PNInfo.getDescriptor().findFieldByName("needBadge"))) {
            pnInfo.setNeedBadge(pnInfoPB.getNeedBadge());
        }
        if(pnInfoPB.hasField(com.pbdata.generated.balancer.ChatPB.PNInfo.getDescriptor().findFieldByName("soundFile"))) {
            pnInfo.setSoundFile(pnInfoPB.getSoundFile());
        }
        if(pnInfoPB.hasField(com.pbdata.generated.balancer.ChatPB.PNInfo.getDescriptor().findFieldByName("customBadge"))) {
            pnInfo.setCustomBadge(pnInfoPB.getCustomBadge());
        }
        pnInfo.setCustomPropertyMap(pnInfoPB.getCustomPropertyMapMap());
        return pnInfo;
    }

    public static ChatPB.PNInfo.Builder toPNInfoPB(PNInfo pnInfo) {
        if(pnInfo == null)
            return null;
        ChatPB.PNInfo.Builder deviceBuilder = ChatPB.PNInfo.newBuilder();
        if(pnInfo.getTerminal() != null)
            deviceBuilder.setTerminal(pnInfo.getTerminal());
        if(pnInfo.getCustomBadge() != null)
            deviceBuilder.setCustomBadge(pnInfo.getCustomBadge());
        if(pnInfo.getSoundFile() != null)
            deviceBuilder.setSoundFile(pnInfo.getSoundFile());
        deviceBuilder.setNeedBadge(pnInfo.isNeedBadge());
        if(pnInfo.getCustomPropertyMap() != null)
            deviceBuilder.putAllCustomPropertyMap(pnInfo.getCustomPropertyMap());
        return deviceBuilder;
    }

}
