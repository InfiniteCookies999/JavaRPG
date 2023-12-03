package vork;

import java.io.IOException;
import java.util.Arrays;

import lombok.Getter;
import vork.gfx.Graphics;
import vork.gfx.Texture;
import vork.gfx.TextureFilter;
import vork.gfx.gui.GuiManager;
import vork.input.Input;

public abstract class App {
	 /** Provides basic graphics utilities for interacting
	   *  with OpenGL. */
	public static final Graphics gfx = new Graphics();
	  /** Handles inputs from the client window such as cursor
	   *  and keyboard inputs */
	public static final Input    input = new Input();
	
	@Getter
	private int fps = 60;
	
	public void run(LaunchSettings settings) throws IOException {
		gfx.window.create(settings);
	
		input.setup();
		
		int[] blankArray = new int[4*4];
		Arrays.fill(blankArray, 0xFFFFFFFF);
		Texture.Buffer emptyBuffer = new Texture.Buffer(4, 4, blankArray);
		gfx.emptyTexture = Texture.createFromBuffer(
				emptyBuffer,
				TextureFilter.NEAREST_NEAREST);
		
		GuiManager.setup();
		
		init();
		
		// Starting the application's render loop
		loop();
		
		gfx.window.dispose();
		GuiManager.dispose();
		
		dispose();
		
	}
	
	/**
	 * Pre-render initialization setup
	 * for the inherited application.
	 */
	protected abstract void init() throws IOException;
	
	/**
	 * Called each frame to perform rendering
	 * and updating the objects.
	 */
	protected abstract void tick();
	
	/**
	 * Cleanup once the window has been
	 * destroyed.
	 */
	protected abstract void dispose();
	
	/**
	 * Using nano-time for accuracy purposes
	 */
	private long getTimeInMilliseconds() {
	    return System.nanoTime() / 1000000L;
	}
	
	private void loop() {
		long fpsBeginTime = getTimeInMilliseconds();
		int fpsCounter = 0;
		
		while (gfx.window.isOpen()) {
			
			++fpsCounter; // count each tick that goes by.
			
			input.update();
			gfx.window.pollInput();
			
			tick();
			
			gfx.window.swapBuffers();
			
			gfx.window.frameLimit(60);
			
			if (getTimeInMilliseconds() - fpsBeginTime > 1000) {
				fps = fpsCounter;
				fpsCounter = 0;
				fpsBeginTime += 1000;
			}
		}
	}
}
