package lulu.im;


import utils.DeployServiceUtils;

public class DeployIMUserInPresenceService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.PATH + "IMUserInPresenceService";
        String dockerName = IMConstants.DOCKERNAME;
        String serviceName = "imuserinpresence";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "C:\\Users\\lulia\\work\\work_new\\IMServers\\IMCore"});
    }
}
