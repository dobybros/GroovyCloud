package deploy.lulu.shared;


import deploy.lulu.tuition.TuitionConstants;
import utils.DeployServiceUtils;

public class DeployRemoteProxy {
    public static void main(String[] args) throws Exception {
        String servicePath = SharedConstants.PATH + "RemoteProxyService";
        String dockerName = SharedConstants.DOCKERNAME;
        String serviceName = "remoteproxy";
//        String gridfsHost = "mongodb://localhost:27017";
        String gridfsHost = TuitionConstants.GRIDFSHOST;
        String version = "1";
        String prefix = TuitionConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "C:\\Users\\lulia\\work\\work_new\\SharedServices\\SharedCore"});
    }
}
