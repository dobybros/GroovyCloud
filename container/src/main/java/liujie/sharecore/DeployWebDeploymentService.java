package liujie.sharecore;

import utils.DeployServiceUtils;

public class DeployWebDeploymentService {
    public static void main(String[] args) throws Exception {
        String servicePath = ShareCoreConstants.PATH + "WebDeployment";
        String dockerName = ShareCoreConstants.DOCKERNAME;
        String libPath = ShareCoreConstants.LIBPATHS;
        String serviceName = "webdeployment";
        String gridfsHost = ShareCoreConstants.GRIDFSHOST;
        String version = "1";
        String prefix = ShareCoreConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", libPath});
    }
}
