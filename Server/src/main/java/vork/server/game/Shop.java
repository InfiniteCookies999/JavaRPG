package vork.server.game;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import vork.server.game.container.ItemStack;

public class Shop {
	
	@AllArgsConstructor
	@Getter
	public class ShopItem {
		private ItemStack item;
		private int price;
	}
	
	@Getter
	@Setter
	private int id;
	
	@Getter
	private List<ShopItem> items = new ArrayList<>();
	
	public void addItem(ItemStack item, int price) {
		items.add(new ShopItem(item, price));
	}
	
	public int priceOf(int index) {
		// TODO: Will want to scale this by
		// how many items were added/taken away from
		// the base amount.
		ShopItem item = items.get(index);
		return item.price;
	}
	
	public boolean hasStockLeft(int index, int amount) {
		ShopItem item = items.get(index);
		return item.item.getAmount() >= amount;
	}
	
	public ItemStack take(int index, int amount) {
		ShopItem shopItem = items.get(index);
		ItemStack taken = shopItem.item.clone();
		shopItem.item.setAmount(shopItem.item.getAmount() - amount);
		taken.setAmount(1);
		return taken;
	}
}
