package script.file;

import chat.utils.MD5InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LocalFileHandler extends FileAdapter {
	
	private String rootPath;
	
	public LocalFileHandler() {
	}

	public static void main(String[] args) throws IOException {
		LocalFileHandler fileHandler = new LocalFileHandler();
		fileHandler.setRootPath("/Users/aplombchen/Desktop/files");
		Collection<FileEntity> files = fileHandler.getFilesInDirectory(new PathEx("/"), new String[]{"zip"}, true);
		System.out.println(files);
		for(FileEntity file : files) {
			fileHandler.readFile(new PathEx(file.getAbsolutePath()), FileUtils.openOutputStream(new File("/Users/aplombchen/Desktop/1.zip")));
		}
	}
	
	private String getAbsolutePath(String path) {
		if(path.startsWith(rootPath)) {
			return path;
		}
	    String prefix = FilenameUtils.getPrefix(path);
	    if (!StringUtils.isEmpty(prefix)) {
	        path = path.substring(1);
	    }
		return FilenameUtils.concat(rootPath, path);
	}
	
	@Override
	public boolean deleteDirectory(PathEx path) throws IOException {
		File directory = new File(getAbsolutePath(path.getPath()));
		if(directory.exists()) {
			if(directory.isFile())
				throw new IOException("Delete directory failed, target is a file. " + getAbsolutePath(path.getPath()));
			return FileUtils.deleteQuietly(directory);
		}
		return false;
	}

	@Override
	public boolean deleteFile(final PathEx path) throws IOException {
		String thePath = getAbsolutePath(path.getPath());
		final String n = FilenameUtils.getName(thePath);
		if(!StringUtils.isBlank(n)) {
			thePath = thePath.substring(0, thePath.length() - n.length());
		}
		File dir = new File(thePath);
		File[] foundFiles = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.startsWith(n);
		    }
		});
		if(foundFiles != null)
			for(File file : foundFiles) {
				if(file.isDirectory())
					FileUtils.deleteDirectory(file);
				else 
					FileUtils.deleteQuietly(file);
			}
		
		return true;
	}

	@Override
	public List<FileEntity> getFilesInDirectory(PathEx path, String[] extensions, boolean recursive) throws IOException {
		File directory = new File(getAbsolutePath(path.getPath()));
		if(directory.exists()) {
			if(directory.isFile())
				throw new IOException("Get files in directory failed, target is a file, " + getAbsolutePath(path.getPath()));
			
			Collection<File>  files = FileUtils.listFiles(directory, extensions, recursive);
//			Iterator<File> files = FileUtils.iterateFiles(directory, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
			List<FileEntity> list = new ArrayList<>();
			for(File file : files) {
				FileEntity entity = new FileEntity();
				entity.setAbsolutePath(FilenameUtils.separatorsToUnix(file.getAbsolutePath()));
				entity.setLastModificationTime(file.lastModified());
				entity.setLength(file.length());
				entity.setType(file.isFile() ? FileEntity.TYPE_FILE : FileEntity.TYPE_DIRECTORY);
				list.add(entity);
			}
			return list;
		} else 
			throw new IOException("Get file in directory failed, target is not exists, " + path);
	}

	@Override
	public boolean isDirectoryExist(PathEx path) throws IOException {
		File directory = new File(getAbsolutePath(path.getPath()));
		if(directory.isFile())
			throw new IOException("Is directory exist failed, target is not a directory, " + getAbsolutePath(path.getPath()));
		return directory.exists();
	}

	@Override
	public boolean isFileExist(PathEx path) throws IOException {
		File file = new File(getAbsolutePath(path.getPath()));
		if(!file.isFile())
			return false;
		return file.exists();
	}

	@Override
	public boolean readFile(PathEx path, OutputStream os) throws IOException {
		File file = new File(getAbsolutePath(path.getPath()));
		if(!file.exists()) 
			return false;
//			throw new IOException(path + " is not exists");
		FileInputStream fis = null;
		try {
			fis = FileUtils.openInputStream(file);
//			 IOUtils.copyLarge(fis, os);
			copy(path, fis, os, READWRITE.READ);
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return true;
	}
	
	@Override
	public boolean readFile(PathEx path, OutputStream os, Integer offset, Integer length) throws IOException {
		File file = new File(getAbsolutePath(path.getPath()));
		if(!file.exists()) 
			return false;
//			throw new IOException(path + " is not exists");
		FileInputStream fis = null;
		try {
			fis = FileUtils.openInputStream(file);
			copyStream(fis, os, offset, length);
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return true;
	}
	
	@Override
	public boolean moveDirectory(PathEx sourcePath, PathEx destPath)
			throws IOException {
		FileUtils.moveDirectory(new File(getAbsolutePath(sourcePath.getPath())), new File(getAbsolutePath(destPath.getPath())));
		return true;
	}

	@Override
	public boolean moveFile(PathEx sourcePath, PathEx destPath)
			throws IOException {
		File destFile = new File(getAbsolutePath(destPath.getPath()));
		if(destFile.exists())
			FileUtils.deleteQuietly(destFile);
		FileUtils.moveFile(new File(getAbsolutePath(sourcePath.getPath())), new File(getAbsolutePath(destPath.getPath())));
		return true;
	}

	@Override
	public FileEntity saveDirectory(PathEx path) throws IOException {
		File directory = new File(getAbsolutePath(path.getPath()));
		if(!directory.exists()) {
			boolean bool = directory.mkdir();
			if(bool) {
				FileEntity entity = new FileEntity();
				entity.fromFile(directory);
				return entity;
			} else {
				return null;
			}
		} else if(directory.isDirectory()) 
			return null;
		else
			throw new IOException("File " + getAbsolutePath(path.getPath()) + " exists and is " + "not a directory. Unable to create directory.");
	}

	@Override
	public FileEntity saveFile(InputStream is, PathEx path,
			FileReplaceStrategy strategy) throws IOException {
		return saveFile(is, path, strategy, null);
	}
	
	@Override
	public FileEntity saveFile(File file, PathEx path,
			FileReplaceStrategy strategy, SaveFileCachedListener listener, boolean isNeedMd5) throws IOException {
		MD5InputStream fis = null;
		InputStream is = FileUtils.openInputStream(file); 
		try {
			if(isNeedMd5) {
				fis = new MD5InputStream(is);
				is = fis;
			} 
			FileEntity entity = saveFile(is, file.length(), path, strategy, listener);
			
			if (isNeedMd5 && fis != null) {
				entity.setMd5(fis.getHashString());
			}
			return entity;
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
		String destPath = getAbsolutePath(path.getPath());
		File targetFile = new File(destPath);
		if(targetFile.exists()) {
			if(targetFile.isFile() && targetFile.canWrite()) {
				if(strategy.equals(FileReplaceStrategy.REPLACE)) {
					targetFile.delete();
				} else if(strategy.equals(FileReplaceStrategy.DONTREPLACE)) {
					return null;
				}
			} else
				throw new IOException("Save file failed, target path is a directory or can not write, " + getAbsolutePath(path.getPath()));
		}
		FileOutputStream fos = null;
		try {
			fos = FileUtils.openOutputStream(targetFile);
			IOUtils.copyLarge(is, fos);
		} finally {
			IOUtils.closeQuietly(fos);
		}
	//	File savedFile = new File(getAbsolutePath(path));
		FileEntity entity = new FileEntity();
		entity.setLength(targetFile.length());
		entity.setLastModificationTime(targetFile.lastModified());
		entity.setType(FileEntity.TYPE_FILE);
		entity.setAbsolutePath(destPath);
		
		if(listener != null) {
			listener.fileCached(entity);
			listener.fileSaved(entity);
		}
		return entity;
	}

	@Override
	public String generateDownloadUrl(PathEx path, String fileName, String contentType,
			String useragent) throws IOException {
		return null;
	}
	@Override
	public Long getLastModificationTime(PathEx path) throws IOException {
		File file = new File(getAbsolutePath(path.getPath()));
		if(file.exists())
			return file.lastModified();
		throw new IOException("Get last modification time failed, target is not exist, " + getAbsolutePath(path.getPath()));
	}
	
	@Override
	public FileEntity getFileEntity(PathEx path) throws IOException {
		String ap = getAbsolutePath(path.getPath());
		File file = new File(ap);
		FileEntity e = new FileEntity();
		e.setAbsolutePath(ap);
		long size = 0;
		if (file.isDirectory()) {
			Collection<File> fs = FileUtils.listFiles(file, null, true);
			for (File f : fs) {
				size += f.length();
			}
			fs = null;
		} else if(file.isFile()){
			size = file.length();
		} else {
			return null;
		}
		e.setLength(size);
		return e;
	}

	@Override
	public InputStream readInputStream(PathEx path) throws IOException {
		String ap = getAbsolutePath(path.getPath());
		File file = new File(ap);
		if(file.exists()){
			return new FileInputStream(file);
		}
		return null;
	}

	public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

	
    public String getRootPath() {
        return rootPath;
    }

	@Override
	public boolean isSupportDownloadUrl() {
		// TODO Auto-generated method stub
		return false;
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
