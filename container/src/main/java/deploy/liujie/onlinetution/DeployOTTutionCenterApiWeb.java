package deploy.liujie.onlinetution;


import utils.DeployServiceUtils;

public class DeployOTTutionCenterApiWeb {

    public static void main(String[] args) throws Exception {
        String servicePath = OnlineTuitionConstants.PATH + "OTTutionCenterApiWeb";
        String dockerName = OnlineTuitionConstants.DOCKERNAME;
        String libPath = OnlineTuitionConstants.LIBPATHS;
        String serviceName = "ottuitionapi";
        String gridfsHost = OnlineTuitionConstants.GRIDFSHOST;
        String version = "1";
        String prefix = OnlineTuitionConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", libPath});
    }
}
