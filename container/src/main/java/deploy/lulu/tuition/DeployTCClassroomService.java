package deploy.lulu.tuition;


import utils.DeployServiceUtils;

public class DeployTCClassroomService {
    public static void main(String[] args) throws Exception {
        String servicePath = TuitionConstants.PATH + "TCClassroomService";
        String dockerName = TuitionConstants.DOCKERNAME;
        String serviceName = "tcclassroom";
        String gridfsHost = TuitionConstants.GRIDFSHOST;
        String version = "2";
        String prefix = TuitionConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", TuitionConstants.tuitionCore});
    }
}