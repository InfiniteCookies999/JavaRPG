package vork.gfx;

import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderType {
	VERTEX(GL_VERTEX_SHADER),
	FRAGMENT(GL_FRAGMENT_SHADER);
	
	@Getter
	private int glType;
	
}
