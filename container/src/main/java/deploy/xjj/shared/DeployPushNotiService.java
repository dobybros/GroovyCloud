package deploy.xjj.shared;


import utils.DeployServiceUtils;

public class DeployPushNotiService {
    public static void main(String[] args) throws Exception {
        String servicePath = SharedConstants.SHARED_PATH + "/PushNotiService";
        String dockerName = SharedConstants.DOCKER_IM_BJ;
        String serviceName = "pushnoti";
        String gridfsHost = SharedConstants.GRIDFS_HOST;
        String version = "1";
        String prefix = SharedConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "/Users/huzhanjing/workspace/SharedServices/SharedCore"});
    }
}
