package script.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import chat.encrypter.EncryptService;
import chat.logs.LoggerEx;


/**
 * protocol://host:port/path?query
 * 
 * hdfs://namenode:9000/user/mac/a.txt
 * 
 * file://192.168.1.200:9000/user/mac/a.txt
 * 
 * @author aplomb
 *
 */
public abstract class FileAdapter {
	private EncryptService encryptService;
	
	public enum READWRITE
	{
	    READ, WRITE;
	}
	protected void copy(PathEx path, InputStream is, OutputStream os, READWRITE readWrite) throws IOException {
		boolean encrypted = false;
		EncryptService encrypterListener = null;
		if (encrypterListener == null) {
			encrypterListener = encryptService;
		}
		if(encrypterListener != null) {
			switch(readWrite) {
			case READ:
				try {
					encrypterListener.decrypt(is, os);
					encrypted = true;
				} catch (Throwable e) {
					LoggerEx.error("D", "rollback...");
				}
				break;
			case WRITE:
				encrypterListener.encrypt(is, os);
				encrypted = true;
				break;
			}
		}
		if(!encrypted)
			IOUtils.copyLarge(is, os);
	}
	
	
	public enum FileReplaceStrategy
	{
	    REPLACE, DONTREPLACE;
	}
	
	public interface SaveFileCachedListener {
		/**
		 * File saved to local
		 * 
		 * @param entity
		 * @throws IOException
		 */
		public void fileCached(FileEntity entity) throws IOException;
		/**
		 * File saved to S3 or other remote system.
		 * 
		 * @param entity
		 * @throws IOException
		 */
		public void fileSaved(FileEntity entity) throws IOException;
		
		/**
		 * File saved to S3 or other remote system failed.
		 * 
		 * @param entity
		 * @throws IOException
		 */
		public void saveFailed(FileEntity entity, Throwable e) throws IOException;
	}
	
	public static class MetadataEx {
		private String type;
		private String targetId;
		
		public static final String FIELD_TYPE = "type";
		public static final String FIELD_TARGETID = "tid";
		
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String etTargetId() {
			return targetId;
		}
		public void setTargetId(String targetId) {
			this.targetId = targetId;
		}
		
		public String toString() {
			return "type " + type + "; targetId " + targetId;
		}
		
		public Map<String, String> toMap() {
			Map<String, String> map = new HashMap<>();
			if(targetId != null)
				map.put(FIELD_TARGETID, targetId);
			if(type != null)
				map.put(FIELD_TYPE, type);
			return map;
		}
	}
	
	public static class PathEx { 
	    public static final String ACU_PATH = "path"; 
        public static final String ACU_PATH_HASH_KEY = "hashkey"; 
        public static final String ACU_PATH_ENCRYPTTYPE = "et"; 
        public static final String ACU_PATH_TYPE = "type"; 
		private String path;
		private String hashKey;
		private MetadataEx metadata;

		@Override
		public String toString() {
			return path + ", hashKey " + hashKey + ", metadata " + metadata;
		}
		public PathEx(String path) {
			this.path = path;
		}
		public PathEx(String path, String hashKey, MetadataEx metadata) {
			this(path);
			this.hashKey = hashKey;
			this.setMetadata(metadata);
		}
		
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public String getHashKey() {
			return hashKey;
		}
		public void setHashKey(String hashKey) {
			this.hashKey = hashKey;
		}
//        @Override
//        public DBObject toDocument() {
//            return new CleanBasicDBObject()
//            .append(ACU_PATH, path)
//            .append(ACU_PATH_TYPE, type)
//            .append(ACU_PATH_HASH_KEY, hashKey);
//        }
//        
//        @Override
//        public void fromDocument(DBObject dbo) {
//            CleanBasicDBObject cdbo = new CleanBasicDBObject(dbo);
//            this.path = cdbo.getString(ACU_PATH);
//            this.hashKey = cdbo.getString(ACU_PATH_HASH_KEY);
//            this.type = cdbo.getString(ACU_PATH_TYPE);
//        }
		public MetadataEx getMetadata() {
			return metadata;
		}
		public void setMetadata(MetadataEx metadata) {
			this.metadata = metadata;
		}
	}

	public static final String DOC_ROOT_PATH = "resources/";
    public static final String DOC_STICKER_SUIT_ROOT_PATH = "stickersuit/";
	
    public abstract FileEntity saveFile(InputStream is, PathEx path, FileReplaceStrategy strategy,
            SaveFileCachedListener listener) throws IOException;

    public abstract FileEntity saveFile(InputStream is, PathEx path, FileReplaceStrategy strategy)
            throws IOException;
	
	public abstract FileEntity saveFile(File file, PathEx path, FileReplaceStrategy strategy,
			SaveFileCachedListener listener, boolean isNeedMd5) throws IOException;

	public abstract FileEntity saveFile(InputStream is, long length, PathEx path,
			FileReplaceStrategy strategy, SaveFileCachedListener listener) throws IOException;
	
	public abstract boolean deleteFile(PathEx path) throws IOException;

	public abstract boolean moveFile(PathEx sourcePath, PathEx destPath) throws IOException;

	public abstract boolean readFile(PathEx path, OutputStream os) throws IOException;
	
	public abstract boolean readFile(PathEx path, OutputStream os, Integer offset, Integer length) throws IOException;
	
	public abstract boolean isFileExist(PathEx path) throws IOException;
	
	public abstract Long getLastModificationTime(PathEx path) throws IOException;
	
	
	public abstract FileEntity saveDirectory(PathEx path) throws IOException;

	public abstract boolean deleteDirectory(PathEx path) throws IOException;
	
	public abstract boolean moveDirectory(PathEx sourcePath, PathEx destPath) throws IOException;
	
	public abstract boolean isDirectoryExist(PathEx path) throws IOException;
	
	public abstract FileEntity getFileEntity(PathEx path) throws IOException;
	
	/**
	 * This is used for generate the download url for directly downloading against file servers. 
	 * 
	 * @param path
	 * @param fileName
	 * @param contentType
	 * @param useragent
	 * @return
	 * @throws IOException
	 */
	public abstract String generateDownloadUrl(PathEx path, String fileName, String contentType,
			String useragent) throws IOException;
	
	/**
	 * Check the file adapter support download url or not. 
	 * 
	 * @return
	 */
	public abstract boolean isSupportDownloadUrl();
	
	/**
	 * The return string if end with /, then it's a directory, otherwise is a file.  
	 * 
	 *  For example, 
	 *  user/mac/afile.txt
	 *  user/mac/adirectory/
	 *  
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public abstract List<FileEntity> getFilesInDirectory(PathEx path, String[] extensions, boolean recursive) throws IOException;
	
	public EncryptService getEncryptService() {
		return encryptService;
	}

	public void setEncryptService(EncryptService encryptService) {
		this.encryptService = encryptService;
	}

	public static class FileEntity {
		public static final int TYPE_FILE = 1;
		public static final int TYPE_DIRECTORY = 2;
		
		
		private long length;
		private long lastModificationTime;
		private int type;
		private String absolutePath;
		private String md5;
		public void setLength(long length) {
			this.length = length;
		}
		public long getLength() {
			return length;
		}
		public void setLastModificationTime(long lastModificationTime) {
			this.lastModificationTime = lastModificationTime;
		}
		public long getLastModificationTime() {
			return lastModificationTime;
		}
		public void setType(int type) {
			this.type = type;
		}
		public int getType() {
			return type;
		}
		public void setAbsolutePath(String absolutePath) {
			this.absolutePath = absolutePath;
		}
		public String getAbsolutePath() {
			return absolutePath;
		}
		
		public void fromFile(File file) {
			if(file.exists()) {
				if(file.isFile()) {
					type = TYPE_FILE;
					length = file.length();
				} else {
					type = TYPE_DIRECTORY;
				}
				absolutePath = FilenameUtils.separatorsToUnix(file.getAbsolutePath());
				lastModificationTime = file.lastModified();
			} 
		}
		public String getMd5() {
			return md5;
		}
		public void setMd5(String md5) {
			this.md5 = md5;
		}
	}

	public void close(){}
}
