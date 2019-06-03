package lulu.im;


import lulu.tuition.TuitionConstants;
import utils.DeployServiceUtils;

public class DeployIMServerStatusService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.PATH + "IMServerStatusService";
        String dockerName = TuitionConstants.DOCKERNAME;
        String serviceName = "imserverstatus";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version});
    }
}
