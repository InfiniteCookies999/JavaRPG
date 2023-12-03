package vork.server.game.container;

import vork.server.Constants;

public class Inventory {
	
	public static final int NUM_SLOTS = Constants.INVENTORY_NUM_ROWS * Constants.INVENTORY_NUM_COLUMNS;
	
	private ItemStack[] slots = new ItemStack[NUM_SLOTS];
	
	public boolean isFull() {
		for (int i = 0; i < slots.length; i++) {
			if (slots[i] == null) {
				return false;
			}
		}
		return true;
	}
	
	public int addItem(ItemStack item) {
		for (int i = 0; i < slots.length; i++) {
			if (slots[i] == null) {
				slots[i] = item;
				return i;
			}
		}
		return -1;
	}
	
	public void setItem(ItemStack item, int slotId) {
		slots[slotId] = item;
	}
	
	public ItemStack getItem(int slotId) {
		return slots[slotId];
	}
}
