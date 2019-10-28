package deploy.lulu.im;


import deploy.lulu.im.IMConstants;
import utils.DeployServiceUtils;

public class DeployIMAgencyService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.PATH + "IMAgencyService";
        String dockerName = "gateway";
        String serviceName = "imagency";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version});
    }
}
