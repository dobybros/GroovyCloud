package com.docker.file.adapters;

import chat.utils.MD5InputStream;
import com.docker.storage.DBException;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.utils.CommonUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.exception.ExceptionUtils;
import script.file.FileAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
//import org.apache.hadoop.fs.FileStatus;
//import docker.db.MongoHelper;
//import docker.utils.MD5InputStream;

/**
 * @author Aplomb
 *
 */
public class GridFSFileHandler extends FileAdapter {
	private MongoHelper resourceHelper;
	private GridFS gridFs;
	private String bucketName;

	public GridFSFileHandler() {
	}

	@Override
	public void close() {
		if(resourceHelper != null) {
			resourceHelper.disconnect();
		}
	}

	public void init() {
		if (bucketName == null) 
	        gridFs = new GridFS(resourceHelper.getDbForGridFS());
	    else
	        gridFs = new GridFS(resourceHelper.getDbForGridFS(), bucketName);


//		List<FileEntity> files = null;
//		try {
//			ByteArrayInputStream bais = new ByteArrayInputStream("hello gridfs".getBytes());
//			PathEx path = new PathEx("/files/hello/1.txt");
//			saveFile(bais, path, FileReplaceStrategy.REPLACE);
//
//			files = getFilesInDirectory(new PathEx("/"), new String[]{"txt"}, true);
//			System.out.println(Arrays.toString(files.toArray()));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public static void main(String[] args) throws IOException, DBException {

		MongoHelper helper = new MongoHelper();
		helper.setHost("mongodb://localhost:7900");
		helper.setDbName("gridfiles");
//		helper.setUsername("socialshopsim");
//		helper.setPassword("eDANviLHQtjwmFlywyKu");
		helper.init();
//		helper.setUsername();

		GridFSFileHandler fileHandler = new GridFSFileHandler();
		fileHandler.setResourceHelper(helper);
		fileHandler.setBucketName("imfs");
		fileHandler.init();

//		File directory = new File("/home/aplomb/dev/github/PKUserService/deploy");
		File directory = new File("/home/aplomb/dev/github/DiscoveryService/deploy");
		Collection<File> files = FileUtils.listFiles(directory, new String[]{"zip"}, true);
		if(files != null) {
			for(File file : files) {
				String filePath = file.getAbsolutePath();
				String dirPath = directory.getAbsolutePath();
				String thePath = filePath.substring(dirPath.length());
//				System.out.println("file " + thePath);

				PathEx path = new PathEx(thePath);
				fileHandler.saveFile(FileUtils.openInputStream(new File(filePath)), path, FileReplaceStrategy.REPLACE);
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
	

	@Override
	public boolean deleteDirectory(PathEx path) throws IOException {
	    throw new NotImplementedException();
	}

	@Override
	public boolean deleteFile(PathEx path) throws IOException {
		gridFs.remove(new BasicDBObject("filename",path.getPath()));
		return true;
	}
	public boolean deleteFileRegix(BasicDBObject basicDBObject){
		gridFs.remove(basicDBObject);
		return true;
	}
	@Override
	public boolean isFileExist(PathEx path) throws IOException {
		GridFSDBFile inputFile = gridFs.findOne(new BasicDBObject("filename",
				path.getPath()));
		if(inputFile == null){
			return false;
		}
		else{
			return true;
		}
	}
	
   @Override
    public boolean isDirectoryExist(PathEx path) throws IOException {
	   //TODO 暂不需使用此方法
       throw new NotImplementedException();
    }

	@Override
	public boolean readFile(PathEx path, OutputStream os) throws IOException {
		try {
			GridFSDBFile inputFile = gridFs.findOne(new BasicDBObject("filename",
					path.getPath()));
			if(inputFile != null) {
				inputFile.writeTo(os);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("read file failed, " + ExceptionUtils.getFullStackTrace(e), e);
		}
		return true;
	}

	@Override
	public boolean readFile(PathEx path, OutputStream os, Integer offset, Integer length) throws IOException {
		try {
			GridFSDBFile inputFile = gridFs.findOne(new BasicDBObject("filename",
					path.getPath()));
			if(inputFile != null) {
				CommonUtils.copyStream(inputFile.getInputStream(), os, offset, length);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("read file failed, " + ExceptionUtils.getFullStackTrace(e), e);
		}
		return true;
	}
	
	@Override
	public boolean moveDirectory(PathEx sourcePath, PathEx destPath)
			throws IOException {
		//TODO 暂不需使用此方法
	    throw new NotImplementedException();
	}

	@Override
	public boolean moveFile(PathEx sourcePath, PathEx destPath)
			throws IOException {
		if(sourcePath == null || sourcePath.getPath() == null || destPath == null || destPath.getPath() == null)
			return false;
		if(sourcePath.getPath().equals(destPath.getPath()))
			return false;
		GridFSFile file = gridFs.findOne(sourcePath.getPath());
		if(file != null) {
			file.put("filename", destPath.getPath());
			file.save();
			return true;
		}
		return false;
	}

	@Override
	public FileEntity saveDirectory(PathEx path) throws IOException {
		//TODO 暂不需使用此方法
	    throw new NotImplementedException();
	}
	@Override
	public FileEntity saveFile(InputStream is, PathEx path,
                               FileReplaceStrategy strategy) throws IOException {
		return saveFile(is, path, strategy, null);
	}
	
	@Override
	public FileEntity saveFile(File file, PathEx path,
                               FileReplaceStrategy strategy, SaveFileCachedListener listener, boolean isNeedMd5) throws IOException {
	    InputStream is = null;
	    try {
	        is = FileUtils.openInputStream(file);
	        if(isNeedMd5) {
	            MD5InputStream fis = new MD5InputStream(is);
                FileEntity entity = saveFile(fis, file.length(), path, strategy, listener);
                entity.setMd5(fis.getHashString());
                return entity;
            } else { 
                return saveFile(is, file.length(), path, strategy, listener);
            }
	    } finally {
	        is.close();
	    }
	}
	
	@Override
	public FileEntity saveFile(InputStream is, long length, PathEx path,
                               FileReplaceStrategy strategy, SaveFileCachedListener listener) throws IOException {
		return saveFile(is, path, strategy, listener);
	}

	@Override
	public FileEntity saveFile(InputStream is, PathEx path,
                               FileReplaceStrategy strategy, SaveFileCachedListener listener) throws IOException {
		//先查询库中是否有filename为path的文件
		GridFSDBFile inputFile = gridFs.findOne(new BasicDBObject("filename",
				path.getPath()));
		if(inputFile != null){
				//库中有filename为path的文件
				if(strategy.equals(FileReplaceStrategy.REPLACE)) {
					gridFs.remove(new BasicDBObject("filename",path.getPath()));
				} else if(strategy.equals(FileReplaceStrategy.DONTREPLACE)) {
					return null;
				}
		}
		//开始存入文件
		GridFSFile fsFile = gridFs.createFile(is);
		// 设置文件名
		fsFile.put("filename", path.getPath());
		//保存
		fsFile.save();
		//保存FileEntity参数
		FileEntity entity = new FileEntity();
		entity.setLength(fsFile.getLength());
		entity.setType(FileEntity.TYPE_FILE);

		if(listener != null) {
			listener.fileCached(entity);
			listener.fileSaved(entity);
		}
		return entity;
	}
	
	@Override
	public Long getLastModificationTime(PathEx path) throws IOException {
		GridFSDBFile inputFile = gridFs.findOne(new BasicDBObject("filename",
				path.getPath()));
		if(inputFile != null)
			return inputFile.getUploadDate().getTime();
		throw new IOException("Get last modification time failed, target is not exist, " + path.getPath());
	}
	@Override
	public String generateDownloadUrl(PathEx path, String fileName, String contentType,
                                      String useragent) throws IOException {
		return null;
	}
	@Override
	public FileEntity getFileEntity(PathEx path) throws IOException {
//		GridFSDBFile inputFile = gridFs.findOne(new BasicDBObject("filename",
//				path.getPath()));
		GridFSDBFile inputFile = null;
		List<GridFSDBFile> inputList = gridFs.find(path.getPath());
		if(inputList != null && !inputList.isEmpty()){
			inputFile = inputList.get(0);
		}
		
		
		if(inputFile != null) {
			FileEntity entity = new FileEntity();
			entity.setLength(inputFile.getLength());
			entity.setLastModificationTime(inputFile.getUploadDate().getTime());
			entity.setType(FileEntity.TYPE_FILE);
			entity.setAbsolutePath(inputFile.getFilename());
			entity.setMd5(inputFile.getMD5());
			return entity;
		}
		return null;
	}


	
	public MongoHelper getResourceHelper() {
		return resourceHelper;
	}

	
	public void setResourceHelper(MongoHelper resourceHelper) {
		this.resourceHelper = resourceHelper;
	}

	@Override
	public boolean isSupportDownloadUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<FileEntity> getFilesInDirectory(PathEx path, String[] extensions, boolean recursive) throws IOException {
		if(path != null){	//若path不为空，根据filename查询
			List<FileEntity> allFiles = new ArrayList<>();
			List<GridFSDBFile> files = gridFs.find(new BasicDBObject("filename", new BasicDBObject("$regex", "^" + path.getPath() + "*").append("$options", "i")));
			if(files != null) {
				for(GridFSDBFile file : files) {
					boolean hit = false;
					if(extensions != null) {
						for(String extension : extensions) {
							String key = file.getFilename();
							if(key.endsWith("." + extension)) {
								hit = true;
								break;
							}
						}
					} else {
						hit = true;
					}
					if(hit && !recursive) {
						String key = file.getFilename();
						String leftPath = key.substring(path.getPath().length());
						if(leftPath.startsWith("/")) {
							leftPath = leftPath.substring(1);
							if(leftPath.contains("/"))
								hit = false;
						}
					}

					if(!hit)
						continue;;
					FileEntity entity = new FileEntity();
					entity.setLength(file.getLength());
					entity.setLastModificationTime(file.getUploadDate().getTime());
					entity.setType(FileEntity.TYPE_FILE);
					entity.setAbsolutePath(file.getFilename());
					entity.setMd5(file.getMD5());
					allFiles.add(entity);
				}
			}
			return allFiles;
		}
		return null;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
}
