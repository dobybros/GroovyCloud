package lulu.tuition;


import lulu.im.IMConstants;
import utils.DeployServiceUtils;

public class DeployKMSMonitorService {
    public static void main(String[] args) throws Exception {
        String servicePath = TuitionConstants.PATH + "KMSMonitorService";
        String dockerName = TuitionConstants.DOCKERNAME;
        String serviceName = "tcsfumonitor";
        String gridfsHost = TuitionConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "C:\\Users\\lulia\\work\\work_new\\TuitionCloud\\TCCore"});
    }
}
