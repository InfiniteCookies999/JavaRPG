package vork.util;

public class Collision {

	public static boolean rectToRect(
			float x0, float y0, float w0, float h0,
			float x1, float y1, float w1, float h1
			) {
		return x0      < x1 + w1 &&
			   x0 + w0 > x1      &&
			   y0      < y1 + h1 &&
			   y0 + h0 > y1;
	}
	
	public static boolean pointToRect(float px, float py,
            float rx, float ry, float rw, float rh) {
		return  px >= rx      &&
				px <= rx + rw &&
				py >= ry      &&
				py <= ry + rh;
	}
}
