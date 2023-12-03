package client;

import vork.gfx.RenderContext;

public interface Screen {

	void init();
	
	void tick(RenderContext context);
	
	void dispose();
	
}
