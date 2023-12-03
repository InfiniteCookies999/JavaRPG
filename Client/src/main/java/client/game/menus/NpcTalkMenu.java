package client.game.menus;

import client.Client;
import client.game.Resources;
import client.game.entity.Entity;
import client.net.out.NpcChatPacketOut;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.VkFont;
import vork.gfx.gui.GuiCallbackType;
import vork.gfx.gui.GuiChildDisplayMode;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLabel;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiRenderMode;
import vork.gfx.gui.GuiTextAlignment;
import vork.gfx.gui.GuiTextArea;

public class NpcTalkMenu extends GuiLabel {
	
	private static final Color ACTION_COLOR = Color.rgb(0x237ed9);
	
	public NpcTalkMenu() {
		super(GuiLocation.CENTER, 350, 150);
		setOutlineColor(Color.BLACK);
		setTextColor(Color.YELLOW);
		setFont(Client.instance.getBigFont());
		setTexture(Resources.getUITexture("window_panel_2.png"));
		setRenderMode(GuiRenderMode.NINE_PATCH);
		setOutlineColor(Color.BLACK);
		setTextColor(Color.YELLOW);
		setHidden(true);
	}
	
	public void say(Entity entity, String message) {
		setHidden(false);
		clearChildren();
		setText(entity.getName());
		
		renderPrimaryMessage(message);
		
		VkFont font = Client.instance.getBoldFont();
		String continueText = "Click here to continue!";
		GuiLabel continueButton = new GuiLabel(continueText, font.getLengthOfText(continueText), font.getGlyphHeight());
		continueButton.setColor(Color.FULL_TRANSPARENT);
		continueButton.setTextColor(ACTION_COLOR);
		continueButton.setTextAlignment(GuiTextAlignment.NONE);
		continueButton.setFont(font);
		continueButton.x = this.width / 2 - font.getLengthOfText(continueText) / 2;
		continueButton.y = 16;
		continueButton.addCallback(GuiCallbackType.HOVER_ENTER, () -> {
			continueButton.setTextColor(Color.rgb(0x2fb028));
		});
		continueButton.addCallback(GuiCallbackType.HOVER_EXIT, () -> {
			continueButton.setTextColor(ACTION_COLOR);
		});
		continueButton.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
			new NpcChatPacketOut(0).send();
		});
		addChild(continueButton);
	}
	
	public void options(Entity entity, String message, String[] optionMessages) {
		setHidden(false);
		clearChildren();
		setText(entity.getName());
		renderPrimaryMessage(message);
		
		GuiComponent optionsDiv = new GuiComponent(this.width, 90);
		optionsDiv.setColor(Color.FULL_TRANSPARENT);
		optionsDiv.setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN, 0, 5);
		addChild(optionsDiv);		
		
		VkFont font = Client.instance.getBoldFont();
		for (int index = 0; index < optionMessages.length; index++) {
			String optionMessage = optionMessages[index];
			GuiLabel optionButton = new GuiLabel(optionMessage, font.getLengthOfText(optionMessage), font.getGlyphHeight());
			optionButton.setColor(Color.FULL_TRANSPARENT);
			optionButton.setFont(font);
			optionButton.setTextColor(ACTION_COLOR);
			optionButton.setLocation(GuiLocation.CENTER);
			optionButton.addCallback(GuiCallbackType.HOVER_ENTER, () -> {
				optionButton.setTextColor(Color.rgb(0x2fb028));
			});
			optionButton.addCallback(GuiCallbackType.HOVER_EXIT, () -> {
				optionButton.setTextColor(ACTION_COLOR);
			});
			final int optionIndex = index;
			optionButton.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
				new NpcChatPacketOut(optionIndex).send();
			});
			
			optionsDiv.addChild(optionButton);
		}
	}
	
	private void renderPrimaryMessage(String message) {
		VkFont font = Client.instance.getBoldFont();
		final int pad = 20;
		GuiTextArea textArea = new GuiTextArea(this.width - pad*2, 100);
		textArea.setPosition(pad, 30);
		textArea.setFont(font);
		textArea.setTextOffsetY(-4);
		textArea.setShouldLineWrap(true);
		textArea.setDisplay(GuiTextArea.Display.TOP_DOWN);
		textArea.setTextColor(Color.rgb(0xebebeb));
		textArea.setColor(Color.FULL_TRANSPARENT);
		textArea.addTextLine(message);
		addChild(textArea);
	}
	
	@Override
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		setTextOffsetY(this.height - getFont().getGlyphHeight() - 2);
		setTextOffsetX(this.width / 2 - getFont().getLengthOfText(getText()) / 2);
		setTextAlignment(GuiTextAlignment.NONE);
		
		super.render(batch, parentRX, parentRY);
	}
}
