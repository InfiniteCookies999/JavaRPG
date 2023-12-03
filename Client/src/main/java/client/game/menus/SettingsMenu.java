package client.game.menus;

import client.Client;
import client.game.Resources;
import vork.App;
import vork.gfx.gui.GuiCallbackType;
import vork.gfx.gui.GuiCheckBox;
import vork.gfx.gui.GuiChildDisplayMode;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiRenderMode;

public class SettingsMenu extends GuiComponent {

	private GuiCheckBox showFpsCheckBox;
	private GuiCheckBox showLocationCheckBox;
	
	public SettingsMenu() {
		super(GuiLocation.CENTER, 250, 150);
	
		setRenderMode(GuiRenderMode.NINE_PATCH);
		setTexture(Resources.getUITexture("window_panel_1.png"));
		setHidden(true);
		setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN, 0, 10);
		
		showFpsCheckBox = new GuiCheckBox(Resources.getUITexture("check_box.png"));
		showFpsCheckBox.setFont(Client.instance.getFont());
		showFpsCheckBox.setCheckedTexture(Resources.getUITexture("check_box_checked.png"));
		showFpsCheckBox.x = 10;
		showFpsCheckBox.setText("show fps");
		showFpsCheckBox.setTextOffsetX(3);
		showFpsCheckBox.setChecked(false);
		
		GuiCheckBox vsyncCheckBox = new GuiCheckBox(Resources.getUITexture("check_box.png"));
		vsyncCheckBox.setFont(Client.instance.getFont());
		vsyncCheckBox.setCheckedTexture(Resources.getUITexture("check_box_checked.png"));
		vsyncCheckBox.x = 10;
		vsyncCheckBox.setText("vsync");
		vsyncCheckBox.setTextOffsetX(3);
		vsyncCheckBox.setChecked(App.gfx.window.isVsyncIsEnabled());
		vsyncCheckBox.addCallback(GuiCallbackType.CHECKBOX_CHANGE, () -> {
			App.gfx.window.vsync(!App.gfx.window.isVsyncIsEnabled());
		});
		
		showLocationCheckBox = new GuiCheckBox(Resources.getUITexture("check_box.png"));
		showLocationCheckBox.setFont(Client.instance.getFont());
		showLocationCheckBox.setCheckedTexture(Resources.getUITexture("check_box_checked.png"));
		showLocationCheckBox.x = 10;
		showLocationCheckBox.setText("show character tile");
		showLocationCheckBox.setTextOffsetX(3);
		showLocationCheckBox.setChecked(false);
		
		addChild(showLocationCheckBox);
		addChild(showFpsCheckBox);
		addChild(vsyncCheckBox);
		
	}

	public boolean shouldShowFps() {
		return showFpsCheckBox.isChecked();
	}
	
	public boolean shouldShowCharacterLocation() {
		return showLocationCheckBox.isChecked();
	}
}
