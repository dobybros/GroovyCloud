package xjj.tc;


import utils.DeployServiceUtils;

public class DeployTCPlayerService {
    public static void main(String[] args) throws Exception {
        String servicePath = TCConstants.TC_PATH + "/GWTCPlayerService";
        String dockerName = TCConstants.DOCKER_IM_BJ;
        String serviceName = "tcplayer";
        String gridfsHost = TCConstants.GRIDFS_HOST;
        String version = "1";
        String prefix = TCConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "/Users/huzhanjing/workspace/TuitionCloud/TCCore"});
    }
}
