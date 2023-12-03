package client.editor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import client.Constants;
import client.game.world.Chunk;
import client.game.world.RenderLayer;
import client.game.world.Tile;
import vork.util.FilePath;

public class ChunkSaver {
	public static void save(FilePath directory, Chunk chunk) throws IOException {
		FilePath file = directory.append("c_" + chunk.chunkX + "_" + chunk.chunkY);
		
		RenderLayer[] renderLayers = RenderLayer.values();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.getSystemFile()))) {
			final char SEPERATOR = ' ';
			
			for (int layerIdx = 0; layerIdx < renderLayers.length; layerIdx++) {
				writer.write(renderLayers[layerIdx].toString().toLowerCase() + "|" + layerIdx + ":\n");
				for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
					for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
						Tile tile = chunk.getTile(x, y);
						Tile.LayerRenderData data = tile.layersData[layerIdx];
						if (data == null) {
							writer.write("-1" + SEPERATOR);
							continue;
						}
						
						int linearIndex = (data.tileSheetIdxX << 16) | data.tileSheetIdxY;
						writer.write(data.tileSheetIdx + ",");
						writer.write(linearIndex + ",");
						writer.write(String.valueOf((int) data.flipBits));
						writer.write(SEPERATOR);
					}
					writer.write("\n");
				}
			}
			
			writer.write("collision:\n");
			for (int y = 0; y < Constants.CHUNK_SIZE; y++) {
				for (int x = 0; x < Constants.CHUNK_SIZE; x++) {
					Tile tile = chunk.getTile(x, y);
					writer.write(tile.isTraversible ? "0" : "1");
				}
				writer.write("\n");
			}
		}
	}
}
