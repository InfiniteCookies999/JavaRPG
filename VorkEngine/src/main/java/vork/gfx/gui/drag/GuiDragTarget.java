package vork.gfx.gui.drag;

import lombok.Getter;
import vork.gfx.gui.GuiComponent;

public abstract class GuiDragTarget {
	
	@Getter
	private GuiComponent container;
	
	public GuiDragTarget(GuiComponent container) {
		this.container = container;
	}
	
	public abstract boolean onDrop(GuiComponent draggedComponent);
	
}
