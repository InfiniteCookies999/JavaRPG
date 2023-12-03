package vork.server.game;

import lombok.Data;
import vork.server.game.container.ItemStack;
import vork.server.game.entity.Player;

@Data
public class GroundItem {
	private int id;
	private ItemStack item;
	// If not null then only this player can pickup the item.
	private Player owner;
	public int timeAlive = 0;
	
	private Location location;
}
