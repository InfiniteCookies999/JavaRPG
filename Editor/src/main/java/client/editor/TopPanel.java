package client.editor;

import client.editor.tools.EditToolType;
import client.game.Resources;
import lombok.Getter;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.VkFont;
import vork.gfx.gui.GuiCallbackType;
import vork.gfx.gui.GuiChildDisplayMode;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLabel;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiTextAlignment;

public class TopPanel extends GuiComponent {
	
	private GuiLabel fpsLabel;
	private GuiComponent toolsDiv;
	
	@Getter
	private EditToolType selectedEditToolType = EditToolType.PLACE_TILES;
	
	public TopPanel() {
		super(GuiLocation.TOP, 0, Editor.TOP_PANEL_HEIGHT);
		setColor(EditorColors.UI_PRIMARY_COLOR);
		setWidthPercent(100);
		
		VkFont font = Editor.instance.getFont();
		
		fpsLabel = new GuiLabel("fps: 60", 47, font.getGlyphHeight());
		fpsLabel.setFont(font);
		fpsLabel.setColor(Color.FULL_TRANSPARENT);
		fpsLabel.setLocation(GuiLocation.RIGHT);
		fpsLabel.setTextAlignment(GuiTextAlignment.NONE);
		fpsLabel.y = 4;
		addChild(fpsLabel);
		
		toolsDiv = new GuiComponent(600, height);
		toolsDiv.setColor(Color.FULL_TRANSPARENT);
		toolsDiv.x = 40;
		toolsDiv.setChildDisplayMode(GuiChildDisplayMode.BLOCK_HORIZONTAL, 10, 0);
		
		for (EditToolType tool : EditToolType.values()) {
			GuiComponent button = new GuiComponent(Resources.getUITexture(tool.getTopPanelTexturePath()));
			button.setLocation(GuiLocation.CENTER_VERTICAL);
			button.setHoverColor(EditorColors.UI_HOVER_COLOR);
			button.addCallback(GuiCallbackType.LEFT_CLICK, () -> {
				selectedEditToolType = tool;
			});
			toolsDiv.addChild(button);
		}
		
		addChild(toolsDiv);
		
	}
	
	@Override
	public void render(SpriteBatch batch, int parentRX, int parentRY) {
		
		fpsLabel.setText("fps: " + Editor.instance.getFps());
		
		// Set the selected for for the tool button
		for (int i = 0; i < EditToolType.values().length; i++) {
			GuiComponent child = toolsDiv.getChild(i);
			if (i == selectedEditToolType.ordinal()) {
				child.setColor(Color.rgb(0x8db0c2));
			} else {
				child.setColor(Color.WHITE);
			}
		}
		
		super.render(batch, parentRX, parentRY);
		
	}
}
