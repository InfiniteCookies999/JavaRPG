package client.game.items;

import lombok.Data;

@Data
public class ItemStack {
	
	private int textureId;
	private ItemCatagory catagory;
	private String name;
	private int amount;

	public ItemStack(ItemDefinition definition) {
		this.textureId = definition.getTextureId();
		this.catagory = definition.getCatagory();
		this.name = definition.getName();
		this.amount = 1;
	}
	
	public ItemStack(ItemDefinition definition, int amount) {
		this.textureId = definition.getTextureId();
		this.catagory = definition.getCatagory();
		this.name = definition.getName();
		this.amount = amount;
	}
}
