package vork.server.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.AllArgsConstructor;
import vork.server.game.container.ItemStack;

public class DropTable {
	
	@AllArgsConstructor
	private class Drop {
		private ItemStack item;
		private int weight;
	}
	
	private static final Random random = new Random();
	
	private List<Drop> drops = new ArrayList<>();
	private List<ItemStack> invariableDrops = new ArrayList<>();
	
	private int totalWeight = 0;
	
	public void addDrop(ItemStack item, int weight) {
		drops.add(new Drop(item, weight));
		totalWeight += weight;
	}
	
	public void addInvariableDrop(ItemStack item) {
		invariableDrops.add(item);
	}
	
	public List<ItemStack> roll(){
		List<ItemStack> items = new ArrayList<>();
		
		if (totalWeight > 0) {
			int x = random.nextInt(totalWeight);
			int count = 0;
			
			for (Drop drop : drops) {
				count += drop.weight;
				if (count > x) {
					items.add(drop.item.clone());
				}
			}	
		}
		
		for (ItemStack invariableDrop : invariableDrops) {
			items.add(invariableDrop.clone());
		}
		
		return items;
	}
}
