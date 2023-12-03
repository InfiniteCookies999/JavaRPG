package vork.gfx;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UvCoords {
	
	public static final UvCoords FULL = new UvCoords(0, 0, 1, 1);
	
	public float u0, v0, u1, v1;

	public UvCoords(UvCoords uv) {
		u0 = uv.u0;
		v0 = uv.v0;
		u1 = uv.u1;
		v1 = uv.v1;
	}
	
	public void flipOnX() {
		float t = u0;
		u0 = u1;
		u1 = t;
	}
	
	public void flipOnY() {
		float t = v0;
		v0 = v1;
		v1 = t;
	}
	
	public static UvCoords createFromPixelSize(int px, int py, int pw, int ph, Texture texture) {
		return createFromPixelSize(px, py, pw, ph, texture.getWidth(), texture.getHeight());
	}
	
	public static UvCoords createFromPixelSize(int px, int py, int pw, int ph,
			                                   int textureWidth, int textureHeight) {
		float u0 = px / (float) textureWidth;
		float v0 = py / (float) textureHeight;
		
		float u1 = u0 + pw / (float) textureWidth;
		float v1 = v0 + ph / (float) textureHeight;
		
		return new UvCoords(u0, v0, u1, v1);
	}
}
