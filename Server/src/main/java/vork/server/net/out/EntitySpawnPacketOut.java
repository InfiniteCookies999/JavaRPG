package vork.server.net.out;

import vork.server.game.entity.BasicMobRenderData;
import vork.server.game.entity.Entity;
import vork.server.game.entity.EntityRenderData;
import vork.server.game.entity.EntityRenderType;
import vork.server.game.entity.HumanRenderData;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class EntitySpawnPacketOut extends PacketOut {

	private boolean forPlayerJoin;
	private Entity entity;
	
	public EntitySpawnPacketOut(Entity entity, boolean forPlayerJoin) {
		super(NetOpcodes.ENTITY_SPAWN);
		this.forPlayerJoin = forPlayerJoin;
		this.entity = entity;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(entity.id);
		handle.writeBoolean(forPlayerJoin);
		handle.writeSmallString(entity.getName());
		handle.writeUnsignedInt24(entity.getMaxHealth());
		handle.writeUnsignedInt24(entity.getHealth());
		handle.writeUnsignedShort(entity.getCombatLevel());
		handle.writeFloat(entity.getMoveSpeed());
		handle.writeUnsignedByte(entity.getFacingDirection().ordinal());
		handle.writeInt(entity.futureLocation.x);
		handle.writeInt(entity.futureLocation.y);
		handle.writeUnsignedInt(entity.selectOptions);
		
		// Sending the render information
		EntityRenderData renderData = entity.renderData;
		handle.writeUnsignedByte(renderData.getType().ordinal());
		if (renderData.getType() == EntityRenderType.HUMAN) {
			HumanRenderData humanRenderData = (HumanRenderData) renderData;
			int bits = humanRenderData.hairColor << 11;
			bits |= humanRenderData.hairStyle << 6;
			bits |= humanRenderData.skinColor << 2;
			handle.writeUnsignedShort(bits);
			handle.writeUnsignedByte(humanRenderData.eyeColor);
			handle.writeUnsignedShort(humanRenderData.bodyType);
			handle.writeUnsignedShort(humanRenderData.legsType);
			handle.writeUnsignedShort(humanRenderData.helmType);
		} else if (renderData.getType() == EntityRenderType.BASIC_MOB) {
			BasicMobRenderData basicMobRenderData = (BasicMobRenderData) renderData;
			handle.writeUnsignedShort(basicMobRenderData.textureId);
		}
	}
}
