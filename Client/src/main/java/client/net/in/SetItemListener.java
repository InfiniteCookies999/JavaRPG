package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.Resources;
import client.game.items.ItemStack;
import client.game.menus.container.InventoryMenu;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.SetItemListener.GiveItemPacket;
import lombok.AllArgsConstructor;

public class SetItemListener extends PacketListener<GiveItemPacket> {
	
	@AllArgsConstructor
	class GiveItemPacket extends PacketData {
		private int itemId;
		private int amount;
		private int slot;
	}
	
	public SetItemListener() {
		super(NetOpcodes.SET_ITEM);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int itemId = handle.readUnsignedInt24();
		int amount = handle.readInt();
		int slot = handle.readUnsignedByte();
		
		return new GiveItemPacket(itemId, amount, slot);
	}

	@Override
	public void onEvent(GiveItemPacket packet) {
		InventoryMenu inventory = Client.instance.inventory;
		if (packet.amount == 0) {
			inventory.setItem(packet.slot, null);
		} else {
			ItemStack itemStack = new ItemStack(Resources.getItemDefinition(packet.itemId), packet.amount);
			inventory.setItem(packet.slot, itemStack);	
		}
	}
}
