package vork.gfx;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwHideWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL.createCapabilities;

import java.io.IOException;

import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;

import lombok.Getter;
import vork.Disposable;
import vork.LaunchSettings;

public class Window implements Disposable {
	@Getter
	private String title;
	
	@Getter
	private int width, height;
	
	@Getter
	private boolean vsyncIsEnabled;
	
	// Pointer address to the windows for the
	// operating system
	@Getter
	private long address;
	
	@Getter
	private Monitor monitor;
	
	public void create(LaunchSettings settings) {
		this.title   = settings.windowTitle;
		this.width   = settings.windowWidth;
		this.height  = settings.windowHeight;
		
		if (!glfwInit())
			throw new IllegalStateException("Failed to initialize GLFW. "
					+ "Your hardware may be incompatable.");
		
		this.monitor = settings.displayMonitor.orElseGet(() -> Monitor.getPrimaryMonitor());
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_RESIZABLE, settings.shouldWindowResize ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		
		address = glfwCreateWindow(width, height, title, 0, 0);
		
		if (address == 0)
			throw new IllegalStateException("Failed to create a GLFW window. "
					+ "Your hardware might be incompatable.");
		
		// For linking OpenGL to the window context
		glfwMakeContextCurrent(address);
		createCapabilities();
		
		vsync(settings.enableVsync);
        
		if (settings.shouldCenterWindow) {
			center();
		}
		
		if (settings.windowIcon.isPresent()) {
			try {
				Texture.Buffer buffer = Texture.loadBufferOfPNG(settings.windowIcon.get());
				GLFWImage iconImage = GLFWImage.malloc();
				iconImage.set(buffer.getWidth(), buffer.getHeight(), buffer.asByteBuffer());
				GLFWImage.Buffer iconImageBuffer = GLFWImage.malloc(1);
				iconImageBuffer.put(0, iconImage);
				glfwSetWindowIcon(address, iconImageBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		 glfwSetWindowSizeCallback(address, (wnd, w, h) -> {
        	width  = w;
        	height = h;
        });
		 
		 show();
	}
	
	// Helps fine tune the tps at a steady rate
	private long variableYieldTime, lastTime;
	
	public void frameLimit(int targetFPS) {
		// How many nanoseconds should go by per tick.
		final long nanosPerTick = 1_000_000_000 / targetFPS;
		long yieldTime = Math.min(nanosPerTick, variableYieldTime + nanosPerTick % (1000 * 1000));
		long overSleptTime = 0; // How many nanoseconds were overslept.
	
		 try {
            while (true) {
                long deltaTime = System.nanoTime() - lastTime;

                if (deltaTime < nanosPerTick - yieldTime) {
                    Thread.sleep(1);
                } else if (deltaTime < nanosPerTick) {
                    // burn the last few CPU cycles to ensure accuracy
                    Thread.yield();
                } else {
                	overSleptTime = deltaTime - nanosPerTick;
                    break; // exitServer while loop
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
        	lastTime = System.nanoTime() - Math.min(overSleptTime, nanosPerTick);
        	
        	// auto tune the time sync should yield
            if (overSleptTime > variableYieldTime) {
                // increase by 200 microseconds (1/5 a ms)
                variableYieldTime = Math.min(variableYieldTime + 200 * 1000, nanosPerTick);
            } else if (overSleptTime < variableYieldTime - 200 * 1000) {
                // decrease by 2 microseconds
                variableYieldTime = Math.max(variableYieldTime - 2 * 1000, 0);
            }
        }
	}
	
	public void vsync(boolean b) {
		vsyncIsEnabled = b;
		glfwSwapInterval(b ? 1 : 0);
	}
	
	public void center() {
		GLFWVidMode videoMode = monitor.getVideoMode();
		if (videoMode == null) return;
        
        setLocation(
        		(videoMode.width()  - width)   / 2,
                (videoMode.height() - height)  / 2);
	}
	
	public void setLocation(int x, int y) {
		glfwSetWindowPos(address, x, y);
	}
	
	public void show() {
		glfwShowWindow(address);
	}
	
	public void hide() {
		glfwHideWindow(address);
	}
	
	public void pollInput() {
		glfwPollEvents();
	}
	
	public boolean isOpen() {
		return !glfwWindowShouldClose(address);
	}
	
	public void swapBuffers() {
		glfwSwapBuffers(address);
	}
	
	@Override
	public void dispose() {
		glfwFreeCallbacks(address);
		glfwDestroyWindow(address);
		glfwTerminate();
	}
}
