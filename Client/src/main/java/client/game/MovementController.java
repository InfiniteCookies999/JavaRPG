package client.game;

import client.Constants;
import client.game.entity.Direction;
import client.game.entity.Entity;
import client.game.world.Location;
import client.game.world.World;
import client.net.out.ChangeFacingDirectionPacketOut;
import client.net.out.MoveRequestPacketOut;
import vork.math.VorkMath;

public class MovementController {
	
	private void startNewMove(Entity entity, Location location) {
		entity.futureLocation = new Location(location);
		int diffX = entity.futureLocation.x - entity.currentLocation.x;
		int diffY = entity.futureLocation.y - entity.currentLocation.y;
		if (diffX > 0) entity.setFacingDirection(Direction.EAST);
		if (diffX < 0) entity.setFacingDirection(Direction.WEST);
		if (diffY > 0) entity.setFacingDirection(Direction.NORTH);
		if (diffY < 0) entity.setFacingDirection(Direction.SOUTH);
		if (entity.isPrimaryPlayer()) {
			entity.moveQueue.add(location);
			new MoveRequestPacketOut(location.x, location.y).send();
		}
	}
	
	public void finishEntityMove(World world, Entity entity) {
		entity.walkTime -= 1.0f;
		entity.currentLocation = new Location(entity.futureLocation);
		
		boolean hasNextMove = entity.predictiveDirection != Direction.NONE;
		if (!entity.isPrimaryPlayer() && !entity.moveQueue.isEmpty()) {
			hasNextMove = true;
		}
		
		if (hasNextMove) {
			tryNextMove(world, entity);
		}
		
		if (!hasNextMove) {
			if (entity.getFutureFacingDirection() != Direction.NONE) {
				entity.setFacingDirection(entity.getFutureFacingDirection());
				entity.setFutureFacingDirection(Direction.NONE);
			}
			
			// Need to clamp the entity to the tile.
			entity.walkTime = 0.0f;
			entity.x = entity.futureLocation.x * Constants.TILE_SIZE;
			entity.y = entity.futureLocation.y * Constants.TILE_SIZE;
		}
	}
	
	private void tryNextMove(World world, Entity entity) {
		if (entity.predictiveDirection != Direction.NONE) {
			Location moveLocation = entity.futureLocation.add(entity.predictiveDirection);
			if (world.isTraversible(moveLocation)) {
				startNewMove(entity, moveLocation);	
			} else if (entity.getFacingDirection() != entity.predictiveDirection) {
				entity.setFacingDirection(entity.predictiveDirection);
				new ChangeFacingDirectionPacketOut(entity.predictiveDirection).send();
			}
		} else if (!entity.isPrimaryPlayer() && !entity.moveQueue.isEmpty()) {
			Location moveLocation = entity.moveQueue.poll();
			startNewMove(entity, moveLocation);
		}
	}

	public void processMovement(World world, Entity entity) {
		
		Location current = entity.currentLocation;
		Location future = entity.futureLocation;
		
		if (current.equals(future)) {
			tryNextMove(world, entity);
		}
		
		if (current.equals(future)) {
			return;
		}
		
		float frameMove = entity.getMoveSpeed() / 60.0f;
		// frameMove *= deltaTime;
		if (entity.isPrimaryPlayer()) {
			if (entity.moveQueue.size() > 2) {
				// If the client is falling behind the
				// server then we want to slow the player
				// down so that they can catch up with the
				// server.
				
				int slowDownCount = entity.moveQueue.size();
				if (slowDownCount > 16) {
					slowDownCount = 16;
				}
				float slowDown = 1 + slowDownCount * 0.15f;
				frameMove /= slowDown;
			}
		} else {
			if (entity.moveQueue.size() > 2) {
				int speedUpCount = entity.moveQueue.size();
				if (speedUpCount > 16) {
					speedUpCount = 16;
				}
				float speedUp = 1 + speedUpCount * 0.35f;
				frameMove *= speedUp;
			}
		}
		
		
		entity.walkTime += frameMove;
		
		entity.x = VorkMath.lerp(current.x, future.x, entity.walkTime) * Constants.TILE_SIZE;
		entity.y = VorkMath.lerp(current.y, future.y, entity.walkTime) * Constants.TILE_SIZE;
		
		if (entity.walkTime >= 1.0f) {
			finishEntityMove(world, entity);
		}
	}
}
