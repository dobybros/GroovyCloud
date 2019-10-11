package client.imclient;

import client.imclient.data.IMMessage;

public interface IMMessageListener {
    public void onMessage(IMMessage message);
}
