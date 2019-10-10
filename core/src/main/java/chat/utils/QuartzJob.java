package chat.utils;

import chat.main.ServerStart;
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
            ServerStart.getInstance().getTimerThreadPoolExecutor().execute(task);
        }
    }
}
