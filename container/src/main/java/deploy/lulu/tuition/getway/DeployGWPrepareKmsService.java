package lulu.tuition.getway;


import lulu.im.IMConstants;
import lulu.tuition.TuitionConstants;
import utils.DeployServiceUtils;

public class DeployGWPrepareKmsService {
    public static void main(String[] args) throws Exception {
        String servicePath = TuitionConstants.PATH + "GWPrepareKmsService";
        String dockerName = TuitionConstants.DOCKERNAME;
//        String dockerName = "tccore";
        String serviceName = "gwnetworkdetection";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", TuitionConstants.tuitionCore});
    }
}
