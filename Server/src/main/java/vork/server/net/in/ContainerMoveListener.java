package vork.server.net.in;

import java.io.IOException;

import lombok.AllArgsConstructor;
import vork.server.game.container.Inventory;
import vork.server.game.container.ItemStack;
import vork.server.game.entity.Player;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;
import vork.server.net.in.ContainerMoveListener.ContainerMovePacket;
import vork.server.net.out.ContainerMovePacketOut;

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
		
		// TODO: Will want to make sure the from and to slot
		// doesnt exceed the inventory constraints
		
		return new ContainerMovePacket(containerId, fromSlot, toSlot);
	}

	@Override
	public void onEvent(ContainerMovePacket packet) {
		Player player = packet.getHandle().getPlayer();
		if (player == null || player.isDead()) {
			return;
		}
		
		Inventory container = player.getInventory();
	
		ItemStack itemToMove = container.getItem(packet.fromSlot);
		if (itemToMove == null) {
			return;
		}
		
		ItemStack swapItem = container.getItem(packet.toSlot);
		container.setItem(swapItem, packet.fromSlot);
		container.setItem(itemToMove, packet.toSlot);
		
		new ContainerMovePacketOut(packet.containerId, packet.fromSlot, packet.toSlot).send(player);
	}
}
