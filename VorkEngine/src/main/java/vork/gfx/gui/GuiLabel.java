package vork.gfx.gui;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.VkFont;

public class GuiLabel extends GuiComponent {

	@Setter @Getter
	private GuiTextAlignment textAlignment = GuiTextAlignment.CENTER;
	
	@Setter @Getter
	private int textOffsetX, textOffsetY;
	
	@Setter @Getter
	private Color textColor = Color.WHITE;
	
	@Setter @Getter
	private Color outlineColor;
	
	@Setter @Getter
	private VkFont font;
	
	private String text = "";
	
	private List<GuiTextFormat> formattedText = null;
	
	public GuiLabel(Texture texture) {
		super(texture);
	}
	
	public GuiLabel(int relX, int relY, Texture texture) {
		super(relX, relY, texture);
	}
	
	public GuiLabel(int width, int height) {
		super(width, height);
	}
	
	public GuiLabel(int relX, int relY, int width, int height) {
		super(relX, relY, width, height);
	}
	
	public GuiLabel(GuiLocation location, int width, int height) {
		super(location, width, height);
	}
	
	public GuiLabel(String text, Texture texture) {
		super(texture);
		this.text = text;
	}
	
	public GuiLabel(String text, int relX, int relY, Texture texture) {
		super(relX, relY, texture);
		this.text = text;
	}
	
	public GuiLabel(String text, int width, int height) {
		super(width, height);
		this.text = text;
	}
	
	public GuiLabel(String text, int relX, int relY, int width, int height) {
		super(relX, relY, width, height);
		this.text = text;
	}
	
	public GuiLabel(String text, GuiLocation location, int width, int height) {
		super(location, width, height);
		this.text = text;
	}
	
	public void setTextOffset(int offsetX, int offsetY) {
		textOffsetX = offsetX;
		textOffsetY = offsetY;
	}
	
	public void setText(String text) {
		formattedText = null;
		this.text = text;
		invokeCallback(GuiCallbackType.TEXT_CHANGED);
	}
	
	public void setFormattedText(List<GuiTextFormat> formattedText) {
		this.text = null;
		this.formattedText = formattedText;
		invokeCallback(GuiCallbackType.TEXT_CHANGED);
	}
	
	public void clearText() {
		setText("");
	}
	
	public String getText() {
		if (formattedText != null) {
			String totalText = "";
			for (GuiTextFormat format : formattedText) {
				totalText += format.getText();
			}
			return totalText;
		} else {
			return text;
		}
	}
	
	@Override
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		
		if (font == null) {
			throw new IllegalStateException("Forgot to set the font");
		}
		
		super.render(batch, parentRX, parentRY);
		if (isHidden()) return;
		
		String text = getText();
		int textX = 0, textY = 0;
		switch (textAlignment) {
		case NONE:
			textX = textOffsetX; textY = textOffsetY;
			break;
		case CENTER:
			textX = textOffsetX + (outWidth/2) - font.getLengthOfText(text)/2;
			textY = textOffsetY + (outHeight/2) - font.getGlyphHeight()/2;
			break;
		case LEFT:
			textX = textOffsetX;
			textY = textOffsetY + (outHeight/2) - font.getGlyphHeight()/2;
			break;
		case RIGHT:
			textX = textOffsetX + outWidth - font.getLengthOfText(text);
			textY = textOffsetY + (outHeight/2) - font.getGlyphHeight()/2;
			break;
		}
		
		if (formattedText != null) {
			int xscan = 0;
			for (GuiTextFormat format : formattedText) {
				if (outlineColor != null) {
					renderText(font, textX + xscan + 1, textY - 1, outlineColor, format.getText());
				}
				renderText(font, textX + xscan, textY, format.getColor(), format.getText());
				xscan += font.getLengthOfText(format.getText());
			}
		} else {
			if (outlineColor != null) {
				renderText(font, textX + 1, textY - 1, outlineColor, text);
			}
			renderText(font, textX, textY, textColor, text);
		}
	}
}
