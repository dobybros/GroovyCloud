package liujie.onlinetution;

import utils.DeployServiceUtils;

public class DeployOTSSOService {
    public static void main(String[] args) throws Exception {
        String servicePath = OnlineTuitionConstants.PATH + "OTSSOService";
        String libPath = OnlineTuitionConstants.LIBPATHS;
        String dockerName = OnlineTuitionConstants.DOCKERNAME;
        String serviceName = "acusso";
        String gridfsHost = OnlineTuitionConstants.GRIDFSHOST;
        String version = "1";
        String prefix = OnlineTuitionConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", libPath});
    }
}
