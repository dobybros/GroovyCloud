package lulu.tuition;


import utils.DeployServiceUtils;

public class DeployConverterTask {
    public static void main(String[] args) throws Exception {
        String servicePath = TuitionConstants.PATH + "ConverterTask";
        String dockerName = TuitionConstants.DOCKERNAME;
        String serviceName = "converter";
        String gridfsHost = TuitionConstants.GRIDFSHOST;
        String version = "1";
        String prefix = TuitionConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", "C:\\Users\\lulia\\work\\work_new\\TuitionCloud\\TCCore"});
    }
}
