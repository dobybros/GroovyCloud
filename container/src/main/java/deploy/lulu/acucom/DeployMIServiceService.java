package deploy.lulu.acucom;


import deploy.lulu.im.IMConstants;
import deploy.lulu.tuition.TuitionConstants;
import utils.DeployServiceUtils;

public class DeployMIServiceService {
    public static void main(String[] args) throws Exception {
        String servicePath = "C:\\Users\\lulia\\work\\work_new\\acucom\\MIPNService";
        String dockerName = TuitionConstants.DOCKERNAME;
        String serviceName = "mipn";
        String gridfsHost = TuitionConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost});
    }
}
