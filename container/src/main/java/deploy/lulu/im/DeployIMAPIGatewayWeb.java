package deploy.lulu.im;


import deploy.lulu.im.IMConstants;
import utils.DeployServiceUtils;

public class DeployIMAPIGatewayWeb {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.PATH + "IMAPIGatewayWeb";
        String dockerName = IMConstants.DOCKERNAME;
        String serviceName = "acuim";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", IMConstants.imCore});
    }
}
