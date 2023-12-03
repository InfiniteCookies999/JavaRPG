package client.game.pathing;

import java.util.Queue;

import client.Client;
import client.game.PlayScreen;
import client.game.entity.Entity;
import client.game.items.GroundItem;
import client.game.world.Location;
import client.game.world.World;
import client.net.out.PickupItemPacketOut;
import lombok.Setter;

public class GroundItemFollower {
	
	private Entity player;
	private PlayScreen playScreen;
	
	@Setter
	private int groundItemId = -1;
	
	private Queue<Location> path;
	
	public GroundItemFollower(Entity player, PlayScreen playScreen) {
		this.player = player;
		this.playScreen = playScreen;
	}
	
	public void update(World world) {
		if (groundItemId == -1) return;
		
		GroundItem groundItem = playScreen.getGroundItem(groundItemId);
		if (groundItem != null) {
			if (player.currentLocation.equals(new Location(groundItem.getLocation()))) {
				Client.instance.inventory.lockAllSlots();
				new PickupItemPacketOut(groundItemId).send();
				cancel();
			} else if (path == null) {
				// Try and start the path to the item
				path = new Pathfinder().findPath(world, player.currentLocation, groundItem.getLocation(), false);
				if (path.isEmpty()) {
					// TODO: send message
					cancel();
				} 
			}
		} else {
			cancel();
		}
		
		if (path != null) {
			if (!player.isMoving()) {
				Location nextLocation = path.poll();
				player.predictiveDirection = nextLocation.sub(player.currentLocation).toDirection();
			}
		}
	}
	
	public void cancel() {
		groundItemId = -1;
		path = null;
	}
}
