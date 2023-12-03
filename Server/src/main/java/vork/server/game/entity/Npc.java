package vork.server.game.entity;

import java.util.Random;

import lombok.Getter;
import lombok.Setter;
import vork.server.Constants;
import vork.server.Server;
import vork.server.Timing;
import vork.server.game.DropTable;
import vork.server.game.Location;
import vork.server.game.scripts.S2Script;
import vork.server.game.world.Area;
import vork.server.net.out.ChangeFacingDirectionPacketOut;

public class Npc extends Entity {

	@Setter
	private float chanceToMove;
	@Setter
	private float chanceToStop;
	
	@Getter @Setter
	private Direction moveDirection = Direction.NONE;
	
	private int numTicksDead = 0;
	private int numTicksNotAttacking = 0;
	
	private static final Random random = new Random();
	
	@Setter
	private Area boundary;
	
	@Setter
	private int maxHit;
	
	@Setter @Getter
	private DropTable dropTable;
	
	@Setter @Getter
	private S2Script script;
	
	public void update() {
		if (!isDead()) {
			numTicksDead = 0;
			
			generateMove();
			Server.instance.getMoveController().processMovement(world, this);	
			
			processAction();
			
			Entity target = getCombatTarget();
			if (target != null) {
				if (this.distanceTo(target) > 1) {
					if (numTicksNotAttacking++ > Timing.seconds(6)) {
						// Haven't been able to attack for some time so give up
						cancelAction();
					}
				} else {
					numTicksNotAttacking = 0;
				}
			} else {
				numTicksNotAttacking = 0;
			}
			
		} else {
			if (numTicksDead > Timing.minutes(1)) {
				respawn();
			}
			++numTicksDead;
		}
	}
	
	private Direction generateRandomMove() {
		
		int count = 0;
		Direction[] validDirections = new Direction[4];
		
		if (world.isTraversible(futureLocation.add(Direction.NORTH))) {
			validDirections[count++] = Direction.NORTH;
		}
		if (world.isTraversible(futureLocation.add(Direction.EAST))) {
			validDirections[count++] = Direction.EAST;
		}
		if (world.isTraversible(futureLocation.add(Direction.SOUTH))) {
			validDirections[count++] = Direction.SOUTH;
		}
		if (world.isTraversible(futureLocation.add(Direction.WEST))) {
			validDirections[count++] = Direction.WEST;
		}
		if (count == 0)
			return Direction.NONE;
		
		return validDirections[random.nextInt(count)];
	}
	
	public void generateMove() {
		Entity defender = getCombatTarget();
		if (defender != null) {
			follow(defender);
		} else {
			generateStandardMove();
		}
	}
	
	private void follow(Entity entity) {
		if (!currentLocation.equals(futureLocation)) {
			return; // already moving.
		}
		
		if (distanceTo(entity) > 1) {
			boolean foundMove = false;
			if (entity.futureLocation.y > currentLocation.y) {
				moveDirection = Direction.NORTH;
				foundMove = world.isTraversible(currentLocation.add(moveDirection));
			}
			if (entity.futureLocation.x > currentLocation.x && !foundMove) {
				moveDirection = Direction.EAST;
				foundMove = world.isTraversible(currentLocation.add(moveDirection));
			}
			if (entity.futureLocation.y < currentLocation.y && !foundMove) {
				moveDirection = Direction.SOUTH;
				foundMove = world.isTraversible(currentLocation.add(moveDirection));
			}
			if (entity.futureLocation.x < currentLocation.x && !foundMove) {
				moveDirection = Direction.WEST;
				foundMove = world.isTraversible(currentLocation.add(moveDirection));
			}
			if (foundMove) {
				// Making sure the entity is with the boundary
				Location moveLocation = currentLocation.add(moveDirection);
				if (boundary != null && !boundary.contains(moveLocation)) {
					moveDirection = Direction.NONE;
				}
			} else {
				moveDirection = Direction.NONE;
			}
		} else {
			moveDirection = Direction.NONE;
			// Making sure the entity is facing its attacker
			Direction directionTowardsEntity = entity.futureLocation.sub(currentLocation).toDirection();
			if (directionTowardsEntity != Direction.NONE && getFacingDirection() != directionTowardsEntity) {
				setFacingDirection(directionTowardsEntity);
				emitPacket(new ChangeFacingDirectionPacketOut(this, directionTowardsEntity));
			}
		}
	}
	
	private void generateStandardMove() {
		final int PRECISION = 100000;
		int chance = random.nextInt(PRECISION);
		Direction generatedDirection = Direction.NONE;
		if (Server.instance.tick % (Constants.TPS/2) == 0) {
			if (moveDirection == Direction.NONE) {
				// Not moving so try and start a new move.
				
				if (chance < PRECISION*chanceToMove) {
					generatedDirection = generateRandomMove();
				}
			} else if (currentLocation.equals(futureLocation)) {
				if (chance < PRECISION*chanceToStop) {
					// Stopped moving
					moveDirection = Direction.NONE;
				} else {
					// Moving but still need to generate a new move.
					// TODO: Maybe have a number that indicates if the
					// new move direction should be random or not?
					generatedDirection = generateRandomMove();
				}
			}	
		} else {
			if (currentLocation.equals(futureLocation)) {
				if (chance < PRECISION*chanceToStop) {
					// Stopped moving
					moveDirection = Direction.NONE;
				}
			}
		}
		
		if (moveDirection != null) {
			// Making sure the entity is with the boundary
			Location moveLocation = currentLocation.add(generatedDirection);
			if (boundary == null || boundary.contains(moveLocation)) {
				moveDirection = generatedDirection;
			}
		}
	}

	@Override
	public int getMaxHit() {
		return maxHit;
	}
}
