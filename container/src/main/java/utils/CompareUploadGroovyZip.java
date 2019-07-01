package utils;

import chat.logs.LoggerEx;
import chat.utils.MD5OutputStream;
import com.docker.file.adapters.GridFSFileHandler;
import com.docker.storage.DBException;
import com.docker.storage.mongodb.MongoHelper;
import org.apache.commons.io.FileUtils;
import script.file.FileAdapter;
import script.groovy.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CompareUploadGroovyZip {
    private static final String TAG = CompareUploadGroovyZip.class.getSimpleName();
    public static String dir ;
    @Bean
    static class  MongoInfo{
        private List<String> getPathList;
        private String md5;
        private List<String> pathList;
    }
    /*
     *  host ： mongodb 地址
     *  bucket ： mongodb 表地址
     *  dir ： 本机server路径
     *  注意：开发环境 bucket 为 imfs 测试环bucket 为 fs
     * */
    public static void main(String[] args) throws IOException, DBException {
        compareUpload("mongodb://192.168.1.170:7900","imfs", "mongodb://192.168.3.200:7900","fs");

    }


    public static void compareUpload(String host1, String bucket1, String host2, String bucket2)  {
        Map<String, MongoInfo> officeMap  = new HashMap();
        Map<String, MongoInfo> beijingMap = new HashMap();
        String localPath = "C:/Users/Administrator/Desktop/officeAo";
        String localPathBeijin = "C:/Users/Administrator/Desktop/beijingAo";
        try {
            //170
            download(host1, bucket1, localPath, officeMap);

            //200
            download(host2, bucket2, localPathBeijin, beijingMap);

            compareMd5(host2, bucket2, officeMap, beijingMap );

        }catch (Exception e){
            System.out.print(e.toString());
        }


    }
    public static void compareMd5(String host, String bucket, Map<String,MongoInfo> officeMap, Map<String,MongoInfo> beiJingMap) throws DBException, IOException {

        MongoHelper helper = new MongoHelper();
        helper.setHost(host);
        helper.setDbName("gridfiles");
        helper.init();
        GridFSFileHandler fileHandler = new GridFSFileHandler();
        fileHandler.setResourceHelper(helper);
        fileHandler.setBucketName(bucket);
        fileHandler.init();

        for (String officeName: officeMap.keySet()) {
            MongoInfo mongoInfo = beiJingMap.get(officeName);
            if(mongoInfo != null) {
                MongoInfo theInfo = officeMap.get(officeName);
                if(theInfo.md5 != null && !theInfo.md5.equals(mongoInfo.md5) && !theInfo.getPathList.isEmpty()){
                    String filePath = theInfo.getPathList.get(0);
                    if(mongoInfo.pathList != null) {
                        for (String upLoad : mongoInfo.pathList) {
                            FileAdapter.PathEx path = new FileAdapter.PathEx(upLoad);
                            fileHandler.saveFile(FileUtils.openInputStream(new File(filePath)), path, FileAdapter.FileReplaceStrategy.REPLACE);
                            LoggerEx.info(TAG, "已部署" + officeName +"到北京环境");
                        }
                    }
                }
            }
        }
    }





    public static void download(String host, String bucket, String path) throws DBException, IOException {
        download(host, bucket, path, null);

    }

    public static void download(String host, String bucket, String loacalPath, Map<String, MongoInfo> map) throws DBException, IOException {
        MongoHelper helper = new MongoHelper();

        //helper.setHost("mongodb://192.168.3.184:7900");
        helper.setHost(host);
        helper.setDbName("gridfiles");
        helper.init();

        GridFSFileHandler fileHandler = new GridFSFileHandler();
        fileHandler.setResourceHelper(helper);
        //这是什么
        fileHandler.setBucketName(bucket);
        fileHandler.init();

//		File directory = new File("/home/aplomb/dev/github/PKUserService/deploy");
        //       File directory = new File("C:/Users/Administrator/Desktop/tc");
        File directory = new File(loacalPath);

        List<FileAdapter.FileEntity> files = fileHandler.getFilesInDirectory(new FileAdapter.PathEx("/"), null, true);
        for (FileAdapter.FileEntity entity : files) {
            System.out.println("entity" + entity.getAbsolutePath());
            FileAdapter.PathEx path = new FileAdapter.PathEx(entity.getAbsolutePath());
            File file = new File(directory.getAbsoluteFile() + "/" + entity.getAbsolutePath());
            OutputStream os = FileUtils.openOutputStream(file);
            MD5OutputStream mos = new MD5OutputStream(os);
            fileHandler.readFile(path, mos);
            String md5 = mos.getHashString();
            String AbsolutePath = entity.getAbsolutePath();
            String[] zipName;
            if (AbsolutePath.startsWith("/")) {
                String endOnlinePath = AbsolutePath.substring(1);
                zipName = endOnlinePath.split("/");
            }else {
                zipName = AbsolutePath.split("/");
            }
            MongoInfo mongoInfo = map.get(zipName);
            if (mongoInfo == null) {
                mongoInfo = new MongoInfo();
                mongoInfo.md5 = md5;
                mongoInfo.getPathList =  new ArrayList();
                mongoInfo.getPathList.add(file.getPath());
            }
            if (mongoInfo.pathList == null) {
                mongoInfo.pathList = new ArrayList<>();
                mongoInfo.pathList.add(AbsolutePath);
            }else {
                mongoInfo.pathList.add(AbsolutePath);
            }

            map.put(zipName[2],mongoInfo);
        }
    }
}
