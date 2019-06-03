package chat.main;

import org.apache.tomcat.util.threads.TaskQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
public class ServerStart {
	private static String coreSize;
	private static String maximumPoolSize;
	private static String keepAliveTime;
	private static String queueCapacity;
	private static final String TAG = "ServerStart";
	private Map<String, String> asyncServletMap;

	private static volatile ServerStart instance;
	private boolean isStarted = false;
//	private static int coreSize = 0;
//	private static int maximumPoolSize = 0;
//	private static long keepAliveTime = 0L;
	public static ServerStart getInstance(){
        ClassPathResource configResource = new ClassPathResource("container.properties");
        Properties properties = new Properties();
        try {
            properties.load(configResource.getInputStream());
            coreSize = properties.getProperty("thread.coreSize");
            maximumPoolSize = properties.getProperty("thread.maximumPoolSize");
            keepAliveTime = properties.getProperty("thread.keepAliveTime");
            queueCapacity = properties.getProperty("thread.queueCapacity");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                configResource.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		if(instance == null){
			synchronized (ServerStart.class){
				if(instance == null){
					instance = new ServerStart();
				}
			}
		}
		return instance;
	}

	public static void main(String[] args) {
		File f = new File("./tmp");
		System.out.println(f.getAbsolutePath());
	}

	public Map<String, String> getAsyncServletMap() {
		return asyncServletMap;
	}

	public void setAsyncServletMap(Map<String, String> asyncServletMap) {
		this.asyncServletMap = asyncServletMap;
	}

	public ThreadPoolExecutor getThreadPool() {
		return new org.apache.tomcat.util.threads.ThreadPoolExecutor(Integer.valueOf(coreSize), Integer.valueOf(maximumPoolSize), Integer.valueOf(keepAliveTime), TimeUnit.SECONDS, new TaskQueue(Integer.valueOf(queueCapacity)));
	}
}
