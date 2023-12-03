package vork.gfx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import vork.util.FilePath;

public class VkFont {
	
	private int spaceWidth;
	private int padding;
	@Getter
	private int glyphHeight;
	private Texture texture;
	private SpriteBatch batch;
	
	public enum TextLocation {
		LEFT,
		RIGHT,
		CENTER
	}
	
	@Getter
	@Setter
	@AllArgsConstructor
	public class TextFormat {
		public TextFormat() {
			
		}
		
		private String text;
		private Color color;
		private TextLocation location;
	}
	
	private static class Glyph {
		private UvCoords uv;
		private int width;
	}
	
	private Glyph[] glyphs = new Glyph[256];
	
	private static int parseKeyValue(String line, final String keyName) {
		line = line.substring(keyName.length());
		if (line.isEmpty())
			throw new RuntimeException("Font format file. Unexpected ending at: " + keyName);
		if (line.charAt(0) != ' ')
			throw new RuntimeException("Font format file. Unexpected character after" + keyName);
		line = line.substring(1);
		if (line.isEmpty())
			new RuntimeException("Font format file. Unexpected ending at: " + keyName);
		try {
			return Integer.parseInt(line);
		} catch (NumberFormatException e) {
			new NumberFormatException("Font format file. " + keyName + " key-value not an integer");
		}
		return 0;
	}
	
	public static VkFont create(FilePath texturePath, FilePath formatPath, SpriteBatch batch) throws IOException {
		if (!formatPath.exist()) {
			throw new IllegalArgumentException("Formath file for font does not exist: " + formatPath);
		}
		
		BufferedReader br = formatPath.getBufferedReader();
		String line = null;
		
		int defaultResolution = 0, spaceWidth = 0, padding = 0;
		boolean foundDefaultRes = false, foundSpaceWidth = false, foundPadding = false;
		int[] map = new int[256];
		
		while ((line = br.readLine()) != null) {
			if (line.isEmpty()) continue;
			line = line.trim();
			
			if (line.startsWith("default")) {
				defaultResolution = parseKeyValue(line, "default");
				foundDefaultRes = true;
			} else if (line.startsWith("space")) {
				spaceWidth = parseKeyValue(line, "space");
				foundSpaceWidth = true;
			} else if (line.startsWith("pad")) {
				padding = parseKeyValue(line, "pad");
				foundPadding = true;
			} else if (line.startsWith("##")) {
				// comments get ignored.
				continue;
			} else {
				char ch = line.charAt(0);
				if (line.length() < 3)
					throw new RuntimeException("Font format file. Bad character key-value length");
				
				if (ch >= 33 && ch <= 126) {
					int idx = (int)ch;
					if (line.charAt(1) != ' ')
						throw new RuntimeException("Font format file. Expected space for character key-value");
					try {
						map[idx] =  Integer.parseInt(line.substring(2));
					} catch (NumberFormatException e) {
						throw new NumberFormatException("Font format file. Character key-value value not an integer");
					}
				} else {
					throw new RuntimeException("Font format file. Invalid character");
				}
			}
		}
		
		if (!foundPadding)
			throw new RuntimeException("Font format file. No 'pad' key set");
		if (!foundDefaultRes)
			throw new RuntimeException("Font format file. No 'default' key set");
		if (!foundSpaceWidth)
			throw new RuntimeException("Font format file. No 'space' key set");
		
		Texture texture = Texture.createFromPNGFile(texturePath, TextureFilter.NEAREST_NEAREST);
		VkFont font = new VkFont();
		font.padding = padding;
		font.spaceWidth = spaceWidth;
		font.glyphHeight = texture.getHeight();
		font.texture = texture;
		font.batch = batch;
		
		int xscan = 1;
		for (char c = 33; c <= 130; c++) {
			int glyphWidth = 0;
			if (map[c] == 0) {
				glyphWidth = defaultResolution;
				if (Character.isUpperCase(c)) {
					++glyphWidth;
				}
			} else {
				glyphWidth = map[c];
			}
			font.glyphs[c] = new Glyph();
			Glyph glyph = font.glyphs[c];
			glyph.uv = UvCoords.createFromPixelSize(xscan, 0, glyphWidth, font.texture.getHeight(), texture);
			glyph.width = glyphWidth;
			xscan += glyphWidth + 1;
		}
		
		return font;
	}
	
	public int render(int x, int y, final String text) {
		return render(x, y, text, Color.WHITE, TextLocation.LEFT, null);
	}
	
	public int render(int x, int y, final String text, final Color color) {
		return render(x, y, text, color, TextLocation.LEFT, null);
	}
	
	public int render(int x, int y, final String text, final Color color, TextLocation location, Consumer<SpriteBatch> uniformUploader) {
		int xscan = 0;
		
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == ' ') {
				xscan += spaceWidth;
				continue;
			}
			
			final Glyph glyph = glyphs[c];
			batch.addQuad(
					x + xscan,
					y,
					glyph.width,
					glyphHeight,
					color,
					glyph.uv,
					texture);
			if (uniformUploader != null) {
				uniformUploader.accept(batch);
			}
			
			xscan += glyph.width + padding;
		}
	
		return xscan;
	}
	
	public int getLengthOfText(final String text) {
		int totalWidth = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			totalWidth += getCharacterWidth(c);
		}
		return totalWidth;
	}
	
	public int getLengthOfText(List<TextFormat> formatedText) {
		int total = 0;
		for (TextFormat textFormat : formatedText) {
			total += getLengthOfText(textFormat.text);
		}
		return total;
	}
	
	public String getTextUntil(String text, int pixelWidth) {
		int totalWidth = 0, idx = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			int cw = getCharacterWidth(c);
			if (totalWidth + cw >= pixelWidth)
				break;
			totalWidth += cw;
			++idx;
		}
		return text.substring(0, idx);
	}
	
	public int getCharacterWidth(char c) {
		if (c == ' ')
			return spaceWidth;
		return glyphs[c].width + padding;
	}
	
	public void dispose() {
		texture.dispose();
	}
}
