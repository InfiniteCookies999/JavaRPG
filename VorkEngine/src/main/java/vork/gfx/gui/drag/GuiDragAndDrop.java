package vork.gfx.gui.drag;

import java.util.ArrayList;
import java.util.List;

import vork.App;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiManager;
import vork.input.Buttons;

public class GuiDragAndDrop {
	
	private List<GuiDragSource> sources = new ArrayList<>();
	private List<GuiDragTarget> targets = new ArrayList<>();
	
	private GuiComponent draggedComponent;
	private GuiDragSource draggedFromSource;
	
	private int dragStartX, dragStartY;
	private int cursorStartX, cursorStartY;
	
	public void addSource(GuiDragSource source) {
		source.getContainer().setAllowClickedThroughChild(true);
		sources.add(source);
	}
	
	public void addTarget(GuiDragTarget target) {
		targets.add(target);
	}
	
	public void update() {
		if (draggedComponent == null) {
			tryBeginDrag();
		}
		if (draggedComponent != null) {
			handleDrag();
		}
	}
	
	public void cancelDrag() {
		if (draggedComponent == null) return;
		draggedFromSource.dragCanceled(draggedComponent);
		GuiManager.removeRootComponent(draggedComponent);
		draggedFromSource = null;
		draggedComponent = null;
	}
	
	private void tryBeginDrag() {
		for (GuiDragSource source : sources) {
			if (source.getContainer().isClicked() && App.input.isButtonJustPressed(Buttons.LEFT)) {
				draggedComponent = source.beginDrag(App.input.getCursorWndX(), App.input.getCursorWndY());
				if (draggedComponent != null) {
					GuiManager.addRootComponent(draggedComponent);
					draggedFromSource = source;
					dragStartX = draggedComponent.x;
					dragStartY = draggedComponent.y;
					cursorStartX = App.input.getCursorWndX();
					cursorStartY = App.input.getCursorWndY();
					break;	
				}
			}
		}
	}
	
	private void handleDrag() {
		if (!App.input.isButtonPressed(Buttons.LEFT)) {
			for (GuiDragTarget target : targets) {
				if (target.getContainer().isCursorHovering() &&
					target.getContainer() != draggedFromSource.getContainer()) {
					// Dragged onto a target!
					if (target.onDrop(draggedComponent)) {
						// Was a successful drop.
						GuiManager.removeRootComponent(draggedComponent);
						draggedFromSource = null;
						draggedComponent = null;
						return;	
					}
				}
			}
			// Did not find a target to drop into!
			cancelDrag();
		} else {
			int dx = App.input.getCursorWndX() - cursorStartX;
			int dy = App.input.getCursorWndY() - cursorStartY;
			draggedComponent.x = dragStartX + dx;
			draggedComponent.y = dragStartY + dy;
		}
	}
}
