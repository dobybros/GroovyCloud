package sdockerboot.boot.lulu.im;


import sdockerboot.boot.utils.DeployServiceUtils;

public class DeployLGNotFoundService {
    public static void main(String[] args) throws Exception {
        String servicePath = IMConstants.PATH + "LGNotFoundService";
        String dockerName =  "login";
        String serviceName = "servicenotfound";
        String gridfsHost = IMConstants.GRIDFSHOST;
//        String gridfsHost = "mongodb://poker.9spirit.cn:7900";
//        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost});
    }
}
