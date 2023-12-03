package client.game.pathing;

import java.util.Queue;

import client.Client;
import client.game.entity.Direction;
import client.game.entity.Entity;
import client.game.world.Location;
import client.game.world.World;
import client.net.out.ChangeFacingDirectionPacketOut;
import lombok.Getter;
import lombok.Setter;

public class EntityFollower {

	@Setter @Getter
	private int followingEntityId = -1;
	
	private Entity player;
	
	public EntityFollower(Entity player) {
		this.player = player;
	}
	
	public void update(World world) {
		if (followingEntityId == -1) {
			return;
		}
		
		Entity entity = Client.instance.getEntity(followingEntityId);
		if (entity != null && !entity.isDead()) {
			
			Queue<Location> path;
			if (entity.isMoving()) {
				path = new Pathfinder().findPath(world, player.futureLocation, entity.currentLocation, false);
			} else {
				path = new Pathfinder().findPath(world, player.futureLocation, entity.futureLocation, true);
			}
			
			
			if (path.isEmpty()) {
				if (!player.isMoving() && !entity.isMoving() &&
					player.currentLocation.equals(entity.currentLocation)) {
					// Standing on top of the entity so try and move off.
					if (world.isTraversible(player.currentLocation.add(Direction.NORTH))) {
						player.predictiveDirection = Direction.NORTH;
					} else if (world.isTraversible(player.currentLocation.add(Direction.SOUTH))) {
						player.predictiveDirection = Direction.SOUTH;
					} else if (world.isTraversible(player.currentLocation.add(Direction.EAST))) {
						player.predictiveDirection = Direction.EAST;
					} else if (world.isTraversible(player.currentLocation.add(Direction.WEST))) {
						player.predictiveDirection = Direction.WEST;
					} else {
						// nowhere to move to.
						player.predictiveDirection = Direction.NONE;
					}
				} else if (!player.isMoving()) {
					player.predictiveDirection = Direction.NONE;
					// Want to make sure the player is facing the entity they are following.
					Direction directionTowardsEntity = entity.futureLocation.sub(player.futureLocation).toDirection();
					if (directionTowardsEntity != Direction.NONE && directionTowardsEntity != player.getFacingDirection()) {
						player.setFacingDirection(directionTowardsEntity);
						new ChangeFacingDirectionPacketOut(directionTowardsEntity).send();
					}
				} else {
					// empty path.
					player.predictiveDirection = Direction.NONE;
				}
			} else {
				Location toLocation = path.poll();
				player.predictiveDirection = toLocation.sub(player.futureLocation).toDirection();
			}
			
		} else {
			// The entity no longer exist.
			followingEntityId = -1;
		}
	}
}
