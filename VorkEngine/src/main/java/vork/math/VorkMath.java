package vork.math;

public class VorkMath {

	public static float lerp(float a, float b, float t) {
		return a + t * (b - a);
	}
	
	public static float clamp(float v, float min, float max) {
		return Math.max(min, Math.min(max, v));
	}
}
