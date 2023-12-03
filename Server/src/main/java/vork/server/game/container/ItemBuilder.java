package vork.server.game.container;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import vork.server.FilePath;

public class ItemBuilder {
	
	private static final List<ItemDefinition> DEFINITIONS = new ArrayList<>();
	private static final Map<String, Integer> NAMES_TO_IDS = new HashMap<>();
	
	public static void loadItemDefinitions() throws IOException {
		FilePath itemsFile = FilePath.internal("items.yaml");	
		try (InputStream itemsInStream = itemsFile.getInputStream()) {
			List<Map<String, Object>> contents = new Yaml().load(itemsInStream);
			
			int idCount = 0;
			for (Map<String, Object> itemDef : contents) {
				String name = (String) itemDef.get("name");
				ItemCatagory catagory = ItemCatagory.valueOf((String) itemDef.get("catagory"));
				
				ItemDefinition definition = new ItemDefinition();
				definition.setId(idCount);
				definition.setName(name);
				definition.setCatagory(catagory);
				DEFINITIONS.add(definition);
				NAMES_TO_IDS.put(name.toLowerCase(), DEFINITIONS.size() - 1);
			
				++idCount;
			}
		}
	}
	
	private ItemStack item;
	
	public ItemBuilder(int itemId) {
		this(itemId, 1);
	}
	
	public ItemBuilder(int itemId, int amount) {
		construct(itemId, amount);	
	}
	
	public ItemBuilder(String itemName, int amount) {
		itemName = itemName.toLowerCase();
		if (!NAMES_TO_IDS.containsKey(itemName)) {
			throw new IllegalArgumentException("Unknown item name: " + itemName);
		}
		construct(NAMES_TO_IDS.get(itemName), amount);
	}
	
	private void construct(int itemId, int amount) {
		ItemDefinition definition = DEFINITIONS.get(itemId);
		item = new ItemStack();
		item.setId(itemId);
		item.setCatagory(definition.getCatagory());
		item.setAmount(amount);
	}
	
	public ItemStack build() {
		return item;
	}
}
