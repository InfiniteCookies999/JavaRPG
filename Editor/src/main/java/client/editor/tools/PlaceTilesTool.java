package client.editor.tools;

import java.util.HashMap;
import java.util.Map;

import client.Constants;
import client.editor.Editor;
import client.editor.tools.PlaceTilesTool.LayersData;
import client.game.world.Tile;
import client.game.world.World;
import lombok.AllArgsConstructor;
import vork.App;
import vork.gfx.Camera;
import vork.input.Buttons;
import vork.math.Vector2;

public class PlaceTilesTool extends EditTool<HashMap<Long, LayersData>> {
	
	@AllArgsConstructor
	class LayersData {
		private int layersIdx;
		private Tile.LayerRenderData data;
	}
	
	public PlaceTilesTool(Editor editor) {
		super(EditToolType.PLACE_TILES, editor);
	}

	@Override
	public boolean canStart() {
		return App.input.isButtonJustPressed(Buttons.LEFT);
	}

	@Override
	public boolean canEnd() {
		return !App.input.isButtonPressed(Buttons.LEFT);
	}

	@Override
	public void act(World world, Camera camera) {
		
		int sx = editor.rightPanel.getTileSelStartX();
		int sy = editor.rightPanel.getTileSelStartY();
		int ex = editor.rightPanel.getTileSelEndX();
		int ey = editor.rightPanel.getTileSelEndY();
		
		Vector2 cursorWorldCoords = camera.getCursorInWorldCoordinates();
		int curWorldTileX = (int) Math.floor(cursorWorldCoords.x / Constants.TILE_SIZE);
		int curWorldTileY = (int) Math.floor(cursorWorldCoords.y / Constants.TILE_SIZE);
		
		int worldStartX = curWorldTileX - (ex - sx)/2;
		int worldStartY = curWorldTileY - (ey - sy)/2;
		
		for (int y = sy; y <= ey; y++) {
			for (int x = sx; x <= ex; x++) {
				Tile tile = world.getTile(worldStartX + x - sx, worldStartY + y - sy);
				if (tile == null) {
					return;
				}
			}
		}
		
		int layersIdx = editor.rightPanel.getSelectedLayerIdx();
		int tileSheetIdx = editor.rightPanel.getSelectedTileSheetIdx();
		
		for (int y = sy; y <= ey; y++) {
			for (int x = sx; x <= ex; x++) {
				int worldTileX = worldStartX + x - sx;
				int worldTileY = worldStartY + y - sy;
				Tile tile = world.getTile(worldTileX, worldTileY);
				
				long index = (((long) worldTileX) << 32) | (worldTileY & 0xffffffffL);
				if (!currentUndoObject.containsKey(index)) {
					// Store the original so we can undo.
					Tile.LayerRenderData oldData = tile.layersData[layersIdx];
					currentUndoObject.put(index, new LayersData(layersIdx, oldData));
				}
				
				tile.layersData[layersIdx] = new Tile.LayerRenderData();
				Tile.LayerRenderData data = tile.layersData[layersIdx];
				
				data.tileSheetIdx = tileSheetIdx;
				data.tileSheetIdxX = x;
				data.tileSheetIdxY = y;	
			}
		}
	}

	@Override
	public HashMap<Long, LayersData> makeUndoObject() {
		return new HashMap<>();
	}

	@Override
	protected void undo(World world, HashMap<Long, LayersData> undoObject) {
		for (Map.Entry<Long, LayersData> entry : undoObject.entrySet()) {
			long index = entry.getKey();
			int worldTileX = (int)(index >> 32);
			int worldtileY = (int)(index);
			Tile tile = world.getTile(worldTileX, worldtileY);
			if (tile == null) {
				// It is possible that the user deleted the chunk.
				continue;
			}
			LayersData layerData = entry.getValue();
			tile.layersData[layerData.layersIdx] = layerData.data;
		}
	}
}
