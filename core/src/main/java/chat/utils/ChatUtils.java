package chat.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ChatUtils {
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSSS");
	static {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8")); 
	}
	public static String dateString(Date date) {
		return sdf.format(date);
	}
	public static String dateString(long time) {
		return sdf.format(new Date(time));
	}
	public static String dateString() {
		return sdf.format(new Date());
	}
	
	public static interface CostClassListener {
		public Object costClass(String str, Class<?> costClass);
	}
	private static Map<Class<?>, CostClassListener> costClassMap = new HashMap<>();
	static {
		CostClassListener intListener = new CostClassListener() {
			@Override
			public Object costClass(String str, Class<?> costClass) {
				try {
					return Integer.parseInt(str);
				} catch (Throwable e) {
				}
				if(costClass.equals(int.class))
					return 0;
				return null;
			}
		};
		costClassMap.put(int.class, intListener);
		costClassMap.put(Integer.class, intListener);
		
		CostClassListener longListener = new CostClassListener() {
			@Override
			public Object costClass(String str, Class<?> costClass) {
				try {
					return Long.parseLong(str);
				} catch (Throwable e) {
				}
				if(costClass.equals(long.class))
					return 0L;
				return null;
			}
		};
		costClassMap.put(long.class, longListener);
		costClassMap.put(Long.class, longListener);
		
		CostClassListener booleanListener = new CostClassListener() {
			@Override
			public Object costClass(String str, Class<?> costClass) {
				try {
					return Boolean.parseBoolean(str);
				} catch (Throwable e) {
				}
				if(costClass.equals(boolean.class))
					return false;
				return null;
			}
		};
		costClassMap.put(boolean.class, booleanListener);
		costClassMap.put(Boolean.class, booleanListener);
		
		CostClassListener doubleListener = new CostClassListener() {
			@Override
			public Object costClass(String str, Class<?> costClass) {
				try {
					return Double.parseDouble(str);
				} catch (Throwable e) {
				}
				if(costClass.equals(double.class))
					return 0.0;
				return null;
			}
		};
		costClassMap.put(double.class, doubleListener);
		costClassMap.put(Double.class, doubleListener);
	}
	public static Object typeCost(String str, Class<?> costClass) {
		CostClassListener costClassListener = costClassMap.get(costClass);
		if(costClassListener != null) {
			return costClassListener.costClass(str, costClass);
		}
		return str;
	}
	
	public static Integer parseInt(String str, Integer defaultValue) {
		if(str != null) {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}
	
	public static Long parseLong(String str, Long defaultValue) {
		if(str != null) {
			try {
				return Long.parseLong(str);
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}
	
	public static String generateId(List<String> keys) {
		Collections.sort(keys);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < keys.size(); i++) {
			buffer.append(keys.get(i));
			if (i < keys.size() - 1) {
				buffer.append("_");
			}
		}
		return buffer.toString();
	}
	
	/*
	 * get the local host ip
	 */
	private static String getLocalHostIpPrivate(){
		try{
			InetAddress addr=InetAddress.getLocalHost();
			return addr.getHostAddress();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getLocalHostIp() {
		return getLocalHostIp(null, null);
	}
	
	public static String getLocalHostIp(String ipStartWith, String faceStartWith) {
		NetworkInterface iface = null;
		String ethr;
		String myip = null;
		
		if(ipStartWith == null && faceStartWith == null) {
			myip = getLocalHostIpPrivate();
			if(myip != null) 
				return myip;
		}
		try
		{
//			String anyIp = null;
			for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();ifaces.hasMoreElements();)
			{
				iface = (NetworkInterface)ifaces.nextElement();
				ethr = iface.getDisplayName();

				if (faceStartWith == null || isStartWith(ethr, faceStartWith))
				{
					InetAddress ia = null;
					for(Enumeration<InetAddress> ips = iface.getInetAddresses();ips.hasMoreElements();)
					{
						ia = (InetAddress)ips.nextElement();
						String anyIp = ia.getHostAddress();
						if (ipStartWith == null || anyIp.startsWith(ipStartWith))
						{
							myip = ia.getHostAddress();
							return myip;
						}
					}
				}
			}
		}
		catch (SocketException e){}
		return myip;
	}

	private static boolean isStartWith(String str, String faceStartWith) {
		if(faceStartWith != null && str != null) {
			if(faceStartWith.contains("|")) {
				String[] compares = faceStartWith.split("|");
				for(String compare : compares) {
					if(str.startsWith(compare)) {
						return true;
					}
				}
			} else {
				if(str.startsWith(faceStartWith)) {
					return true;
				}
			}
		}
		return false;
	}

	static SimpleDateFormat gmtFormatter = new SimpleDateFormat("yyyy/MM/dd"); 
	static {
		gmtFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
    public static int generateSecret(String sid, int seed, int sequence) {
		int xor = 0;
		for(int i = 0; i < sid.length(); i++) {
			char c = sid.charAt(i);
			xor ^= (c << seed);
		}
		return (sequence << (sequence % seed)) ^ xor;
	}
    
    public static String toString(Object[] objs, String sperator) {
    	if(objs == null)
			return null;
		if(objs.length == 0)
			return "";
		StringBuffer buffer = new StringBuffer();
		for(Object obj : objs) {
			buffer.append(obj).append(sperator);
		}
		return buffer.substring(0, buffer.length() - 1);
	}
    
    public static String toString(String[] strs) {
    	return toString(strs, ",");
    }
    
    public static String toString(String[] strs, String sperator) {
		if(strs == null)
			return null;
		if(strs.length == 0)
			return "";
		StringBuffer buffer = new StringBuffer();
		for(String str : strs) {
			buffer.append(str).append(sperator);
		}
		return buffer.substring(0, buffer.length() - 1);
	}
	public static String[] fromString(String str) {
		if(str == null)
			return null;
		return str.split(",");
	}
	public static String toString(Collection<?> strs, String sperator) {
		if(strs == null || strs.size() == 0)
			return "";
		StringBuffer buffer = new StringBuffer();
		for(Object str : strs) {
			buffer.append(str).append(sperator);
		}
		return buffer.substring(0, buffer.length() - 1);
	}
	public static String toString(Collection<?> strs) {
		return toString(strs, ",");
	}
	
	static RandomString randomString = new RandomString(6);
	public static String generateFixedRandomString() {
		return randomString.nextString();
	}
	
	public static void main(String[] args) {
		for(int i = 0; i < 100; i++)
			System.out.println(randomString.nextString());;
	}
	
	public static byte[] intToByte(int i) {

        byte[] abyte0 = new byte[4];

        abyte0[0] = (byte) (0xff & i);

        abyte0[1] = (byte) ((0xff00 & i) >> 8);

        abyte0[2] = (byte) ((0xff0000 & i) >> 16);

        abyte0[3] = (byte) ((0xff000000 & i) >> 24);

        return abyte0;

    }

    public  static int bytesToInt(byte[] bytes) {

        int addr = bytes[0] & 0xFF;

        addr |= ((bytes[1] << 8) & 0xFF00);

        addr |= ((bytes[2] << 16) & 0xFF0000);

        addr |= ((bytes[3] << 24) & 0xFF000000);

        return addr;

    }
}
