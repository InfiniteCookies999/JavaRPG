package client;

import org.lwjgl.opengl.GL20;

import vork.App;
import vork.gfx.Camera;
import vork.gfx.RenderContext;

public class TestScreen implements Screen {

	Camera cam;
	
	@Override
	public void init() {
		cam = new Camera();
		cam.zoom = 3.0f;
	}

	@Override
	public void tick(RenderContext context) {
		App.gfx.clear(GL20.GL_COLOR_BUFFER_BIT);
		App.gfx.enableBlend(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		
		//Client.instance.getBigFont().render(0, 0, "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		Client.instance.getBigFont().render(0, 0, "{|}~");
		
		cam.update();
		context.render(program -> {
			program.uniformMatrix4fv("u_proj", cam.getProjMat());
		});
	}

	@Override
	public void dispose() {
		
	}
}
