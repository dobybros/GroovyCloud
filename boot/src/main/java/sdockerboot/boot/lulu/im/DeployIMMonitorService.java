package sdockerboot.boot.lulu.im;


import sdockerboot.boot.lulu.tuition.TuitionConstants;
import sdockerboot.boot.utils.DeployServiceUtils;

public class DeployIMMonitorService {
    public static void main(String[] args) throws Exception {
        String servicePath = "C:\\Users\\lulia\\work\\work_new\\SharedServices\\IMMonitorService";
        String dockerName = TuitionConstants.DOCKERNAME;
        String serviceName = "immonitor";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version});
    }
}
