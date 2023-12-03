package client.game.menus.container;

import client.Constants;
import client.game.Resources;
import client.game.items.ItemStack;
import vork.gfx.Color;
import vork.gfx.gui.GuiComponent;
import vork.gfx.gui.GuiLocation;
import vork.gfx.gui.GuiRenderMode;
import vork.gfx.gui.drag.GuiDragAndDrop;

public class InventoryMenu extends GuiComponent {
	
	private static final int NUM_COLUMNS = Constants.INVENTORY_NUM_COLUMNS;
	private static final int NUM_ROWS = Constants.INVENTORY_NUM_ROWS;
	
	private static final int WIDTH = 200;
	private static final int SLOT_SIZE = WIDTH / NUM_COLUMNS;
	
	private static final int HEIGHT = NUM_ROWS * SLOT_SIZE;
	
	private ContainerSlot[] slots = new ContainerSlot[NUM_COLUMNS * NUM_ROWS];
	
	public InventoryMenu(GuiDragAndDrop dragAndDrop) {
		super(GuiLocation.BOTTOM_RIGHT, WIDTH, HEIGHT);
		setRenderMode(GuiRenderMode.NINE_PATCH);
		setTexture(Resources.getUITexture("window_panel_1.png"));
	
		for (int y = 0; y < NUM_ROWS; y++) {
			for (int x = 0; x < NUM_COLUMNS; x++) {
				int slotIndex = NUM_ROWS*NUM_COLUMNS - (y * NUM_COLUMNS) - NUM_COLUMNS + x;
				GuiComponent slotComponent = new GuiComponent(SLOT_SIZE, SLOT_SIZE);
				slotComponent.setColor(Color.FULL_TRANSPARENT);
				slotComponent.setPosition(x * SLOT_SIZE, y * SLOT_SIZE);
				ContainerSlot slot = new ContainerSlot(slotComponent, slotIndex);
				dragAndDrop.addSource(slot.getSource());
				dragAndDrop.addTarget(slot.getTarget());
				addChild(slotComponent);
				slots[slotIndex] = slot;
			}
		}
	}
	
	public void lockAllSlots() {
		for (ContainerSlot slot : slots) {
			slot.lock();
		}
	}
	
	public void unlockAllSlots() {
		for (ContainerSlot slot : slots) {
			slot.unlock();
		}
	}
	
	public void setItem(int slot, ItemStack item) {
		slots[slot].setItemStack(item);
		slots[slot].unlock();
	}
	
	public void moveItem(int fromSlot, int toSlot) {
		ItemStack fromItem = slots[fromSlot].getItemStack();
		ItemStack toItem = slots[toSlot].getItemStack();
		slots[fromSlot].setItemStack(toItem);
		slots[toSlot].setItemStack(fromItem);;
		slots[fromSlot].unlock();
		slots[toSlot].unlock();
	}
	
	public ItemStack getItem(int index) {
		ContainerSlot slot = slots[index];
		return slot.getItemStack();
	}
}
