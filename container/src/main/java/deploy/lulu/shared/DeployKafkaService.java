package lulu.shared;


import utils.DeployServiceUtils;

public class DeployKafkaService {
    public static void main(String[] args) throws Exception {
        String servicePath = SharedConstants.PATH + "KafkaService";
        String dockerName = SharedConstants.DOCKERNAME;
        String serviceName = "kafka";
        String gridfsHost = SharedConstants.GRIDFSHOST;
        String version = "1";
        String prefix = SharedConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version, "-l", SharedConstants.sharedCore});
    }
}
