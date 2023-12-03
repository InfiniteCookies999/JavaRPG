package client.game.items;

import client.game.Resources;
import lombok.Data;
import vork.gfx.Texture;
import vork.gfx.UvCoords;

@Data
public class ItemDefinition {
	private static final int ITEM_SIZE = 16;
	
	private int textureId;
	private ItemCatagory catagory;
	private String name;
	private String description;
	
	public static Texture getTexture(int textureId) {
		return Resources.getItemTexture(0);
	}
	
	public static UvCoords getUvCoords(int textureId) {
		 return UvCoords.createFromPixelSize(
					textureId*ITEM_SIZE,
					0,
					ITEM_SIZE, ITEM_SIZE,
					Resources.getItemTexture(0));
	}
}
