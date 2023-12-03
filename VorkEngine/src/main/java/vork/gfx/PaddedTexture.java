package vork.gfx;

public class PaddedTexture extends Texture {

	public int padWidth;
	public int padHeight;
	public int originalWidth;
	public int originalHeight;
	
	public PaddedTexture(int glId, int width, int height, TextureFilter filter) {
		super(glId, width, height, filter);
	}
}
