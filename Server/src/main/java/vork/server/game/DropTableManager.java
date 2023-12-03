package vork.server.game;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import vork.server.FilePath;
import vork.server.game.container.ItemBuilder;

public class DropTableManager {

	private static final Map<String, DropTable> DROP_TABLES = new HashMap<>();
	
	public static void loadTables() throws IOException {
		FilePath path = FilePath.internal("drop_tables.yaml");
		try (InputStream stream = path.getInputStream()) {
			List<Map<String, Object>> tablesInfo = new Yaml().load(stream);
			for (Map<String, Object> tableInfo : tablesInfo) {
				String tableName = (String) tableInfo.get("name");
				DropTable dropTable = new DropTable();
				
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> itemsInfo = (List<Map<String, Object>>) tableInfo.get("items");
				for (Map<String, Object> itemInfo : itemsInfo) {
					String itemName = (String) itemInfo.get("item_name");
					int itemAmount = (int) itemInfo.get("amount");	
					int weight = (int) itemInfo.get("weight");
					boolean invariant = false;
					if (weight < 0) {
						weight = 0;
						invariant = true;
					}
					
					ItemBuilder builder = new ItemBuilder(itemName, itemAmount);
					if (invariant) {
						dropTable.addInvariableDrop(builder.build());
					} else {
						dropTable.addDrop(builder.build(), weight);
					}
				}
				
				DROP_TABLES.put(tableName, dropTable);
			}
		}
	}
	
	public static DropTable getTable(String tableName) {
		if (!DROP_TABLES.containsKey(tableName)) {
			throw new IllegalArgumentException("Unknown drop table: " + tableName);
		}
		
		return DROP_TABLES.get(tableName);
	}
}
