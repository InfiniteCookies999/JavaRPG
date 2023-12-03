package client.game.items;

import client.Constants;
import client.game.Resources;
import client.game.world.Location;
import lombok.Getter;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.UvCoords;

public class GroundItem {

	@Getter
	private int id;
	
	@Getter
	private Location location;
	
	private Texture texture;
	private UvCoords uv;
	
	@Getter
	private int x, y;
	
	@Getter
	private int width, height;
	
	@Getter
	private ItemDefinition itemDefinition; 
	
	private static float scale = 0.8f;
	
	public GroundItem(int id, int tileX, int tileY, int itemId) {
		this.location = new Location(tileX, tileY);
		this.id = id;
		itemDefinition = Resources.getItemDefinition(itemId);
		texture = ItemDefinition.getTexture(itemDefinition.getTextureId());
		uv = ItemDefinition.getUvCoords(itemDefinition.getTextureId());
		width = (int) (16*scale);
		height = (int) (16*scale);
		x = tileX * Constants.TILE_SIZE + Constants.TILE_SIZE/2 - width/2;
		y = tileY * Constants.TILE_SIZE + Constants.TILE_SIZE/2 - height/2;
	}
	
	public void render(SpriteBatch batch) {
		batch.addQuad(
				x, y,
				width, height,
				Color.WHITE,
				uv,
				texture);
	}
}
