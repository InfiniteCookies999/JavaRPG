package vork.input;

import vork.App;

public class Input {

	private final Keyboard keyboard = new Keyboard();

	private final Cursor cursor = new Cursor();
	
	public void setup() {
		keyboard.setup(App.gfx.window.getAddress());
		cursor.setup(App.gfx.window.getAddress());
	}

	public void update() {
		keyboard.update();
		cursor.update();
	}
	
	/**
	 * Tells whether or not a key is being held down.  *
	 * @param key to test.                             */
	public boolean isKeyPressed(int key) {
		return keyboard.isPressed(key);
	}
	
	public boolean isAnyKeyPressed(int... keys) {
		for (int key : keys) {
			if (keyboard.isPressed(key)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tells whether or not a key was just pressed
	 * this frame.                                  *
	 * @param key to test.                          */
	public boolean isKeyJustPressed(int key) {
		return keyboard.isJustPressed(key);
	}
	
	public boolean isAnyKeyJustPressed(int... keys) {
		for (int key : keys) {
			if (keyboard.isJustPressed(key)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tells whether or not a key has been held
	 * down for a while.                            *
	 * @param key to test.                          */
	public boolean isKeyRepeated(int key) {
		return keyboard.isRepeated(key);
	}
	
	public boolean isAnyKeyRepeated(int... keys) {
		for (int key : keys) {
			if (keyboard.isRepeated(key)) {
				return true;
			}
		}
		return false;
	}
	
	public void addKeyInputListener(KeyInputListener listener) {
		keyboard.addKeyListener(listener);
	}
	
	public void removeKeyInputListener(KeyInputListener listener) {
		keyboard.removeKeyListener(listener);
	}
	
	public int getCursorWndX() {
		return cursor.getWndX();
	}
	
	public int getCursorWndY() {
		return cursor.getWndY();
	}
	
	public double getYScroll() {
		return cursor.getYScroll();
	}
	
	public boolean isCursorInWindow() {
		return cursor.isCursorInWindow();
	}
	
	public boolean isButtonPressed(int button) {
		return cursor.isPressed(button);
	}
	
	public boolean isAnyButtonPressed() {
		return cursor.isPressed(Buttons.LEFT)  ||
			   cursor.isPressed(Buttons.RIGHT) ||
			   cursor.isPressed(Buttons.MIDDLE);
	}
	
	public boolean isButtonJustPressed(int button) {
		return cursor.isJustPressed(button);
	}
	
	public boolean isAnyButtonJustPressed() {
		return cursor.isJustPressed(Buttons.LEFT)  ||
			   cursor.isJustPressed(Buttons.RIGHT) ||
			   cursor.isJustPressed(Buttons.MIDDLE);
	}
}
