package vork.input;

public interface KeyInputListener {

	void onKeyJustPressed(int key);
	
	void onKeyRepeat(int key);
	
	void onKeyReleased(int key);
	
}
