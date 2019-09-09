package xjj.im.person;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SendPersonMain {
	private static final String TAG = SendPersonMain.class.getSimpleName();
	
	private static final String SERVER = "M0000000410";

	static String host = "localhost";

    private static final String ACCOUNTS_HOST = host + ":10052";
	private static final String ACUCOM_HOST = host + ":5882";

	private static final String TCP_HOST = host;
	private static final String ACUCOMDESTORY_HOST = host + ":5882";
	private static String SERVICE = "gwsfusignal";

//			"zh_CN" "en_US" "es_ES" "tr_TR" "ru_RU"
	private static String LANGUAGE = "zh_CN";

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
		final String accountName = "hzj_room";
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
		
//		String theAccountName = "mark";
//		Person person = new SendPerson();
////		Person person = new IdlePerson();
//		person.setAccountsHost(ACCOUNTS_HOST);
//		person.setAcucomHost(ACUCOM_HOST);
//		person.setTcpHost(TCP_HOST);
//		person.setAccount(theAccountName);
//		person.setTerminal(DeviceInfo.TERMINAL_ANDROID);
//		person.setDeviceToken("androiddevicetoken");
////		person.setPassword("12345678");
//		person.setLogPath(logPath);
//		person.setService("SS");
		
//		new Thread(person).start();

//		String theAccountName1 = "2fasdfasdfsd";
		final SendPerson person1 = new SendPerson();
//		Person person = new IdlePerson();
		person1.setAccountsHost(ACCOUNTS_HOST);
		person1.setAcucomHost(ACUCOM_HOST);
		person1.setTcpHost(TCP_HOST);
		person1.setAccount(accountName);
		person1.setTerminal(Person.TERMINAL_WEB_PC);
		person1.setDeviceToken("iosdevicetoken");
//		person.setPassword("12345678");
		person1.setLogPath(logPath);
		person1.setService(SERVICE);
		person1.setLanguage(LANGUAGE);
		
		new Thread(person1).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (person1) {
					try {
						person1.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				for(int i = 0; i < 1; i++) {
//					person1.sendMessage("test20170214");
					//D8136C28-8BBE-4DE7-A2B5-909EAE2E750C
//					person1.sendMessage(SERVICE, "liyazhou");
//					person1.sendMessage("saas_icson_buyer", "uu20170314205811000100002");
//					person1.sendMessage("SS", "uu20170314190203000100001");
//					person1.sendMessage("pgone_web", "hzjreceiver");
//					person1.sendOrdewrMessage("saasicsonseller", "aplomb");
					// 发送json消息
//					person1.sendTextMessageJson("pgone_room", "room1");
					// 标记readTime
//					person1.sendReadtimeMessageJson("8a6nc92c95i0m5hmgcl7hbhc1n7l", 1511417447707L, "pgone_web", "hzjreceiver1");
					// 获取历史消息
//					person1.getHistoryMessageJson("sessionId", 78897897897L, "pgone_web", "hzjreceiver1");
					// 删除session
//					person1.deleteSessionJson("sessionid", "pgone_web", "hzjreceiver1");
					// 发送IncomingData
//					person1.sendMessageIncomingData("tcplayer", "room1");

					try {
						Thread.sleep(200L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
//				try {
//					Thread.sleep(500L);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				System.exit(1);
			}
		}).start();
		
		persons.add(person1);
		
		
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
