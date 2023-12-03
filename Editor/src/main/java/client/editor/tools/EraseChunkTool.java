package client.editor.tools;

import client.Constants;
import client.editor.Editor;
import client.editor.EraseChunkWarningMenu;
import client.game.world.Chunk;
import client.game.world.World;
import vork.App;
import vork.gfx.Camera;
import vork.gfx.gui.GuiComponent;
import vork.input.Buttons;
import vork.math.Vector2;

public class EraseChunkTool extends EditTool<Object> {

	private boolean optionSelected = false;
	private Chunk selectedChunk;
	
	public EraseChunkTool(Editor editor) {
		super(EditToolType.ERASE_CHUNK, editor);
	}

	@Override
	public boolean canStart() {
		return App.input.isButtonJustPressed(Buttons.LEFT);
	}

	@Override
	public boolean canEnd() {
		return optionSelected;
	}
	
	@Override
	public void act(World world, Camera camera) {
		EraseChunkWarningMenu menu = editor.eraseChunkWarningMenu;
		if (menu.isHidden()) {
			Vector2 cursorWorldCoords = camera.getCursorInWorldCoordinates();
			int chunkX = ((int) Math.floor(cursorWorldCoords.x / Constants.TILE_SIZE)) >> Constants.CHUNK_SIZE_LOG2;
			int chunkY = ((int) Math.floor(cursorWorldCoords.y / Constants.TILE_SIZE)) >> Constants.CHUNK_SIZE_LOG2;
			
			Chunk chunk = world.getChunk(chunkX, chunkY);
			if (chunk == null) {
				optionSelected = true;
				return; // Chuk doesnt exist
			}
			
			optionSelected = false;
			editor.eraseChunkWarningMenu.setHidden(false);
			selectedChunk = chunk;	
		} else {
			GuiComponent confirmButton = menu.findChildByName("confirm-btn");
			GuiComponent cancelButton = menu.findChildByName("cancel-btn");
			if (confirmButton.isClicked() &&
				App.input.isButtonJustPressed(Buttons.LEFT)) {
				// TODO: delete chunk off file
				world.chunks.remove(selectedChunk);
				menu.setHidden(true);
				optionSelected = true;
			} else if (cancelButton.isClicked() &&
					App.input.isButtonJustPressed(Buttons.LEFT)
					) {
				menu.setHidden(true);
				optionSelected = true;
			}
		}
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
