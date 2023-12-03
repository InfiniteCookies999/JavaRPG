package client.game.menus;

import client.Client;
import client.game.PlayScreen;
import client.game.Resources;
import vork.gfx.Color;
import vork.gfx.gui.GuiCallbackType;
import vork.gfx.gui.GuiChildDisplayMode;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLabel;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiRenderMode;

public class EscMenu extends GuiComponent {

	public EscMenu(SettingsMenu settingsMenu) {
		super(GuiLocation.CENTER, 150, 150);
		setRenderMode(GuiRenderMode.NINE_PATCH);
		setTexture(Resources.getUITexture("window_panel_1.png"));
		setHidden(true);
		setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN, 0, 20);
		PlayScreen playScreen = Client.instance.getScreen();
		createButton("logout", () -> {
			playScreen.logout();
		});
		createButton("settings", () -> {
			playScreen.menuTakeFullControl(settingsMenu);
		});
		createButton("close", () -> {
			playScreen.menuResignFullControl();
		});
	}
	
	private void createButton(String name, Runnable clickCallback) {
		GuiLabel button = new GuiLabel(name, GuiLocation.CENTER_HORIZONTAL, 100, 20);
		button.setFont(Client.instance.getFont());
		button.setRenderMode(GuiRenderMode.HORIZONTAL_3PATCH);
		button.setTexture(Resources.getUITexture("panel_button.png"));
		button.setTextOffsetY(1);
		button.setHoverColor(Color.WHITE.adjustBrightness(-80));
		button.addCallback(GuiCallbackType.LEFT_CLICK, clickCallback);
		addChild(button);
	}
	
}
