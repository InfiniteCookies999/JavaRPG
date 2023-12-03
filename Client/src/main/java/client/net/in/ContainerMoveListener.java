package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.menus.container.InventoryMenu;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.ContainerMoveListener.ContainerMovePacket;
import lombok.AllArgsConstructor;

public class ContainerMoveListener extends PacketListener<ContainerMovePacket> {

	@AllArgsConstructor
	class ContainerMovePacket extends PacketData {
		private int containerId;
		private int fromSlot;
		private int toSlot;
	}
	
	public ContainerMoveListener() {
		super(NetOpcodes.CONTAINER_MOVE);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int containerId = handle.readUnsignedByte();
		int fromSlot = handle.readUnsignedShort();
		int toSlot = handle.readUnsignedShort();
		
		return new ContainerMovePacket(containerId, fromSlot, toSlot);
	}

	@Override
	public void onEvent(ContainerMovePacket packet) {
		InventoryMenu inventory = Client.instance.inventory;
		inventory.moveItem(packet.fromSlot, packet.toSlot);
	}
}
