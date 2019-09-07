package xjj.im;


import utils.DeployServiceUtils;

public class DeployIMAgencyService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.SHARED_PATH + "/IMAgencyService";
        String dockerName = IMConstants.DOCKER_IM_BJ;
        String serviceName = "imagency";
        String gridfsHost = IMConstants.GRIDFS_HOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "/Users/huzhanjing/workspace/IMServers/IMCore"});
    }
}