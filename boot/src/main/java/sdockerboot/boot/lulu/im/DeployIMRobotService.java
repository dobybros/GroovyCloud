package sdockerboot.boot.lulu.im;


import sdockerboot.boot.utils.DeployServiceUtils;

public class DeployIMRobotService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.PATH + "IMRobotService";
        String dockerName = IMConstants.DOCKERNAME;
        String serviceName = "robot";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version});
    }
}
