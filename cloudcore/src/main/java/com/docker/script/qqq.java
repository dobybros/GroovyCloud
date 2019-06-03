package com.docker.script;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lick on 2019/1/8.
 * Description：
 */
public class qqq {
    public static void main(String[] args) {
        File zipFile = new File("C:\\Users\\lulia\\work\\work_new\\IMServers\\IMRobotService\\build\\deploy\\scripts\\rybot\\robot_v1\\groovy.zip");
        ZipFile zFile = null;
        try {
            zFile = new ZipFile(zipFile);
            File destDir = new File("C:\\Users\\lulia\\work\\work_new\\test\\");
            if (destDir.isDirectory() && !destDir.exists()) {
                destDir.mkdir();
            }
            if (zFile.isEncrypted()) {
//                zFile.setPassword(passwd.toCharArray());
            }
            zFile.extractAll("C:\\Users\\lulia\\work\\work_new\\test\\");

            List<FileHeader> headerList = zFile.getFileHeaders();
            List<File> extractedFileList = new ArrayList<File>();
            for (FileHeader fileHeader : headerList) {
                if (!fileHeader.isDirectory()) {
                    extractedFileList.add(new File(destDir, fileHeader.getFileName()));
                }
            }
            File[] extractedFiles = new File[extractedFileList.size()];
            extractedFileList.toArray(extractedFiles);
        } catch (net.lingala.zip4j.exception.ZipException e) {
            e.printStackTrace();
//            LoggerEx.error(TAG, "password is error,destFile:" + dir);
        }
    }
}
