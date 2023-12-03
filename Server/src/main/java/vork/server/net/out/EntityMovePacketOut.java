package vork.server.net.out;

import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class EntityMovePacketOut extends PacketOut {

	private int entityId;
	private int tileX;
	private int tileY;
	
	public EntityMovePacketOut(int entityId, int tileX, int tileY) {
		super(NetOpcodes.ENTITY_MOVE);
		this.entityId = entityId;
		this.tileX = tileX;
		this.tileY = tileY;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(entityId);
		handle.writeInt(tileX);
		handle.writeInt(tileY);
	}
}
