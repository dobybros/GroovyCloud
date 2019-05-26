package sdockerboot.boot.lulu.shared;


import sdockerboot.boot.lulu.tuition.TuitionConstants;
import sdockerboot.boot.utils.DeployServiceUtils;

public class DeployScheduledTask {
    public static void main(String[] args) throws Exception {
        String servicePath = SharedConstants.PATH + "ScheduledTaskService";
        String dockerName = SharedConstants.DOCKERNAME;
        String serviceName = "scheduledtask";
//        String gridfsHost = "mongodb://localhost:7900";
        String gridfsHost = TuitionConstants.GRIDFSHOST;
        String version = "1";
        String prefix = TuitionConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "C:\\Users\\lulia\\work\\work_new\\SharedServices\\SharedCore"});
    }
}
