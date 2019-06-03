package lulu.im;


import utils.DeployServiceUtils;

public class DeployIMApnService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.PATH + "IMApnService";
        String dockerName = IMConstants.DOCKERNAME;
        String serviceName = "imapn";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version});
    }
}
