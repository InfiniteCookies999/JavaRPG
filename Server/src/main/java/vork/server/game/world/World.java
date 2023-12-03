package vork.server.game.world;

import vork.server.Constants;
import vork.server.game.Location;

public class World {

	private static final int MIN_CHUNK_X = Constants.WORLD_MIN_CHUNK_X, MIN_CHUNK_Y = Constants.WORLD_MIN_CHUNK_Y;
	private static final int MAX_CHUNK_X = Constants.WORLD_MAX_CHUNK_X, MAX_CHUNK_Y = Constants.WORLD_MAX_CHUNK_Y;
	private static final int CHUNKS_WIDTH  = MAX_CHUNK_X - MIN_CHUNK_X;
	private static final int CHUNKS_HEIGHT = MAX_CHUNK_Y - MIN_CHUNK_Y;
	
	public Chunk[][] chunks = new Chunk[CHUNKS_WIDTH+1][CHUNKS_HEIGHT+1];
	
	public void loadChunks() {
		ChunkLoader loader = new ChunkLoader();
		for (int y = MIN_CHUNK_Y; y <= MAX_CHUNK_Y; y++) {
			for (int x = MIN_CHUNK_X; x <= MAX_CHUNK_X; x++) {
				int ux = x - MIN_CHUNK_X;
				int uy = y - MIN_CHUNK_Y;
				Chunk chunk = loader.loadChunk(x, y);
				if (chunk != null) {
					System.out.println("found chunk for: " + ux + "," + uy);
					chunks[ux][uy] = chunk;	
				}
			}	
		}
	}
	
	public Chunk getChunkFromTileLocation(Location location) {
		return getChunkFromTileLocation(location.x, location.y);
	}
	
	public Chunk getChunkFromTileLocation(int worldTileX, int worldTileY) {
		int chunkX = worldTileX >> Constants.CHUNK_SIZE_LOG2;
		int chunkY = worldTileY >> Constants.CHUNK_SIZE_LOG2;
		return getChunk(chunkX, chunkY);
	}
	
	public Chunk getChunk(int chunkX, int chunkY) {
		if (chunkX < MIN_CHUNK_X || chunkX > MAX_CHUNK_X) return null;
		if (chunkY < MIN_CHUNK_Y || chunkY > MAX_CHUNK_Y) return null;
		int uChunkX = chunkX - MIN_CHUNK_X;
		int uChunkY = chunkY - MIN_CHUNK_Y;
		return chunks[uChunkX][uChunkY];
	}
	
	public boolean isTraversible(Location location) {
		return isTraversible(location.x, location.y);
	}

	public boolean isTraversible(int worldTileX, int worldTileY) {
		Chunk chunk = getChunkFromTileLocation(worldTileX, worldTileY);
		if (chunk == null) {
			return false;
		}
		int localX = worldTileX - Constants.CHUNK_SIZE * chunk.chunkX;
		int localY = worldTileY - Constants.CHUNK_SIZE * chunk.chunkY;
		return chunk.collision[localY*Constants.CHUNK_SIZE + localX];
	}
	
}
