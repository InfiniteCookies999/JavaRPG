package client.net.out;

import client.net.NetOpcodes;
import client.net.NetworkHandle;

public class DropItemPacketOut extends PacketOut {

	private int slot;
	
	public DropItemPacketOut(int slot) {
		super(NetOpcodes.DROP_ITEM);
		this.slot = slot;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedByte(slot);
	}
}
