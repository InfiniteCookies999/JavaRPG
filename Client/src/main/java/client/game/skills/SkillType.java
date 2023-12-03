package client.game.skills;

public class SkillType {
	public static final int MELEE   = 0;
	public static final int DEFENSE = 1;
	
	public static final int NUM_SKILLS = 2;
	
	public static final String[] NAMES = {
		"melee",
		"defense"
	};
	
	public static String getName(int type) {
		return NAMES[type];
	}
}
