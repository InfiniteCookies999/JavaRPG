package vork.gfx.gui.drag;

import lombok.Getter;
import vork.gfx.gui.GuiComponent;

public abstract class GuiDragSource {
	
	@Getter
	private GuiComponent container;
	
	public GuiDragSource(GuiComponent container) {
		this.container = container;
	}
	
	public abstract GuiComponent beginDrag(int cursorX, int cursorY);
	
	public abstract void dragCanceled(GuiComponent draggedComponent);
	
}
