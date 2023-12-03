package vork.util;

import java.io.IOException;

import vork.gfx.PaddedTexture;
import vork.gfx.Texture;

public class PaddedTextureResource extends Resource<PaddedTexture> {

	public PaddedTextureResource(FilePath file, PaddedTexture data) {
		super(file, data);
	}

	@Override
	protected void handleReload(FilePath file, PaddedTexture texture) throws IOException {
		PaddedTexture newTexture = Texture.createFromPNGFile(file, texture.getFilter(), texture.padWidth, texture.padHeight);
		texture.dispose();
		texture.exchange(newTexture);
		texture.originalWidth = newTexture.originalWidth;
		texture.originalHeight = newTexture.originalHeight;
	}
}
