package vork.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

import lombok.Getter;

public class Matrix4 {
	
	@Getter
	private float[] buf = new float[16];

	private Matrix4() {
		
	}
	
	public void zero() {
		Arrays.fill(buf, 0.0F);
	}
	
	public static Matrix4 identity() {
		Matrix4 mat = new Matrix4();
		mat.buf[0]  = 1.0f;
		mat.buf[5]  = 1.0f;
		mat.buf[10] = 1.0f;
		mat.buf[15] = 1.0f;
		return mat;
	}
	
	public static Matrix4 ortho2D(float left, float right, float bottom, float top) {
		Matrix4 mat = Matrix4.identity();
		mat.buf[0]  = 2.0f / (right - left);
		mat.buf[5]  = 2.0f / (top - bottom);
		mat.buf[10] = -1.0f;
		mat.buf[12] = -(right + left) / (right - left);
		mat.buf[13] = -(top + bottom) / (top - bottom);
		return mat;
	}
	
	public Matrix4 translate(float x, float y, float z) {
		buf[12] = buf[0]*x + buf[4]*y + buf[8]*z  + buf[12];
		buf[13] = buf[1]*x + buf[5]*y + buf[9]*z  + buf[13];
		buf[14] = buf[2]*x + buf[6]*y + buf[10]*z + buf[14];
		buf[15] = buf[3]*x + buf[7]*y + buf[11]*z + buf[15];
		return this;
	}
	
	public Matrix4 scale(float x, float y, float z) {
		buf[0]  *= x;
		buf[1]  *= x;
		buf[2]  *= x;
		buf[3]  *= x;
		buf[4]  *= y;
		buf[5]  *= y;
		buf[6]  *= y;
		buf[7]  *= y;
		buf[8]  *= z;
		buf[9]  *= z;
		buf[10] *= z;
		buf[11] *= z;
		return this;
	}
	
	public Matrix4 scale(float x, float y) {
		buf[0]  *= x;
		buf[1]  *= x;
		buf[2]  *= x;
		buf[3]  *= x;
		buf[4]  *= y;
		buf[5]  *= y;
		buf[6]  *= y;
		buf[7]  *= y;
		return this;
	}
	
	public Matrix4 invertOrtho2D() {
		float invM00 = 1.0f / buf[0];
        float invM11 = 1.0f / buf[5];
        float invM22 = 1.0f / buf[10];
        Matrix4 dest = new Matrix4();
        dest.buf[0] = invM00;
        dest.buf[1] = 0.0f;
        dest.buf[2] = 0.0f;
        dest.buf[3] = 0.0f;
        dest.buf[4] = 0.0f;
        dest.buf[5] = invM11;
        dest.buf[6] = 0.0f;
        dest.buf[7] = 0.0f;
        dest.buf[8] = 0.0f;
        dest.buf[9] = 0.0f;
        dest.buf[10] = invM22;
        dest.buf[11] = 0.0f;
        dest.buf[12] = -buf[12] * invM00;
        dest.buf[13] = -buf[13] * invM11;
        dest.buf[14] = -buf[14] * invM22;
        dest.buf[15] = 1.0f;
		return dest;
	}
	
	public Vector2 mul(Vector2 v) {
		Vector4 v4 = mul(new Vector4(v.x, v.y, 0, 1));
		return new Vector2(v4.x, v4.y);
	}
	
	public Vector4 mul(Vector4 v) {
		Vector4 r = new Vector4();
		r.x = buf[0]*v.x + buf[4]*v.y + buf[8]*v.z  + buf[12]*v.w;
		r.y = buf[1]*v.x + buf[5]*v.y + buf[9]*v.z  + buf[13]*v.w;
		r.z = buf[2]*v.x + buf[6]*v.y + buf[10]*v.z + buf[14]*v.w;
		r.w = buf[3]*v.x + buf[7]*v.y + buf[11]*v.z + buf[15]*v.w;
		return r;
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder("\n");
		
		Function<Float, Integer> len = f -> f.toString().length();
		
		int[] maxOnColumn = { len.apply(buf[0]), len.apply(buf[4]), len.apply(buf[8]), len.apply(buf[12]) };
		for (int i = 0; i < 4; i++) {
			maxOnColumn[0] = Math.max(maxOnColumn[0], len.apply(buf[0 + i]));
			maxOnColumn[1] = Math.max(maxOnColumn[1], len.apply(buf[4 + i]));
			maxOnColumn[2] = Math.max(maxOnColumn[2], len.apply(buf[8 + i]));
			maxOnColumn[3] = Math.max(maxOnColumn[3], len.apply(buf[12+ i]));
		}
		
		for (int i = 0; i < 4; i++) {
			builder.append("[ ");
			
			int dif0 = maxOnColumn[0] - len.apply(buf[0+i]);
			builder.append(buf[i]).append(String.join("", Collections.nCopies(dif0, " "))).append(" ");
			
			int dif1 = maxOnColumn[1] - len.apply(buf[4+i]);
			builder.append(buf[4+i]).append(String.join("", Collections.nCopies(dif1, " "))).append(" ");
			
			int dif2 = maxOnColumn[2] - len.apply(buf[8+i]);
			builder.append(buf[8+i]).append(String.join("", Collections.nCopies(dif2, " "))).append(" ");
			
			int dif3 = maxOnColumn[3] - len.apply(buf[12+i]);
			builder.append(buf[12+i]).append(String.join("", Collections.nCopies(dif3, " "))).append(" ");
			builder.append("]\n");
		}
		
		return builder.toString();
	}
}
