package chat.utils;

/**
 * Created by lick on 2019/6/9.
 * Description：
 */
public abstract class TimerTask implements Runnable{
    public abstract void execute();

    @Override
    public void run() {
        execute();
    }
}
