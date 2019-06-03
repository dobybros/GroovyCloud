package chat.utils;

import chat.logs.LoggerEx;

import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public abstract class TimerTaskEx extends TimerTask {
	private Future future;
	private ScheduledThreadPoolExecutor scheduledExecutorService;

	public void setScheduledExecutorService(ScheduledThreadPoolExecutor scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}

	public void setFuture(Future future) {
		this.future = future;
	}
	@Override
	public final void run() {
		try {
			execute();
		} catch (Throwable t) {
			t.printStackTrace();
			LoggerEx.error("TimerTaskEx", "execute failed, " + t.getMessage());
		}
	}

	public abstract void execute();

	@Override
	public boolean cancel() {
		if(future != null) {
			boolean bool = future.cancel(false);
			scheduledExecutorService.remove(this);
			return bool;
		}
		return false;
	}
}
