package deploy.lulu.tuition.getway;


import deploy.lulu.im.IMConstants;
import deploy.lulu.tuition.TuitionConstants;
import utils.DeployServiceUtils;

public class DeploySignalService {
    public static void main(String[] args) throws Exception {
        String servicePath = TuitionConstants.PATH + "GWSignalService";
        String dockerName = TuitionConstants.DOCKERNAME;
//        String dockerName = "tccore";
        String serviceName = "gwsignal";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", TuitionConstants.tuitionCore});
    }
}
