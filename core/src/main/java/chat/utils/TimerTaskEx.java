package chat.utils;

import chat.main.ServerStart;
import chat.scheduled.QuartzHandler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TimerTaskEx extends TimerTask implements Job{
	private String id;
	private Long delay;
	private Long period;
	private String cron;
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		TimerTaskEx task = (TimerTaskEx) jobExecutionContext.getMergedJobDataMap().get("TimerTaskEx");
		if(task != null){
			task.execute();
		}
	}

	public void cancel(){
		if(this.id != null){
			QuartzHandler.getInstance().removeJob(this.id);
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
}
