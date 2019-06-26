package com.dobybros.chat.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 计算大文件MD5 
 *  David  2012-10-12
 */
public class GetBigFileMD5 {
   
    static MessageDigest MD5 = null;


    static {
        try {
        MD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ne) {
        ne.printStackTrace();
        }
    }

    public static String getMD5(File file) {
    	return getMD5(file, null);
    }

    /**
     * 对一个文件获取md5值
     * @return md5串
     */
    public static String getMD5(File file, String tail) {
        FileInputStream fileInputStream = null;
        try {
        fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
            MD5.update(buffer, 0, length);
            }
            if(tail != null) {
            	byte[] tailBytes = tail.getBytes();
            	MD5.update(tailBytes, 0, tailBytes.length);
            }

            return new String(Hex.encodeHex(MD5.digest()));
        } catch (FileNotFoundException e) {
        e.printStackTrace();
            return null;
        } catch (IOException e) {
        e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null)
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    
    public static String a(File f) {
    	return getMD5(f);
    }

    /**
     * 求一个字符串的md5值
     * @param target 字符串
     * @return md5 value
     */
    public static String MD5(String target) {
        return DigestUtils.md5Hex(target);
    }


    public static void main(String[] args){
    
    long beginTime =System.currentTimeMillis();
      File fileZIP = new File("D:/TEST/IMAGE2.zip");
      String md5=getMD5(fileZIP);
      long endTime =System.currentTimeMillis();
     System.out.println("MD5:"+md5+"\n time:"+((endTime-beginTime)/1000)+"s");
    }
}