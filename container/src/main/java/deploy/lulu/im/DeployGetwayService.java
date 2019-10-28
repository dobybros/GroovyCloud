package deploy.lulu.im;


import deploy.lulu.tuition.TuitionConstants;
import utils.DeployServiceUtils;

public class DeployGetwayService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.PATH + "GatewayService";
        String dockerName = TuitionConstants.DOCKERNAME;
        String serviceName = "gws";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-l", IMConstants.imCore});
    }
}
