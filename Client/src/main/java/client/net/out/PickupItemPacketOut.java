package client.net.out;

import client.net.NetOpcodes;
import client.net.NetworkHandle;

public class PickupItemPacketOut extends PacketOut {

	private int id;
	
	public PickupItemPacketOut(int id) {
		super(NetOpcodes.PICKUP_ITEM);
		this.id = id;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(id);
	}
}
