package vork.gfx.gui;

import lombok.AllArgsConstructor;
import lombok.Data;
import vork.gfx.Color;

@AllArgsConstructor
@Data
public class GuiTextFormat {
	private String text;
	private Color color = Color.WHITE;
	private boolean underlined = false;
	
	public GuiTextFormat(String text) {
		this.text = text;
	}

	public GuiTextFormat(GuiTextFormat format) {
		this.text = format.text;
		this.color = format.color;
		this.underlined = format.underlined;
	}
}
