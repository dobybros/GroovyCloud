package liujie.scheduled;

import utils.DeployServiceUtils;

class DeployScheduledTaskService {
    public static void main(String[] args) throws Exception {
        String servicePath = ScheduledConstants.PATH + "ScheduledTaskService";
        String dockerName = ScheduledConstants.DOCKERNAME;
        String libPath = ScheduledConstants.LIBPATHS;
        String serviceName = "scheduledtask";
        String gridfsHost = ScheduledConstants.GRIDFSHOST;
        String version = "1";
        String prefix = ScheduledConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", libPath});
    }
}
