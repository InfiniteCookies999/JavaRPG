package vork.server.game.entity;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import vork.server.Constants;
import vork.server.game.Location;
import vork.server.game.event.CombatEvent;
import vork.server.game.event.Event;
import vork.server.game.world.Chunk;
import vork.server.game.world.World;
import vork.server.net.out.EntityTeleportPacketOut;
import vork.server.net.out.HealthChangePacketOut;
import vork.server.net.out.PacketOut;

public abstract class Entity {

	public int id;
	
	@Getter @Setter
	private String name;
	
	@Getter @Setter
	private int maxHealth;
	
	@Getter @Setter
	private int health;
	
	@Setter @Getter
	private int combatLevel;
	
	@Setter @Getter
	private float moveSpeed;
	
	@Getter @Setter
	private Direction facingDirection;
	
	@Setter
	protected World world;
	
	@Setter @Getter
	protected Chunk chunk;
	
	@Setter @Getter
	private Location spawnLocation;
	
	public Location currentLocation;
	public Location futureLocation;
	
	public float walkTime = 0.0f;
	
	public int selectOptions;
	
	public EntityRenderData renderData;
	
	/**
	 * The event the entity is engaging in.
	 */
	@Getter
	private Event action;
	
	@Getter
	private Entity combatTarget = null;
	public List<Entity> attackingEntities = new ArrayList<>();
	
	public boolean isDead() {
		return health <= 0;
	}
	
	public void targetEntity(Entity target) {
		if (target == this) {
			// Entities cannot attack themselves.
			return;
		}
		
		this.combatTarget = target;
	}
	
	public void handleTileChange() {
		Chunk newChunk = world.getChunkFromTileLocation(currentLocation);
		if (newChunk != chunk) {
			chunk.removeEntity(this);
			newChunk.addEntity(this);
		}
	}
	
	/**
	 * Sends packets to all the entities in the nearby
	 * chunks that would be able to see the entity.
	 * 
	 * @param packet the packet to send.
	 */
	public void emitPacket(PacketOut packet) {
		for (int y = chunk.chunkY - 1; y <= chunk.chunkY + 1; y++) {
			for (int x = chunk.chunkX - 1; x <= chunk.chunkX + 1; x++) {
				Chunk chunk = world.getChunk(x, y);
				if (chunk == null) continue;
				
				for (Player player : chunk.players) {
					packet.send(player);
				}
			}	
		}
	}
	
	public int distanceTo(Entity entity) {
		int diffX = entity.futureLocation.x - futureLocation.x;
		int diffY = entity.futureLocation.y - futureLocation.y;
		return Math.abs(diffX) + Math.abs(diffY);
	}

	public void damage(int damageDelt) {
		if (damageDelt < 0) {
			throw new IllegalArgumentException("Cannot deal negative damage");
		}
		
		health -= damageDelt;
		if (health < 0) {
			health = 0; // Don't let the health fall below zero.
		}
		
		emitPacket(new HealthChangePacketOut(this, -damageDelt, false));
		
		if (health == 0) {
			handleDeath();
		}
	}
	
	public void heal(int amount, boolean isPassive) {
		health += amount;
		if (health > maxHealth) {
			health = maxHealth;
		}
		
		emitPacket(new HealthChangePacketOut(this, amount, isPassive));
	}
	
	public void handleDeath() {
		// Entity is dead so they can no longer do whatever
		// action they were doing.
		cancelAction();
	}
	
	public void cancelAction() {
		Entity target = getCombatTarget();
		if (target != null) {
			target.removeAttacker(this);	
		}
		combatTarget = null;
		action = null;
	}
	
	protected void processAction() {
		if (combatTarget != null && action == null) {
			tryToAttack();
		}
		
		
		if (action == null) return;
		if (action instanceof CombatEvent) {
			if (!((CombatEvent) action).ourTurn(this)) {
				return;
			}
		}
		action.process();
		if (action.isOver()) {
			cancelAction();
		}
	}
	
	public void tryToAttack() {
		Entity target = getCombatTarget();
		if (target == null || target.isDead()) {
			cancelAction();
			return;
		}
		
		if (this.distanceTo(target) > 1) {
			return;
		}
		
		if (!CombatEvent.checkAgainstMulticombat(this, target)) {
			cancelAction();
			return;
		}
		
		Entity entityTargetIsTargetting = target.getCombatTarget();
		if (entityTargetIsTargetting == null ||
				entityTargetIsTargetting.id != id) {
			// If the defending entity is not attacking or they are attacking a
			// different entity from this entity then a new combat cycle can begin.
			setAction(new CombatEvent(this, target));
		} else {
			// The other entity is already attacking this entity so we join their action.
			Event targetAction = target.getAction();
			if (targetAction == null) {
				// They are targetting but have not started combat.
				setAction(new CombatEvent(this, target));
			} else {
				setAction(targetAction);
			}
		}
		
		target.addAttacker(this);
	}
	
	public void setAction(Event action) {
		Entity combatTarget = this.combatTarget;
		cancelAction();
		this.action = action;
		this.combatTarget = combatTarget;
	}
	
	public void addAttacker(Entity attacker) {
		attackingEntities.add(attacker);
	}
	
	public void removeAttacker(Entity attacker) {
		attackingEntities.remove(attacker);
	}
	
	public boolean isUnderAttack() {
		return !attackingEntities.isEmpty();
	}
	
	public abstract int getMaxHit();
	
	/**
	 * How much time must go by before the entity
	 * can attack again.
	 */
	public int getAttackSpeed() {
		return Constants.TPS;
	}
	
	public void teleport(Location location) {
		if (this instanceof Player) {
			Player player = ((Player) this);
			player.predictiveFacingDirection = Direction.NONE;
			player.clearMoveQueue();
		}
		currentLocation = new Location(location);
		futureLocation = new Location(location);
		
		cancelAction();
		handleTileChange();
		
		emitPacket(new EntityTeleportPacketOut(this));
	}
	
	public void respawn() {
		teleport(spawnLocation);
		setHealth(getMaxHealth());
		emitPacket(new HealthChangePacketOut(this, getMaxHealth(), true));
	}

	public boolean isAttacking() {
		return combatTarget != null;
	}
}
