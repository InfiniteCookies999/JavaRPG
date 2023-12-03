package vork.server.net.out;

import vork.server.game.GroundItem;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class SpawnGroundItemPacketOut extends PacketOut {

	private GroundItem groundItem;
	private int tileX, tileY;
	
	public SpawnGroundItemPacketOut(GroundItem groundItem, int tileX, int tileY) {
		super(NetOpcodes.SPAWN_GROUND_ITEM);
		this.tileX = tileX;
		this.tileY = tileY;
		this.groundItem = groundItem;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(groundItem.getId());
		handle.writeUnsignedInt24(groundItem.getItem().getId());
		handle.writeInt(tileX);
		handle.writeInt(tileY);
	}
}
