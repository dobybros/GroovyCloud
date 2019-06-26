package com.docker.utils;

import chat.errors.CoreException;
import chat.logs.LoggerEx;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.*;
import java.lang.Character.UnicodeBlock;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;


public class CommonUtils {
	private static final String TAG = CommonUtils.class.getSimpleName();

	public static final String RESOURCES_ROOT = "resources/";
	public static final String RESOURCES_TEMP = "temp/";

	public static final String PROPERTY_PATH = "props"; 
	public static Properties loadProperty(String fileName) throws CoreException {
		ClassPathResource resource = new ClassPathResource(PROPERTY_PATH + "/" + fileName);
		Properties props  = null;
		try {
			props  = PropertiesLoaderUtils.loadProperties(resource);
			return props;
		} catch (IOException e) {
			throw new CoreException(e.getMessage());
		}
	}
	
	static SimpleDateFormat gmtFormatter = new SimpleDateFormat("yyyy/MM/dd"); 
	static {
		gmtFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
//	public static String getDocumentPath(String resourceId) throws IOException {
//		if(resourceId != null && ObjectId.isValid(resourceId)) {
//			StringBuilder builder = new StringBuilder(FileAdapter.DOC_ROOT_PATH);
//			ObjectId oid = new ObjectId(resourceId);
//			String timePath = null;
//			//XXX  多线程环境下，日期formate是非线程安全的，所以在此处加锁，以后需要找更好的解决方案
//			synchronized (gmtFormatter) {
//				timePath = gmtFormatter.format(oid.getDate());
//			}
//			builder.append(timePath).append("/").append(resourceId);
//			return builder.toString();
//		} else {
//			throw new IOException("Invalid resourceId " + resourceId);
//		}
//	}
	
	public static String getDocumentPath(String prefix, String resourceId) throws IOException {
		return getDocumentPath(prefix, resourceId, null);
	}

	public static String getDocumentPath(String prefix, String resourceId, String fileName) throws IOException {
        if(resourceId != null && ObjectId.isValid(resourceId)) {
            StringBuilder builder = new StringBuilder(prefix);
            ObjectId oid = new ObjectId(resourceId);
            String timePath = null;
            //XXX format is not thread safe, so sync it. But need better performance solution.
            synchronized (gmtFormatter) {
                timePath = gmtFormatter.format(oid.getDate());
            }
            builder.append(timePath).append("/").append(resourceId);
            if(fileName != null)
                builder.append("/").append(fileName);
            return builder.toString();
        } else {
            throw new IOException("Invalid resourceId " + resourceId);
        }
    }
	
	public static String toString(String[] strs) {
		if(strs == null)
			return null;
		if(strs.length == 0)
			return "";
		StringBuffer buffer = new StringBuffer();
		for(String str : strs) {
			buffer.append(str).append(",");
		}
		return buffer.substring(0, buffer.length() - 1);
	}
	public static String[] fromString(String str) {
		if(str == null)
			return null;
		return str.split(",");
	}
	public static String toString(Collection<String> strs) {
		if(strs == null || strs.size() == 0)
			return "";
		StringBuffer buffer = new StringBuffer();
		for(String str : strs) {
			buffer.append(str).append(",");
		}
		return buffer.substring(0, buffer.length() - 1);
	}
	
	public static String getRuntimePath() {
		return System.getProperty("user.dir");
	}
	
	public static String getUserLanguage() {
		return System.getProperty("user.language");
	}
	
	/*
	 * get the local host name
	 */
	public static String getLocalHostName(){
		try{
			InetAddress addr=InetAddress.getLocalHost();
			return addr.getHostName();
		}catch(Exception e){
			return "";
		}
	}
	
	public static boolean isSortString(String str) {
		if(str != null && str.length() > 2) {
			char sortHead = str.charAt(0);
			char seperator = str.charAt(1);
			char head = str.charAt(2);
			if(seperator == '^') {
				if(sortHead == head || sortHead - head == 32 || isChinese(head)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String cleanUpSortString(String str) {
		if(isSortString(str)) {
			return str.substring(2);
		}
		return str;
	}
	
	private static Set<UnicodeBlock> chineseUnicodeBlocks = new HashSet<UnicodeBlock>() {
		private static final long serialVersionUID = -4141782340333688579L;
	{
	    add(UnicodeBlock.CJK_COMPATIBILITY);
	    add(UnicodeBlock.CJK_COMPATIBILITY_FORMS);
	    add(UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
	    add(UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT);
	    add(UnicodeBlock.CJK_RADICALS_SUPPLEMENT);
	    add(UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
	    add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
	    add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
	    add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B);
	    add(UnicodeBlock.KANGXI_RADICALS);
	    add(UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS);
	}};
	
	public static boolean isChinese(char c) {
		return chineseUnicodeBlocks.contains(UnicodeBlock.of(c));
	}
	
	public static String getAttachmentRelativePath(String path, String resourceId) {
		String attachmentPath = path;
		int pos = attachmentPath.indexOf(resourceId);
		String relativePath = null;
		if(pos != -1) {
			relativePath = attachmentPath.substring(pos);
		} else {
			relativePath = attachmentPath;
		}
		return FilenameUtils.normalize(relativePath, true);
	}
	
	public static void copyStream(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[8192]; // Adjust if you want
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}

	public static boolean isDatabaseId(String userId) {
		if(userId == null)
			return false;
		return true;
	}
	
	public static boolean isDatabaseId(String[] ids) {
	    if (ids != null) {
	        boolean valid = true;
	        for (String id : ids) {
	            if (!isDatabaseId(id)) {
	                valid = false;
	                break;
	            }
	        }
	        return valid;
	    }
	    return false;
	}

	public static String[] joinStringArray(String[] array, String[] t) {
		if(array == null && t == null)
			return new String[0];
		else if (array == null && t != null)
			return t;
		else if (array != null && t == null)
			return array;
		String[] newArray = new String[array.length + t.length];
		System.arraycopy(array, 0, newArray, 0, array.length);
		System.arraycopy(t, 0, newArray, array.length, t.length);
		return newArray;
	}
	
	public static Collection<String> joinStringArray(Collection<String> array, String t) {
		if(array == null)
			return null;
		if(t == null)
			return array;
		Collection<String> strs = new ArrayList<>();
		strs.addAll(array);
		strs.add(t);
		return strs;
	}
	public static List<String> joinStringList(String[] array, String t) {
		if(array == null)
			return null;
		List<String> strs = new ArrayList<>();
		strs.addAll(Arrays.asList(array));
		strs.add(t);
		return strs;
	}
	public static String[] joinStringArray(String[] array, String t) {
	    if(t == null)
	        return array;
		if(array == null && t != null)
			return new String[] {t};
		String[] newArray = new String[array.length + 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[newArray.length - 1] = t;
		return newArray;
	}
	
	public static String formatSpringSensitiveString(String str) {
		if(str == null)
			return null;
		return str.replaceAll("[/\\\\;:]", "_").replace("<", "(").replace(">", ")");
	}
	
	private static String specialCharacters = "^()./+[]-"; 
	private static String s3specialCharacters = "~"; 
	private static String fileNameSpecialCharacters = "*:?<>|^()./+[]-\""; 
	public static String formatForRegularExpression(String str) {
		if(str == null)
			return "";
		str = str.replace("\\", "\\\\");
		for(int i = 0; i < specialCharacters.length(); i++) {
			String c = specialCharacters.substring(i, i + 1);
			str = str.replace(c, "\\" + c);
		}
		return str;
	}
	public static String formatAsRegularExpressionForName(String str) {
		if(str == null)
			return "";
		str = str.replace("\\", "\\\\");
		for(int i = 0; i < fileNameSpecialCharacters.length(); i++) {
			String c = fileNameSpecialCharacters.substring(i, i + 1);
			str = str.replace(c, "_");
		}
		return str;
	}
	public static String formatS3Name(String str) {
		if(str == null)
			return "";
		for(int i = 0; i < s3specialCharacters.length(); i++) {
			String c = s3specialCharacters.substring(i, i + 1);
			str = str.replace(c, "_");
		}
		return str;
	}
	
	public static String formatTxt2Html(String str) {
		if (str == null || str.trim().length() == 0) return str;
		return str.replaceAll("(\r\n|\n\r|\r|\n)", "<br/>").replaceAll("(\t)", "&nbsp; &nbsp; ").replaceAll("   ", "&nbsp; &nbsp;").replaceAll("  ", "&nbsp; ");
	}
	
	public static String formatSecureString(String str) {
		if (str == null || str.trim().length() == 0) return str;
		return str.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	public static void sortNumberString(String[] keys) {
		Arrays.sort(keys, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int left = 0, right = 0;
				
				try {
					left = Integer.parseInt(o1);
					right = Integer.parseInt(o2);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return 0;
				}
				
				if (left < right)
					return -1;
				else 
					return 1;
			}
		});
		
	}

	public static void destroyProcess(Process process) {
		if(process == null)
			return;
		process.destroy();
		while(true) {
			try {
				process.exitValue();
				process = null;
				break;
			} catch(IllegalThreadStateException e) {
				try {
					Thread.sleep(200L);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				LoggerEx.debug(TAG, "Waiting for process really destroyed. " + process);
			}
		}		
	}
	
	public static boolean isProcessExist(String name) throws IOException {
		String[] command = new String[]{"tasklist", "/fo", "table", "/nh"};
		ProcessBuilder pb = new ProcessBuilder(command);
		Process tasks = pb.start();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(tasks.getInputStream()));
		String info = null;
		while ((info = reader.readLine()) != null) {
			String pname = info.split(" ")[0];
			
			if (pname.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args) throws IOException {
//		System.out.println(getLocalHostIP());
//		String str = generateSortString("大家好Zab^BaAAAA");
//		System.out.println(getLocalHostIP());
//		System.out.println(cleanUpSortString(str));
		
//		try {
//			System.out.println(ArrayUtils.toString(PinyinHelper.toHanyuPinyinStringArray('鑫', pinYinFormat)));
//		} catch (BadHanyuPinyinOutputFormatCombination e) {
//			e.printStackTrace();
//		}
	}
	
	public static String escapeAngleBracket(String asString) {
		return asString == null ? null : asString.replaceAll("<", "&lt;");
				//replaceAll(">", "&gt;");
	}

	public static String filterCarriageAndWrapChar(String wh) {
		return wh == null ? null : wh.replaceAll("(\r\n|\n\r|\r|\n)", "");
	}

	/**
	 * 该方法使用了InputStream的skip方法，效率不高，尽量不要在生产环境使用
	 * @param input
	 * @param output
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public static void copyStream(InputStream input, OutputStream output, Integer offset, Integer length)
			throws IOException {
		final int BUFFERSIZE = 8192;
		byte[] buffer = null; // Adjust if you want
		int bytesRead;
		int lengthTemp = length;
		if(offset != null && offset > 0) {
			int actualSkipped = (int) input.skip(offset);
			if(actualSkipped != offset)
				throw new IOException();
		}
		if(length < BUFFERSIZE)
			buffer = new byte[length];
		else
			buffer = new byte[BUFFERSIZE];
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
			lengthTemp -= bytesRead;
			if(lengthTemp == 0)
				break;
			else if(lengthTemp < 0)
				throw new IOException("Unexpected lengthTemp " + lengthTemp);
			else if(lengthTemp < buffer.length)
				buffer = new byte[lengthTemp];
		}
	}
}