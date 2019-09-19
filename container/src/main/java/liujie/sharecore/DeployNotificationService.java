package liujie.sharecore;

import liujie.onlinetution.OnlineTuitionConstants;
import utils.DeployServiceUtils;

public class DeployNotificationService {
    public static void main(String[] args) throws Exception {
        String servicePath = ShareCoreConstants.PATH + "NotificationService";
        String dockerName = ShareCoreConstants.DOCKERNAME;
        String libPath = ShareCoreConstants.LIBPATHS;
        String serviceName = "notification";
        String gridfsHost = ShareCoreConstants.GRIDFSHOST;
        String version = "1";
        String prefix = ShareCoreConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", libPath});
    }
}
