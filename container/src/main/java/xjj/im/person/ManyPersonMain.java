package xjj.im.person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ManyPersonMain {
	private static final String TAG = ManyPersonMain.class.getSimpleName();
	
	private static final String SERVER = "M0000000410";
	
//	static String host = "42.62.78.99";
//	static String host = "localhost";
//	static String host = "192.168.222.18";
	static String host = "127.0.0.1";


	private static final String ACCOUNTS_HOST = host + ":10051";
//	private static final String ACUCOM_HOST = host + ":5882";
	private static final String TCP_HOST = host;
	
	
//	private static final String ACUCOMDESTORY_HOST = host + ":5882";
	
	public static void main(String[] args) {
		final Object lock = new Object();
		final List<Person> persons = new ArrayList<Person>();
//		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//			@Override
//			public void run() {
//				DefaultHttpClient httpClient = new DefaultHttpClient();
//				for(Person p : persons) {
//					HttpGet get = new HttpGet("http://" + ACUCOMDESTORY_HOST + "/rest/sys/destroy/user/" + p.getUserId());
//					get.setHeader("Cookie", "s=" + SERVER + ";");
//					HttpResponse response;
//					try {
//						response = httpClient.execute(get);
//						HttpEntity getEntity = response.getEntity();
//						System.out.println("kill " + p.getAccount() + "||" + EntityUtils.toString(getEntity));
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//				synchronized (lock) {
//					lock.notify();
//				}
//			}
//		}));
		final String accountName = "bd";
		final String logPath = "./logs";
		for(int j = 0; j < 2; j++) {
			for(int i = 0;i < 100; i++) {
				String theAccountName = accountName + j + "_" + i;
				SendPerson person = new SendPerson();
				person.setAccountsHost(ACCOUNTS_HOST);
				person.setAccount(theAccountName);
				person.setDeviceToken("androiddevicetoken_" + theAccountName);
				person.setTerminal(Person.TERMINAL_ANDROID);
				person.setLogPath(logPath);
				person.setService("SS");
				
				new Thread(person).start();
				persons.add(person);
			}
		}
		
		/*
		String theAccountName = "mark";
		Person person = new SendPerson();
//		Person person = new IdlePerson();
		person.setAccountsHost(ACCOUNTS_HOST);
//		person.setAcucomHost(ACUCOM_HOST);
		person.setTcpHost(TCP_HOST);
		person.setAccount(theAccountName);
		person.setTerminal(DeviceInfo.TERMINAL_ANDROID);
		person.setDeviceToken("androiddevicetoken");
//		person.setPassword("12345678");
		person.setLogPath(logPath);
		
		new Thread(person).start();*/
		
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
