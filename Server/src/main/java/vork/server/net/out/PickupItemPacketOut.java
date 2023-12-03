package vork.server.net.out;

import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class PickupItemPacketOut extends PacketOut {

	public PickupItemPacketOut() {
		super(NetOpcodes.PICKUP_ITEM);
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		
	}
}
