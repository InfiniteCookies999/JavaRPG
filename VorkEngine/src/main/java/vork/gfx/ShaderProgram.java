package vork.gfx;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;
import static org.lwjgl.opengl.GL31.GL_INVALID_INDEX;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vork.math.Matrix4;
import vork.math.Vector2;
import vork.math.Vector4;

public class ShaderProgram {

	@Getter
	private int glId;
	
	@AllArgsConstructor
	private class Shader {
		String source;
		ShaderType type;
		int glId;
	}
	
	private List<Shader> shaders = new ArrayList<>();
	
	public void attachShader(String source, ShaderType type) {
		shaders.add(new Shader(source, type, 0));
	}
	
	public void compile() {
		for (Shader shader : shaders) {
			shader.glId = -1;
		}
		
		for (Shader shader : shaders) {
			shader.glId = compileShader(shader.source, shader.type.getGlType());
		}
		
		glId = glCreateProgram();
		
		for (Shader shader : shaders) {
			glAttachShader(glId, shader.glId);	
		}
		
		glLinkProgram(glId);
		
		if (glGetProgrami(glId, GL_LINK_STATUS) != GL_TRUE) {
			for (Shader shader : shaders)
				glDeleteShader(shader.glId);
			throw new GLSLException(glGetProgramInfoLog(glId));	
		}
		
		glValidateProgram(glId);
		if (glGetProgrami(glId, GL_VALIDATE_STATUS) != GL_TRUE) {
			for (Shader shader : shaders)
				glDeleteShader(shader.glId);
			throw new RuntimeException(glGetProgramInfoLog(glId));	
		}
		
		for (Shader shader : shaders) {
			glDetachShader(glId, shader.glId);
			glDeleteShader(shader.glId);
		}
	}
	
	private int compileShader(String source, int type) {
		int glShaderId = glCreateShader(type);
		
		glShaderSource(glShaderId, source);
        glCompileShader(glShaderId);
        
        if (glGetShaderi(glShaderId, GL_COMPILE_STATUS) != GL_TRUE) {
        	String errorMessage = glGetShaderInfoLog(glShaderId);
        	
        	// Delete any currently compiled shaders.
            glDeleteShader(glShaderId);
            for (Shader shader : shaders) {
            	if (shader.glId != -1) {
            		glDeleteShader(shader.glId);
            	}
            }
            throw new GLSLException(errorMessage);
        }
		
		return glShaderId;
	}
	
	public int getUniformLocation(String uniformName) {
		int location = glGetUniformLocation(glId, uniformName);
		if (location == GL_INVALID_INDEX)
			throw new RuntimeException(new StringBuilder("Failed to get uniform location by name: ").append(uniformName).toString());
        return location;
	}
	
	public void uniformMatrix4fv(String uniformName, Matrix4 mat) {
		glUniformMatrix4fv(getUniformLocation(uniformName), false, mat.getBuf());
	}
	
	public void uniform1i(String name, int x) {
		glUniform1i(getUniformLocation(name), x);
	}
	
	public void uniform1f(String name, float x) {
		glUniform1f(getUniformLocation(name), x);
	}
	
	public void uniform2f(String name, float x, float y) {
		glUniform2f(getUniformLocation(name), x, y);
	}
	
	public void uniform2f(String name, Vector2 v) {
		glUniform2f(getUniformLocation(name), v.x, v.y);
	}
	
	public void uniform3f(String name, float x, float y, float z) {
		glUniform3f(getUniformLocation(name), x, y, z);
	}
	
	public void uniform3f(String name, Color c) {
		glUniform3f(getUniformLocation(name), c.r, c.g, c.b);
	}
	
	public void uniform4f(String name, float x, float y, float z, float w) {
		glUniform4f(getUniformLocation(name), x, y, z, w);
	}
	
	public void uniform4f(String name, Vector4 v) {
		glUniform4f(getUniformLocation(name), v.x, v.y, v.z, v.w);
	}
	
	public void uniform4f(String name, Color color) {
		glUniform4f(getUniformLocation(name), color.r, color.g, color.b, color.a);
	}
	
	public void bind() {
		glUseProgram(glId);	
	}
	
	public void unbind() {
		glUseProgram(0);
	}
	
	public void dispose() {
		glDeleteProgram(glId);
	}	
}
