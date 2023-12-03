package vork.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import vork.App;

public class FilePath {
	
	private enum FileType {
		
		/**
		 * The location of the file exist
		 * within the contents of the jar
		 * as part of it's resources.
		 */
		INTERNAL,
		
		/**
		 * The file exist somewhere on
		 * the hardrive of the computer.
		 */
		EXTERNAL
		
	}
	
	private FileType type;
	private String path;
	
	private static String HOME_PATH;
	static {
		HOME_PATH = System.getProperty("user.home");
	}
	
	private FilePath(FileType type, String path) {
		this.type = type;
		this.path = path;
	}
	
	public static FilePath internal(String path) {
		return new FilePath(FileType.INTERNAL, path);
	}
	
	public static FilePath external(String path) {
		return new FilePath(FileType.EXTERNAL, path);
	}
	
	public static FilePath home(String path) {
		return new FilePath(FileType.EXTERNAL, HOME_PATH + "/" + path);
	}
	
	public String getName() {
		String[] parts = path.split("[/|\\\\]");
		return parts[parts.length - 1];
	}
	
	public String getExtension() {
		String name = getName();
		
		String[] parts = name.split("\\.");
		if (parts.length == 1)
			return "";
		return parts[parts.length - 1];
	}
	
	public boolean isDirectory() {
		File systemFile = getSystemFile();
		if (systemFile == null) return false;
		return systemFile.isDirectory();
	}
	
	public File getSystemFile() {
		switch (type) {
		case EXTERNAL:
			return new File(path);
		case INTERNAL:
			URL url = getURL();
			if (url == null) return null;
			if (url.getProtocol().equals("jar")) {
				throw new RuntimeException("Cant get system directory for files inside a jar.");
			}
			return new File(url.getFile());
		default:
			return null;
		}
	}
	
	public URL getURL() {
		return App.class.getResource("/" + path);
	}
	
	public boolean exist() {
		switch (type) {
		case EXTERNAL:
			return new File(path).exists();
		case INTERNAL:
			URL url = getURL();
			if (url == null) return false;
			if (url.getProtocol().equals("jar")) {
				return true;
			}
			return new File(url.getFile()).exists();
		default:
			return false;
		}
	}
	
	public boolean mkDir() {
		File systemFile = getSystemFile();
		if (systemFile == null)
			throw new IllegalStateException("Failed to make directory: " + this);
		return systemFile.mkdir();
	}
	
	public boolean delete() {
		File systemFile = getSystemFile();
		if (systemFile == null)
			return false;
		return systemFile.delete();
	}
	
	public InputStream getInputStream() throws IOException {
		switch (type) {
		case EXTERNAL:
			return new FileInputStream(path);
		case INTERNAL:
			return FilePath.class.getResourceAsStream("/" + path);
		default:
			return null;
		}
	}
	
	public BufferedReader getBufferedReader() throws IOException {
		switch (type) {
		case EXTERNAL:
			return new BufferedReader(new FileReader(path));
		case INTERNAL:
			return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
		default:
			return null;
		}
	}
	
	public String readFileIntoString() throws IOException {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader br = getBufferedReader()) {
			String line = null; 
			while ((line = br.readLine()) != null) {
                builder.append(line).append("\n");
            }
		}
		return builder.toString();
	}
	
	public Iterator<FilePath> getDirectoryIterator() throws IOException {
		if (!exist()) {
			throw new IOException("Tried to search the directory for path: " + this + " but does not exist");
		}
		
		FilePath[] files = getFilesInDirectory();
		return new Iterator<FilePath>() {
			
			private int count = 0;
			
			@Override
			public boolean hasNext() {
				return count < files.length;
			}

			@Override
			public FilePath next() {
				FilePath file = files[count];
				++count;
				return file;
			}
		};
	}
	
	public FilePath[] getFilesInDirectory() throws IOException {
		if (!exist()) {
			throw new IOException("Tried to search the directory for path: " + this + " but does not exist");
		}
		
		List<String> stringedPaths = new ArrayList<>();
		if (isJarProtocol()) {
			File jarFile = new File(URLDecoder.decode(getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
			String extendedPath = endsWithSeparator() ? path : path + "/";
			try (JarFile jar = new JarFile(jarFile)) {
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
			        String fileName = entries.nextElement().getName();
			        
			        if (!fileName.startsWith(extendedPath) || fileName.equals(extendedPath)) {
			        	continue;
			        }
			        
			        stringedPaths.add(fileName);
			    }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (!isDirectory()) {
			throw new RuntimeException("Can't get directory files for non-directory: " + this);
		}
		for (File file : getSystemFile().listFiles()) {	
			FilePath sysPath = append(file.getName());
			stringedPaths.add(sysPath.path);
		}
		
		// Alphabetically sorting
		Collections.sort(stringedPaths);
		
		FilePath[] files = new FilePath[stringedPaths.size()];
		for (int i = 0; i < stringedPaths.size(); i++) {
			files[i] = new FilePath(type, stringedPaths.get(i));
		}
		
		return files;
	}
	
	public boolean isJarProtocol() {
		if (type == FileType.INTERNAL) {
			URL url = getURL();
			if (url.getProtocol().equals("jar")) {
				return true;
			}
		}
		return false;
	}
	
	public boolean endsWithSeparator() {
		return path.endsWith("/") || path.endsWith("\\");
	}
	
	public String getPathSpecifier() {
		return new StringBuilder(type.toString()
						        .toLowerCase())
						        .append(":")
						        .append(path)
						        .toString();
	}
	
	public FilePath append(String path) {
		if (path.startsWith("/|\\")) {
			path = path.substring(1);
		}
		String newPath = this.path + (!endsWithSeparator() ? "/" : "") + path;
		return new FilePath(type, newPath);
	}
	
	public long lastModified() {
		File file = getSystemFile();
		if (file == null) return 0L;
		return file.lastModified();
	}

	@Override
	public String toString() {
		return getPathSpecifier();
	}
}
