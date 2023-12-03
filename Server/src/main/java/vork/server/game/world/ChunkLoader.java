package vork.server.game.world;

import java.io.BufferedReader;
import java.io.IOException;

import vork.server.Constants;
import vork.server.FilePath;

public class ChunkLoader {
	
	public Chunk loadChunk(int chunkX, int chunkY) {
		
		FilePath chunkPath = FilePath.internal("world/c_" + chunkX + "_" + chunkY);
		if (!chunkPath.exist()) {
			return null;
		}
		
		Chunk chunk = new Chunk(chunkX, chunkY);
		
		try (BufferedReader reader = chunkPath.getBufferedReader()) {
			
			String header = null;
			while ((header = reader.readLine()) != null) {
				if (header.equals("collision:")) {
					readCollision(reader, chunk);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return chunk;
	}
	
	private void readCollision(BufferedReader reader, Chunk chunk) throws IOException {
		for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
			String dataLine = reader.readLine();
			for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
				boolean isTraversible = dataLine.charAt(x) == '0';
				chunk.collision[y*Constants.CHUNK_SIZE + x] = isTraversible;
			}	
		}
	}
}
