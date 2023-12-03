package vork.server.game;

import vork.server.Constants;
import vork.server.game.entity.Direction;
import vork.server.game.entity.Entity;
import vork.server.game.entity.Npc;
import vork.server.game.entity.Player;
import vork.server.game.world.World;
import vork.server.net.out.ChangeFacingDirectionPacketOut;
import vork.server.net.out.EntityMovePacketOut;

public class MoveController {
	
	private void startNewEntityMove(Entity entity, Location location) {
		entity.futureLocation = location;
		int diffX = entity.futureLocation.x - entity.currentLocation.x;
		int diffY = entity.futureLocation.y - entity.currentLocation.y;
		if (diffX > 0) entity.setFacingDirection(Direction.EAST);
		if (diffX < 0) entity.setFacingDirection(Direction.WEST);
		if (diffY > 0) entity.setFacingDirection(Direction.NORTH);
		if (diffY < 0) entity.setFacingDirection(Direction.SOUTH);
		entity.emitPacket(new EntityMovePacketOut(entity.id, location.x, location.y));
	}
	
	private void finishEntityMove(World world, Entity entity) {
		entity.walkTime -= 1.0f;
		
		entity.currentLocation = new Location(entity.futureLocation);
		entity.handleTileChange();
		
		if (entity instanceof Player) {
			Player player = (Player) entity;
			if (player.moveQueueSize == 0) {
				// Player finished their movement
				
				entity.walkTime = 0.0f;
			
				player.tryToPickupGroundItem();
				
				if (player.predictiveFacingDirection != Direction.NONE) {
					player.emitPacket(new ChangeFacingDirectionPacketOut(player, player.predictiveFacingDirection));
					player.setFacingDirection(player.predictiveFacingDirection);
					player.predictiveFacingDirection = Direction.NONE;
				}
			}
		} else {
			// TODO: Uhh, this really what I want?
			entity.walkTime = 0.0f;
		}
	}
	
	public void processMovement(World world, Entity entity) {
		Location current = entity.currentLocation;
		Location future = entity.futureLocation;
		
		if (current.equals(future)) {
			if (entity instanceof Player) {
				Player player = (Player) entity;
				if (player.moveQueueSize != 0) {
					Location moveLocation = player.moveQueue[0];
					// rotating the array left to pop the value
					for (int i = 1; i < player.moveQueueSize; i++) {
						player.moveQueue[i-1] = player.moveQueue[i];
					}
					--player.moveQueueSize;
					startNewEntityMove(player, moveLocation);
				}
			} else {
				Npc npc = (Npc) entity;
				if (npc.getMoveDirection() != Direction.NONE) {
					Location moveLocation = npc.futureLocation.add(npc.getMoveDirection());
					if (world.isTraversible(moveLocation)) {
						startNewEntityMove(npc, moveLocation);	
					}
				}
			}
		}
		
		if (current.equals(future)) {
			return;
		}
		
		float frameMove = entity.getMoveSpeed() / Constants.TPS;
		entity.walkTime += frameMove;
		
		if (entity.walkTime >= 1.0f) {
			finishEntityMove(world, entity);
		}
	}
}
