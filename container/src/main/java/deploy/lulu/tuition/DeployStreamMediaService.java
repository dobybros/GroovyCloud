package deploy.lulu.tuition;


import utils.DeployServiceUtils;

public class DeployStreamMediaService {
    public static void main(String[] args) throws Exception {
        String servicePath = TuitionConstants.PATH + "StreamMediaService";
        String dockerName = TuitionConstants.DOCKERNAME;
        String serviceName = "streammedia";
        String gridfsHost = TuitionConstants.GRIDFSHOST;
        String version = "1";
        String prefix = TuitionConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", TuitionConstants.tuitionCore});
    }
}
