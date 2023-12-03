package vork.gfx;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class RenderContext {
	
	private ShaderProgram program;
	private Batch batch;
	
	public void render() {
		program.bind();
		
		uploadUniforms(program);
		
		batch.bind();
		batch.render();
		batch.unbind();
		program.unbind();
	}
	
	public void render(Consumer<ShaderProgram> uniformUploader) {
		program.bind();
		
		uniformUploader.accept(program);
		
		batch.bind();
		batch.render();
		batch.unbind();
		program.unbind();
	}
	
	protected abstract void uploadUniforms(ShaderProgram program);
	
	public void dispose() {
		program.dispose();
		batch.dispose();
	}
}
