package script.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by lick on 2020/6/29.
 * Description：
 */
public class CmdUtils {
    public static String execute(String cmd) throws Throwable{
        //"mvn dependency:list -f " + FilenameUtils.separatorsToUnix(pomFile.getAbsolutePath())
        final CommandLine cmdLine = CommandLine.parse(cmd);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(TimeUnit.MINUTES.toMillis(5));//设置超时时间
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(baos, baos));
        executor.setWatchdog(watchdog);
        executor.setExitValue(0);//由于ping被到时间终止，所以其默认退出值已经不是0，而是，所以要设置它
        executor.execute(cmdLine);
        return baos.toString().trim();
    }
}
