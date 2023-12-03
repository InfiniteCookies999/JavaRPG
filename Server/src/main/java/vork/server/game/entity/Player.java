package vork.server.game.entity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import vork.server.Constants;
import vork.server.Server;
import vork.server.Timing;
import vork.server.game.ChatTextType;
import vork.server.game.GroundItem;
import vork.server.game.Location;
import vork.server.game.container.Inventory;
import vork.server.game.container.ItemStack;
import vork.server.game.scripts.S2Interpreter;
import vork.server.game.scripts.S2Script;
import vork.server.game.skills.Skill;
import vork.server.game.skills.SkillType;
import vork.server.game.world.Chunk;
import vork.server.net.NetworkHandle;
import vork.server.net.out.ChangeFacingDirectionPacketOut;
import vork.server.net.out.ChatTextPacketOut;
import vork.server.net.out.DespawnGroundItemPacketOut;
import vork.server.net.out.EntityDespawnPacketOut;
import vork.server.net.out.EntitySpawnPacketOut;
import vork.server.net.out.NpcChatPacketOut;
import vork.server.net.out.PacketOut;
import vork.server.net.out.PickupItemPacketOut;
import vork.server.net.out.SetItemPacketOut;
import vork.server.net.out.SpawnGroundItemPacketOut;

public class Player extends Entity {

	@Getter
	private NetworkHandle networkHandle;

	private Set<Player> visiblePlayers = new HashSet<>();
	private Set<Npc> visibleNpcs = new HashSet<>();
	
	public Location[] moveQueue = new Location[Constants.MAX_MOVE_QUEUE_SIZE];
	public int moveQueueSize = 0;
	
	// Facing direction once the entity has finished moving.
	public Direction predictiveFacingDirection = Direction.NONE;
	// Ground item that the player wants to pickup once they finish
	// there move queue.
	@Setter
	private int groundItemToPickup = -1;
	
	@Setter
	private Npc npcToTalkTo;
	
	private int passiveHealTick = 0;
	
	public Skill[] skills = new Skill[SkillType.NUM_SKILLS];
	
	@Getter
	private Inventory inventory = new Inventory();
	
	private S2Interpreter npcScriptInterpreter;
	
	@Setter
	private int shopId = -1;
	
	private static final int[][] VISIBLE_CHUNK_ORDER = {
			{ +0, +0 },
			{ +1, +0 },
			{ +0, +1 },
			{ -1, +0 },
			{ +0, -1 },
			{ +1, +1 },
			{ +1, -1 },
			{ -1, +1 },
			{ -1, -1 },
	};
	
	public Player(NetworkHandle networkHandle) {
		this.networkHandle = networkHandle;
	}
	
	public void update() {
		// Update the visibility to know who to send packets to.
		updateEntityVisibility();
		
		if (!isDead()) {
			Server.instance.getMoveController().processMovement(world, this);
			
			if (predictiveFacingDirection != Direction.NONE &&
				moveQueueSize == 0 &&
				currentLocation.equals(futureLocation)) {
				
				setFacingDirection(predictiveFacingDirection);
				// Letting the other clients know about the change in direction.
				emitPacket(new ChangeFacingDirectionPacketOut(this, predictiveFacingDirection));
				
				predictiveFacingDirection = Direction.NONE;
			}
			
			passiveHeal();
			
			processAction();
			
			tryToTalkToNpc();
			
		}
	}
	
	private void passiveHeal() {
		if (getHealth() < getMaxHealth()) {
			if (++passiveHealTick % Timing.seconds(30) == 0) {
				heal(1, true);
			}
		} else {
			passiveHealTick = 0;
		}
	}
	
	@Override
	public void emitPacket(PacketOut packet) {
		for (Player player : visiblePlayers) {
			packet.send(player);
		}
		packet.send(this);
	}
	
	public void updateEntityVisibility() {
		
		// We want to limit how many other players the player can see
		int visiblePlayerCount = 0;
		
		// Spawning entities
		for (int[] ord : VISIBLE_CHUNK_ORDER) {
			int y = chunk.chunkY + ord[1];
			int x = chunk.chunkX + ord[0];
		
			Chunk chunk = world.getChunk(x, y);
			if (chunk == null) continue;
			
			if (visiblePlayerCount < Constants.MAX_VISIBLE_PLAYERS) {
				for (Player otherPlayer : chunk.players) {
					if (otherPlayer == this) continue;
					
					if (!visiblePlayers.contains(otherPlayer)) {
						new EntitySpawnPacketOut(otherPlayer, false).send(this);
						visiblePlayers.add(otherPlayer);
					}
				}	
			}
			
			for (Npc npc : chunk.npcs) {
				if (!visibleNpcs.contains(npc)) {
					new EntitySpawnPacketOut(npc, false).send(this);
					visibleNpcs.add(npc);
				}
			}
		}
		
		// Despawning entities
		Iterator<Player> playerItr = visiblePlayers.iterator();
		while (playerItr.hasNext()) {
			Player otherPlayer = playerItr.next();
			
			// TODO: Is there a faster way than taking abs?
			if (otherPlayer == null ||
				Math.abs(otherPlayer.chunk.chunkX - this.chunk.chunkX) > 1 ||
				Math.abs(otherPlayer.chunk.chunkY - this.chunk.chunkY) > 1
					) {
				new EntityDespawnPacketOut(otherPlayer.id).send(this);	
				playerItr.remove();
			}
		}
		
		Iterator<Npc> npcItr = visibleNpcs.iterator();
		while (npcItr.hasNext()) {
			Npc npc = npcItr.next();
			
			// TODO: Is there a faster way than taking abs?
			if (npc == null ||
				Math.abs(npc.chunk.chunkX - this.chunk.chunkX) > 1 ||
				Math.abs(npc.chunk.chunkY - this.chunk.chunkY) > 1
					) {
				new EntityDespawnPacketOut(npc.id).send(this);	
				npcItr.remove();
			}
		}
	}
	
	public void sendServerMessage(String message) {
		new ChatTextPacketOut(message, ChatTextType.SERVER, 0).send(this);
	}
	
	public void clearMoveQueue() {
		moveQueueSize = 0;
	}
	
	public void tryToPickupGroundItem() {
		if (groundItemToPickup == -1) {
			return;
		}
		
		GroundItem groundItem = Server.instance.getGroundItem(groundItemToPickup);
		if (groundItem == null) {
			new PickupItemPacketOut().send(this);
			groundItemToPickup = -1;
			return;
		}
		
		if (!currentLocation.equals(groundItem.getLocation())) {
			new PickupItemPacketOut().send(this);
			groundItemToPickup = -1;
			return;
		}
		
		Player owner = groundItem.getOwner();
		if (owner != null && owner != this) {
			new PickupItemPacketOut().send(this);
			groundItemToPickup = -1;
			return;
		}
		
		Server.instance.removeGroundItem(groundItemToPickup);
		giveItem(groundItem.getItem());
		new PickupItemPacketOut().send(this);
		
		if (owner != null) {
			new DespawnGroundItemPacketOut(groundItemToPickup).send(this);
		} else {
			// TODO: Send to everyone near the ground item?
		}
		
		groundItemToPickup = -1;
		
	}
	
	public void tryToTalkToNpc() {
		if (npcToTalkTo == null) return;
	
		if (npcToTalkTo.isDead()) {
			npcToTalkTo = null;
			return;
		}
		if (distanceTo(npcToTalkTo) > 1) {
			return;
		}
		
		S2Script npcScript = npcToTalkTo.getScript();
		npcScriptInterpreter = new S2Interpreter(npcToTalkTo, this, npcScript);
		npcScriptInterpreter.run(0);
		
		npcToTalkTo = null;
	}
	
	public void continueNpcScript(int option) {
		if (npcScriptInterpreter == null) return;
		
		if (!npcScriptInterpreter.run(option)) {
			NpcChatPacketOut.end().send(this);
			npcScriptInterpreter = null;
		}
	}
	
	public GroundItem dropItem(ItemStack item, Location location) {
		GroundItem groundItem = new GroundItem();
		groundItem.setItem(item);
		groundItem.setLocation(location);
		groundItem.setOwner(this);
		
		if (!Server.instance.addGroundItem(groundItem)) {
			return null;
		}
		
		new SpawnGroundItemPacketOut(
				groundItem,
				location.x,
				location.y).send(this);
		
		return groundItem;
	}
	
	@Override
	public void handleDeath() {
		super.handleDeath();
		predictiveFacingDirection = Direction.NONE;
		groundItemToPickup = -1;
		clearMoveQueue();
	}

	@Override
	public void cancelAction() {
		super.cancelAction();
		groundItemToPickup = -1;
		npcToTalkTo = null;
		npcScriptInterpreter = null;
		shopId = -1;
	}
	
	public void giveXp(int skillType, int xp) {
		skills[skillType].give(xp, false);
	}

	@Override
	public int getMaxHit() {
		int level = skills[SkillType.MELEE].getBoostedLevel();
		int maxHit = (int) (level / 3.8f) + 1;
		return maxHit;
	}
	
	public boolean giveItem(ItemStack item) {
		int slot = inventory.addItem(item);
		if (slot == -1) {
			return false;
		}
		new SetItemPacketOut(item.getId(), item.getAmount(), slot).send(this);
		return true;
	}

	public boolean isMoving() {
		return !currentLocation.equals(futureLocation) || moveQueueSize != 0;
	}
}
