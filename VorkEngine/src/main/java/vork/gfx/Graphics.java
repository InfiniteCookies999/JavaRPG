package vork.gfx;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;


public class Graphics {

	public final Window window = new Window();

	public Texture emptyTexture;
	
	public int getWndWidth() {
		return window.getWidth();
	}
	
	public int getWndHeight() {
		return window.getHeight();
	}
	
	public void clearColor(Color color) {
		glClearColor(color.r, color.g, color.b, color.a);
	}

	public void clearColor(float red, float green, float blue, float alpha) {
		glClearColor(red, green, blue, alpha);
	}
	
	public void clear(int mask) {
		glClear(mask);
	}
	
	public void enableBlend(int sfactor, int dfactor) {
		glEnable(GL_BLEND);
		glBlendFunc(sfactor, dfactor);
	}
	
	public void disableBlend() {
		glDisable(GL_BLEND);
	}
	
	public void setViewport(int w, int h) {
		setViewport(0, 0, w, h);
	}
	
	public void setViewport(int x, int y, int w, int h) {
		glViewport(x, y, w, h);
	}
}
