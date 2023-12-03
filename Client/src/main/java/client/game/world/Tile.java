package client.game.world;

import client.Constants;
import client.game.Resources;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.UvCoords;

public class Tile {

	public int worldX, worldY;
	public boolean isTraversible;

	public static class LayerRenderData {
		public int tileSheetIdxX, tileSheetIdxY;
		public int tileSheetAniEndIdxX = -1, aniTickRate;
		public int tileSheetIdx = -1;
		public int flipBits;
		
		public void render(SpriteBatch batch, int worldTileX, int worldTileY, Color color) {
			if (tileSheetIdx == -1) return;
			
			Texture tileSheet = Resources.getTileSheet(tileSheetIdx);
			
			UvCoords coords = UvCoords.createFromPixelSize(
					(Constants.TILE_SIZE+2) * tileSheetIdxX+1,
					(Constants.TILE_SIZE+2) * tileSheetIdxY+1,
					Constants.TILE_SIZE,
					Constants.TILE_SIZE,
					tileSheet);
			
			batch.addQuad(
					worldTileX*Constants.TILE_SIZE,
					worldTileY*Constants.TILE_SIZE,
					Constants.TILE_SIZE,
					Constants.TILE_SIZE,
					color,
					coords,
					tileSheet);
		}
	}
	
	public LayerRenderData[] layersData = new LayerRenderData[RenderLayer.values().length];

}
