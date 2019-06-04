package lulu.im;


import utils.DeployServiceUtils;

public class DeployIMAdminWeb {
    public static void main(String[] args) throws Exception {
        String servicePath = "C:\\Users\\lulia\\work\\work_new\\SharedServices\\IMAdminWeb";
        String dockerName = IMConstants.DOCKERNAME;
        String serviceName = "imadminweb";
        String gridfsHost = IMConstants.GRIDFSHOST;
        String version = "1";
        String prefix = IMConstants.PREFIX;
        DeployServiceUtils.main(new String[]{"-x", prefix, "-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version});
    }
}
