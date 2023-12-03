package client.editor.tools;

import client.Constants;
import client.editor.Editor;
import client.game.world.Chunk;
import client.game.world.World;
import vork.App;
import vork.gfx.Camera;
import vork.input.Buttons;
import vork.math.Vector2;

public class CreateChunkTool extends EditTool<Object> {

	public CreateChunkTool(Editor editor) {
		super(EditToolType.CREATE_CHUNK, editor);
	}

	@Override
	public boolean canStart() {
		return App.input.isButtonJustPressed(Buttons.LEFT);
	}

	@Override
	public boolean canEnd() {
		return true;
	}

	@Override
	public void act(World world, Camera camera) {
		
		Vector2 cursorWorldCoords = camera.getCursorInWorldCoordinates();
		int chunkX = ((int) Math.floor(cursorWorldCoords.x / Constants.TILE_SIZE)) >> Constants.CHUNK_SIZE_LOG2;
		int chunkY = ((int) Math.floor(cursorWorldCoords.y / Constants.TILE_SIZE)) >> Constants.CHUNK_SIZE_LOG2;
		
		Chunk chunk = world.getChunk(chunkX, chunkY);
		if (chunk != null) {
			// Chunk already exist.
			return;
		}
		
		Chunk newChunk = new Chunk(chunkX, chunkY);
		world.addChunk(newChunk);
		
	}

	@Override
	public Object makeUndoObject() {
		return null;
	}

	@Override
	protected void undo(World world, Object undoObject) {
		
	}
}
