package vork.server.net.out;

import vork.server.game.entity.Entity;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

/**
 * If you want to teleport entities use the teleport method
 * of the entity create an instance of this yourself.
 */
public class EntityTeleportPacketOut extends PacketOut {

	private int entityId;
	private int tileX, tileY;
	
	public EntityTeleportPacketOut(Entity entity) {
		super(NetOpcodes.ENTITY_TELEPORT);
		this.entityId = entity.id;
		this.tileX = entity.futureLocation.x;
		this.tileY = entity.futureLocation.y;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(entityId);
		handle.writeInt(tileX);
		handle.writeInt(tileY);
	}
}
