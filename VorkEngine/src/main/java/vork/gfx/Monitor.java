package vork.gfx;

import org.lwjgl.glfw.GLFWVidMode;

import lombok.Getter;

import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;

public class Monitor {
	
	@Getter
	private long address;
	
	@Getter
	private GLFWVidMode videoMode;

	public Monitor(long address, GLFWVidMode videoMode) {
		this.address   = address;
		this.videoMode = videoMode;
	}
	
	public static Monitor getPrimaryMonitor() {
		
		long pointer = glfwGetPrimaryMonitor();
		if (pointer == 0) return null;
		
		return new Monitor(pointer, glfwGetVideoMode(pointer));
	}
	
}
