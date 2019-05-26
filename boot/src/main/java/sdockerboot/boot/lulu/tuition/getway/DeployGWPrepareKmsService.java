package sdockerboot.boot.lulu.tuition.getway;


import sdockerboot.boot.lulu.im.IMConstants;
import sdockerboot.boot.lulu.tuition.TuitionConstants;
import sdockerboot.boot.utils.DeployServiceUtils;

public class DeployGWPrepareKmsService {
    public static void main(String[] args) throws Exception {
        String servicePath = TuitionConstants.PATH + "GWPrepareKmsService";
        String dockerName = "gateway";
//        String dockerName = "tccore";
        String serviceName = "tcplayer";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "C:\\Users\\lulia\\work\\work_new\\TuitionCloud\\TCCore"});
    }
}
