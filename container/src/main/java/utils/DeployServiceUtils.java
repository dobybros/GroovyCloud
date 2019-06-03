package utils;

import com.docker.file.adapters.GridFSFileHandler;
import com.docker.storage.mongodb.MongoHelper;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.StringUtils;
import script.file.FileAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DeployServiceUtils {
    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new PosixParser();
        Options opt = new Options();
        opt.addOption("h", "help", false, "help")
                .addOption("p", true, "Service path")
//			.addOption("a",true, "async servlet map")
                .addOption("l", true, "Dependency library path")
                .addOption("x", true, "Prefix name")
                .addOption("d", true, "Docker name")
                .addOption("s", true, "Service name")
                .addOption("f", true, "Mongodb GridFS host, or other dfs host")
                .addOption("v", true, "Version")
                .addOption("b", true, "bucket");

        org.apache.commons.cli.CommandLine line = parser.parse(opt, args);
        System.out.println("commandLine " + Arrays.toString(args));
        List<String> argList = line.getArgList();
        if (line.hasOption('h') || line.hasOption("help")) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }
        String prefix = null;
        String servicePath = null;
        String dockerName = null;
        String serviceName = null;
        String gridfsHost = null;
        String versionStr = null;
        String libPath = null;
        String bucket = "imfs";

        if (line.hasOption('x')) {
            prefix = line.getOptionValue('x');
        }
        if (line.hasOption('p')) {
            servicePath = line.getOptionValue('p');
        } else {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }
        if (line.hasOption('l')) {
            libPath = line.getOptionValue('l');
        }
        if (line.hasOption('d')) {
            dockerName = line.getOptionValue('d');
        } else {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }
        if (line.hasOption('s')) {
            serviceName = line.getOptionValue('s');
        } else {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }
        if (line.hasOption('f')) {
            gridfsHost = line.getOptionValue('f');
        } else {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }
        Integer version = null;
        if (line.hasOption('v')) {
            versionStr = line.getOptionValue('v');
            try {
                version = Integer.valueOf(versionStr);
            } catch (Exception e) {
            }
        }
        if (line.hasOption('b')) {
            bucket = line.getOptionValue('b');
        }

        deploy(prefix, servicePath, dockerName, serviceName, gridfsHost, version, libPath, bucket);
    }

    public static void deploy(String prefix, String servicePath, String dockerName, String serviceName, String gridfsHost, Integer version) throws Exception {
        deploy(prefix, servicePath, dockerName, serviceName, gridfsHost, version, null, null);
    }

    public static void deploy(String prefix, String servicePath, String dockerName, String serviceName, String gridfsHost, Integer version, String libPath, String bucket) throws Exception {
        File deploy = new File(servicePath + "/build/deploy/course");
        File root = new File(servicePath + "/build/deploy");
        FileUtils.deleteDirectory(root);

        Boolean needMergeProperties = false;
        File deployPropertiesFile = new File(deploy + "/config.properties");
        //copy libs
        if (libPath != null) {
            String[] libPaths = libPath.split(",");
            for (String libP : libPaths) {
                File libGroovyFile = new File(libP + "/src/main/groovy");
                File libPropertiesFile = new File(libP + "/src/main/groovy/config.properties");
                if (!needMergeProperties && deployPropertiesFile.exists() && deployPropertiesFile.isFile() && libPropertiesFile.exists() && libPropertiesFile.isFile()) {
                    needMergeProperties = true;
                }
                if (libGroovyFile.isDirectory() && libGroovyFile.exists()) {
                    FileUtils.copyDirectory(libGroovyFile, deploy);
                }
                File libResourceFile = new File(libP + "/src/main/resources");
                if (libResourceFile.exists() && libResourceFile.isDirectory()) {
                    FileUtils.copyDirectory(libResourceFile, deploy);
                }
            }

        }

        //copy source
        File groovyFile = new File(servicePath + "/src/main/groovy");
        File groovyPropertiesFile = new File(servicePath + "/src/main/groovy/config.properties");
        if (!needMergeProperties && deployPropertiesFile.exists() && deployPropertiesFile.isFile() && groovyPropertiesFile.exists() && groovyPropertiesFile.isFile()) {
            needMergeProperties = true;
        }
        if (groovyFile.isDirectory() && groovyFile.exists()) {
            FileUtils.copyDirectory(groovyFile, deploy);
        }
        File resourceFile = new File(servicePath + "/src/main/resources");
        if (resourceFile.exists() && resourceFile.isDirectory()) {
            FileUtils.copyDirectory(resourceFile, deploy);
        }

        //合并properties
        if (needMergeProperties) {
            Properties mainProperties = new Properties();
            FileInputStream fin = new FileInputStream(deployPropertiesFile);
            mainProperties.load(fin);

            String[] libPaths = libPath.split(",");
            for (String libP : libPaths) {
                File libPropertiesFile = new File(libP + "/src/main/groovy/config.properties");
                if (libPropertiesFile.exists() && libPropertiesFile.isFile()) {
                    Properties libProperties = new Properties();
                    FileInputStream fin2 = new FileInputStream(libPropertiesFile);
                    libProperties.load(fin2);
                    Set<String> propertyNames = libProperties.stringPropertyNames();

                    FileWriter fout = new FileWriter(deployPropertiesFile,true);
                    fout.write("\r\n\r\n");
                    fout.write("#merge \r\n");
                    for (String key : propertyNames) {
                        String value = mainProperties.getProperty(key);
                        if (value == null) {
                            fout.write(key + "=" + libProperties.getProperty(key) + "\r\n");
                        }
                    }
                    fout.close();
                    fin2.close();
                    fin.close();
                }
            }
        }


        if (version != null) {
            serviceName = serviceName + "_v" + version;
        }
        doZip(new File(FilenameUtils.separatorsToUnix(root.getAbsolutePath()) + (prefix != null ? "/" + prefix : "") + "/" + dockerName + "/" + serviceName + "/1.zip"), deploy);
//        clean(deploy, ".zip");
        FileUtils.deleteQuietly(deploy);

        File[] toRemoveEmptyFolders = root.listFiles();
        for (File findEmptyFolder : toRemoveEmptyFolders) {
            if (getAllEmptyFoldersOfDir(findEmptyFolder)) {
                FileUtils.deleteDirectory(findEmptyFolder);
            }
        }
//        if(true)
//            return;

        MongoHelper helper = new MongoHelper();
        helper.setHost(gridfsHost);
        helper.setDbName("gridfiles");
//		helper.setUsername("socialshopsim");
//		helper.setPassword("eDANviLHQtjwmFlywyKu");
        helper.init();
//		helper.setUsername();

        if (StringUtils.isBlank(bucket)) {
            bucket = "imfs";
        }
        GridFSFileHandler fileHandler = new GridFSFileHandler();
        fileHandler.setResourceHelper(helper);
        fileHandler.setBucketName(bucket);
        fileHandler.init();

        File directory = new File(servicePath + "/build/deploy");
//        File directory = new File("/home/aplomb/dev/github/DiscoveryService/deploy");
        Collection<File> files = FileUtils.listFiles(directory, new String[]{"zip"}, true);
        if (files != null) {
            for (File file : files) {
                String filePath = FilenameUtils.separatorsToUnix(file.getAbsolutePath());
                String dirPath = FilenameUtils.separatorsToUnix(directory.getAbsolutePath());
                String thePath = filePath.substring(dirPath.length());
//				System.out.println("file " + thePath);

                FileAdapter.PathEx path = new FileAdapter.PathEx(thePath);
                fileHandler.saveFile(FileUtils.openInputStream(new File(filePath)), path, FileAdapter.FileReplaceStrategy.REPLACE);

                System.out.println("File " + thePath + " saved!");
            }
        }
    }


    static boolean getAllEmptyFoldersOfDir(File current) {
        if (current.isDirectory()) {
            File[] files = current.listFiles();
            if (files.length == 0) { //There is no file in this folder - safe to delete
                System.out.println("Safe to delete - empty folder: " + FilenameUtils.separatorsToUnix(current.getAbsolutePath()));
                return true;
            } else {
                int totalFolderCount = 0;
                int emptyFolderCount = 0;
                for (File f : files) {
                    if (f.isDirectory()) {
                        totalFolderCount++;
                        if (getAllEmptyFoldersOfDir(f)) { //safe to delete
                            emptyFolderCount++;
                        }
                    }

                }
                if (totalFolderCount == files.length && emptyFolderCount == totalFolderCount) { //only if all folders are safe to delete then this folder is also safe to delete
                    System.out.println("Safe to delete - all subfolders are empty: " + FilenameUtils.separatorsToUnix(current.getAbsolutePath()));
                    return true;
                }
            }
        }
        return false;
    }

    public static void clean(File folder, String endWith) {
        Collection<File> fList = FileUtils.listFiles(folder, null, true);
        for (File file : fList) {
            if (file.isFile() && !file.getName().endsWith(endWith)) {
//                file.delete();
                try {
                    FileUtils.forceDelete(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        FileUtils.listFiles(folder, new FileFileFilter() {
        }, new DirectoryFileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    Collection<File> hasFiles = FileUtils.listFiles(file, null, true);
                    if (hasFiles == null || hasFiles.isEmpty()) {
//                        file.delete();
                        try {
                            FileUtils.forceDelete(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });
    }

    //压缩文件夹内的文件
    public static void doZip(File zipFile, File zipDirectory) {//zipDirectoryPath:需要压缩的文件夹名
        File file;
        File zipDir;

        zipDir = zipDirectory;
        CRC32 crc = new CRC32();
        String str = "groovy.zip";
        crc.update(str.getBytes());
        long value = crc.getValue();
        zip(zipDir.getAbsolutePath(), zipFile.getAbsolutePath().split("1.zip")[0], true, String.valueOf(value));
    }

    public static String zip(String src, String dest, boolean isCreateDir, String passwd) {
        File srcFile = new File(src);
        dest = buildDestinationZipFilePath(srcFile, dest);
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);            // 压缩方式
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);    // 压缩级别
        if (!StringUtils.isEmpty(passwd)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);    // 加密方式
            parameters.setPassword(passwd.toCharArray());
        }
        try {
            ZipFile zipFile = new ZipFile(dest);
            File[] subFiles = srcFile.listFiles();
            ArrayList allFiles = new ArrayList();
            for (File file : subFiles) {
//                allFiles.add(file);
                if (file.isDirectory()) {
                    zipFile.addFolder(file, parameters);
                } else {
                    zipFile.addFile(file, parameters);
                }
            }
            return dest;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String buildDestinationZipFilePath(File srcFile, String destParam) {
        createDestDirectoryIfNecessary(destParam);    // 在指定路径不存在的情况下将其创建出来
        if (destParam.endsWith(File.separator)) {
//            String fileName = "";
//            if (srcFile.isDirectory()) {
//                fileName = srcFile.getName();
//            } else {
//                fileName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf("."));
//            }
            destParam += "groovy.zip";
        }
        return destParam;
    }

    private static void createDestDirectoryIfNecessary(String destParam) {
        File destDir = null;
        if (destParam.endsWith(File.separator)) {
            destDir = new File(destParam);
        } else {
            destDir = new File(destParam.substring(0, destParam.lastIndexOf(File.separator)));
        }
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }

    //由doZip调用,递归完成目录文件读取
    private static void handleDir(File root, File dir, ZipOutputStream zipOut, File zipFile) throws IOException {
        FileInputStream fileIn;
        File[] files;

        files = dir.listFiles();

        if (files.length == 0) {//如果目录为空,则单独创建之.
            //ZipEntry的isDirectory()方法中,目录以"/"结尾.
            zipOut.putNextEntry(new ZipEntry(dir.toString() + "/"));
            zipOut.closeEntry();
        } else {//如果目录不为空,则分别处理目录和文件.
            for (File fileName : files) {
                //System.out.println(fileName);
                int readedBytes;
                byte[] buf = new byte[64 * 1024];
                if (fileName.isDirectory()) {
                    handleDir(root, fileName, zipOut, zipFile);
                } else if (!fileName.getAbsolutePath().equals(zipFile.getAbsolutePath())) {
                    fileIn = new FileInputStream(fileName);
                    String zipPath = FilenameUtils.separatorsToUnix(fileName.getAbsolutePath()).substring(FilenameUtils.separatorsToUnix(root.getAbsolutePath()).length());
                    if (zipPath.startsWith("/")) {
                        zipPath = zipPath.substring(1);
                    }
                    zipOut.putNextEntry(new ZipEntry(zipPath));

                    while ((readedBytes = fileIn.read(buf)) > 0) {
                        zipOut.write(buf, 0, readedBytes);
                    }

                    zipOut.closeEntry();
                }
            }
        }
    }
}
