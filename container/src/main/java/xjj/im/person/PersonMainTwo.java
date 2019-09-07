package xjj.im.person;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PersonMainTwo {
	private static final String TAG = PersonMainTwo.class.getSimpleName();
	
	private static final String SERVER = "M0000000410";
//	cn aws
//	static String host = "10.201.5.176";
//static String host = "elb0601-446394537.cn-north-1.elb.amazonaws.com.cn";


//	static String host = "192.168.222.18";
//	static String host = "127.0.0.1";
	static String host = "localhost";
//	static String host = "192.168.222.53";
//	static String host = "127.0.0.1";
//	static String host = "localhost";

//	static String host = "im.dobybros.com";

	private static final String ACCOUNTS_HOST = host + ":10052";
//	private static final String ACCOUNTS_HOST = host + ":80";
//	private static final String ACUCOM_HOST = host + ":5882";
	private static final String TCP_HOST = host;
	
	
	private static final String ACUCOMDESTORY_HOST = host + ":5882";
	
//	private static String SERVICE = "icson";
//	private static String SERVICE = "saas_icson_seller";
//	private static String SERVICE = "SS";
//	private static String SERVICE = "saasnovoshopsseller";
	private static String SERVICE = "saasicsonbuyer";
//	private static String SERVICE = "saasnovoshopsbuyer";

//	"zh_CN" "en_US" "es_ES" "tr_TR" "ru_RU"
	private static String LANGUAGE = "en_US";

	public static void main(String[] args) {
		final Object lock = new Object();
		final List<Person> persons = new ArrayList<Person>();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				for(Person p : persons) {
					HttpGet get = new HttpGet("http://" + ACUCOMDESTORY_HOST + "/rest/sys/destroy/user/" + p.getUserId());
					get.setHeader("Cookie", "s=" + SERVER + ";");
					HttpResponse response;
					try {
						response = httpClient.execute(get);
						HttpEntity getEntity = response.getEntity();
						System.out.println("kill " + p.getAccount() + "||" + EntityUtils.toString(getEntity));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				synchronized (lock) {
					lock.notify();
				}
			}
		}));
		final String accountName = "test";
//		final String logPath = "/Users/aplombchen/Desktop/logs";
		final String logPath = "./logs";
//		for(int j = 0; j < 20; j++) {
//			for(int i = 0;i < 100; i++) {
//				String theAccountName = accountName + j + "_" + i;
//				Person person = new Person();
//				person.setAccountsHost(ACCOUNTS_HOST);
//				person.setAcucomHost(ACUCOM_HOST);
//				person.setAccount(theAccountName);
//				person.setDomain(domain);
//				person.setPassword("12345678");
//				person.setLogPath(logPath);
//				
//				new Thread(person).start();
//				persons.add(person);
//			}
//		}
//		String theAccountName = "mark124";
//		String theAccountName = "liyazhou";
		String theAccountName = "hzjreceiver";
		SendPerson person = new SendPerson();
//		Person person = new IdlePerson();
		person.setAccountsHost(ACCOUNTS_HOST);
//		person.setAcucomHost(ACUCOM_HOST);
		person.setTcpHost(TCP_HOST);
		person.setAccount(theAccountName);
		person.setTerminal(Person.TERMINAL_IOS_PAD);
		person.setDeviceToken("device2222device");
//		person.setPassword("12345678");
		person.setLogPath(logPath);
		person.setService(SERVICE);
		person.setLanguage(LANGUAGE);
		
		new Thread(person).start();

		// 退出逻辑测试
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(5000L);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				person.logout();
//			}
//		}).start();

//		String theAccountName1 = "aplomb";
//		final SendPerson person1 = new SendPerson();
////		Person person = new IdlePerson();
//		person1.setAccountsHost(ACCOUNTS_HOST);
//		person1.setAcucomHost(ACUCOM_HOST);
//		person1.setTcpHost(TCP_HOST);
//		person1.setAccount(theAccountName1);
//		person1.setTerminal(DeviceInfo.TERMINAL_IOS);
//		person1.setDeviceToken("iosdevicetoken");
////		person.setPassword("12345678");
//		person1.setLogPath(logPath);
//		
//		new Thread(person1).start();
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				synchronized (person1) {
//					try {
//						person1.wait();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//				person1.sendMessage("mark");
//			}
//		}).start();
//		
//		persons.add(person1);
		
		
		if (Boolean.parseBoolean(System.getenv("RUNNING_IN_ECLIPSE")) == true) {
			System.out
					.println("You're using Eclipse; click in this console and     "
							+ "press ENTER to call System.exit() and run the shutdown routine.");
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(0);
		} else {
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
