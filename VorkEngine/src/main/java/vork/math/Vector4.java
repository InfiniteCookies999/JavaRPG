package vork.math;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Vector4 {
	public float x, y, z, w;

	public Vector4() {
		
	}
	
	public Vector4(Vector4 v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
		this.w = v.w;
	}

	public Vector4 add(Vector4 v) {
		return new Vector4(v.x + x, v.y + y, v.z + z, v.w + w);
	}
	
	public Vector4 add(float x, float y, float z, float w) {
		return new Vector4(this.x + x, this.y + y, this.z + z, this.w + w);
	}
	
	public Vector4 sub(Vector4 v) {
		return new Vector4(this.x - v.x , this.y - y, this.z - v.z, this.w - v.w);
	}
	
	public Vector4 sub(float x, float y, float z, float w) {
		return new Vector4(this.x - x, this.y - y, this.z - z, this.w - w);
	}
	
	@Override
	public String toString() {
		return "{" + x + ", " + y + ", " + z + ", " + w + "}";
	}
}
