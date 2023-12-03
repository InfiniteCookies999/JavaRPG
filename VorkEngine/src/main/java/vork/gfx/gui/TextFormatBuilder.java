package vork.gfx.gui;

import java.util.ArrayList;
import java.util.List;

import vork.gfx.Color;

public class TextFormatBuilder {
	
	private List<GuiTextFormat> formattedText = new ArrayList<>();
	
	public TextFormatBuilder() {
		
	}
	
	public TextFormatBuilder(String text, Color color, boolean underlined) {
		formattedText.add(new GuiTextFormat(text, color, underlined));
	}
	
	public TextFormatBuilder(String text, Color color) {
		formattedText.add(new GuiTextFormat(text, color, false));
	}
	
	public TextFormatBuilder append(String text, Color color, boolean underlined) {
		formattedText.add(new GuiTextFormat(text, color, underlined));
		return this;
	}
	
	public TextFormatBuilder append(String text, Color color) {
		formattedText.add(new GuiTextFormat(text, color, false));	
		return this;
	}
	
	public List<GuiTextFormat> build(){
		return formattedText;
	}
}
