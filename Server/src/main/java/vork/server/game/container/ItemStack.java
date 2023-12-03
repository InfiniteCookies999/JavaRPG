package vork.server.game.container;

import lombok.Data;

@Data
public class ItemStack {
	private int id;
	private ItemCatagory catagory;
	private int amount;
	
	public ItemStack clone() {
		ItemStack item = new ItemStack();
		item.id = this.id;
		item.catagory = this.catagory;
		item.amount = this.amount;
		return item;
	}
}
