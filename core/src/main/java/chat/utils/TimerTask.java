package chat.utils;

import chat.thread.ThreadTaskRecord;

/**
 * Created by lick on 2019/6/9.
 * Description：
 */
public abstract class TimerTask implements Runnable{
    public abstract void execute();

    @Override
    public void run() {
        execute();
        ThreadTaskRecord.getInstance().removeTask(((TimerTaskEx)this).getId());
    }
}
