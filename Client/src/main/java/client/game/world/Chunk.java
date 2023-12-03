package client.game.world;

import client.Constants;
import vork.gfx.Camera;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;

public class Chunk {
	
	public Tile[] tiles = new Tile[Constants.CHUNK_SIZE*Constants.CHUNK_SIZE];
	
	public World world;
	public int chunkX, chunkY;
	
	public Chunk(int chunkX, int chunkY) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
			for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
				Tile tile = new Tile();
				tiles[y*Constants.CHUNK_SIZE + x] = tile;
				tile.worldX = chunkX*Constants.CHUNK_SIZE + x;
				tile.worldY = chunkY*Constants.CHUNK_SIZE + y;
			}	
		}
	}
	
	public Tile getTile(int localTileX, int localTileY) {
		return tiles[localTileY * Constants.CHUNK_SIZE + localTileX];
	}
	
	public void renderNonOverhead(Camera camera, SpriteBatch batch) {
		for (Tile tile : tiles) {
			if (camera.isInView(
					tile.worldX*Constants.TILE_SIZE, tile.worldY*Constants.TILE_SIZE,
					Constants.TILE_SIZE, Constants.TILE_SIZE)) {
				for (int layerIdx = 0; layerIdx < RenderLayer.OVERHEAD1.ordinal(); layerIdx++) {
					renderTile(tile, batch, layerIdx);	
				}
			}
		}
	}
	
	public void renderOverhead(Camera camera, SpriteBatch batch) {
		for (Tile tile : tiles) {
			if (camera.isInView(
					tile.worldX*Constants.TILE_SIZE, tile.worldY*Constants.TILE_SIZE,
					Constants.TILE_SIZE, Constants.TILE_SIZE)) {
				for (int layerIdx = RenderLayer.OVERHEAD1.ordinal(); layerIdx < RenderLayer.values().length; layerIdx++) {
					renderTile(tile, batch, layerIdx);
				}
			}
		}
	}
	
	private void renderTile(Tile tile, SpriteBatch batch, int layerIdx) {
		final Tile.LayerRenderData renderData = tile.layersData[layerIdx];
		if (renderData == null) return;
		renderData.render(batch, tile.worldX, tile.worldY,
				world.visibleLayers[layerIdx] ? Color.WHITE : new Color(Color.WHITE, 0.3f));
	}
}
