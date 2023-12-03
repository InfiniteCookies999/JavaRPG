package vork.server.net.in;

import java.io.IOException;

import lombok.AllArgsConstructor;
import vork.server.game.GroundItem;
import vork.server.game.container.Inventory;
import vork.server.game.container.ItemStack;
import vork.server.game.entity.Player;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;
import vork.server.net.in.DropItemListener.DropItemPacket;
import vork.server.net.out.SetItemPacketOut;

public class DropItemListener extends PacketListener<DropItemPacket> {

	@AllArgsConstructor
	class DropItemPacket extends PacketData {
		private int slot;
	}
	
	public DropItemListener() {
		super(NetOpcodes.DROP_ITEM);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int slot = handle.readUnsignedByte();
		if (slot >= Inventory.NUM_SLOTS) {
			return null;
		}
		
		return new DropItemPacket(slot);
	}

	@Override
	public void onEvent(DropItemPacket packet) {
		Player player = packet.getHandle().getPlayer();
		if (player == null) {
			return;
		}
		
		Inventory inventory = player.getInventory();
		ItemStack itemToRemove = inventory.getItem(packet.slot);
		if (itemToRemove == null) {
			return;
		}
		
		GroundItem groundItem = player.dropItem(itemToRemove, player.currentLocation);
		if (groundItem == null) {
			return;
		}
		
		inventory.setItem(null, packet.slot);
		new SetItemPacketOut(-1, 0, packet.slot).send(player);
		
	}
}
