package vork;

import java.util.Optional;

import vork.gfx.Monitor;
import vork.util.FilePath;

public class LaunchSettings {
	
	public Optional<Monitor> displayMonitor = Optional.empty();
	
	public String windowTitle = "vork.engine";
	
	public int windowWidth  = 500;
	public int windowHeight = 500;
	
	public Optional<FilePath> windowIcon = Optional.empty();
	
	public boolean shouldWindowResize = true;
	public boolean shouldCenterWindow = true;
	public boolean enableVsync        = false;

}
