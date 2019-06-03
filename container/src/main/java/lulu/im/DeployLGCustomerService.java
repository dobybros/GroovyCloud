package lulu.im;


import utils.DeployServiceUtils;

public class DeployLGCustomerService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.PATH + "LGCustomerService";
        String dockerName = "login";
        String serviceName = "lgcustomer";
        String gridfsHost = IMConstants.GRIDFSHOST;
//        String gridfsHost = "mongodb://poker.9spirit.cn:7900";
//        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost});
    }
}
