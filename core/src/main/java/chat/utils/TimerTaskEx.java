package chat.utils;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import chat.scheduled.QuartzFactory;
import org.apache.commons.lang.exception.ExceptionUtils;

public abstract class TimerTaskEx extends TimerTask{
	protected final String TAG = TimerTaskEx.class.getSimpleName();
	private String id;
	private Long delay;
	private Long period;
	private String cron;
	private Long scheduleTime;
	private String description;
	public TimerTaskEx(){
		this.description = "NOID_";
	}
	public TimerTaskEx(String description){
		this.description = description + "_";
	}
	@Override
	public abstract void execute();

	public void cancel(){
		if(this.id != null){
			try {
				QuartzFactory.getInstance().removeJob(this.id);
			} catch (CoreException e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "Remove timetask filed, taskId: " + this.id + ",e: " + ExceptionUtils.getFullStackTrace(e));
			}
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

	public Long getScheduleTime() {
		return scheduleTime;
	}

	public void setScheduleTime(Long scheduleTime) {
		this.scheduleTime = scheduleTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
