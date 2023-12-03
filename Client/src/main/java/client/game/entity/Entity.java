package client.game.entity;

import java.util.LinkedList;
import java.util.Queue;

import client.game.world.Location;
import lombok.Getter;
import lombok.Setter;
import vork.gfx.SpriteBatch;

public class Entity {

	public int id; // Id generated by server to uniquely identify the entity.
	
	@Getter @Setter
	private String name;
	
	@Getter @Setter
	private boolean isPrimaryPlayer = false;
	
	public float x, y;
	
	@Getter @Setter
	private float moveSpeed;
	
	public float walkTime = 0.0f;
	
	@Getter @Setter
	private int health, maxHealth;
	
	@Getter  @Setter
	private int combatLevel;
	
	@Setter
	private int changeInHealth;
	
	public Location currentLocation;
	public Location futureLocation;
	
	public int selectOptions = 0;
	
	@Getter @Setter
	private Direction facingDirection;
	
	// Direction to face after the move queue is empty
	@Setter @Getter
	private Direction futureFacingDirection = Direction.NONE;
	
	// Rendering interface for all the ways an entity
	// may be rendered.
	public EntityRenderer renderer;
	
	// Only applies to the player. Basically tells which
	// direction to move next when holding keys.
	public Direction predictiveDirection = Direction.NONE;
	
	public Queue<Location> moveQueue = new LinkedList<>();
	
	public boolean isDead() {
		return health <= 0;
	}
	
	public boolean isMoving() {
		return !currentLocation.equals(futureLocation) ||
				(!isPrimaryPlayer && !moveQueue.isEmpty())
				;
	}
	
	public void render(SpriteBatch batch) {
		renderer.render(batch, this);
	}
}
