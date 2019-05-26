package com.docker.rpc.impl;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServer extends Remote{
	public boolean alive() throws RemoteException;
	public byte[] call(byte[] data, String type, Byte encode) throws RemoteException;
}
