package lulu.im;


import utils.DeployServiceUtils;

public class DeployServerKeeperService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.PATH + "ServerKeeperService";
        String dockerName = IMConstants.DOCKERNAME;
        String serviceName = "serverkeeper";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version});
    }
}
