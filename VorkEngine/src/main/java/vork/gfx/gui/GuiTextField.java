package vork.gfx.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import vork.App;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.UvCoords;
import vork.gfx.VkFont;
import vork.input.KeyInputListener;
import vork.input.Keys;

public class GuiTextField extends GuiComponent implements KeyInputListener {

	private class Caret {
		private int chPos;
		private Color color = Color.WHITE;
		private int tick, tickRate = 20;
		private int width = 1, height = 20;
		private int offsetX, offsetY;
	}
	
	private Caret caret = new Caret();
	
	/* The text inputed by the user */
	private String inputText = "";
	
	@Setter @Getter
	private String placeholderText = "";
	
	@Setter @Getter
	private Color textColor = Color.BLACK;
	
	@Setter @Getter
	private Color placeholderColor = new Color(Color.BLACK, 0.5f);
	
	/* Input restrictions */
	@Setter @Getter
	private int characterLimit = 100;
	// The amount of pixels the text in the window is allowed to cover.
	@Setter @Getter
	private int maxDisplayWindowTextLength = 100;
	
	/* Where the display window starts (since it displays left to right) */
	private int displayWindowLeftIndex;
	/* Where the display window ends (since it displays left to right) */
	private int displayWindowRightIndex;
	
	@Setter @Getter
	private int textOffsetX, textOffsetY;
	
	private Pattern requiredPattern = null;
	
	@Setter @Getter
	private VkFont font;
	
	private static final Map<Character, Character> SPECIAL_KEY_MAP = new HashMap<>();
	static {
		SPECIAL_KEY_MAP.put('1', '!');
		SPECIAL_KEY_MAP.put('2', '@');
		SPECIAL_KEY_MAP.put('3', '#');
		SPECIAL_KEY_MAP.put('4', '$');
		SPECIAL_KEY_MAP.put('5', '%');
		SPECIAL_KEY_MAP.put('6', '^');
		SPECIAL_KEY_MAP.put('7', '&');
		SPECIAL_KEY_MAP.put('8', '*');
		SPECIAL_KEY_MAP.put('9', '(');
		SPECIAL_KEY_MAP.put('0', ')');
		SPECIAL_KEY_MAP.put('`', '~');
		SPECIAL_KEY_MAP.put('-', '_');
		SPECIAL_KEY_MAP.put('=', '+');
		SPECIAL_KEY_MAP.put('[', '{');
		SPECIAL_KEY_MAP.put(']', '}');
		SPECIAL_KEY_MAP.put('\\', '|');
		SPECIAL_KEY_MAP.put(';', ':');
		SPECIAL_KEY_MAP.put('\'', '"');
		SPECIAL_KEY_MAP.put(',', '<');
		SPECIAL_KEY_MAP.put('.', '>');
		SPECIAL_KEY_MAP.put('/', '?');
	}
	
	public GuiTextField(Texture texture) {
		super(texture);
		App.input.addKeyInputListener(this);
	}
	
	public GuiTextField(int relX, int relY, Texture texture) {
		super(relX, relY, texture);
		App.input.addKeyInputListener(this);
	}
	
	public GuiTextField(int width, int height) {
		super(width, height);
		App.input.addKeyInputListener(this);
	}
	
	public GuiTextField(int relX, int relY, int width, int height) {
		super(relX, relY, width, height);
		App.input.addKeyInputListener(this);
	}
	
	public GuiTextField(GuiLocation location, int width, int height) {
		super(location, width, height);
		App.input.addKeyInputListener(this);
	}
	
	public String getText() {
		return inputText;
	}
	
	public void setCaretTickRate(int tickRate) {
		caret.tickRate = tickRate;
	}
	
	public void setCaretWidth(int width) {
		caret.width = width;
	}
	
	public void setCaretHeight(int height) { 
		caret.height = height;
	}
	
	public void setCaretSize(int width, int height) {
		setCaretWidth(width);
		setCaretHeight(height);
	}
	
	public int getCaretWidth() {
		return caret.width;
	}
	
	public int getCaretHeight() {
		return caret.height;
	}
	
	public void setCaretOffsetX(int offsetX) {
		caret.offsetX = offsetX;
	}
	
	public void setCaretOffsetY(int offsetY) {
		caret.offsetY = offsetY;
	}
	
	public int getCaretOffsetX() {
		return caret.offsetX;
	}
	
	public int getCaretOffsetY() {
		return caret.offsetY;
	}
	
	public void setCaretOffset(int offsetX, int offsetY) {
		setCaretOffsetX(offsetX);
		setCaretOffsetY(offsetY);
	}
	
	public void setTextOffset(int offsetX, int offsetY) {
		this.textOffsetX = offsetX;
		this.textOffsetY = offsetY;
	}
	
	public void setRequiredPattern(String pattern) {
		if (pattern.isEmpty()) {
			requiredPattern = null;
		} else {
			requiredPattern = Pattern.compile(pattern);
		}
	}
	
	@Override
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		if (font == null) {
			throw new IllegalStateException("Forgot to set the font");
		}
		
		super.render(batch, parentRX, parentRY);
		if (isHidden()) return;
		
		String displayText = getOutputText().substring(displayWindowLeftIndex, displayWindowRightIndex);
		
		// Displaying the caret
		if ((caret.tick++ / caret.tickRate) % 2 == 0 && !isDisabled() && focused) {
			int caretIndexRelWindow = caret.chPos - displayWindowLeftIndex;
			if (caretIndexRelWindow > displayText.length())
				caretIndexRelWindow = displayText.length();
			int caretX = textOffsetX + font.getLengthOfText(displayText.substring(0, caretIndexRelWindow)) + caret.offsetX;
			int caretY = textOffsetY + caret.offsetY;
			
			renderRelQuad(batch, caretX, caretY, caret.width, caret.height, caret.color, App.gfx.emptyTexture, UvCoords.FULL);
		}
		
		// Displaying the text
		String textToRender = displayText.isEmpty() ? placeholderText : displayText;
		renderText(font, textOffsetX, textOffsetY, displayText.isEmpty() ? placeholderColor : textColor, textToRender);
	}
	
	public void setText(String text) {
		clearText();
		for (int i = 0; i < text.length(); i++) {
			processCharacter(text.charAt(i), true);
		}
	}
	
	@Override
	public void onKeyJustPressed(int key) {
		processKey(key);
	}

	@Override
	public void onKeyRepeat(int key) {
		processKey(key);
	}

	@Override
	public void onKeyReleased(int key) {
		
	}
	
	private void processKey(int key) {
		if (isHidden() || isDisabled()) return;
		
		if (!focused) {
			return;
		}
		
		if (key == Keys.TAB) {
			processCharacter(' ', false);
			processCharacter(' ', false);
			processCharacter(' ', false);
			processCharacter(' ', false);
		} else if (key == Keys.BACKSPACE) {
			processBackspace();
		} else if (key == Keys.LEFT) {
			processLeft();
		} else if (key == Keys.RIGHT) {
			processRight();
		} else if (key >= 32 && key <= 126) {
			processCharacter((char) key, false);
		}
	}
	
	private void processCharacter(char ch, boolean ignoreMods) {
		if (inputText.length() >= characterLimit) return;
		
		boolean shifting = App.input.isKeyPressed(Keys.LEFT_SHIFT) || App.input.isKeyPressed(Keys.RIGHT_SHIFT);
		// Modifying the key based on control keys.
		if (!ignoreMods) {
			if (Character.isAlphabetic(ch)) {
				ch = Character.toLowerCase(ch);
				if (shifting)
					ch -= 32;
			} else if (shifting) {
				if (SPECIAL_KEY_MAP.containsKey(ch)) {
					ch = SPECIAL_KEY_MAP.get(ch);
				}
			}
		}
		
		String newInputText = inputText + ch;
		if (requiredPattern != null) {
			if (!requiredPattern.matcher(newInputText).matches()) {
				return;
			}
		}
		
		inputText = newInputText;
		
		// Move the caret just past where the new character was inputed into the text.
		++caret.chPos;
		
		///    leftIndex --chPos----- rightIndex
		///    leftIndex ------------ (chPos)rightIndex << Got to shift everything to the right
		if (caret.chPos > displayWindowRightIndex) {
			displayWindowRightIndex = caret.chPos;
			calcWindowFromRightIndex();
		} else {
			calcWindowFromLeftIndex();
		}
		
		invokeCallback(GuiCallbackType.TEXT_CHANGED);
	}
	
	private void processBackspace() {
		if (inputText.isEmpty() || caret.chPos == 0) return; // Nothing to delete.
		inputText = new StringBuilder(inputText).deleteCharAt(caret.chPos - 1).toString();
		--caret.chPos;
		if (caret.chPos < displayWindowLeftIndex) {
			displayWindowLeftIndex = caret.chPos;
			calcWindowFromLeftIndex();
		} else {
			--displayWindowRightIndex;
			calcWindowFromRightIndex();
		}
		
		invokeCallback(GuiCallbackType.TEXT_CHANGED);
	}
	
	private void processLeft() {
		if (inputText.isEmpty() || caret.chPos == 0) return; // Can't move back
		--caret.chPos;
		if (caret.chPos < displayWindowLeftIndex) {
			displayWindowLeftIndex = caret.chPos;
			calcWindowFromLeftIndex();
		}
	}
	
	private void processRight() {
		if (inputText.isEmpty() || caret.chPos == getOutputText().length()) return; // At end of text
		++caret.chPos;
		if (caret.chPos > displayWindowRightIndex) {
			displayWindowRightIndex = caret.chPos;
			calcWindowFromRightIndex();
		}
	}
	
	/**
	 * Treats the right index as fixed and continuously
	 * moves the left index away from the right index
	 * as long as the text fits within the window.
	 *
	 * However, the right index won't stay fixed if the left
	 * index becomes zero and more text can fit more to the right.
	 */
	private void calcWindowFromRightIndex() {
		String outputText = getOutputText();
		int charCount = 0;
		while (true) {
			++charCount;
			displayWindowLeftIndex = displayWindowRightIndex - charCount;
			if (displayWindowLeftIndex < 0) {
				displayWindowLeftIndex = 0;
				// There might be more characters that can fit to the right
				while (true) {
					++displayWindowRightIndex;
					if (displayWindowRightIndex > outputText.length()) {
						displayWindowRightIndex = outputText.length();
						break;
					}
					String windowText = outputText.substring(displayWindowLeftIndex, displayWindowRightIndex);
					if (font.getLengthOfText(windowText) > maxDisplayWindowTextLength) {
						--displayWindowRightIndex;
						break;
					}
				}
				break;
			}
			String windowText = outputText.substring(displayWindowLeftIndex, displayWindowRightIndex);
			if (font.getLengthOfText(windowText) > maxDisplayWindowTextLength) {
				// No more space left so ending
				++displayWindowLeftIndex;
				break;
			}
		}
	}
	
	/**
	 * Treats the left index as fixed and continuously
	 * moves the right index away from the left index
	 * as long as the text fits within the window.
	 * 
	 * However, the left index won't stay fixed if the right
	 * index hits the max limit and more text can fit more to
	 * the left.
	 */
	private void calcWindowFromLeftIndex() {
		String outputText = getOutputText();
		int charCount = 0;
		while (true) {
			++charCount;
			displayWindowRightIndex = displayWindowLeftIndex + charCount;
			if (displayWindowRightIndex > outputText.length()) {
				displayWindowRightIndex = outputText.length();
				// There might be more characters that can fit to the left
				while (true) {
					--displayWindowLeftIndex;
					if (displayWindowLeftIndex < 0) {
						displayWindowLeftIndex = 0;
						break;
					}
					String windowText = outputText.substring(displayWindowLeftIndex, displayWindowRightIndex);
					if (font.getLengthOfText(windowText) > maxDisplayWindowTextLength) {
						// No more space right so ending
						++displayWindowLeftIndex;
						break;
					}
				}
				break;
			}
			String windowText = outputText.substring(displayWindowLeftIndex, displayWindowRightIndex);
			if (font.getLengthOfText(windowText) > maxDisplayWindowTextLength) {
				// No more space right so ending
				--displayWindowRightIndex;
				break;
			}
		}
	}
	
	public void clearText() {
		caret.chPos = 0;
		displayWindowLeftIndex = 0;
		displayWindowRightIndex = 0;
		inputText = "";
		invokeCallback(GuiCallbackType.TEXT_CHANGED);
	}
	
	private String getOutputText() {
		return inputText;
	}
	
	@Override
	protected void onDestroyed() {
		App.input.removeKeyInputListener(this);
	}
}
