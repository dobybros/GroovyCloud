package chat.logs;

import chat.utils.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AnalyticsLogger {
    private static Logger analytics = LoggerFactory.getLogger("analytics");

    private AnalyticsLogger() {
    }

    public static void info(String tag, String msg) {
        String log = getLogMsg(tag, msg);
        analytics.info(log);
        System.out.println(log);
    }

    public static void warn(String tag, String msg) {
        String log = getLogMsg(tag, msg);
        analytics.warn(log);
        System.out.println(log);
    }

    public static void error(String tag, String msg) {
        String log = getLogMsg(tag, msg);
        analytics.error(log);
        System.out.println(log);
    }

    public static void fatal(String tag, String msg) {
        String log = getLogMsg(tag, msg);
        analytics.error(log);
        System.out.println(log);
    }

    private static String getLogMsg(String tag, String msg) {
//        returnnew StringBuilder("[").append(ChatUtils.dateString()).append("|").append(tag).append("] ").append(msg).toString();
        StringBuilder builder = new StringBuilder();
        builder.append("$$time:: " + ChatUtils.dateString()).
                append(" $$tag:: " + tag).
                append(" ").
                append(msg);
        return builder.toString();
    }
}

