package client.editor.tools;

import client.editor.Editor;
import client.game.world.World;
import lombok.Getter;
import vork.gfx.Camera;

public abstract class EditTool <T> {

	@Getter
	private EditToolType type;
	
	protected Editor editor;
	
	@Getter
	private boolean acting = false;
	
	protected T currentUndoObject;
	
	public EditTool(EditToolType type, Editor editor) {
		this.type = type;
		this.editor = editor;
	}
	
	public abstract boolean canStart();
	
	public abstract boolean canEnd();
	
	public abstract void act(World world, Camera camera);
	
	public abstract T makeUndoObject();
	
	@SuppressWarnings("unchecked")
	public void unUndo(World world, Object o) {
		undo(world, (T) o);
	}
	
	protected abstract void undo(World world, T undoObject);
	
	public void start() {
		currentUndoObject = makeUndoObject();
		acting = true;
	}
	
	public void end() {
		acting = false;
		if (currentUndoObject != null) {
			editor.addUndoState(this, currentUndoObject);
		}
	}
}
