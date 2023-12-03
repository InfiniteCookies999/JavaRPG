package vork.gfx;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_INVALID_VALUE;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import vork.math.Matrix4;
import vork.math.Vector2;
import vork.math.Vector4;

public class SpriteBatch implements Batch {

	private static final int ATTRIB_STRIDE = 2 + 4 + 2;
	
	private int glVAO;
	private int glVBO;
	
	private float[] buffer;
	private int bufferPtr = 0;
	
	@Getter @Setter
	private ShaderProgram defaultProgram;
	
	public void create(@NonNull ShaderProgram defaultProgram) {
		this.defaultProgram = defaultProgram;
		
		glVAO = glGenVertexArrays();
		if (glVAO == GL_INVALID_VALUE)
			throw new RuntimeException("Failed to create a vertex array.");
		glBindVertexArray(glVAO);
		
		glVBO = glGenBuffers();
		if (glVBO == GL_INVALID_VALUE)
			throw new RuntimeException("Failed to create a vertex buffer.");
		
		bind();
		
		final int STRIDE = ATTRIB_STRIDE * 4;
		glVertexAttribPointer(0, 2, GL_FLOAT, false, STRIDE, 0 * 4);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, STRIDE, 2 * 4);
		glVertexAttribPointer(2, 2, GL_FLOAT, false, STRIDE, 6 * 4);
		
		unbind();
	}
	
	@AllArgsConstructor
	private class Uniform {
		private String name;
		private Object payload;
	}
	
	private class Quad {
		private float[] vBuffer = new float[4*ATTRIB_STRIDE];
		private Texture texture;
		private ShaderProgram program;
		private List<Uniform> uniforms;
	}
	
	private List<Quad> quads = new ArrayList<>();
	
	public void attachUniform1f(String name, float f) {
		attachUniform(name, f);
	}
	
	public void attachUniform1i(String name, boolean b) {
		attachUniform(name, b);
	}
	
	public void attachUniform1i(String name, int i) {
		attachUniform(name, i);
	}
	
	public void attachUniform2f(String name, float f1, float f2) {
		attachUniform(name, new Vector2(f1, f2));
	}
	
	public void attachUniform2f(String name, Vector2 v) {
		attachUniform(name, v);
	}
	
	public void attachUniform4f(String name, float f1, float f2, float f3, float f4) {
		attachUniform(name, new Vector4(f1, f2, f3, f4));
	}
	
	public void attachUniform4f(String name, Vector4 v) {
		attachUniform(name, v);
	}
	
	public void attachUniform4f(String name, Color c) {
		attachUniform(name, c);
	}
	
	private void attachUniform(String name, Object payload) {
		Quad quad = quads.get(quads.size() - 1);
		if (quad.uniforms == null) {
			quad.uniforms = new ArrayList<>();
		}
		quad.uniforms.add(new Uniform(name, payload));
	}

	public void addQuad(float x, float y, float w, float h,
			            @NonNull Color color,
			            @NonNull UvCoords uv,
			            @NonNull Texture texture) {
		addQuad(x, y, w, h, color, uv, texture, null);
	}
	
	public void addQuad(float x, float y, float w, float h,
			            @NonNull Color color,
			            @NonNull UvCoords uv,
			            @NonNull Texture texture,
			            ShaderProgram program) {
		Quad quad = new Quad();
		quad.program = program;
		
		quad.vBuffer[0] = x;
		quad.vBuffer[1] = y;
		quad.vBuffer[2] = color.r;
		quad.vBuffer[3] = color.g;
		quad.vBuffer[4] = color.b;
		quad.vBuffer[5] = color.a;
		quad.vBuffer[6] = uv.u0;
		quad.vBuffer[7] = uv.v0;
		
		quad.vBuffer[8]  = x + w;
		quad.vBuffer[9]  = y;
		quad.vBuffer[10] = color.r;
		quad.vBuffer[11] = color.g;
		quad.vBuffer[12] = color.b;
		quad.vBuffer[13] = color.a;
		quad.vBuffer[14] = uv.u1;
		quad.vBuffer[15] = uv.v0;
		
		quad.vBuffer[16] = x;
		quad.vBuffer[17] = y + h;
		quad.vBuffer[18] = color.r;
		quad.vBuffer[19] = color.g;
		quad.vBuffer[20] = color.b;
		quad.vBuffer[21] = color.a;
		quad.vBuffer[22] = uv.u0;
		quad.vBuffer[23] = uv.v1;
		
		quad.vBuffer[24] = x + w;
		quad.vBuffer[25] = y + h;
		quad.vBuffer[26] = color.r;
		quad.vBuffer[27] = color.g;
		quad.vBuffer[28] = color.b;
		quad.vBuffer[29] = color.a;
		quad.vBuffer[30] = uv.u1;
		quad.vBuffer[31] = uv.v1;
		
		quad.texture = texture;
		
		quads.add(quad);
	}
	
	@Override
	public void render() {
		if (quads.isEmpty()) {
			return;
		}
		
		bufferPtr = 0;
		buffer = new float[ATTRIB_STRIDE * 6 * quads.size()];
		quads.forEach(this::uploadQuad);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW);
		
		glActiveTexture(GL_TEXTURE0);
		
		ShaderProgram currentProgram = defaultProgram;
		int quadCount = 0;
		for (Quad quad : quads) {
			if (quad.program != null) {
				if (quad.program != currentProgram) {
					currentProgram = quad.program;
					currentProgram.bind();
				}
			} else {
				if (currentProgram != defaultProgram) {
					currentProgram = defaultProgram;
					currentProgram.bind();
				}
			}
			
			quad.texture.bind();
			
			if (quad.uniforms != null) {
				for (Uniform uniform : quad.uniforms) {
					if (uniform.payload instanceof Float) {
						currentProgram.uniform1f(uniform.name, (float) uniform.payload);
					} else if (uniform.payload instanceof Boolean) {
						boolean b = (boolean) uniform.payload;
						currentProgram.uniform1i(uniform.name, b ? 1 : 0);
					} else if (uniform.payload instanceof Integer) {
						currentProgram.uniform1i(uniform.name, (Integer) uniform.payload);
					} else if (uniform.payload instanceof Vector2) {
						currentProgram.uniform2f(uniform.name, (Vector2) uniform.payload);
					} else if (uniform.payload instanceof Vector4) {
						currentProgram.uniform4f(uniform.name, (Vector4) uniform.payload);
					} else if (uniform.payload instanceof Color) {
						currentProgram.uniform4f(uniform.name, (Color) uniform.payload);
					} else if (uniform.payload instanceof Matrix4) {
						currentProgram.uniformMatrix4fv(uniform.name, (Matrix4) uniform.payload);
					}
				}
			}
			
			glDrawArrays(GL_TRIANGLES, quadCount * 6, 6);
			
			++quadCount;
		}
		
		quads.clear();
	}
	
	private void uploadQuad(Quad quad) {
		// v1
		buffer[bufferPtr++] = quad.vBuffer[0]; // position
		buffer[bufferPtr++] = quad.vBuffer[1];
		buffer[bufferPtr++] = quad.vBuffer[2]; // color
		buffer[bufferPtr++] = quad.vBuffer[3];
		buffer[bufferPtr++] = quad.vBuffer[4];
		buffer[bufferPtr++] = quad.vBuffer[5];
		buffer[bufferPtr++] = quad.vBuffer[6]; // uv
		buffer[bufferPtr++] = quad.vBuffer[7];
		// v2
		buffer[bufferPtr++] = quad.vBuffer[8];  // position
		buffer[bufferPtr++] = quad.vBuffer[9];
		buffer[bufferPtr++] = quad.vBuffer[10]; // color
		buffer[bufferPtr++] = quad.vBuffer[11];
		buffer[bufferPtr++] = quad.vBuffer[12];
		buffer[bufferPtr++] = quad.vBuffer[13];
		buffer[bufferPtr++] = quad.vBuffer[14]; // uv
		buffer[bufferPtr++] = quad.vBuffer[15];
		// v4
		buffer[bufferPtr++] = quad.vBuffer[24]; // position
		buffer[bufferPtr++] = quad.vBuffer[25];
		buffer[bufferPtr++] = quad.vBuffer[26]; // color
		buffer[bufferPtr++] = quad.vBuffer[27];
		buffer[bufferPtr++] = quad.vBuffer[28];
		buffer[bufferPtr++] = quad.vBuffer[29];
		buffer[bufferPtr++] = quad.vBuffer[30]; // uv
		buffer[bufferPtr++] = quad.vBuffer[31];
		// v1
		buffer[bufferPtr++] = quad.vBuffer[0]; // position
		buffer[bufferPtr++] = quad.vBuffer[1];
		buffer[bufferPtr++] = quad.vBuffer[2]; // color
		buffer[bufferPtr++] = quad.vBuffer[3];
		buffer[bufferPtr++] = quad.vBuffer[4];
		buffer[bufferPtr++] = quad.vBuffer[5];
		buffer[bufferPtr++] = quad.vBuffer[6]; // uv
		buffer[bufferPtr++] = quad.vBuffer[7];
		// v4
		buffer[bufferPtr++] = quad.vBuffer[24]; // position
		buffer[bufferPtr++] = quad.vBuffer[25];
		buffer[bufferPtr++] = quad.vBuffer[26]; // color
		buffer[bufferPtr++] = quad.vBuffer[27];
		buffer[bufferPtr++] = quad.vBuffer[28];
		buffer[bufferPtr++] = quad.vBuffer[29];
		buffer[bufferPtr++] = quad.vBuffer[30]; // uv
		buffer[bufferPtr++] = quad.vBuffer[31];
		// v3
		buffer[bufferPtr++] = quad.vBuffer[16]; // position
		buffer[bufferPtr++] = quad.vBuffer[17];
		buffer[bufferPtr++] = quad.vBuffer[18]; // color
		buffer[bufferPtr++] = quad.vBuffer[19];
		buffer[bufferPtr++] = quad.vBuffer[20];
		buffer[bufferPtr++] = quad.vBuffer[21];
		buffer[bufferPtr++] = quad.vBuffer[22]; // uv
		buffer[bufferPtr++] = quad.vBuffer[23];
	}
	
	@Override
	public void bind() {
		glBindVertexArray(glVAO);
		glBindBuffer(GL_ARRAY_BUFFER, glVBO);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
	}
	
	@Override
	public void unbind() {
		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
	}
	
	@Override
	public void dispose() {
		glDeleteVertexArrays(glVAO);
		glDeleteBuffers(glVBO);
	}
}
