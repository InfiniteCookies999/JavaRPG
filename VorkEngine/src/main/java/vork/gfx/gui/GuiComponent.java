package vork.gfx.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import lombok.Getter;
import lombok.Setter;
import vork.App;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.UvCoords;
import vork.gfx.VkFont;
import vork.gfx.VkFont.TextLocation;
import vork.input.Buttons;
import vork.util.Collision;

public class GuiComponent {

	@Getter
	private GuiComponent parent;
	private List<GuiComponent> children = new ArrayList<>();
	
	public int x, y;
	public int width, height;
	
	private float widthPercent = -1, heightPercent = -1;
	
	protected int outX, outY;
	protected int outWidth, outHeight;
	
	public boolean flipX, flipY;
	
	@Getter @Setter
	private GuiRenderMode renderMode = GuiRenderMode.DEFAULT;
	@Getter @Setter
	private int renderModeRepeats = 0;
	
	@Setter @Getter
	private GuiLocation location = GuiLocation.RELATIVE;
	
	@Setter @Getter
	private Color color = Color.WHITE;
	
	@Setter @Getter
	private Color hoverColor = null;
	
	private Color outColor;
	
	@Setter @Getter
	private Texture texture = App.gfx.emptyTexture;
	
	// Only applies when default rendering
	@Setter @Getter
	private UvCoords uv = UvCoords.FULL;
	
	@Setter
	private boolean hidden;
	
	@Setter
	private boolean disabled;
	
	protected boolean focused = false;
	
	@Setter @Getter
	private boolean allowClickedThroughChild;
	
	@Setter @Getter
	private GuiChildDisplayMode childDisplayMode = GuiChildDisplayMode.NONE;
	private int childPadX, childPadY;
	
	// states
	private boolean stateClickedOnChild;
	private boolean stateClicked;
	private boolean stateHovering;
	
	private Runnable[] callbacks = new Runnable[GuiCallbackType.values().length];
	
	public class Bounding {
		public int offsetX = -1, offsetY = -1, width = -1, height = -1;
	}
	public Bounding bounding = new Bounding();
	
	private int outBoundX1, outBoundY1, outBoundX2, outBoundY2;
	
	@Setter @Getter
	private int scrollX, scrollY;
	
	@Setter @Getter
	private String name = "";
	
	@Setter @Getter
	private boolean beingDragged;
	
	public GuiComponent(Texture texture) {
		this.texture = texture;
		this.width = texture.getWidth();
		this.height = texture.getHeight();
	}
	
	public GuiComponent(int x, int y, Texture texture) {
		this.texture = texture;
		this.width = texture.getWidth();
		this.height = texture.getHeight();
		this.x = x;
		this.y = y;
	}
	
	public GuiComponent(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public GuiComponent(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public GuiComponent(GuiLocation location, int width, int height) {
		this.location = location;
		this.width = width;
		this.height = height;
	}
	
	public void addCallback(GuiCallbackType type, Runnable callback) {
		callbacks[type.ordinal()] = callback;
	}
	
	public void giveFocus() {
		focused = true;
		GuiManager.unfocusOtherComponents(this);
	}
	
	public boolean isHidden() {
		if (hidden) return true;
		if (parent != null && parent.isHidden()) return true;
		return false;
	}
	
	public boolean isDisabled() {
		if (disabled) return true;
		if (parent != null && parent.isDisabled()) return true;
		return false;
	}
	
	public void removeFocusRecursively(GuiComponent component) {
		if (this != component) {
			removeFocus();
		}
		for (GuiComponent child : children) {
			child.removeFocusRecursively(component);
		}
	}
	
	public void clearChildren() {
		Iterator<GuiComponent> itr = children.iterator();
		while (itr.hasNext()) {
			GuiComponent child = itr.next();
			child.processDestruction();
			itr.remove();
		}
	}
	
	public void removeChild(GuiComponent child) {
		child.parent = null;
		children.remove(child);
	}
	
	public void removeFromParent() {
		if (parent != null) {
			parent.removeChild(this);
		}
	}
	
	public Iterator<GuiComponent> getChildrenIterator() {
		return children.iterator();
	}
	
	public int getNumberOfChildren() {
		return children.size();
	}
	
	public GuiComponent getChild(int index) {
		return children.get(index);
	}
	
	public GuiComponent findChildByName(String name) {
		for (GuiComponent child : children) {
			if (child.name.equals(name)) {
				return child;
			}
		}
		for (GuiComponent child : children) {
			GuiComponent childChild = child.findChildByName(name);
			if (childChild != null) {
				return childChild;
			}
		}
		return null;
	}
	
	public boolean hasFocus() {
		return focused;
	}
	
	public void setWidthPercent(float percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent must be between 0 and 100");
		}
		this.widthPercent = percent;
	}
	
	public void setHeightPercent(float percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent must be between 0 and 100");
		}
		this.heightPercent = percent;
	}
	
	public boolean hasBounding() {
		return bounding.offsetX != -1 || bounding.offsetY != -1 ||
				bounding.width != -1 || bounding.height != -1 ||
				parent != null && parent.hasBounding();
	}
	
	public void setChildDisplayMode(GuiChildDisplayMode mode, int childPadX, int childPadY) {
		this.childDisplayMode = mode;
		this.childPadX = childPadX;
		this.childPadY = childPadY;
	}
	
	public void removeFocus() {
		focused = false;
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public boolean isCursorHovering() {
		return stateHovering;
	}
	
	public boolean isClicked() {
		return stateClicked;
	}
	
	public void update() {
		
		boolean wasHovering = stateHovering;
		
		// resetting states
		stateClickedOnChild = false;
		stateClicked = false;
		stateHovering = false;
		
		// Iterating in a reverse order since things that render
		// last have higher update priority than things rendered
		// first.
		ListIterator<GuiComponent> childrenItr = children.listIterator(children.size());
		while (childrenItr.hasPrevious()) {
			GuiComponent child = childrenItr.previous();
			child.update();
			if (child.stateClicked || child.stateClickedOnChild) {
				stateClickedOnChild = true;
			}
		}
		
		if (isDisabled() || isHidden()) return;
		
		// The reason for the epislon is so that the cursor doesn't
		// think its touching two different componenets at the same
		// time
		final float epsilon = 0.0001f;
		stateHovering = Collision.pointToRect(
				App.input.getCursorWndX(), App.input.getCursorWndY(),
				outX, outY, outWidth - epsilon, outHeight - epsilon
				);
		
		if (hasBounding()) {
			int boundWidth  = outBoundX2 - outBoundX1;
			int boundHeight = outBoundY2 - outBoundY1;
			if (!Collision.pointToRect(
					App.input.getCursorWndX(), App.input.getCursorWndY(),
					outBoundX1, outBoundY1, boundWidth, boundHeight
					)) {
				stateHovering = false;
			}
		}
		
		if (stateHovering) {
			
			GuiManager.setCursorHovering(true);
			
			stateClicked = App.input.isAnyButtonJustPressed();
			if (stateClickedOnChild && !allowClickedThroughChild) {
				stateClicked = false;
			}
			
			// If an component was clicked other than one of the children
			// before this component was clicked then it the component
			// that was clicked must be positioned above this component and
			// discards this component from being clicked.
			if (GuiManager.isCursorClicked()) {
				if (!(stateClickedOnChild && allowClickedThroughChild)) {
					stateClicked = false;
				}
			}
			
			if (stateClicked) {
				GuiManager.setCursorClicked(true);
				
				giveFocus();
				
				if (App.input.isButtonJustPressed(Buttons.LEFT))
					invokeCallback(GuiCallbackType.LEFT_CLICK);
				if (App.input.isButtonJustPressed(Buttons.RIGHT))
					invokeCallback(GuiCallbackType.RIGHT_CLICK);
				if (App.input.isButtonJustPressed(Buttons.MIDDLE))
					invokeCallback(GuiCallbackType.MIDDLE_CLICK);
			}
			
			if (!wasHovering) {
				invokeCallback(GuiCallbackType.HOVER_ENTER);
			}
		} else {
			if (wasHovering) {
				invokeCallback(GuiCallbackType.HOVER_EXIT);
			}
		}
	}
	
	protected void invokeCallback(GuiCallbackType type) {
		Runnable callback = callbacks[type.ordinal()];
		if (callback != null) {
			callback.run();
		}
	}
	
	public void addChild(GuiComponent child) {
		if (child.parent != null) {
			throw new IllegalArgumentException("Tried adding a child but the child already has a parent");
		}
		if (child == this) {
			throw new IllegalArgumentException("Cannot add itself as a child");
		}
		
		if (childDisplayMode == GuiChildDisplayMode.BLOCK_DOWN) {
			switch (child.location) {
			case RELATIVE:
			case LEFT:
			case TOP_LEFT:
				child.location = GuiLocation.TOP;
				break;
			case RIGHT:
				child.location = GuiLocation.TOP_RIGHT;
				break;
			case TOP:
			case TOP_RIGHT:
				break;
			case BOTTOM:
			case BOTTOM_LEFT:
			case BOTTOM_RIGHT:
			case CENTER_BOTTOM:
				throw new RuntimeException("Child should not be placed at bottom when in a block down parent");
			case CENTER:
			case CENTER_LEFT:
			case CENTER_TOP:
			case CENTER_HORIZONTAL:
				child.location = GuiLocation.CENTER_TOP;
				break;
			case CENTER_RIGHT:
				throw new RuntimeException("child should not be placed in the center right when in a block down parent");
			case CENTER_VERTICAL:
				throw new RuntimeException("child should not be placed in the center vertical when in a block down parent");
			}
		}
		
		child.parent = this;
		children.add(child);
	}
	
	public void destroy() {
		if (parent != null) {
			parent.children.remove(this);
			processDestruction();
		} else {
			GuiManager.removeRootComponent(this);
		}
	}
	
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		if (hidden) return;
	
		calculateOutSize();
		calculateOutPosition(parentRX, parentRY);
		calculateOutColor();
		calculateOutBounding();
		
		switch (renderMode) {
		case DEFAULT:
			renderDefault(batch);
			break;
		case NINE_PATCH:
			renderNinePatch(batch);
			break;
		case HORIZONTAL_3PATCH:
			renderHorizontal3Patch(batch);
			break;
		case VERTICAL_3PATH:
			renderVertical3Patch(batch);
			break;
		}
		
		int accW = childPadX, accH = childPadY;
		for (GuiComponent child : children) {
			if (child.isBeingDragged()) continue;
			switch (childDisplayMode) {
			case NONE:
				child.render(batch, 0, 0);
				break;
			case BLOCK_DOWN:
				child.render(batch, childPadX, -accH);
				break;
			case BLOCK_UP:
				child.render(batch, childPadX, accH);
				break;
			case BLOCK_HORIZONTAL:
				child.render(batch, accW, childPadY);
				break;
			}
			accH += child.outHeight + childPadY;
			accW += child.outWidth + childPadX;
		}
	}
	
	private void renderDefault(SpriteBatch batch) {
		UvCoords outUv = new UvCoords(uv);
		if (flipX) {
			outUv.flipOnX();
		}
		if (flipY) {
			outUv.flipOnY();
		}
		renderQuad(batch, outX, outY, outWidth, outHeight, outUv);
	}
	
	private void renderNinePatch(SpriteBatch batch) {
		final int cxs = texture.getWidth() / 3;
		final int cys = texture.getHeight() / 3;
		// corners
		renderQuad(batch, outX, outY, cxs, cys, new UvCoords(0, 0, 1/3f, 1/3f)); // bottom left
		renderQuad(batch, outX+outWidth-cxs, outY, cxs, cys, new UvCoords(2/3f, 0, 1, 1/3f)); // bottom right
		renderQuad(batch, outX, outY+outHeight-cys, cxs, cys, new UvCoords(0, 2/3f, 1/3f, 1)); // top left
		renderQuad(batch, outX+outWidth-cxs, outY+outHeight-cys, cxs, cys, new UvCoords(2/3f, 2/3f, 1, 1)); // top right
		// bottom
		if ((renderModeRepeats & RenderModeRepeats.BOTTOM) != 0) {
			for (int i = 0; i+cxs <= outWidth-2*cxs; i += cxs)
				renderQuad(batch, outX+cxs+i, outY, cxs, cys, new UvCoords(1/3f, 0, 2/3f, 1/3f));
		} else {
			renderQuad(batch, outX+cxs, outY, outWidth-2*cxs, cys, new UvCoords(1/3f, 0, 2/3f, 1/3f));
		}
		// left
		if ((renderModeRepeats & RenderModeRepeats.LEFT) != 0) {
			for (int i = 0; i+cys <= outHeight-2*cys; i += cys)
				renderQuad(batch, outX, outY+cys+i, cxs, cys, new UvCoords(0, 1/3f, 1/3f, 2/3f));
		} else {
			renderQuad(batch, outX, outY+cys, cxs, outHeight-2*cys, new UvCoords(0, 1/3f, 1/3f, 2/3f));
		}
		//right
		if ((renderModeRepeats & RenderModeRepeats.RIGHT) != 0) {
			for (int i = 0; i+cys <= outHeight-2*cys; i += cys)
				renderQuad(batch, outX+outWidth-cxs, outY+cys+i, cxs, cys, new UvCoords(2/3f, 1/3f, 1, 2/3f));
		} else {
			renderQuad(batch, outX+outWidth-cxs, outY+cys, cxs, outHeight-2*cys, new UvCoords(2/3f, 1/3f, 1, 2/3f));
		}
		//top
		if ((renderModeRepeats & RenderModeRepeats.TOP) != 0) {
			for (int i = 0; i+cxs <= outWidth-2*cxs; i += cxs)
				renderQuad(batch, outX+cxs+i, outY+outHeight-cys, cxs, cys, new UvCoords(1/3f, 2/3f, 2/3f, 1));
		} else {
			renderQuad(batch, outX+cxs, outY+outHeight-cys, outWidth-2*cxs, cys, new UvCoords(1/3f, 2/3f, 2/3f, 1));
		}
		if ((renderModeRepeats & RenderModeRepeats.CENTER) != 0) {
			for (int y = 0; y < outHeight-2*cys; y += cys)
				for (int x = 0; x < outWidth-2*cxs; x += cxs)
					renderQuad(batch, outX+cxs+x, outY+cys+y, cxs, cys, new UvCoords(1/3f, 1/3f, 2/3f, 2/3f));
		} else {
			renderQuad(batch, outX+cxs, outY+cys, outWidth-2*cxs, outHeight-2*cys, new UvCoords(1/3f, 1/3f, 2/3f, 2/3f));
		}
	}
	
	private void renderHorizontal3Patch(SpriteBatch batch) {
		renderQuad(batch, outX, outY, texture.getWidth()/3, outHeight, new UvCoords(0, 0, 1/3f, 1));
		renderQuad(batch, outX + texture.getWidth()/3, outY, outWidth - 2*(texture.getWidth()/3), outHeight, new UvCoords(1/3f, 0, 2/3f, 1));
		renderQuad(batch, outX + outWidth - texture.getWidth()/3, outY, texture.getWidth()/3, outHeight, new UvCoords(2/3f, 0, 1, 1));
	}
	
	private void renderVertical3Patch(SpriteBatch batch) {
		renderQuad(batch, outX, outY, outWidth, texture.getHeight()/3, new UvCoords(0, 0, 1, 1/3f));
		renderQuad(batch, outX, outY+texture.getHeight()/3, outWidth, outHeight - 2*(texture.getHeight()/3), new UvCoords(0, 1/3f, 1, 2/3f));
		renderQuad(batch, outX, outY+outHeight - texture.getHeight()/3, outWidth, texture.getHeight()/3, new UvCoords(0, 2/3f, 1, 1));
	}
	
	protected void renderRelQuad(SpriteBatch batch, int x, int y, int width, int height,
			                     Color color, Texture texture, UvCoords uv) {
		renderQuad(batch, outX + x, outY + y, width, height, color, texture, uv);
	}
	
	
	private void renderQuad(SpriteBatch batch, int x, int y, int width, int height, UvCoords uv) {
		renderQuad(batch, x, y, width, height, outColor, texture, uv);
	}
	
	private void renderQuad(SpriteBatch batch, int x, int y, int width, int height,
			                final Color color, final Texture texture, final UvCoords uv) {
		batch.addQuad(x, y, width, height, color, uv, texture);
		boolean hasBounding = hasBounding();
		batch.attachUniform1i("u_hasBounding", hasBounding);
		if (hasBounding) {
			batch.attachUniform4f("u_bounding", outBoundX1, outBoundY1, outBoundX2, outBoundY2);
		}
	}
	
	protected void renderText(VkFont font, int offsetX, int offsetY,
			                  Color color, String text) {
		int x = outX + offsetX;
		int y = outY + offsetY;
		font.render(x, y, text, color, TextLocation.LEFT, batch -> {
			boolean hasBounding = hasBounding();
			batch.attachUniform1i("u_hasBounding", hasBounding);
			if (hasBounding) {
				batch.attachUniform4f("u_bounding", outBoundX1, outBoundY1, outBoundX2, outBoundY2);
			}
		});
	}
	
	private void calculateOutSize() {
		outWidth = 0; outHeight = 0;
		if (widthPercent > 0) {
			outWidth = (int) (parent != null ? (parent.outWidth*(widthPercent/100.0f))
				                             : (App.gfx.getWndWidth()*(widthPercent/100.0f)));
		}
		if (heightPercent > 0) {
			outHeight = (int) (parent != null ? (parent.outHeight*(heightPercent/100.0f))
				                              : (App.gfx.getWndHeight()*(heightPercent/100.0f)));
		}
		outWidth  += width;
		outHeight += height;
	}
	
	private void calculateOutPosition(int parentRX, int parentRY) {
		if (isBeingDragged()) return;
		
		outX = parent != null ? parent.outX : 0;
		outY = parent != null ? parent.outY : 0;
		
		switch (location) {
		case RELATIVE:
		case LEFT:
		case BOTTOM:
		case BOTTOM_LEFT:
			break;
		case RIGHT:
		case BOTTOM_RIGHT:
			clampRight();
			break;
		case TOP:
		case TOP_LEFT:
			clampTop();
			break;
		case TOP_RIGHT:
			clampRight();
			clampTop();
			break;
		case CENTER:
			centerHorizontal();
			centerVertical();
			break;
		case CENTER_LEFT:
			centerVertical();
			break;
		case CENTER_RIGHT:
			centerVertical();
			clampRight();
			break;
		case CENTER_BOTTOM:
			centerHorizontal();
			break;
		case CENTER_TOP:
			centerHorizontal();
			clampTop();
			break;
		case CENTER_VERTICAL:
			centerVertical();
			break;
		case CENTER_HORIZONTAL:
			centerHorizontal();
			break;
		}
		
		outX += x + parentRX + scrollX;
		outY += y + parentRY + scrollY;
	}
	
	private void clampTop() {
		outY = parent != null ? parent.outY + parent.outHeight - outHeight
		                      : App.gfx.getWndHeight() - outHeight;
	}
	
	private void clampRight() {
		outX = parent != null ? parent.outX + parent.outWidth - outWidth
		                      : App.gfx.getWndWidth() - outWidth;
	}
	
	private void centerHorizontal() {
		outX = parent != null ? parent.outX + parent.outWidth/2 - outWidth/2 
		                      : App.gfx.getWndWidth()/2 - outWidth/2;
	}
	
	private void centerVertical() {
		outY = parent != null ? parent.outY + parent.outHeight/2 - outHeight/2 
		                      : App.gfx.getWndHeight()/2 - outHeight/2;
	}
	
	private void calculateOutColor() {
		if (hoverColor != null && stateHovering) {
			outColor = hoverColor;
		} else {
			outColor = color;
		}
	}
	
	public void calculateOutBounding() {
		
		outBoundX1 = bounding.offsetX == -1 ? outX : outX + bounding.offsetX;
		outBoundY1 = bounding.offsetY == -1 ? outY : outY + bounding.offsetY;
		
		outBoundX2 = bounding.width  == -1 ? outBoundX1 + width  : outBoundX1 + bounding.width;
		outBoundY2 = bounding.height == -1 ? outBoundY1 + height : outBoundY1 + bounding.height;
		
		if (parent != null && parent.hasBounding()) {
			outBoundX1 = parent.outBoundX1 > outBoundX1 ? parent.outBoundX1 : outBoundX1;
			outBoundY1 = parent.outBoundY1 > outBoundY1 ? parent.outBoundY1 : outBoundY1;		
			outBoundX2 = parent.outBoundX2 < outBoundX2 ? parent.outBoundX2 : outBoundX2;
			outBoundY2 = parent.outBoundY2 < outBoundY2 ? parent.outBoundY2 : outBoundY2;
		}
	}
	
	void processDestruction() {
		onDestroyed();
		for (GuiComponent child : children)
			child.processDestruction();
	}
	
	protected void onDestroyed() {
		
	}
}
