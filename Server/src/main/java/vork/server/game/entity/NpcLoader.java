package vork.server.game.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.yaml.snakeyaml.Yaml;

import vork.server.FilePath;
import vork.server.game.ArmorSpecsManager;
import vork.server.game.DropTable;
import vork.server.game.DropTableManager;
import vork.server.game.Location;
import vork.server.game.SelectOptions;
import vork.server.game.scripts.S2Script;
import vork.server.game.world.Area;

public class NpcLoader {
	
	private static final Map<String, NpcTemplate> NPC_TEMPLATES = new HashMap<>();
	
	public static void loadTemplate() throws IOException {
		
		FilePath file = FilePath.internal("entities/npc_templates.yaml");
		try (InputStream stream = file.getInputStream()) {
			List<Map<String, Object>> templatesInfo = new Yaml().load(stream);
			for (Map<String, Object> templateInfo : templatesInfo) {
				
				String name = (String) templateInfo.get("name");
				float moveSpeed = (float) (double) templateInfo.get("speed");
				int maxHealth = (int) templateInfo.get("max_health");
				int maxHit = (int) templateInfo.get("max_hit");
				boolean isAttackable = (boolean) templateInfo.get("is_attackable");
				int combatLevel = (int) templateInfo.get("level");
				float chanceToMove = (float) (double) templateInfo.get("chance_to_move");
				float chanceToStop = (float) (double) templateInfo.get("chance_to_stop");
				@SuppressWarnings("unchecked")
				Map<String, Object> rendererInfo = (Map<String, Object>) templateInfo.get("renderer");
				String rendererType = (String) rendererInfo.get("type");
				DropTable dropTable = null;
				if (templateInfo.containsKey("drop_table")) {
					String tableName = (String) templateInfo.get("drop_table");
					dropTable = DropTableManager.getTable(tableName);
				}
				
				NpcTemplate template = new NpcTemplate();
				template.name = name;
				template.moveSpeed = moveSpeed;
				template.maxHealth = maxHealth;
				template.maxHit = maxHit;
				template.combatLevel = combatLevel;
				template.chanceToMove = chanceToMove;
				template.chanceToStop = chanceToStop;
				template.dropTable = dropTable;
				if (isAttackable) {
					template.selectOptions |= SelectOptions.ATTACK;
				}
				switch (rendererType) {
				case "BASIC_MOB": {
					BasicMobRenderData renderData = new BasicMobRenderData();
					renderData.textureId = (int) rendererInfo.get("texture_id");
					template.renderData = renderData;
					break;
				}
				case "HUMAN": {
					HumanRenderData renderData = new HumanRenderData();
					renderData.skinColor = (int) rendererInfo.get("skin_color");
					renderData.eyeColor = (int) rendererInfo.get("eye_color");
					renderData.hairStyle = (int) rendererInfo.get("hair_style");
					renderData.hairColor = (int) rendererInfo.get("hair_color");
					String bodyType = (String) rendererInfo.get("body_type");
					String legsType = (String) rendererInfo.get("legs_type");
					if (bodyType.startsWith("shirt")) {
						String[] tup = bodyType.substring(6).split("_");
						int style = Integer.parseInt(tup[0]);
						int color = Integer.parseInt(tup[1]);
						renderData.bodyType = HumanRenderData.getHumanShirtType(style, color);
					} else {
						renderData.bodyType = ArmorSpecsManager.getArmorIdForName(bodyType);
					}
					if (legsType.startsWith("pants")) {
						String[] tup = legsType.substring(6).split("_");
						int style = Integer.parseInt(tup[0]);
						int color = Integer.parseInt(tup[1]);
						renderData.legsType = HumanRenderData.getHumanPantsType(style, color);
					} else {
						renderData.legsType = ArmorSpecsManager.getArmorIdForName(legsType);
					}
					
					if (rendererInfo.containsKey("helm_type")) {
						String helmType = (String) rendererInfo.get("helm_type");
						renderData.helmType = ArmorSpecsManager.getArmorIdForName(helmType);
					}
					template.renderData = renderData;
					break;
				}
				default:
					throw new RuntimeException("Unknown renderer type: " + rendererType);
				}
				
				NPC_TEMPLATES.put(name, template);
			}
		}
	}
	
	public static Npc createFromTemplate(String name) {
		NpcTemplate template = NPC_TEMPLATES.get(name);
		if (template == null) {
			throw new IllegalArgumentException("Unknown template: " + name);
		}
		
		Npc npc = new Npc();
		npc.setName(name);
		npc.setMaxHealth(template.maxHealth);
		npc.setHealth(template.maxHealth);
		npc.setMaxHit(template.maxHit);
		npc.setMoveSpeed(template.moveSpeed);
		npc.setCombatLevel(template.combatLevel);
		npc.setChanceToMove(template.chanceToMove);
		npc.setChanceToStop(template.chanceToStop);
		npc.setDropTable(template.dropTable);
		npc.selectOptions = template.selectOptions;
		npc.renderData = template.renderData;
		
		return npc;
	}
	
	@SuppressWarnings("unchecked")
	public static void loadNpcs(Consumer<Npc> npcHandler) throws IOException {
		FilePath file = FilePath.internal("entities/npcs.yaml");
		try (InputStream stream = file.getInputStream()) {
			List<Map<String, Object>> npcsInfo = new Yaml().load(stream);
			for (Map<String, Object> npcInfo : npcsInfo) {
				String templateName = (String) npcInfo.get("template");
				Npc npc = createFromTemplate(templateName);
				
				List<Integer> spawn = (List<Integer>) npcInfo.get("spawn_location");
				int spawnX = spawn.get(0), spawnY = spawn.get(1);
				
				npc.setSpawnLocation(new Location(spawnX, spawnY));
				npc.currentLocation = new Location(spawnX, spawnY);
				npc.futureLocation = new Location(spawnX, spawnY);
				npc.setFacingDirection(Direction.SOUTH);
				if (npcInfo.containsKey("boundry")) {
					List<Integer> boundryCoords = (List<Integer>) npcInfo.get("boundry");
					int minX = boundryCoords.get(0), minY = boundryCoords.get(1), maxX = boundryCoords.get(2), maxY = boundryCoords.get(3);
					Area boundry = new Area(minX, minY, maxX, maxY);
					npc.setBoundary(boundry);
				}
				if (npcInfo.containsKey("script")) {
					S2Script script = NpcScriptManager.getScript((String) npcInfo.get("script"));
					npc.setScript(script);
					npc.selectOptions |= SelectOptions.TALK_TO;
				}
				
				npcHandler.accept(npc);
			}
		}
	}
}
