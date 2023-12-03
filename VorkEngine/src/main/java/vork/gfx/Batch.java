package vork.gfx;

public interface Batch {
	
	void bind();
	
	void render();
	
	void unbind();
	
	void dispose();
	
}
