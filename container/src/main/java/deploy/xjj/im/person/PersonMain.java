package deploy.xjj.im.person;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PersonMain {
	private static final String TAG = PersonMain.class.getSimpleName();
	
	private static final String SERVER = "M0000000410";
	static String host = "localhost";
	private static final String ACCOUNTS_HOST = host + ":10066";
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
		final String accountName = "test";
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

		String theAccountName = "hzj2";
		SendPerson person = new SendPerson();
//		Person person = new IdlePerson();
//		person.set
		person.setAccountsHost(ACCOUNTS_HOST);
//		person.setAcucomHost(ACUCOM_HOST);
		person.setTcpHost(TCP_HOST);
		person.setAccount(theAccountName);
		person.setTerminal(Person.TERMINAL_IOS);
		person.setDeviceToken("8b62e3fd 7102cdda a51500b9 7616b00d 52acc87a 787426b6 c1ecbbfe 690f6ad1");
//		person.setPassword("12345678");
		person.setLogPath(logPath);
		person.setService(SERVICE);
		person.setLanguage(LANGUAGE);
		
		new Thread(person).start();

		// 退出逻辑测试
		/*new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				person.logout();
			}
		}).start();

		String theAccountName1 = "aplomb";
		final SendPerson person1 = new SendPerson();
//		Person person = new IdlePerson();
		person1.setAccountsHost(ACCOUNTS_HOST);
		person1.setAcucomHost(ACUCOM_HOST);
		person1.setTcpHost(TCP_HOST);
		person1.setAccount(theAccountName1);
		person1.setTerminal(DeviceInfo.TERMINAL_IOS);
		person1.setDeviceToken("iosdevicetoken");
//		person.setPassword("12345678");
		person1.setLogPath(logPath);

		new Thread(person1).start();*/
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (person) {
					try {
						person.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// 标记readTime
//					person.sendReadtimeMessageJson("8a6nc92c95i0m5hmgcl7hbhc1n7l", 1511417447707L, "pgone_web", "uu20170516085734000102");
					// 获取历史消息
//					person.getHistoryMessageJson("8a6nc92c95i0m5hmgcl7hbhc1n7l", 1511417936037L, "pgone_web", "uu20170516085734000102");
					// 删除session
//					person.deleteSessionJson("8a6nc92c95i0m5hmgcl7hbhc1n7l", "pgone_web", "uu20170516085734000102");
				}
			}
		}).start();

		persons.add(person);
		
		
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
