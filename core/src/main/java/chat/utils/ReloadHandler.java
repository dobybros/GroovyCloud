package chat.utils;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import chat.logs.LoggerEx;


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
			LoggerEx.error(TAG, "Load in period " + period + " failed, " + e.getMessage());
		}
		TimerEx.schedule(new TimerTaskEx() {
			@Override
			public void execute() {
				try {
					load();
				} catch (Throwable e) {
					e.printStackTrace();
					LoggerEx.error(TAG, "Load in period " + period + " failed, " + e.getMessage());
				}
			}
		}, period, period);
	};
	
	public abstract void load() throws Throwable;

	public Long getPeriod() {
		return period;
	}

	public void setPeriod(Long period) {
		this.period = period;
	}
}
