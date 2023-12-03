package vork.gfx;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Color {
	
	public float r, g, b, a;

	public static final Color WHITE = new Color(1, 1, 1, 1);
	public static final Color GRAY  = new Color(0.5F, 0.5F, 0.5F, 1);
	public static final Color BLACK = new Color(0, 0, 0, 1);
	
	public static final Color RED   = new Color(1, 0, 0, 1);
	public static final Color GREEN = new Color(0, 1, 0, 1);
	public static final Color BLUE  = new Color(0, 0, 1, 1);
	
	public static final Color YELLOW  = new Color(1, 1, 0, 1);
	public static final Color MAGENTA = new Color(1, 0, 1, 1);
	public static final Color CYAN    = new Color(0, 1, 1, 1);
	
	public static final Color FULL_TRANSPARENT = new Color(0, 0, 0, 0);
	
	public Color() {
		
	}

	public Color(Color c) {
		r = c.r;
		g = c.g;
		b = c.b;
		a = c.a;
	}
	
	public Color(Color color, float alpha) {
		this(color);
		a = alpha;
	}
	
	public static Color rgb(int hex) {
		float r = ((hex >> 16) & 0xFF) / 255.0f;
		float g = ((hex >> 8 ) & 0xFF) / 255.0f;
		float b = ((hex      ) & 0xFF) / 255.0f;
		return new Color(r, g, b, 1);	
	}
	
	public static Color rgba(int hex) {
		float r = ((hex >> 24) & 0xFF) / 255.0f;
		float g = ((hex >> 16) & 0xFF) / 255.0f;
		float b = ((hex >> 8 ) & 0xFF) / 255.0f;
		float a = ((hex      ) & 0xFF) / 255.0f;
		return new Color(r, g, b, a);	
	}
	
	public Color adjustHue(int adjustment) {
		float[] hsb = toHSB();
		hsb[0] += adjustment / 255.0f;
		hsb[0] = Math.max(0.0f, Math.min(1.0f, hsb[0]));
		return fromHSB(hsb, a);
	}
	
	public Color adjustSaturation(int adjustment) {
		float[] hsb = toHSB();
		hsb[1] += adjustment / 255.0f;
		hsb[1] = Math.max(0.0f, Math.min(1.0f, hsb[1]));
		return fromHSB(hsb, a);
	}
	
	public Color adjustBrightness(int adjustment) {
		float[] hsb = toHSB();
		hsb[2] += adjustment / 255.0f;
		hsb[2] = Math.max(0.0f, Math.min(1.0f, hsb[2]));
		return fromHSB(hsb, a);
	}
	
	public float[] toHSB() {
		float[] hsb = new float[3];
		java.awt.Color.RGBtoHSB((int) (r*255.0f), (int) (g*255.0f), (int) (b*255.0f), hsb);
		return hsb;
	}
	
	public Color fromHSB(float[] hsb, float a) {
		Color color = Color.rgb(java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
		color.a = a;
		return color;
	}
	
	@Override
	public String toString() {
		return String.format("(r = %s, g = %s, b = %s, a = %s)", r, g, b, a);
	}
}
