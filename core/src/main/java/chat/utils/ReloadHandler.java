package chat.utils;

import chat.logs.LoggerEx;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public abstract class ReloadHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5599710163250746638L;
	protected static final String TAG = ReloadHandler.class.getSimpleName();
	private Long period = TimeUnit.SECONDS.toMillis(15);
	public ReloadHandler() {
	}
	
	public void init() throws IOException {
		try {
			load();
		} catch (Throwable e) {
			e.printStackTrace();
			LoggerEx.error(TAG, "Load in period " + period + " failed, " + ExceptionUtils.getFullStackTrace(e));
		}
		TimerEx.schedule(new TimerTaskEx(ReloadHandler.class.getSimpleName()) {
			@Override
			public void execute() {
				try {
					load();
				} catch (Throwable e) {
					e.printStackTrace();
					LoggerEx.error(TAG, "Load in period " + period + " failed, " + ExceptionUtils.getFullStackTrace(e));
				}
			}
		}, period, period);
	};
	public void init(boolean once) throws IOException {
		if(once){
			try {
				load();
			} catch (Throwable e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "Load in period " + period + " failed, " + ExceptionUtils.getFullStackTrace(e));
			}
		}else {
			init();
		}
	};
	public abstract void load() throws Throwable;

	public Long getPeriod() {
		return period;
	}

	public void setPeriod(Long period) {
		this.period = period;
	}
}
