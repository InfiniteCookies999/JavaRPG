package vork.gfx.gui;

import lombok.Getter;
import lombok.Setter;
import vork.App;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.input.Buttons;
import vork.math.VorkMath;

public class GuiHorizontalScrollBar extends GuiComponent {

	public static class DragBar extends GuiComponent {
		
		private boolean isBeingDragged = false;
		private int startCursorX;
		private float scrollPercentStart;
		
		@Setter
		private int offsetX, offsetY;
		
		private GuiHorizontalScrollBar scrollBar;
		
		public DragBar(Texture texture) {
			super(texture);
			setRenderMode(GuiRenderMode.HORIZONTAL_3PATCH);
		}
		
		public DragBar(int relX, int relY, Texture texture) {
			super(relX, relY, texture);
			setRenderMode(GuiRenderMode.HORIZONTAL_3PATCH);
		}
		
		public DragBar(int relX, int relY) {
			super(relX, relY, 0, 0);
			setRenderMode(GuiRenderMode.HORIZONTAL_3PATCH);
		}
		
		public DragBar(GuiLocation location) {
			super(location, 0, 0);
			setRenderMode(GuiRenderMode.HORIZONTAL_3PATCH);
		}
		
		public void setOffset(int offsetX, int offsetY) {
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}
		
		@Override
		public void update() {
			super.update();
			if (isHidden() || isDisabled()) return;
		
			if (isClicked() && App.input.isButtonJustPressed(Buttons.LEFT)) {
				isBeingDragged = true;
				startCursorX = App.input.getCursorWndX();
				scrollPercentStart = scrollBar.scrollPercent;
			}
			
			if (!App.input.isButtonPressed(Buttons.LEFT)) {
				isBeingDragged = false;
			}
			
			if (isBeingDragged && scrollBar.amountNotCovered > 0) {
				int xDragDiff = App.input.getCursorWndX() - startCursorX;
				float percentageDragged = Math.abs(xDragDiff) / scrollBar.amountNotCovered;
			
				if (xDragDiff >= 0) {
					scrollBar.scrollPercent = scrollPercentStart + percentageDragged*100.0f;
				} else {
					scrollBar.scrollPercent = scrollPercentStart - percentageDragged*100.0f;
				}
			}
			
			scrollBar.scrollPercent = VorkMath.clamp(scrollBar.scrollPercent, 0f, 100f);
		}
	}
	
	private DragBar dragBar;
	
	/**
	 *  The area that gets shown depending on the scroll amount. */
	@Setter @Getter
	private GuiComponent viewArea;
	
	/** The amount scrolled by the bar. 0.0F means the
     * bar is at the bottom of the scrolling container. */
	@Getter
	private float scrollPercent;
	
	@Setter
	private int additionalViewAreaWidthBounding;
	
	private float amountNotCovered;
	
	public GuiHorizontalScrollBar(Texture texture, DragBar dragBar) {
		super(texture);
		this.dragBar = dragBar;
		init();
	}
	
	public GuiHorizontalScrollBar(int relX, int relY, Texture texture, DragBar dragBar) {
		super(relX, relY, texture);
		this.dragBar = dragBar;
		init();
	}
	
	public GuiHorizontalScrollBar(int width, int height, DragBar dragBar) {
		super(width, height);
		this.dragBar = dragBar;
		init();
	}
	
	public GuiHorizontalScrollBar(int relX, int relY, int width, int height, DragBar dragBar) {
		super(relX, relY, width, height);
		this.dragBar = dragBar;
		init();
	}
	
	public GuiHorizontalScrollBar(GuiLocation location, int width, int height, DragBar dragBar) {
		super(location, width, height);
		this.dragBar = dragBar;
		init();
	}
	
	private void init() {
		setRenderMode(GuiRenderMode.VERTICAL_3PATH);
		dragBar.scrollBar = this;
		addChild(dragBar);
	}
	
	public void setScrollPercent(float percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent must be between 0 and 100");
		}
		scrollPercent = percent;
	}
	
	@Override
	public void update() {
		super.update();
		if (isHidden() || isDisabled()) return;
		
		scrollPercent = VorkMath.clamp(scrollPercent, 0f, 100f);
	}
	
	@Override
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		if (viewArea == null) {
			throw new IllegalStateException("Must set the view area for GuiVerticalScrollBar");
		} else {
			float widthScale = this.width / (float) viewArea.width;
			if (widthScale > 1.0) widthScale = 1.0f;
			
			dragBar.width = (int) (this.width*widthScale - (dragBar.offsetX * 2));
			dragBar.y = dragBar.offsetY;
			dragBar.height = this.height - dragBar.offsetY*2;
		
			amountNotCovered = this.width - (this.width * widthScale);
			int addedWidthOffset = (int) (scrollPercent/100.0f * amountNotCovered);
			dragBar.x = dragBar.offsetX + addedWidthOffset;
			
			int areaMove = (int) ((viewArea.width - this.width) * scrollPercent/100.0f);
			
			viewArea.bounding.offsetX = areaMove;
			viewArea.bounding.width = this.width + additionalViewAreaWidthBounding;
			viewArea.setScrollX(-areaMove);
		}
		
		super.render(batch, parentRX, parentRY);
	}
}
