package vork.server.game.skills;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SkillType {
	public static final int MELEE   = 0;
	public static final int DEFENSE = 1;
	
	public static final int NUM_SKILLS = 2;
	
	public static final String[] NAMES = {
		"melee",
		"defense"
	};
	
	private static final Map<String, Integer> NAME_TO_ID_MAP = new HashMap<>();
	static {
		for (int i = 0; i < NAMES.length; i++) {
			NAME_TO_ID_MAP.put(NAMES[i], i);
		}
	}
	
	public static String getName(int type) {
		return NAMES[type];
	}
	
	public static Optional<Integer> getSkillTypeByName(String skillName) {
		if (NAME_TO_ID_MAP.containsKey(skillName)) {
			return Optional.of(NAME_TO_ID_MAP.get(skillName));
		}
		return Optional.empty();
	}
}
