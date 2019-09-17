package chat.main;

import org.apache.tomcat.util.threads.TaskQueue;
import org.eclipse.jetty.util.thread.ThreadPool;
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
	private ThreadPoolExecutor threadPoolExecutor;
	private ThreadPoolExecutor coreThreadPoolExecutor;

	private static volatile ServerStart instance;
	public static ServerStart getInstance(){

		if(instance == null){
			synchronized (ServerStart.class){
				if(instance == null){
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
	//业务使用
	public ThreadPoolExecutor getThreadPool() {
		if(threadPoolExecutor == null){
			threadPoolExecutor = new ThreadPoolExecutor(Integer.valueOf(coreSize), Integer.valueOf(maximumPoolSize), Integer.valueOf(keepAliveTime), TimeUnit.SECONDS, new TaskQueue(Integer.valueOf(queueCapacity)));
		}
		return threadPoolExecutor;
	}
	//底层使用
	public ThreadPoolExecutor getCoreThreadPoolExecutor(){
		if(coreThreadPoolExecutor == null){
			coreThreadPoolExecutor = new ThreadPoolExecutor(Integer.valueOf(coreSize), Integer.valueOf(maximumPoolSize), Integer.valueOf(keepAliveTime), TimeUnit.SECONDS, new TaskQueue(Integer.valueOf(queueCapacity)));
		}
		return coreThreadPoolExecutor;
	}
//	public String threadPoolStatus() {
//		StringBuffer buffer = new StringBuffer("Web ThreadPool");
//		buffer.append(" threadPool: " + threadPoolExecutor);
//		return buffer.toString();
//	}
//	public String coretThreadPoolStatus() {
//		StringBuffer buffer = new StringBuffer("Web ThreadPool");
//		buffer.append(" threadPool: " + coreThreadPoolExecutor);
//		return buffer.toString();
//	}
}
