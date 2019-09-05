package chat.utils;

import java.util.TimerTask;
import java.util.concurrent.*;

import chat.logs.LoggerEx;
import chat.main.ServerStart;
import chat.scheduled.QuartzHandler;
import org.bson.types.ObjectId;

import javax.annotation.Resource;

public class TimerEx {
    private static final String TAG = TimerEx.class.getSimpleName();

    public static void schedule(TimerTaskEx task, Long delay) {
        try {
            if (delay != null) {
                task.setDelay(delay);
            }
            if (task.getId() == null) {
                task.setId(ObjectId.get().toString());
            }
            if(task.getThreadPoolExecutor() == null){
                task.setThreadPoolExecutor((ThreadPoolExecutor) Executors.newFixedThreadPool(1));
            }
            QuartzHandler.getInstance().addJob(task);
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Schedule TimerTask " + task + " failed, " + e.getMessage());
        }
    }

    public static void schedule(TimerTaskEx task, Long delay, Long period) {
        try {
            if (delay != null) {
                task.setDelay(delay);
            }
            if (period != null) {
                task.setPeriod(period);
            }
            if (task.getId() == null) {
                task.setId(ObjectId.get().toString());
            }
            if(task.getThreadPoolExecutor() == null){
                task.setThreadPoolExecutor((ThreadPoolExecutor) Executors.newFixedThreadPool(1));
            }
            QuartzHandler.getInstance().addJob(task);
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Schedule Period TimerTask " + task + " failed, " + e.getMessage());
        }
    }
    public static void schedule(TimerTaskEx task, String cron) {
        try {
            if (cron != null) {
                task.setCron(cron);
            }
            if (task.getId() == null) {
                task.setId(ObjectId.get().toString());
            }
            if(task.getThreadPoolExecutor() == null){
                task.setThreadPoolExecutor((ThreadPoolExecutor) Executors.newFixedThreadPool(1));
            }
            QuartzHandler.getInstance().addCronJob(task);
        } catch (Throwable e) {
            e.printStackTrace();
            LoggerEx.error(TAG, "Schedule TimerTask " + task + " failed, " + e.getMessage());
        }
    }
    public static void cancel(TimerTaskEx task) {
        try {
            if(task.getId() != null){
                QuartzHandler.getInstance().removeJob(task.getId());
            }
        }catch (Throwable e){
            e.printStackTrace();
            LoggerEx.error(TAG, "Remove timetask filed, taskId: " + task.getId());
        }
    }


//	public static void main(String args[]) {
//		System.out.println("start");
////		TimerEx.schedule(new TimerTaskEx() {
////			@Override
////			public void execute() {
////				System.out.println("done");
////			}
////		}, -1234);
////		TimerEx.schedule(new TimerTaskEx() {
////			@Override
////			public void execute() {
////				System.out.println("done1");
////			}
////		}, 2000);
//		TimerTaskEx timerTask = new TimerTaskEx() {
//			@Override
//			public void execute() {
//				try {
//					System.out.println("将要执行");
//					Thread.sleep(2000L);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				System.out.println("执行结果:123");
//			}
//		};
//		TimerEx.schedule(timerTask, 2000, 1);
////		TimerEx.schedule(new TimerTaskEx() {
////			@Override
////			public void execute() {
////				System.out.println("negetive");
////			}
////		}, -123);
//
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(2100L);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				boolean bool = timerTask.cancel();
//				System.out.println("canceled " + bool);
//			}
//		}).start();
//	}

}