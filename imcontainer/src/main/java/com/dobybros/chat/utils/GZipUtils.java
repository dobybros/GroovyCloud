package com.dobybros.chat.utils;

import chat.logs.LoggerEx;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** 
 * GZIP工具 
 *  
 * @author <a href="mailto:zlex.dongliang@gmail.com">梁栋</a> 
 * @since 1.0 
 */  
public abstract class GZipUtils {  
  
    public static final int BUFFER = 1024;  
    public static final String EXT = ".gz";  
  
    /** 
     * 数据压缩 
     *  
     * @param data 
     * @return 
     * @throws Exception 
     */  
    public static byte[] compress(byte[] data) throws IOException {  
        ByteArrayInputStream bais = new ByteArrayInputStream(data);  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
  
        // 压缩  
        compress(bais, baos);  
  
        byte[] output = baos.toByteArray();  
  
        baos.flush();  
        baos.close();  
  
        bais.close();  
  
        return output;  
    }  
  
    /** 
     * 文件压缩 
     *  
     * @param file 
     * @throws Exception 
     */  
    public static void compress(File file) throws IOException {  
        compress(file, true);  
    }  
  
    /** 
     * 文件压缩 
     *  
     * @param file 
     * @param delete 
     *            是否删除原始文件 
     * @throws Exception 
     */  
    public static void compress(File file, boolean delete) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);FileOutputStream fos = new FileOutputStream(file.getPath() + EXT)) {
            compress(fis, fos);

            fis.close();
            fos.flush();
            fos.close();

        } catch (Throwable t) {
            LoggerEx.error("GZip utils", "compress file " + file.getAbsolutePath() + " error, eMsg: " + t.getMessage());
        }

        if (delete) {
            file.delete();
        }
    }  
  
    /** 
     * 数据压缩 
     *  
     * @param is 
     * @param os 
     * @throws IOException 
     * @throws Exception 
     */  
    public static void compress(InputStream is, OutputStream os) throws IOException  {  
        GZIPOutputStream gos = new GZIPOutputStream(os);

        try {
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = is.read(data, 0, BUFFER)) != -1) {
                gos.write(data, 0, count);
            }
            gos.finish();

            gos.flush();
        } finally {
            gos.close();
        }
    }
  
    /** 
     * 文件压缩 
     *  
     * @param path 
     * @throws Exception 
     */  
    public static void compress(String path) throws IOException {  
        compress(path, true);  
    }  
  
    /** 
     * 文件压缩 
     *  
     * @param path 
     * @param delete 
     *            是否删除原始文件 
     * @throws Exception 
     */  
    public static void compress(String path, boolean delete) throws IOException {  
        File file = new File(path);  
        compress(file, delete);  
    }  
  
    /** 
     * 数据解压缩 
     *  
     * @param data 
     * @return 
     * @throws IOException 
     * @throws Exception 
     */  
    public static byte[] decompress(byte[] data) throws IOException  {  
        ByteArrayInputStream bais = new ByteArrayInputStream(data);  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
  
        // 解压缩  
        try {
            decompress(bais, baos);
            baos.flush();
            data = baos.toByteArray();
            return data;
        } finally {
            baos.close();
            bais.close();
        }
    }
  
    /** 
     * 文件解压缩 
     *  
     * @param file 
     * @throws Exception 
     */  
    public static void decompress(File file) throws IOException {  
        decompress(file, true);  
    }  
  
    /** 
     * 文件解压缩 
     *  
     * @param file 
     * @param delete 
     *            是否删除原始文件 
     * @throws Exception 
     */  
    public static void decompress(File file, boolean delete) throws IOException {

        try (FileInputStream fis = new FileInputStream(file);FileOutputStream fos = new FileOutputStream(file.getPath().replace(EXT,
                ""))) {
            decompress(fis, fos);
            fis.close();
            fos.flush();
            fos.close();
        } catch (Throwable t) {
            LoggerEx.error("GZip utils", "compress file " + file.getAbsolutePath() + " error, eMsg: " + t.getMessage());
        }
        if (delete) {
            file.delete();
        }
    }
  
    /** 
     * 数据解压缩 
     *  
     * @param is 
     * @param os 
     * @throws IOException 
     * @throws Exception 
     */  
    public static void decompress(InputStream is, OutputStream os) throws IOException {
        GZIPInputStream gis = new GZIPInputStream(is);
        try {
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = gis.read(data, 0, BUFFER)) != -1) {
                os.write(data, 0, count);
            }
        } finally {
            gis.close();
        }
    }
  
    /** 
     * 文件解压缩 
     *  
     * @param path 
     * @throws Exception 
     */  
    public static void decompress(String path) throws IOException {  
        decompress(path, true);  
    }  
  
    /** 
     * 文件解压缩 
     *  
     * @param path 
     * @param delete 
     *            是否删除原始文件 
     * @throws Exception 
     */  
    public static void decompress(String path, boolean delete) throws IOException {  
        File file = new File(path);  
        decompress(file, delete);  
    }  
  
}  