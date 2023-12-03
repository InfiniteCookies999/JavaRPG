package client.editor;
import vork.gfx.Color;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLabel;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiTextAlignment;

public class EraseChunkWarningMenu extends GuiComponent {
	public EraseChunkWarningMenu() {
		super(0, 0);
		setWidthPercent(100);
		setHeightPercent(100);
		setColor(new Color(Color.BLACK, 0.3f));
		setHidden(true);
		
		GuiComponent eraseChunkWArningMenuBorder = new GuiComponent(300, 150);
		eraseChunkWArningMenuBorder.setLocation(GuiLocation.CENTER);
		eraseChunkWArningMenuBorder.setColor(EditorColors.UI_WHITE_COLOR);
		
		GuiComponent eraseChunkWarningMenuArea = new GuiComponent(eraseChunkWArningMenuBorder.width-2, eraseChunkWArningMenuBorder.height-2);
		eraseChunkWarningMenuArea.setPosition(1, 1);
		eraseChunkWarningMenuArea.setColor(EditorColors.UI_PRIMARY_COLOR);
		
		GuiLabel warningLabel1 = new GuiLabel("!! Warning: You are about to delete a chunk !!", 100, 20);
		warningLabel1.setFont(Editor.instance.getFont());
		warningLabel1.x = 6;
		warningLabel1.setColor(Color.FULL_TRANSPARENT);
		warningLabel1.setTextAlignment(GuiTextAlignment.LEFT);
		warningLabel1.setLocation(GuiLocation.TOP);
		warningLabel1.setTextColor(Color.RED);
		GuiLabel warningLabel2 = new GuiLabel("This action cannot be undone!", 100, 20);
		warningLabel2.setFont(Editor.instance.getFont());
		warningLabel2.y = -warningLabel1.height;
		warningLabel2.x = 6;
		warningLabel2.setColor(Color.FULL_TRANSPARENT);
		warningLabel2.setTextAlignment(GuiTextAlignment.LEFT);
		warningLabel2.setLocation(GuiLocation.TOP);
		warningLabel2.setTextColor(Color.RED);
		
		eraseChunkWarningMenuArea.addChild(warningLabel1);
		eraseChunkWarningMenuArea.addChild(warningLabel2);
		
		int xOff = 70;
		int yOff = -30;
		
		GuiLabel confirmButton = new GuiLabel("confirm", 80, 30);
		confirmButton.setFont(Editor.instance.getFont());
		confirmButton.setName("confirm-btn");
		confirmButton.setLocation(GuiLocation.CENTER);
		confirmButton.setPosition(-xOff, yOff);
		confirmButton.setColor(EditorColors.UI_WHITE_COLOR);
		confirmButton.setTextColor(EditorColors.UI_OFFSET_TEXT_COLOR);
		confirmButton.setHoverColor(EditorColors.UI_HOVER_COLOR);
		
		GuiLabel cancelButton = new GuiLabel("cancel", 80, 30);
		cancelButton.setFont(Editor.instance.getFont());
		cancelButton.setName("cancel-btn");
		cancelButton.setLocation(GuiLocation.CENTER);
		cancelButton.setPosition(+xOff, yOff);
		cancelButton.setColor(EditorColors.UI_WHITE_COLOR);
		cancelButton.setTextColor(EditorColors.UI_OFFSET_TEXT_COLOR);
		cancelButton.setHoverColor(EditorColors.UI_HOVER_COLOR);
		
		eraseChunkWarningMenuArea.addChild(confirmButton);
		eraseChunkWarningMenuArea.addChild(cancelButton);
		
		eraseChunkWArningMenuBorder.addChild(eraseChunkWarningMenuArea);
		addChild(eraseChunkWArningMenuBorder);
		
	}
}
