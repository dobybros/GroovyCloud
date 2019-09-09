package xjj.acucom;


import utils.DeployServiceUtils;

public class DeployACUserService {
    public static void main(String[] args) throws Exception {
        String servicePath = ACConstants.SHARED_PATH + "/ACUserService";
        String dockerName = ACConstants.DOCKER_IM_BJ;
        String serviceName = "acuser";
        String gridfsHost = ACConstants.GRIDFS_HOST;
        String version = "1";
        String prefix = ACConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "/Users/huzhanjing/workspace/ACServers/ACCore"});
    }
}
