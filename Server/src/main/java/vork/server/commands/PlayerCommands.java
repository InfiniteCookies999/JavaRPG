package vork.server.commands;

import vork.server.Constants;
import vork.server.Server;
import vork.server.game.container.ItemBuilder;
import vork.server.game.container.ItemStack;
import vork.server.game.entity.Player;
import vork.server.game.skills.Skill;
import vork.server.game.skills.SkillType;

public class PlayerCommands {

	private static Player getPlayerByName(String name) {
		return Server.instance.getPlayerByName(name);
	}
	
	@Command(cmd = "level")
	public void onSetLevel(String playerName, String skillName, int level) {
		Player player = getPlayerByName(playerName);
		if (player == null) {
			System.out.println("Player '" + playerName + "' not found.");
			return;
		}
		
		if (level <= 0) {
			System.out.println("Level must be greater than or equal to zero");
			return;
		}
		
		if (level > Constants.MAX_SKILL_LEVEL) {
			System.out.println("Level was greater than the maximum allowed level");
			return;
		}
		
		SkillType.getSkillTypeByName(skillName.toLowerCase())
			.ifPresentOrElse(skillType -> {
				Skill skill = player.skills[skillType];
				skill.setXp(Skill.getXpForLevel(level));
				System.out.println("(+) set '" + playerName + "' " + skillName + " level to " + level);
			},
			() -> {
				System.out.println("Failed to find skill by name: " + skillName);
			});
	}
	
	@Command(cmd = "giveitem")
	public void onGiveItem(String playerName, String itemName) {
		Player player = getPlayerByName(playerName);
		if (player == null) {
			System.out.println("Player '" + playerName + "' not found.");
			return;
		}
		
		try {
			ItemStack item = new ItemBuilder(itemName, 1).build();
			player.giveItem(item);
		} catch (IllegalArgumentException e) {
			System.out.println("Could not find item by name '" + itemName + "'");
		}
	}
}
