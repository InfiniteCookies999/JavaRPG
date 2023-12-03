package client.net.in;

import java.io.IOException;

import client.Client;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.PickupItemListener.PickupItemPacket;

public class PickupItemListener extends PacketListener<PickupItemPacket> {

	class PickupItemPacket extends PacketData {
		
	}
	
	public PickupItemListener() {
		super(NetOpcodes.PICKUP_ITEM);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		return new PickupItemPacket();
	}

	@Override
	public void onEvent(PickupItemPacket packet) {
		Client.instance.inventory.unlockAllSlots();
	}
}
