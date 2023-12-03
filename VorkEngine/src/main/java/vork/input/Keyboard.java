package vork.input;

import static org.lwjgl.glfw.GLFW.GLFW_LOCK_KEY_MODS;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

class Keyboard {
	
	private static final int NUM_KEYS = (Keys.LAST_MASK - 1) | Keys.LAST_MASK;

	private boolean[] pressedKeys     = new boolean[NUM_KEYS];
	private boolean[] justPressedKeys = new boolean[NUM_KEYS];
	private boolean[] repeatedKeys    = new boolean[NUM_KEYS];

	private List<KeyInputListener> keyInputListeners = new ArrayList<>();
	
	Keyboard() {
		
	}
	
	void setup(long windowId) {
			
		glfwSetInputMode(windowId, GLFW_LOCK_KEY_MODS, GLFW_TRUE);
		glfwSetKeyCallback(windowId, (wndId, key, scancode, action, mods) -> {
			
			switch (action) {
			case GLFW_PRESS:
				
				pressedKeys[key] = true;
				justPressedKeys[key] = true;
				
				if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) {
					pressedKeys[key | Keys.SHIFT_MASK] = true;
					justPressedKeys[key | Keys.SHIFT_MASK] = true;
				}
				
				if ((mods & GLFW.GLFW_MOD_CAPS_LOCK) != 0) {
					pressedKeys[key | Keys.CAPS_MASK] = true;
					justPressedKeys[key | Keys.CAPS_MASK] = true;
				}
				
				keyInputListeners.forEach(listener -> listener.onKeyJustPressed(key));
				
				break;
			case GLFW_REPEAT:
				repeatedKeys[key] = true;
				if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) {
					repeatedKeys[key | Keys.SHIFT_MASK] = true;
				}
				if ((mods & GLFW.GLFW_MOD_CAPS_LOCK) != 0) {
					repeatedKeys[key | Keys.CAPS_MASK] = true;
				}
				
				keyInputListeners.forEach(listener -> listener.onKeyRepeat(key));
				break;
			case GLFW_RELEASE:
				pressedKeys[key] = false;
				justPressedKeys[key] = false;
				repeatedKeys[key] = false;
				pressedKeys[key | Keys.SHIFT_MASK] = false;
				justPressedKeys[key | Keys.SHIFT_MASK] = false;
				repeatedKeys[key | Keys.SHIFT_MASK] = false;
				
				keyInputListeners.forEach(listener -> listener.onKeyReleased(key));
				break;
			}
			
		});
	}
	
	void update() {
		for (int i = 0; i < justPressedKeys.length; i++) {
			justPressedKeys[i] = false;
		}
	}
	
	boolean isPressed(int key) {
		int fkey = key & ~(Keys.NO_SHIFT_MASK | Keys.NO_CAPS_MASK);
		if (checkNegateMask(key, fkey)) return false;
		return pressedKeys[fkey] || pressedKeys[fkey | Keys.SHIFT_MASK] || pressedKeys[fkey | Keys.CAPS_MASK];
	}
	
	boolean isJustPressed(int key) {
		int fkey = key & ~(Keys.NO_SHIFT_MASK | Keys.NO_CAPS_MASK);
		if (checkNegateMask(key, fkey)) return false;
		return justPressedKeys[key] || justPressedKeys[key | Keys.SHIFT_MASK] || justPressedKeys[key | Keys.CAPS_MASK];
	}
	
	boolean isRepeated(int key) {
		int fkey = key & ~(Keys.NO_SHIFT_MASK | Keys.NO_CAPS_MASK);
		if (checkNegateMask(key, fkey)) return false;
		return repeatedKeys[key] || repeatedKeys[key | Keys.SHIFT_MASK] || repeatedKeys[key | Keys.CAPS_MASK];
	}
	
	boolean checkNegateMask(int key, int fkey) {
		if ((key & Keys.NO_SHIFT_MASK) != 0 && pressedKeys[fkey | Keys.SHIFT_MASK]) return true;
		if ((key & Keys.NO_CAPS_MASK) != 0 && pressedKeys[fkey | Keys.CAPS_MASK]) return true;
		return false;
	}
	
	void addKeyListener(KeyInputListener listener) {
		keyInputListeners.add(listener);
	}
	
	void removeKeyListener(KeyInputListener listener) {
		keyInputListeners.remove(listener);
	}
}
