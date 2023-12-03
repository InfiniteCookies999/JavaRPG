package client.net.in;

import java.io.IOException;

import client.Client;
import client.Constants;
import client.game.PlayScreen;
import client.game.ShaderProgramType;
import client.game.entity.BasicMobRenderer;
import client.game.entity.Direction;
import client.game.entity.Entity;
import client.game.entity.EntityRenderType;
import client.game.entity.EntityRenderer;
import client.game.entity.HumanRenderer;
import client.game.world.Location;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.EntitySpawnListener.EntitySpawnPacket;

public class EntitySpawnListener extends PacketListener<EntitySpawnPacket> {

	class EntitySpawnPacket extends PacketData {
		private int entityId;
		private boolean forPlayerLogin;
		private String name;
		private int maxHealth;
		private int health;
		private int combatLevel;
		private float moveSpeed;
		private Direction facingDirection;
		private int worldTileX, worldTileY;
		private int selectOptions;
		private EntityRenderer renderer;
	}
	
	public EntitySpawnListener() {
		super(NetOpcodes.ENTITY_SPAWN);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int entityId = handle.readUnsignedInt24();
		boolean forPlayerJoin = handle.readBoolean();
		String name = handle.readSmallString();
		int maxHealth = handle.readUnsignedInt24();
		int health = handle.readUnsignedInt24();
		int combatLevel = handle.readUnsignedShort();
		float moveSpeed = handle.readFloat();
		Direction facingDirection = Direction.values()[handle.readUnsignedByte()];
		int worldTileX = handle.readInt();
		int worldTileY = handle.readInt();
		int selectOptions = handle.readUnsignedInt();
		
		EntitySpawnPacket packet = new EntitySpawnPacket();
		packet.entityId = entityId;
		packet.forPlayerLogin = forPlayerJoin;
		packet.name = name;
		packet.maxHealth = maxHealth;
		packet.health = health;
		packet.combatLevel = combatLevel;
		packet.moveSpeed = moveSpeed;
		packet.facingDirection = facingDirection;
		packet.worldTileX = worldTileX;
		packet.worldTileY = worldTileY;
		packet.selectOptions = selectOptions;
		
		EntityRenderType renderType = EntityRenderType.values()[handle.readUnsignedByte()];
		
		if (renderType == EntityRenderType.HUMAN) {
			int bits = handle.readUnsignedShort();
			int eyeColor = handle.readUnsignedByte();
			int bodyType = handle.readUnsignedShort();
			int legsType = handle.readUnsignedShort();
			int helmType = handle.readUnsignedShort();
			int skinColor = (bits >> 2) & 15;
			int hairStyle = (bits >> 6) & 31;
			int hairColor = (bits >> 11) & 31;
			
			HumanRenderer renderer = new HumanRenderer();
			renderer.skinColor = skinColor;
			renderer.hairStyle = hairStyle;
			renderer.hairColor = hairColor;
			renderer.eyeColor = eyeColor;
			renderer.bodyType = bodyType;
			renderer.legsType = legsType;
			renderer.helmType = helmType;
			
			packet.renderer = renderer;
		} else if (renderType == EntityRenderType.BASIC_MOB) {
			int textureId = handle.readUnsignedShort();
			
			BasicMobRenderer renderer = new BasicMobRenderer();
			renderer.setTextureId(textureId);
			
			packet.renderer = renderer;
		}
		
		
		return packet;
	}

	@Override
	public void onEvent(EntitySpawnPacket packet) {
		
		Entity entity = new Entity();
		entity.id = packet.entityId;
		entity.setName(packet.name);
		entity.x = packet.worldTileX * Constants.TILE_SIZE;
		entity.y = packet.worldTileY * Constants.TILE_SIZE;
		entity.setFacingDirection(packet.facingDirection);
		entity.setMoveSpeed(packet.moveSpeed);
		entity.currentLocation = new Location(packet.worldTileX, packet.worldTileY);
		entity.futureLocation  = new Location(packet.worldTileX, packet.worldTileY);
		entity.setMaxHealth(packet.maxHealth);
		entity.setHealth(packet.health);
		entity.setCombatLevel(packet.combatLevel);
		entity.selectOptions = packet.selectOptions;
		
		Client.instance.entities.put(packet.entityId, entity);
		entity.renderer = packet.renderer;
		entity.renderer.setup();
		
		if (entity.isDead()) {
			// The entity was dead on spawn so need to set it to render as if
			// it is dead.
			entity.renderer.setShaderProgramType(ShaderProgramType.ENTITY_DIED);
			entity.renderer.setPresentationTick(10000);
		}
		
		if (packet.forPlayerLogin) {
			Client.instance.player = entity;
			entity.setPrimaryPlayer(true);
			Client.instance.setScreen(new PlayScreen());
		}
	}
	
}
