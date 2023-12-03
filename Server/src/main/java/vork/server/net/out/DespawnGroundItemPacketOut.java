package vork.server.net.out;

import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class DespawnGroundItemPacketOut extends PacketOut {

	private int id;
	
	public DespawnGroundItemPacketOut(int id) {
		super(NetOpcodes.DESPAWN_GROUND_ITEM);
		this.id = id;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(id);
	}
}
