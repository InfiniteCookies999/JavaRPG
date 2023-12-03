package vork.gfx.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import lombok.Getter;
import lombok.Setter;
import vork.App;
import vork.gfx.ShaderProgram;
import vork.gfx.ShaderType;
import vork.gfx.SpriteBatch;
import vork.math.Matrix4;

public class GuiManager {
	
	private static final List<GuiComponent> rootComponents = new ArrayList<>();
	
	private static final String vertexSource =
			"#version 430\n" +
			"\n" +
			"layout (location = 0) in vec2 v_pos;\n" +
			"layout (location = 1) in vec4 v_color;\n" +
			"layout (location = 2) in vec2 v_uv;\n" +
			"\n" +
			"out vec4 f_color;\n" +
			"out vec2 f_uv;\n" +
			"\n" +
			"uniform mat4 u_proj;\n" +
			"\n" +
			"void main() {\n" +
			"	gl_Position = u_proj * vec4(v_pos, 0.0, 1.0);\n" +
			"	f_uv = vec2(v_uv.x, 1.0 - v_uv.y);\n" +
			"	f_color = v_color;\n" +
			"}\n";
	
	private static final String fragmentSoruce =
			"#version 430\n" +
			"\n" +
			"in vec4 f_color;\n" +
			"in vec2 f_uv;\n" +
			"\n" +
			"uniform sampler2D u_txSampler;\n" +
			"\n" +
			"uniform int u_hasBounding;" +
			"uniform vec4 u_bounding;" +
			"\n" +
			"out vec4 o_color;\n" +
			"\n" +
			"void main() {\n" +
			"	if (u_hasBounding == 1) {\n" +
			"		if (!(gl_FragCoord.x >= u_bounding.x &&\n" +
			"			  gl_FragCoord.x <= u_bounding.z &&\n" +
			"			  gl_FragCoord.y >= u_bounding.y &&\n" +
			"			  gl_FragCoord.y <= u_bounding.w)) {\n" +
			"			discard;" +
			"			return;" +
			"		}\n" +
			"	}\n" +
			"	o_color = texture(u_txSampler, f_uv) * f_color;\n" +
			"}\n";
	
	private static final ShaderProgram shaderProgram = new ShaderProgram();
	
	@Getter
	private static Matrix4 projMatrix;
	
	@Setter @Getter
	private static boolean cursorHovering;
	
	@Setter @Getter
	private static boolean cursorClicked;
	
	public static void setup() {
		shaderProgram.attachShader(vertexSource, ShaderType.VERTEX);
		shaderProgram.attachShader(fragmentSoruce, ShaderType.FRAGMENT);
		shaderProgram.compile();
	}
	
	public static void addRootComponent(GuiComponent component) {
		rootComponents.add(component);			
	}
	
	public static void removeRootComponent(GuiComponent component) {
		rootComponents.remove(component);
		component.processDestruction(); // TODO: This seems not to make much sense here.
	}
	
	public static void update() {
		cursorHovering = false;
		cursorClicked = false;
		
		ListIterator<GuiComponent> iterator = rootComponents.listIterator(rootComponents.size());
		while (iterator.hasPrevious()) {
			GuiComponent component = iterator.previous();
			component.update();
		}
	}
	
	public static void render(SpriteBatch batch) {
		for (GuiComponent component : rootComponents) {
			if (component.isBeingDragged()) continue;
			component.render(batch, 0, 0);
		}
		
		flushRendering(batch);
	}
	
	public static void flushRendering(SpriteBatch batch) {
		shaderProgram.bind();
		batch.bind();
		
		projMatrix = Matrix4.ortho2D(0, App.gfx.getWndWidth(), 0, App.gfx.getWndHeight());
		
		shaderProgram.uniformMatrix4fv("u_proj", projMatrix);
		
		ShaderProgram prevDefaultProgram = batch.getDefaultProgram();
		batch.setDefaultProgram(shaderProgram);
		batch.render();
		batch.setDefaultProgram(prevDefaultProgram);
		
		batch.unbind();
		shaderProgram.unbind();
	}
	
	public static void unfocusOtherComponents(GuiComponent component) {
		rootComponents.forEach(c -> c.removeFocusRecursively(component));
	}
	
	public static void clear() {
		rootComponents.forEach(GuiComponent::processDestruction);
		rootComponents.clear();
	}
	
	public static void dispose() {
		shaderProgram.dispose();
	}
}
