package vork.input;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetCursorEnterCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import lombok.Getter;
import vork.App;

class Cursor {
	
	@Getter
	private int wndX, wndY;

	@Getter
	private double yScroll;
	
	@Getter
	private boolean cursorInWindow;
	
	private boolean[] buttonsPressed = new boolean[3];
	
	private boolean[] buttonsJustPressed = new boolean[3];
	
	Cursor() {
		
	}
	
	void setup(long windowId) {
		
		glfwSetCursorPosCallback(windowId, (wnd, ix, iy) -> {
			wndX = (int) ix;
			wndY = (int) (App.gfx.getWndHeight() - iy);
		});
		
		glfwSetScrollCallback(windowId, (wnd, x, y) -> {
			yScroll = y;
		});
		
		glfwSetMouseButtonCallback(windowId, (wnd, button, action, mods) -> {
			switch (action) {
			case GLFW_PRESS:
				if (button < 3) {
					buttonsPressed[button] = true;
					buttonsJustPressed[button] = true;
				}
				break;
			case GLFW_RELEASE:
				buttonsPressed[button] = false;
				buttonsJustPressed[button] = false;
				break;
			}
		});
		
		glfwSetCursorEnterCallback(windowId, (wnd, entered) -> {
			cursorInWindow = entered;
		});
	}

	void update() {
		for (int i = 0; i < buttonsJustPressed.length; i++) {
			buttonsJustPressed[i] = false;
		}
		yScroll = 0.0f;
	}
	
	boolean isPressed(int button) {
		return buttonsPressed[button];
	}
	
	boolean isJustPressed(int button) {
		return buttonsJustPressed[button];
	}
	
}
