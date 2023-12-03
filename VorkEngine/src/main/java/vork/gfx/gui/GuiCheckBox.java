package vork.gfx.gui;

import lombok.Getter;
import lombok.Setter;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.VkFont;

public class GuiCheckBox extends GuiComponent {

	@Getter
	private boolean checked;
	
	private Texture originalTexture;
	private Color originalColor;
	
	@Getter
	private Texture checkedTexture;
	@Getter
	private Color checkedColor;
	
	@Setter @Getter
	private String text;
	@Setter @Getter
	private Color textColor = Color.WHITE;
	@Setter @Getter
	private int textOffsetX = 0, textOffsetY = 0;
	@Setter @Getter
	private VkFont font;
	
	public GuiCheckBox(Texture texture) {
		super(texture);
		init();
	}
	
	public GuiCheckBox(int relX, int relY, Texture texture) {
		super(relX, relY, texture);
		init();
	}
	
	public GuiCheckBox(int width, int height) {
		super(width, height);
		init();
	}
	
	public GuiCheckBox(int relX, int relY, int width, int height) {
		super(relX, relY, width, height);
		init();
	}
	
	public GuiCheckBox(GuiLocation location, int width, int height) {
		super(location, width, height);
		init();
	}
	
	private void init() {
		originalTexture = this.getTexture();
		originalColor = this.getColor();
	}

	public void setTextOffset(int offsetX, int offsetY) {
		textOffsetX = offsetX;
		textOffsetY = offsetY;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
		invokeCallback(GuiCallbackType.CHECKBOX_CHANGE);
		if (checked) {
			if (checkedTexture != null) setTexture(checkedTexture);
			if (checkedColor != null) setColor(checkedColor);
		} else {
			if (checkedTexture != null) setTexture(originalTexture);
			if (checkedColor != null) setColor(originalColor);
		}
	}
	
	public void setCheckedTexture(Texture texture) {
		this.originalTexture = getTexture();
		this.checkedTexture = texture;
	}
	
	public void setCheckedColor(Color color) {
		this.originalColor = getColor();
		this.checkedColor = color;
	}
	
	@Override
	public void update() {
		super.update();
		if (isHidden() || isDisabled()) return;
		
		if (isClicked()) {
			setChecked(!checked);
		}
	}
	
	@Override
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		super.render(batch, parentRX, parentRY);
		if (isHidden()) return;
		
		if (text != null) {
			if (font == null) {
				throw new IllegalStateException("Forgot to set the font");
			}
			
			renderText(font, outWidth + textOffsetX + 2, textOffsetY, textColor, text);
		}
	}
}
