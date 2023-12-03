package client.game.world;

import java.util.ArrayList;
import java.util.List;

import client.Constants;
import vork.gfx.Camera;
import vork.gfx.SpriteBatch;

public class World {

	public List<Chunk> chunks = new ArrayList<>();
	
	public boolean[] visibleLayers = new boolean[RenderLayer.values().length];
	
	public World() {
		for (int i = 0; i < visibleLayers.length; i++) {
			visibleLayers[i] = true;
		}
	}
	
	public void addChunk(Chunk chunk) {
		chunk.world = this;
		chunks.add(chunk);
	}
	
	public void renderNonOverhead(Camera camera, SpriteBatch batch) {
		for (Chunk chunk : chunks) {
			chunk.renderNonOverhead(camera, batch);
		}
	}
	
	public void renderOverhead(Camera camera, SpriteBatch batch) {
		for (Chunk chunk : chunks) {
			chunk.renderOverhead(camera, batch);
		}
	}
	
	public Chunk getChunk(int chunkX, int chunkY) {
		return chunks.stream()
				     .filter(c -> c.chunkX == chunkX && c.chunkY == chunkY)
				     .findFirst()
				     .orElse(null);
	}
	
	public Chunk getChunkFromTileLocation(int worldTileX, int worldTileY) {
		int chunkX = worldTileX >> Constants.CHUNK_SIZE_LOG2;
		int chunkY = worldTileY >> Constants.CHUNK_SIZE_LOG2;
		return getChunk(chunkX, chunkY);
	}
	
	public Tile getTile(int worldTileX, int worldTileY) {
		Chunk chunk = getChunkFromTileLocation(worldTileX, worldTileY);
		if (chunk == null) return null;
		int localTileX = worldTileX - chunk.chunkX * Constants.CHUNK_SIZE;
		int localTileY = worldTileY - chunk.chunkY * Constants.CHUNK_SIZE;
		return chunk.getTile(localTileX, localTileY);
	}
	
	public boolean isTraversible(Location location) {
		return isTraversible(location.x, location.y);
	}
	
	public boolean isTraversible(int worldTileX, int worldTileY) {
		Tile tile = getTile(worldTileX, worldTileY);
		if (tile == null) return false;
		return tile.isTraversible;
	}
}
