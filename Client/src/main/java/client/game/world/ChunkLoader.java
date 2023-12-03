package client.game.world;

import java.io.BufferedReader;
import java.io.IOException;

import client.Constants;
import vork.util.FilePath;

public class ChunkLoader {
	
	private FilePath worldDirectory;
	
	public ChunkLoader(FilePath worldDirectory) {
		this.worldDirectory = worldDirectory;
	}
	
	public Chunk loadChunk(int chunkX, int chunkY) {
		
		FilePath chunkPath = worldDirectory.append("c_" + chunkX + "_" + chunkY);
		if (!chunkPath.exist()) {
			return null;
		}
		
		Chunk chunk = new Chunk(chunkX, chunkY);
		
		try (BufferedReader reader = chunkPath.getBufferedReader()) {
			
			String header = null;
			int layerIndex = 0;
			while ((header = reader.readLine()) != null) {
				if (header.equals("collision:")) {
					readCollision(reader, chunk);
				} else {
					readLayer(reader, chunk, layerIndex++);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// TODO: Want to do collision info as well.
		
		return chunk;
	}
	
	private void readLayer(BufferedReader reader, Chunk chunk, int layerIndex) throws IOException {
		for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
		
			String dataLine = reader.readLine();
			String[] tileInfoList = dataLine.split(" ");
			for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
				
				String tileInfo = tileInfoList[x];
				
				// Check for null tile.
				if (tileInfo.equals("-1")) {
					continue;
				}
				
				String[] infoPieces = tileInfo.split(",");
				int tileSheetIdx = Integer.parseInt(infoPieces[0]);
				int linearIndex = Integer.parseInt(infoPieces[1]);
				int flipBits = Integer.parseInt(infoPieces[2]);

				Tile tile = chunk.getTile(x, y);
				
				Tile.LayerRenderData layerData = new Tile.LayerRenderData();
				tile.layersData[layerIndex] = layerData;
				
				layerData.tileSheetIdx = tileSheetIdx;
				layerData.tileSheetIdxX = (linearIndex >> 16) & 0xFFFF;
				layerData.tileSheetIdxY = linearIndex & 0xFFFF;
				layerData.flipBits = flipBits;
				
				chunk.tiles[y*Constants.CHUNK_SIZE + x] = tile;
				
			}
		}
	}
	
	private void readCollision(BufferedReader reader, Chunk chunk) throws IOException {
		for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
			String dataLine = reader.readLine();
			for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
				boolean isTraversible = dataLine.charAt(x) == '0';
				Tile tile = chunk.getTile(x, y);
				tile.isTraversible = isTraversible;
			}	
		}
	}
}
