package vork.gfx.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.VkFont;

public class GuiTextArea extends GuiComponent {

	public enum Display {
		TOP_DOWN,
		BOTTOM_UP
	}
	
	@Setter @Getter
	private Color textColor = Color.BLACK;
	@Setter @Getter
	private int textDisplayWidth;
	@Setter @Getter
	private int textOffsetX = 0, textOffsetY = 0;
	@Setter @Getter
	private boolean shouldLineWrap = false;
	
	@Setter @Getter
	private Display display = Display.BOTTOM_UP;
	
	/**
	 * As more lines are added it increases the height of the
	 * component to meet to new capacity.
	 */
	@Setter
	private boolean resizeHeightWithLines;
	
	private LinkedList<List<GuiTextFormat>> textLines = new LinkedList<>();
	
	@Setter @Getter
	private VkFont font;
	
	public GuiTextArea(Texture texture) {
		super(texture);
		textDisplayWidth = texture.getWidth();
	}
	
	public GuiTextArea(int relX, int relY, Texture texture) {
		super(relX, relY, texture);
		textDisplayWidth = texture.getWidth();
	}
	
	public GuiTextArea(int width, int height) {
		super(width, height);
		textDisplayWidth = width;
	}
	
	public GuiTextArea(int relX, int relY, int width, int height) {
		super(relX, relY, width, height);
		textDisplayWidth = width;
	}
	
	public GuiTextArea(GuiLocation location, int width, int height) {
		super(location, width, height);
		textDisplayWidth = width;
	}
	
	public void setTextOffset(int offsetX, int offsetY) {
		textOffsetX = offsetX;
		textOffsetY = offsetY;
	}
	
	public void addTextLine(String line) {
		addTextLine(line, textColor);
	}
	
	public void addTextLine(String line, Color color) {
		ArrayList<GuiTextFormat> lineFormat = new ArrayList<>();
		lineFormat.add(new GuiTextFormat(line, color, false));
		textLines.addLast(lineFormat);
	}
	
	public void addTextLine(List<GuiTextFormat> formattedLine) {
		textLines.addLast(formattedLine);
	}
	
	public void addLineToFront(String line) {
		addTextLine(line, textColor);
	}
	
	public void addLineToFront(String line, Color color) {
		addTextLine(line, color);
	}
	
	public void addLineToFront(List<GuiTextFormat> formattedLine) {
		textLines.addLast(formattedLine);
	}
	
	public void addTextLineToBack(String line) {
		addTextLineToBack(line, textColor);
	}
	
	public void addTextLineToBack(String line, Color color) {
		ArrayList<GuiTextFormat> lineFormat = new ArrayList<>();
		lineFormat.add(new GuiTextFormat(line, color, false));
		textLines.addFirst(lineFormat);
	}
	
	public void addTextLineToBack(List<GuiTextFormat> formattedLine) {
		textLines.addFirst(formattedLine);
	}
	
	public int getNumberOfTextLines() {
		return textLines.size();
	}
	
	public void removeTextLineFromFront() {
		textLines.pollLast();
	}
	
	public void removeTextLineFromBack() {
		textLines.pollFirst();
	}
	
	public void clearTextLines() {
		textLines.clear();
	}
	
	private static String getFormattedLineText(List<GuiTextFormat> formattedLine) {
		return formattedLine.stream()
				            .map(line -> line.getText())
				            .reduce("", (a, b) -> a + b);
	}
	
	private static boolean isValidSplit(String lineText, String aWrappedLine) {
		boolean validSplit = aWrappedLine.endsWith(" ");
		validSplit |= lineText.length() == aWrappedLine.length();
		validSplit |= lineText.length() > aWrappedLine.length() &&
				      lineText.charAt(aWrappedLine.length()) == ' ';
		return validSplit;
	}
	
	@AllArgsConstructor
	private class OffsetPair {
		private int offset;
		private int end;
	}
	
	private int drawnLineCount = 0;
	
	public int renderWrappedLine(List<GuiTextFormat> formattedLine, int offsetY) {
		// pre calculate the number of lines that will be rendered once wrapping is done.
		int lineCount = 0;
		int offset = 0, prevOffset = 0;
		String lineText = getFormattedLineText(formattedLine);
		List<OffsetPair> offsetPairs = new ArrayList<>();
		while (!lineText.isEmpty()) {
			String aWrappedLine = font.getTextUntil(lineText, textDisplayWidth - textOffsetX);
			
			// Maybe we split in the middle of a word?
			boolean validSplit = isValidSplit(lineText, aWrappedLine);
			if (!validSplit) {
				// Must have split in the middle of a line.
				int spaceIndex = aWrappedLine.lastIndexOf(" ");
				if (spaceIndex != -1 && spaceIndex != 0) {
					aWrappedLine = aWrappedLine.substring(0, spaceIndex);
				} // else ... Yikes, must be a really long line so 
				  // we are forced to split in the middle of a line
			}
			
			offset += aWrappedLine.length();
			lineText = lineText.substring(aWrappedLine.length());
			// Want to left trim white space
			int ltrimIdx = 0;
			while (ltrimIdx < lineText.length() && lineText.charAt(ltrimIdx) == ' ') {
				++ltrimIdx;
			}
			offset += ltrimIdx;
			lineText = lineText.substring(ltrimIdx);
			
			offsetPairs.add(new OffsetPair(prevOffset, offset));
			
			prevOffset = offset;
			++lineCount;
		}
		
		// Now rendering the lines
		int formatLineCount = 0;
		for (OffsetPair offsetPair : offsetPairs) {
			
			// Checking to make sure there isn't nothing to render
 			if (offsetPair.end == offsetPair.offset) {
 				continue;
 			}
			
			// Want to find the starting format and the index into it
			
 			// Finding the place to start
 			int currentFormatIndex = 0;
 			int count = 0;
 			// say its length is 5
 			// such as "hello"
 			// if it ends at o the offset is 4
 			//
 			// So if it can go to the next format
 			// then the offset would equal the length
 			
 			for (GuiTextFormat format : formattedLine) {
 				String formatText = format.getText();
 				if (offsetPair.offset >= count + formatText.length()) {
 					// Can keep going
 					count += formatText.length();
 					++currentFormatIndex;
 				} else {
 					break;
 				}
 			}
 			int indexIntoFormat = offsetPair.offset - count;
 			
			int xscan = 0;
 			int totalCharCount = 0;
 			boolean firstRound = true;
 			int adjHeight = display == Display.BOTTOM_UP ? ((lineCount-1) * font.getGlyphHeight()) : 0;
 			while (totalCharCount < offsetPair.end - offsetPair.offset) {
 				GuiTextFormat currentFormat = formattedLine.get(currentFormatIndex);
 				String currentText = currentFormat.getText();
 				int charCount = firstRound ? indexIntoFormat : 0;
 				firstRound = false;
 				while (charCount < currentText.length()) {
 					char characterToRender = currentText.charAt(charCount);
 					
 					renderText(
							font,
							textOffsetX + xscan,
							textOffsetY + offsetY + adjHeight - formatLineCount * font.getGlyphHeight(),
							currentFormat.getColor(), String.valueOf(characterToRender)
							);
 					xscan += font.getCharacterWidth(characterToRender);
 					
 					++charCount;
 					++totalCharCount;
 					if (totalCharCount >= offsetPair.end - offsetPair.offset) {
 						break;
 					}
 				}
 				if (++currentFormatIndex >= formattedLine.size()) {
 					break;
 				}
 			}
 			++formatLineCount;
		}
		
		drawnLineCount += lineCount;
		return lineCount * font.getGlyphHeight();
	}
	
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		
		if (font == null) {
			throw new IllegalStateException("Forgot to set the font");
		}
		
		super.render(batch, parentRX, parentRY);
		
		drawnLineCount = 0;
		if (!isHidden()) {
			
			int offsetY = display == Display.BOTTOM_UP ? 0 : this.height - font.getGlyphHeight();
			for (int i = textLines.size() - 1; i >= 0; i--) {
				List<GuiTextFormat> formattedLine = textLines.get(i);
				if (shouldLineWrap) {
					if (display == Display.BOTTOM_UP) {
						offsetY += renderWrappedLine(formattedLine, offsetY);
					} else {
						offsetY -= renderWrappedLine(formattedLine, offsetY);	
					}
				} else {
					int xscan = 0;
					for (GuiTextFormat format : formattedLine) {
						renderText(
								font,
								textOffsetX + xscan,
								textOffsetY + offsetY,
								format.getColor(), format.getText()
								);
						xscan += font.getLengthOfText(format.getText());
					}
					offsetY += display == Display.BOTTOM_UP ? font.getGlyphHeight() : -font.getGlyphHeight();
					++drawnLineCount;
				}
			}
			
			if (resizeHeightWithLines) {
				int possibleNewHeight = drawnLineCount * font.getGlyphHeight();
				if (possibleNewHeight > this.height) {
					this.height = possibleNewHeight;
				}
			}
		}
	}
}
