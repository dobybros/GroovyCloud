package lulu.im;


import com.docker.file.adapters.GridFSFileHandler;
import com.docker.storage.mongodb.MongoHelper;
import org.apache.commons.io.FileUtils;
import script.file.FileAdapter;

import java.io.File;
import java.util.Collection;

public class DeployUpdateP12Files {
    public static void main(String[] args) throws Exception {

        String gridfsHost = "mongodb://localhost:7900";
        MongoHelper helper = new MongoHelper();
        helper.setHost(gridfsHost);
        helper.setDbName("gridfiles");
//		helper.setUsername("socialshopsim");
//		helper.setPassword("eDANviLHQtjwmFlywyKu");
        helper.init();
//		helper.setUsername();

        GridFSFileHandler fileHandler = new GridFSFileHandler();
        fileHandler.setResourceHelper(helper);
        fileHandler.setBucketName("imfs");
        fileHandler.init();
        File directory = new File("/Users/admin/workSpace/files");
//        File directory = new File("/home/aplomb/dev/github/DiscoveryService/deploy");
        Collection<File> files = FileUtils.listFiles(directory, new String[]{"p12"}, true);
        if(files != null) {
            for(File file : files) {
                String filePath = file.getAbsolutePath();
                String dirPath = directory.getAbsolutePath();
                String thePath = filePath.substring(dirPath.length());
//				System.out.println("file " + thePath);

                FileAdapter.PathEx path = new FileAdapter.PathEx(thePath);
                fileHandler.saveFile(FileUtils.openInputStream(new File(filePath)), path, FileAdapter.FileReplaceStrategy.REPLACE);

                System.out.println("File " + thePath + " saved!");
            }
        }



//        String servicePath = "/Users/admin/workSpace/SDockerService/AgencyService";
//        String dockerName = "docker";
//        String serviceName = "agency";
////        String gridfsHost = "mongodb://localhost:7900";
//        String gridfsHost = "mongodb://poker.9spirit.cn:7900";
//        String version = "1";
//        DeployServiceUtils.main(new String[]{"-p", servicePath, "-d", dockerName, "-s", serviceName, "-f", gridfsHost, "-v", version});
    }
}
