package vork.server.net.out;

import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class EntityDespawnPacketOut extends PacketOut {

	private int entityId;
	
	public EntityDespawnPacketOut(int entityId) {
		super(NetOpcodes.ENTITY_DESPAWN);
		this.entityId = entityId;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(entityId);
	}

}
