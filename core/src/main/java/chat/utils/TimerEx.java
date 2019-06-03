package chat.utils;

import java.util.TimerTask;
import java.util.concurrent.*;

import chat.logs.LoggerEx;

public class TimerEx {
	private static final String TAG = TimerEx.class.getSimpleName();
	private static ScheduledThreadPoolExecutor scheduledExecutorService;
	static {
		scheduledExecutorService = new ScheduledThreadPoolExecutor(3);
		scheduledExecutorService.setRemoveOnCancelPolicy(true);
	}
	
	public static void schedule(TimerTask task, long delay) {
		try {
            ScheduledFuture future = scheduledExecutorService.schedule(task, delay, TimeUnit.MILLISECONDS);
            if(task instanceof TimerTaskEx) {
                ((TimerTaskEx)task).setFuture(future);
				((TimerTaskEx)task).setScheduledExecutorService(scheduledExecutorService);
            } else {
                LoggerEx.warn(TAG, "There still a TimerTask " + task + " delay " + delay + " not using TimerTaskEx, the cancel method will not be available");
            }
		} catch(Throwable e) {
		    e.printStackTrace();
			LoggerEx.error(TAG, "Schedule TimerTask " + task + " failed, " + e.getMessage());
		}
	}
	public static void schedule(TimerTask task, long delay, long period) {
		try {
            ScheduledFuture future = scheduledExecutorService.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
            BlockingQueue queue = scheduledExecutorService.getQueue();
            if(task instanceof TimerTaskEx) {
                ((TimerTaskEx)task).setFuture(future);
				((TimerTaskEx)task).setScheduledExecutorService(scheduledExecutorService);
            } else {
                LoggerEx.warn(TAG, "There still a  periodic TimerTask " + task + " delay " + delay + " not using TimerTaskEx, the cancel method will not be available");
            }
		} catch(Throwable e) {
            e.printStackTrace();
			LoggerEx.error(TAG, "Schedule Period TimerTask " + task + " failed, " + e.getMessage());
		}
	}
	
	public static void cancel() {
		LoggerEx.warn(TAG, "Why you want to cancel a Timer? Please email to aplomb@aculearn.com.cn");
	}


	public static void main(String args[]) {
		System.out.println("start");
//		TimerEx.schedule(new TimerTaskEx() {
//			@Override
//			public void execute() {
//				System.out.println("done");
//			}
//		}, -1234);
//		TimerEx.schedule(new TimerTaskEx() {
//			@Override
//			public void execute() {
//				System.out.println("done1");
//			}
//		}, 2000);
		TimerTaskEx timerTask = new TimerTaskEx() {
			@Override
			public void execute() {
				try {
					System.out.println("将要执行");
					Thread.sleep(2000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("执行结果:123");
			}
		};
		TimerEx.schedule(timerTask, 2000, 1);
//		TimerEx.schedule(new TimerTaskEx() {
//			@Override
//			public void execute() {
//				System.out.println("negetive");
//			}
//		}, -123);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				boolean bool = timerTask.cancel();
				System.out.println("canceled " + bool);
			}
		}).start();
	}

}