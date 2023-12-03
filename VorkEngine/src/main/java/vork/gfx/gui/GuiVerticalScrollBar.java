package vork.gfx.gui;

import lombok.Getter;
import lombok.Setter;
import vork.App;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.input.Buttons;
import vork.math.VorkMath;

public class GuiVerticalScrollBar extends GuiComponent {

	public static class DragBar extends GuiComponent {
		
		private boolean isBeingDragged = false;
		private int startCursorY;
		private float scrollPercentStart;
		
		@Setter
		private int offsetX, offsetY;
		
		private GuiVerticalScrollBar scrollBar;
		
		public DragBar(Texture texture) {
			super(texture);
			setRenderMode(GuiRenderMode.VERTICAL_3PATH);
		}
		
		public DragBar(int relX, int relY, Texture texture) {
			super(relX, relY, texture);
			setRenderMode(GuiRenderMode.VERTICAL_3PATH);
		}
		
		public DragBar(int relX, int relY) {
			super(relX, relY, 0, 0);
			setRenderMode(GuiRenderMode.VERTICAL_3PATH);
		}
		
		public DragBar(GuiLocation location) {
			super(location, 0, 0);
			setRenderMode(GuiRenderMode.VERTICAL_3PATH);
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
				startCursorY = App.input.getCursorWndY();
				scrollPercentStart = scrollBar.scrollPercent;
			}
			
			if (!App.input.isButtonPressed(Buttons.LEFT)) {
				isBeingDragged = false;
			}
			
			if (isBeingDragged && scrollBar.amountNotCovered > 0) {
				int yDragDiff = App.input.getCursorWndY() - startCursorY;
				float percentageDragged = Math.abs(yDragDiff) / scrollBar.amountNotCovered;
				
				if (yDragDiff >= 0) {
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
	private int additionalViewAreaHeightBounding;
	
	private float amountNotCovered;
	
	public GuiVerticalScrollBar(Texture texture, DragBar dragBar) {
		super(texture);
		this.dragBar = dragBar;
		init();
	}
	
	public GuiVerticalScrollBar(int relX, int relY, Texture texture, DragBar dragBar) {
		super(relX, relY, texture);
		this.dragBar = dragBar;
		init();
	}
	
	public GuiVerticalScrollBar(int width, int height, DragBar dragBar) {
		super(width, height);
		this.dragBar = dragBar;
		init();
	}
	
	public GuiVerticalScrollBar(int relX, int relY, int width, int height, DragBar dragBar) {
		super(relX, relY, width, height);
		this.dragBar = dragBar;
		init();
	}
	
	public GuiVerticalScrollBar(GuiLocation location, int width, int height, DragBar dragBar) {
		super(location, width, height);
		this.dragBar = dragBar;
		init();
	}
	
	private void init() {
		setRenderMode(GuiRenderMode.VERTICAL_3PATH);
		dragBar.scrollBar = this;
		addChild(dragBar);
	}
	
	public void scrollWheelUp() {
		scrollPercent += 5.0f;
		scrollPercent = VorkMath.clamp(scrollPercent, 0f, 100f);
	}
	
	public void scrollWheelDown() {
		scrollPercent -= 5.0f;
		scrollPercent = VorkMath.clamp(scrollPercent, 0f, 100f);
	}
	
	public void setScrollPercent(float percent) {
		if (percent < 0 || percent > 100.0f) {
			throw new IllegalArgumentException("Percent must be between 0 and 100");
		}
		scrollPercent = percent;
	}
	
	@Override
	public void update() {
		super.update();
		if (isHidden() || isDisabled()) return;
		
		if (isCursorHovering()) {
			if (App.input.getYScroll() > 0) {
				scrollWheelUp();
			} else if (App.input.getYScroll() < 0) {
				scrollWheelDown();
			}
		}
		
		scrollPercent = VorkMath.clamp(scrollPercent, 0f, 100f);
	}
	
	@Override
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		
		if (viewArea == null) {
			throw new IllegalStateException("Must set the view area for GuiVerticalScrollBar");
		} else {
			float heightScale = this.height / (float) viewArea.height;
			if (heightScale > 1.0f) heightScale = 1.0f;
			
			dragBar.height = (int) (this.height * heightScale - dragBar.offsetY * 2);
			dragBar.x = dragBar.offsetX;
			dragBar.width = this.width - dragBar.offsetX*2;
			
			amountNotCovered = this.height - (this.height * heightScale);
			int addedHeightOffset = (int) (scrollPercent/100.0f * amountNotCovered);
			dragBar.y = dragBar.offsetY + addedHeightOffset;
			
			int areaMove = (int) ((viewArea.height - this.height) * scrollPercent/100.0f);
			
			viewArea.bounding.offsetY = areaMove;
			viewArea.bounding.height = this.height + additionalViewAreaHeightBounding;
			viewArea.setScrollY(-areaMove);
			
		}
		
		super.render(batch, parentRX, parentRY);
	}
}
