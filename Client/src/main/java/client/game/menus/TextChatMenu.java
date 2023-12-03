package client.game.menus;

import java.util.List;

import client.Client;
import client.Constants;
import client.game.Resources;
import client.net.out.ChatTextPacketOut;
import lombok.Getter;
import vork.App;
import vork.gfx.Color;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiRenderMode;
import vork.gfx.gui.GuiTextArea;
import vork.gfx.gui.GuiTextField;
import vork.gfx.gui.GuiTextFormat;
import vork.gfx.gui.GuiVerticalScrollBar;
import vork.input.Keys;

public class TextChatMenu extends GuiComponent {
	
	private static final int CHAT_WINDOW_WIDTH = 270;
	private static final int CHAT_WINDOW_HEIGHT = 180;
	private static final int CHAT_TEXT_FIELD_HEIGHT = 20;
	
	public static final Color PUBLIC_CHAT_COLOR = Color.rgb(0xf7efc8);
	public static final Color SERVER_CHAT_COLOR = Color.WHITE;
	
	private GuiTextArea textArea;
	
	@Getter
	private GuiTextField textField;
	
	public TextChatMenu() {
		super(CHAT_WINDOW_WIDTH, CHAT_WINDOW_HEIGHT);
		
		textField = new GuiTextField(CHAT_WINDOW_WIDTH, CHAT_TEXT_FIELD_HEIGHT);
		textField.setFont(Client.instance.getFont());
		textField.setPlaceholderText("> global chat");
		textField.setTexture(Resources.getUITexture("chat_text_field.png"));
		textField.setTextColor(PUBLIC_CHAT_COLOR);
		textField.setPlaceholderColor(new Color(PUBLIC_CHAT_COLOR.adjustBrightness(-40) , 0.5f));
		textField.setCaretHeight(15);
		textField.setTextOffset(4, 1);
		textField.setMaxDisplayWindowTextLength(CHAT_WINDOW_WIDTH - 4 * 2);
		textField.setCharacterLimit(Constants.MAX_CHAT_TEXT_LENGTH);
		textField.setRenderMode(GuiRenderMode.HORIZONTAL_3PATCH);
		
		GuiComponent chatTextAreaGrouping = new GuiComponent(CHAT_WINDOW_WIDTH - 16, CHAT_WINDOW_HEIGHT - CHAT_TEXT_FIELD_HEIGHT);
		chatTextAreaGrouping.setTexture(Resources.getUITexture("window_panel_2.png"));
		chatTextAreaGrouping.setRenderMode(GuiRenderMode.NINE_PATCH);
		chatTextAreaGrouping.setColor(new Color(Color.WHITE, 0.95f));
		chatTextAreaGrouping.y = CHAT_TEXT_FIELD_HEIGHT;
		
		textArea = new GuiTextArea(chatTextAreaGrouping.width, chatTextAreaGrouping.height);
		textArea.setFont(Client.instance.getFont());
		textArea.setColor(Color.FULL_TRANSPARENT);
		textArea.setTextColor(PUBLIC_CHAT_COLOR);
		textArea.setTextOffset(6, 2);
		textArea.setShouldLineWrap(true);
		textArea.setTextDisplayWidth(textArea.getTextDisplayWidth() - 6);
		textArea.setResizeHeightWithLines(true);
		
		GuiVerticalScrollBar.DragBar chatTextAreaDragBar = new GuiVerticalScrollBar.DragBar(Resources.getUITexture("chat_text_drag_bar.png"));
		chatTextAreaDragBar.setOffset(4, 2);
		GuiVerticalScrollBar chatTextAreaScrollBar = new GuiVerticalScrollBar(Resources.getUITexture("chat_text_scroll_bar.png"), chatTextAreaDragBar);
		chatTextAreaScrollBar.setLocation(GuiLocation.RIGHT);
		chatTextAreaScrollBar.height = chatTextAreaGrouping.height;
		chatTextAreaScrollBar.setViewArea(textArea);
		chatTextAreaScrollBar.y = CHAT_TEXT_FIELD_HEIGHT;
		chatTextAreaScrollBar.setAdditionalViewAreaHeightBounding(-2);
		
		chatTextAreaGrouping.addChild(textArea);
		addChild(textField);
		addChild(chatTextAreaScrollBar);
		addChild(chatTextAreaGrouping);
	
	}
	
	public void addMessage(String message, Color color) {
		textArea.addTextLine(message, color);
		if(textArea.getNumberOfTextLines() > Constants.MAX_CHAT_MESSAGES) {
			textArea.removeTextLineFromBack();
		}
	}
	
	public void addMessage(List<GuiTextFormat> formattedText) {
		textArea.addTextLine(formattedText);
		if(textArea.getNumberOfTextLines() > Constants.MAX_CHAT_MESSAGES) {
			textArea.removeTextLineFromBack();
		}
	}
	
	public boolean textFieldHasFocus() {
		return textField.hasFocus();
	}
	
	@Override
	public void update() {
		super.update();
		if (isHidden() || isDisabled()) {
			return;
		}
		
		if (App.input.isKeyJustPressed(Keys.ENTER)) {
			if (textField.hasFocus()) {
				String text = textField.getText();
				if (!text.isEmpty()) {
					new ChatTextPacketOut(text).send();
				}
				
				textField.removeFocus();
				textField.clearText();
			} else { // start chatting
				textField.giveFocus();
			}
		}
	}
}
