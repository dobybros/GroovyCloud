package lulu.tuition;


import utils.DeployServiceUtils;

public class DeployTCEnterpriseService {
    public static void main(String[] args) throws Exception {
        String servicePath = TuitionConstants.PATH + "TCEnterpriseService";
        String dockerName = TuitionConstants.DOCKERNAME;
        String serviceName = "tcenterprise";
        String gridfsHost = TuitionConstants.GRIDFSHOST;
        String version = "2";
        String prefix = TuitionConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "C:\\Users\\lulia\\work\\work_new\\TuitionCloud\\TCCore"});
    }
}
