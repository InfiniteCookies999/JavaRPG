package client.editor.tools;

import client.Constants;
import client.editor.Editor;
import client.game.world.Tile;
import client.game.world.World;
import vork.App;
import vork.gfx.Camera;
import vork.input.Buttons;
import vork.math.Vector2;

public class EraseTilesTool extends EditTool<Object> {

	public EraseTilesTool(Editor editor) {
		super(EditToolType.ERASE_TILES, editor);
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
		
		Vector2 cursorWorldCoords = camera.getCursorInWorldCoordinates();
		int curWorldTileX = (int) Math.floor(cursorWorldCoords.x / Constants.TILE_SIZE);
		int curWorldTileY = (int) Math.floor(cursorWorldCoords.y / Constants.TILE_SIZE);
		
		Tile tile = world.getTile(curWorldTileX, curWorldTileY);
		
		// Null tile means null chunk
		if (tile == null) {
			return;
		}
		
		int layerIdx = editor.rightPanel.getSelectedLayerIdx();
		tile.layersData[layerIdx] = null;
	}

	@Override
	public Object makeUndoObject() {
		return null;
	}

	@Override
	protected void undo(World world, Object undoObject) {
		// TODO Auto-generated method stub
		
	}
}
