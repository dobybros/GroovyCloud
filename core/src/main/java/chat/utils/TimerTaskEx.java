package chat.utils;

import chat.scheduled.QuartzHandler;
import org.quartz.*;

import java.util.concurrent.ThreadPoolExecutor;
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class TimerTaskEx extends TimerTask implements Job{
	private String id;
	private Long delay;
	private Long period;
	private String cron;
	private ThreadPoolExecutor threadPoolExecutor;
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		TimerTaskEx task = (TimerTaskEx) jobExecutionContext.getMergedJobDataMap().get("TimerTaskEx");
		if(task != null){
			task.threadPoolExecutor.execute(task);
		}
	}

	public void cancel(){
		if(this.id != null){
			QuartzHandler.getInstance().removeJob(this.id);
		}
		if(this.threadPoolExecutor != null){
			this.threadPoolExecutor.shutdownNow();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getDelay() {
		return delay;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	public Long getPeriod() {
		return period;
	}

	public void setPeriod(Long period) {
		this.period = period;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public ThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}

	public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
		this.threadPoolExecutor = threadPoolExecutor;
	}
}
