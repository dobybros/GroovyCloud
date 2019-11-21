package chat.utils;

import chat.main.ServerStart;
import chat.thread.MultipleFixedThreadManager;
import org.quartz.*;

/**
 * Created by lick on 2019/10/10.
 * Description：
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class QuartzJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        TimerTaskEx task = (TimerTaskEx) jobExecutionContext.getMergedJobDataMap().get("TimerTaskEx");
        if(task != null){
            MultipleFixedThreadManager.getInstance().execute(ServerStart.getInstance().getTimerThreadPoolExecutor(), 1, task, task.getId(), null);
        }
    }
}
