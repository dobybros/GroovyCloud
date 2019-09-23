package liujie.sharecore;

import liujie.scheduled.ScheduledConstants;
import utils.DeployServiceUtils;

class DeployScheduledTaskService {
    public static void main(String[] args) throws Exception {
        String servicePath = ShareCoreConstants.PATH + "ScheduledTaskService";
        String dockerName = ShareCoreConstants.DOCKERNAME;
        String libPath = ShareCoreConstants.LIBPATHS;
        String serviceName = "scheduledtask";
        String gridfsHost = ShareCoreConstants.GRIDFSHOST;
        String version = "1";
        String prefix = ShareCoreConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", libPath});
    }
}
