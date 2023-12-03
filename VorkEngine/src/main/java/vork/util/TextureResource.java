package vork.util;

import java.io.IOException;

import vork.gfx.Texture;

public class TextureResource extends Resource<Texture> {

	public TextureResource(FilePath file, Texture data) {
		super(file, data);
	}

	@Override
	protected void handleReload(FilePath file, Texture texture) throws IOException {
		Texture newTexture = Texture.createFromPNGFile(file, texture.getFilter());
		texture.dispose();
		texture.exchange(newTexture);
	}
}
