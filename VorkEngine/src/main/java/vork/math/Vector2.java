package vork.math;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Vector2 {
	public float x, y;

	public Vector2() {
		
	}
	
	public Vector2(Vector2 v) {
		this.x = v.x;
		this.y = v.y;
	}
	
	public Vector2 add(Vector2 v) {
		return new Vector2(v.x + x, v.y + y);
	}
	
	public Vector2 add(float x, float y) {
		return new Vector2(this.x + x, this.y + y);
	}
	
	public Vector2 sub(Vector2 v) {
		return new Vector2(this.x - v.x , this.y - y);
	}
	
	public Vector2 sub(float x, float y) {
		return new Vector2(this.x - x, this.y - y);
	}
	
	@Override
	public String toString() {
		return "{" + x + ", " + y + "}";
	}
}
