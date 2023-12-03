package vork.server.game.skills;

import lombok.Getter;
import vork.server.Constants;
import vork.server.game.entity.AnimationType;
import vork.server.game.entity.Player;
import vork.server.net.out.AnimationPacketOut;
import vork.server.net.out.SetXpPacketOut;

public class Skill {
	
	private int type;
	@Getter
	private int xp;
	@Getter
	private int level = 1;
	@Getter
	private int boostedLevel = 1;
	private Player player;
	
	private static final int[] LEVEL_XP_CACHE = new int [Constants.MAX_SKILL_LEVEL+1];
	
	static {
		double base = 322.0;
		double limiter = 4.5;
		//int prevXpNeeded = 0;
		for (int level = 1, x = 0; level <= Constants.MAX_SKILL_LEVEL; level++) {
			int xpNeeded = 0;
			if (level > 1) {
				int r = level-1;
				x += Math.floor(r + base * Math.pow(2, (r/7.0) - (r * 0.008)));
			}
			
			xpNeeded = (int) Math.floor(x / limiter);
			
			LEVEL_XP_CACHE[level] = xpNeeded;
			//System.out.println(String.format("level = %s, xp needed = %s, difference = %s", level, xpNeeded, xpNeeded - prevXpNeeded));
			//prevXpNeeded = xpNeeded;
		}
	}
	
	public Skill(int type, Player player, int initXp) {
		this.type = type;
		this.player = player;
		give(initXp, true);
	}
	
	public void give(int xp, boolean init) {
		if (this.xp == Integer.MAX_VALUE) return;
		if (this.xp + xp < this.xp) {
			this.xp = Integer.MAX_VALUE;
		}
		this.xp += xp;
		
		int newLevel = calculateLevelBasedOnXp();
		if (newLevel > level) {
			if (boostedLevel >= level && boostedLevel < newLevel) {
				// If their level is boosted to be more than their level
				// then don't let it become less than the new level.
				boostedLevel = newLevel;
			}
			level = newLevel;
			
			if (!init) {
				player.sendServerMessage("You earned " + xp + " xp!");
				player.emitPacket(new AnimationPacketOut(player, AnimationType.LEVEL_UP));
			}
		}
		
		new SetXpPacketOut(type, xp).send(player);
	}
	
	public void setXp(int xp) {
		this.xp = xp;
		boostedLevel = level = calculateLevelBasedOnXp();
		new SetXpPacketOut(type, xp).send(player);
	}
	
	private int calculateLevelBasedOnXp() {
		if (xp >= LEVEL_XP_CACHE[Constants.MAX_SKILL_LEVEL]) {
			return Constants.MAX_SKILL_LEVEL;
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
	
	public static int getXpForLevel(int level) {
		return LEVEL_XP_CACHE[level];
	}
}
