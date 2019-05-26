package sdockerboot.boot.utils;

import com.docker.file.adapters.GridFSFileHandler;
import com.docker.storage.DBException;
import com.docker.storage.mongodb.MongoHelper;
import org.apache.commons.io.FileUtils;
import script.file.FileAdapter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class UploadGroovyZip {
    /*
     *  host ： mongodb 地址
     *  bucket ： mongodb 表地址
     *  dir ： 本机server路径
     *  注意：开发环境 bucket 为 imfs 测试环bucket 为 fs
     * */
    public void main(String[] args) throws IOException, DBException {
        upload("mongodb://192.168.1.170:7900", "imfs" , "/Users/aplomb/dev/ao/deploy" );
    }


    public void upload(String host, String bucket, String dir) throws DBException, IOException {
        MongoHelper helper = new MongoHelper();
        helper.setHost(host);
        helper.setDbName("gridfiles");
//		helper.setUsername("socialshopsim");
//		helper.setPassword("eDANviLHQtjwmFlywyKu");
        helper.init();
//		helper.setUsername();

        GridFSFileHandler fileHandler = new GridFSFileHandler();
        fileHandler.setResourceHelper(helper);
        fileHandler.setBucketName(bucket);
        fileHandler.init();

//		File directory = new File("/home/aplomb/dev/github/PKUserService/deploy");
        File directory = new File(dir);
        Collection<File> files = FileUtils.listFiles(directory, new String[]{"zip"}, true);
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

        /*保存文件*/
//		ByteArrayInputStream bais = new ByteArrayInputStream("hello gridfs".getBytes());
//		PathEx path = new PathEx("/files/hello/3.txt");
//		fileHandler.saveFile(bais, path, FileReplaceStrategy.REPLACE);
//
//		List<FileEntity> files = fileHandler.getFilesInDirectory(new PathEx("/"), new String[]{"txt"}, true);
//		System.out.println(Arrays.toString(files.toArray()));

        /*取目录下文件list*/
//		PathEx path = new PathEx("D:\\data\\files\\2015\\3\\test.txt" , null);
//		System.out.println(fileHandler.getFilesInDirectory(path));

        /*读取文件*/
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		PathEx path = new PathEx("D:\\data\\files\\2015\\3\\test.txt" , null);
//		fileHandler.readFile(path, baos);
//		baos.toString();

        /*删除文件*/
//		PathEx path = new PathEx("\\2015\\3\\test.txt" , null);
//		fileHandler.deleteFile(path);

        /*取上次修改时间（上传时间）*/
//		PathEx path = new PathEx("D:\\data\\files\\2015\\3\\test.txt" , null);
//		fileHandler.getLastModificationTime(path);

        /*关闭连接*/
//		fileHandler.destory();
    }
}
