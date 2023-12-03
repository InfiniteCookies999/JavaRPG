package vork.server.game;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import vork.server.Constants;
import vork.server.FilePath;

public class ArmorSpecsManager {
	
	private static class ArmorSpec {
		private int id;
	}
	
	private static final Map<String, ArmorSpec> ARMOR_SPECS = new HashMap<>();
	
	public static void loadArmorSpecs() throws IOException {
		int idCounter = Constants.ARMOR_IDS_OFFSET;
		FilePath file = FilePath.internal("armor.yaml");
		try (InputStream stream = file.getInputStream()) {
			List<Map<String, Object>> armorsInfo = new Yaml().load(stream);
			for (Map<String, Object> armorInfo : armorsInfo) {
				String name = (String) armorInfo.get("name");
				ArmorSpec armorSpec = new ArmorSpec();
				armorSpec.id = idCounter++;
				ARMOR_SPECS.put(name, armorSpec);
			}
		}
	}
	
	public static int getArmorIdForName(String name) {
		if (!ARMOR_SPECS.containsKey(name)) {
			throw new IllegalArgumentException("Unkown armor by name: " + name);
		}
		
		return ARMOR_SPECS.get(name).id;
	}
}
