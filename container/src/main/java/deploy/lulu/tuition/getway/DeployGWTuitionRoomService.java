package lulu.tuition.getway;


import lulu.im.IMConstants;
import lulu.tuition.TuitionConstants;
import utils.DeployServiceUtils;

public class DeployGWTuitionRoomService {
    public static void main(String[] args) throws Exception {
        String servicePath = TuitionConstants.PATH + "GWTuitionRoomService";
//        String dockerName = "gateway";
        String dockerName = TuitionConstants.DOCKERNAME;
        String serviceName = "gwtuitionroom";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", TuitionConstants.tuitionCore});
    }
}
