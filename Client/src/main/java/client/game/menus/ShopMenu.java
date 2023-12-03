package client.game.menus;

import client.Client;
import client.Constants;
import client.game.Resources;
import client.game.items.ItemDefinition;
import client.game.items.ItemStack;
import client.game.menus.container.InventoryMenu;
import vork.gfx.Color;
import vork.gfx.VkFont;
import vork.gfx.gui.GuiChildDisplayMode;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLabel;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiRenderMode;
import vork.gfx.gui.GuiTextAlignment;

public class ShopMenu extends GuiComponent {
	
	private GuiComponent sellSide;
	private GuiComponent buySide;
	
	public ShopMenu() {
		super(400, 250);
		createSellSide();
		createBuySide();
		this.y = 20;
		setLocation(GuiLocation.CENTER);
		setHidden(true);
	}
	
	private void createSellSide() {
		sellSide = createSide("SELL");
		sellSide.setLocation(GuiLocation.LEFT);
		addChild(sellSide);
	}
	
	private void createBuySide() {
		buySide = createSide("BUY");
		buySide.setLocation(GuiLocation.RIGHT);
		addChild(buySide);
	}
	
	private GuiLabel createSide(String text) {
		VkFont font = Client.instance.getBigFont();
		GuiLabel side = new GuiLabel(text, this.width/2, this.height);
		side.setFont(font);
		side.setTextColor(Color.YELLOW);
		side.setOutlineColor(Color.BLACK);
		side.setTextAlignment(GuiTextAlignment.NONE);
		side.setTextOffsetX(side.width/2 - font.getLengthOfText(text)/2);
		side.setTextOffsetY(side.height - font.getGlyphHeight() - 4);
		side.setTexture(Resources.getUITexture("window_panel_2.png"));
		side.setRenderMode(GuiRenderMode.NINE_PATCH);
		side.setChildDisplayMode(GuiChildDisplayMode.BLOCK_HORIZONTAL);
		GuiComponent div1 = new GuiComponent(side.width/4, side.height - 30);
		div1.setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN);
		div1.setColor(Color.GREEN);
		side.addChild(div1);
		GuiComponent div2 = new GuiComponent(side.width/4, side.height - 30);
		div2.setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN);
		div2.setColor(Color.BLUE);
		side.addChild(div2);
		
		GuiComponent div3 = new GuiComponent(side.width/4, side.height - 30);
		div3.setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN);
		div3.setColor(Color.GREEN);
		side.addChild(div3);
		GuiComponent div4 = new GuiComponent(side.width/4, side.height - 30);
		div4.setChildDisplayMode(GuiChildDisplayMode.BLOCK_DOWN);
		div4.setColor(Color.BLUE);
		side.addChild(div4);
		return side;
	}
	
	public void open() {
		setHidden(false);
		
		InventoryMenu inventory = Client.instance.inventory;
		GuiComponent[] divs = { sellSide.getChild(0), sellSide.getChild(1), sellSide.getChild(2), sellSide.getChild(3) };
		for (GuiComponent div : divs) {
			div.clearChildren();
		}
		
		int addCount = 0;
		for (int index = 0; index < Constants.INVENTORY_NUM_ROWS*Constants.INVENTORY_NUM_COLUMNS; index++) {
			ItemStack item = inventory.getItem(index);
			if (item == null) continue;
			GuiComponent sellComponent = new GuiComponent(ItemDefinition.getTexture(item.getTextureId()));
			sellComponent.setUv(ItemDefinition.getUvCoords(item.getTextureId()));
			sellComponent.setSize(16*2, 16*2);
			divs[addCount % 4].addChild(sellComponent);
			++addCount;
			//leftDiv.addChild(sellComponent);
		}
	}
}
