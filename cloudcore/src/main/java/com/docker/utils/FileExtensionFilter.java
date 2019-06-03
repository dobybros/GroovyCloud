package com.docker.utils;

import chat.utils.IteratorEx;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class FileExtensionFilter implements FilenameFilter{
	private IteratorEx<File> iterator;
	private List<File> files = new ArrayList<>();
	
	public FileExtensionFilter() {
	}
	
	public FileExtensionFilter(IteratorEx<File> iterator) {
		this.iterator = iterator;
	}
	public FileExtensionFilter filter(File folder) {
		if(folder.exists()) {
			folder.listFiles(this);
		}
		return this;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		boolean hit = false;
		if(name.endsWith(".js"))
			hit = true;
		File file = new File(FilenameUtils.separatorsToUnix(dir.getAbsolutePath()) + "/" + name);
		if(hit) {
			files.add(file);
			if(this.iterator != null)
				this.iterator.iterate(file);
		} else if(file.isDirectory()) {
			file.listFiles(this);
		}
		
		return false;
	}
	
	public List<File> getFiles() {
		return files;
	}

}
