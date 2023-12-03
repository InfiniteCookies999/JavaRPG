package vork.gfx;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_NEAREST;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;;

@AllArgsConstructor
@Getter
public enum TextureFilter {
	NEAREST_NEAREST(GL_NEAREST, GL_NEAREST),
	LINEAR_NEAREST(GL_LINEAR, GL_NEAREST),
	NEAREST_LINEAR(GL_NEAREST, GL_LINEAR),
	LINEAR_LINEAR(GL_LINEAR, GL_LINEAR),
	MIP_LINEAR_NEAREST(GL_LINEAR_MIPMAP_LINEAR),
	MIP_LINEAR_LINEAR(GL_LINEAR_MIPMAP_LINEAR),
	MIP_NEAREST_NEAREST(GL_NEAREST_MIPMAP_NEAREST),
	MIP_NEAREST_LINEAR(GL_NEAREST_MIPMAP_LINEAR);
	
	private int minFilter;
	
	@Setter
	private int magFilter;
	
	private TextureFilter(int minFilter) {
		this.minFilter = minFilter;
		this.magFilter = GL_LINEAR;
	}
}
