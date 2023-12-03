package client.game.entity;

import client.game.Resources;
import lombok.Setter;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.UvCoords;

public class BasicMobRenderer extends EntityRenderer {

	private int tick = 0;
	@Setter
	private int textureId;
	private Texture texture;
	private int dirIdx = 0;
	
	@Override
	public void setup() {
		texture = Resources.getMobTexture(textureId);
	}
	
	@Override
	public void render(SpriteBatch batch, Entity entity) {
	
		int xIdx = (tick++ % 40) / 15;
		
		if (entity.getFacingDirection() == Direction.WEST) {
			dirIdx = 0;
		} else if (entity.getFacingDirection() == Direction.EAST) {
			dirIdx = 1;
		}
		
		UvCoords uv = UvCoords.createFromPixelSize(
				xIdx*(texture.getWidth()/2), 0,
				texture.getWidth()/2, texture.getHeight(), texture);
		if (dirIdx == 1) {
			uv.flipOnX();
		}
		
		batch.addQuad(
				entity.x, entity.y,
				texture.getWidth()/2, texture.getHeight(),
				Color.WHITE,
				uv,
				texture,
				Resources.getShaderProgram(shaderProgramType)
				);
		uploadShaderProgramInfo(batch, entity);
	}

	@Override
	public int getEntityWidth() {
		return texture.getWidth()/2;
	}

	@Override
	public int getEntityHeight() {
		return texture.getHeight();
	}

	@Override
	public void dispose() {
		
	}
}
