package deploy.lulu.tuition.getway;


import deploy.lulu.im.IMConstants;
import deploy.lulu.tuition.TuitionConstants;
import utils.DeployServiceUtils;

public class DeployGWTCPlayerService {
    public static void main(String[] args) throws Exception {
        String servicePath = TuitionConstants.PATH + "GWTCPlayerService";
        String dockerName = TuitionConstants.DOCKERNAME;
//        String dockerName = "tccore";
        String serviceName = "tcplayer";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", TuitionConstants.tuitionCore});
    }
}
