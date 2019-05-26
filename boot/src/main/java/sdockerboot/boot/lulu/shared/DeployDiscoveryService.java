package sdockerboot.boot.lulu.shared;


import sdockerboot.boot.utils.DeployServiceUtils;

public class DeployDiscoveryService {
    public static void main(String[] args) throws Exception {
        String servicePath = SharedConstants.PATH + "DiscoveryService";
        String dockerName = SharedConstants.DOCKERNAME;
        String serviceName = "discovery";
        String gridfsHost = SharedConstants.GRIDFSHOST;
        String version = "1";
        String prefix = SharedConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "C:\\Users\\lulia\\work\\work_new\\SharedServices\\SharedCore"});
    }
}
