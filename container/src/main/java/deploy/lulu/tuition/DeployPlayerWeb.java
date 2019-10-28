package deploy.lulu.tuition;


import utils.DeployServiceUtils;

public class DeployPlayerWeb {
    public static void main(String[] args) throws Exception {
        String servicePath = TuitionConstants.PATH + "PlayerWeb";
        String dockerName = TuitionConstants.DOCKERNAME;
        String serviceName = "player";
        String gridfsHost = TuitionConstants.GRIDFSHOST;
        String version = "1";
        String prefix = TuitionConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", TuitionConstants.tuitionCore});
    }
}
