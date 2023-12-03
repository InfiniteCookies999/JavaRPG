package client.game.skills;

import lombok.Getter;

public class Skill {

	@Getter
	private int xp;
	@Getter
	private int level = 1;
	
	private static final int MAX_SKILL_LEVEL = 140;
	private static final int[] LEVEL_XP_CACHE = new int [MAX_SKILL_LEVEL+1];
	
	static {
		double base = 322.0;
		double limiter = 4.5;
		for (int level = 1, x = 0; level <= MAX_SKILL_LEVEL; level++) {
			int xpNeeded = 0;
			if (level > 1) {
				int r = level-1;
				x += Math.floor(r + base * Math.pow(2, (r/7.0) - (r * 0.008)));
			}
			
			xpNeeded = (int) Math.floor(x / limiter);
			
			LEVEL_XP_CACHE[level] = xpNeeded;
		}
	}
	
	public int give(int xp) {
		this.xp += xp;
		
		int newLevel = calculateLevelBasedOnXp();
		int levelsGained = newLevel - level;
		
		level = newLevel;
		
		return levelsGained;
	}
	
	public void setXp(int xp) {
		this.xp = xp;
		level = calculateLevelBasedOnXp();
	}
	
	private int calculateLevelBasedOnXp() {
		if (xp >= LEVEL_XP_CACHE[MAX_SKILL_LEVEL]) {
			return MAX_SKILL_LEVEL;
		}
		
		int level = 1;
		while (true) {
			if (xp >= LEVEL_XP_CACHE[level+1]) {
				++level;
			} else {
				break;
			}
		}
		
		return level;
	}
}
