package vork.util;

import java.io.IOException;

import lombok.Getter;

public abstract class Resource<T> {
	
	@Getter
	private FilePath file;
	
	@Getter
	private T data;
	
	private long lastModifiedTime;
	private int modifiedWaitTime;
	private boolean reloading;
	
	private static final int WAIT_TIME = 5;
	
	public Resource(FilePath file, T data) {
		this.file = file;
		this.data = data;
		lastModifiedTime = file.lastModified();
	}
	
	public void hotReload() throws IOException {
		if (lastModifiedTime == 0) {
			// Something went wrong with getting the last modified time.
			return;
		}
		
		// Check if we need to reload
		long newLastModifiedTime = file.lastModified();
		if (newLastModifiedTime != lastModifiedTime) {
			if (newLastModifiedTime != 0) {
				lastModifiedTime = newLastModifiedTime;	
			}
			modifiedWaitTime = 0;
			reloading = true;
		}
		
		if (!reloading) return;
		if (modifiedWaitTime >= WAIT_TIME) {
			reloading = false;
			handleReload(file, data);
			lastModifiedTime = file.lastModified();
		}
		
		++modifiedWaitTime;
	}
	
	protected abstract void handleReload(FilePath file, T data) throws IOException;
}
