package liujie.onlinetution;

import utils.DeployServiceUtils;

public class DeployLinepayService {
    public static void main(String[] args) throws Exception {
        String servicePath = OnlineTuitionConstants.PATH + "LinePayService";
        String dockerName = OnlineTuitionConstants.DOCKERNAME;
        String libPath = OnlineTuitionConstants.LIBPATHS;
        String serviceName = "linepay";
        String gridfsHost = OnlineTuitionConstants.GRIDFSHOST;
        String version = "1";
        String prefix = OnlineTuitionConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", libPath});
    }
}
