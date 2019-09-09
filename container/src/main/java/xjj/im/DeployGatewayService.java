package xjj.im;


import utils.DeployServiceUtils;

public class DeployGatewayService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.SHARED_PATH + "/GatewayService";
        String dockerName = IMConstants.DOCKER_IM_BJ;
        String serviceName = "gws";
        String gridfsHost = IMConstants.GRIDFS_HOST;
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-l", "/Users/huzhanjing/workspace/IMServers/IMCore"});
    }
}
