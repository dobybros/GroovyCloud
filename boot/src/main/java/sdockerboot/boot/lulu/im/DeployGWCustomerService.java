package sdockerboot.boot.lulu.im;

import sdockerboot.boot.utils.DeployServiceUtils;

public class DeployGWCustomerService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.PATH + "GWCustomerService";
        String dockerName = "gateway";
        String serviceName = "gwcustomer";
        String gridfsHost = IMConstants.GRIDFSHOST;
//        String gridfsHost = "mongodb://poker.9spirit.cn:7900";
//        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost});
    }
}
